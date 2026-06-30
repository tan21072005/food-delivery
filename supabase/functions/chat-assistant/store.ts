import type {
  ChatMessage,
  ContextMessage,
  Conversation,
  MenuContext,
  OrderContext,
} from "./domain.ts";
import type { SupabaseClient } from "npm:@supabase/supabase-js@2.108.2";

export interface ChatStore {
  findUserId(authUid: string, email: string | null): Promise<number | null>;
  countCompletedToday(userId: number, now: Date): Promise<number>;
  getOrCreateConversation(
    userId: number,
    conversationId: string | null,
    title: string,
  ): Promise<Conversation>;
  findUserMessage(
    conversationId: string,
    requestId: string,
  ): Promise<ChatMessage | null>;
  findAssistantReply(userMessageId: number): Promise<ChatMessage | null>;
  claimUserMessage(
    conversationId: string,
    requestId: string,
    content: string,
  ): Promise<{ message: ChatMessage; claimed: boolean }>;
  markUserMessage(
    id: number,
    status: "pending" | "complete" | "failed",
  ): Promise<ChatMessage>;
  createAssistantMessage(
    conversationId: string,
    userMessageId: number,
    content: string,
  ): Promise<ChatMessage>;
  getRecentMessages(
    conversationId: string,
    limit: number,
  ): Promise<ContextMessage[]>;
  getMenuContext(question: string, limit: number): Promise<MenuContext[]>;
  getOrderContext(userId: number, limit: number): Promise<OrderContext[]>;
  touchConversation(conversationId: string): Promise<void>;
  setFeedback(
    userId: number,
    messageId: number,
    value: -1 | 1,
  ): Promise<{
    id: number;
    message_id: number;
    user_id: number;
    value: -1 | 1;
    created_at: string;
    updated_at: string;
  }>;
}

export class ChatStoreError extends Error {
  constructor(public readonly code: "FORBIDDEN" | "DATABASE_ERROR") {
    super(code);
    this.name = "ChatStoreError";
  }
}

function databaseError(): ChatStoreError {
  return new ChatStoreError("DATABASE_ERROR");
}

function utcDayBounds(now: Date): { start: string; end: string } {
  const start = new Date(Date.UTC(
    now.getUTCFullYear(),
    now.getUTCMonth(),
    now.getUTCDate(),
  ));
  const end = new Date(start);
  end.setUTCDate(end.getUTCDate() + 1);
  return { start: start.toISOString(), end: end.toISOString() };
}

export class SupabaseChatStore implements ChatStore {
  constructor(
    private readonly scoped: SupabaseClient,
    private readonly admin: SupabaseClient,
  ) {}

  async findUserId(
    authUid: string,
    email: string | null,
  ): Promise<number | null> {
    const { data, error } = await this.admin
      .from("users")
      .select("id")
      .eq("auth_uid", authUid)
      .maybeSingle();
    if (error) throw databaseError();
    if (data !== null) return Number(data.id);

    const username = email?.split("@")[0]?.trim() ||
      `customer-${authUid.slice(0, 8)}`;
    const { data: created, error: createError } = await this.admin
      .from("users")
      .insert({
        auth_uid: authUid,
        email,
        username,
        role: "customer",
        status: "active",
      })
      .select("id")
      .maybeSingle();
    if (!createError && created !== null) return Number(created.id);

    const { data: existing, error: rereadError } = await this.admin
      .from("users")
      .select("id")
      .eq("auth_uid", authUid)
      .maybeSingle();
    if (rereadError) throw databaseError();
    return existing === null ? null : Number(existing.id);
  }

  async countCompletedToday(userId: number, now: Date): Promise<number> {
    const { start, end } = utcDayBounds(now);
    const { count, error } = await this.admin
      .from("chat_messages")
      .select(
        "id,chat_conversations!inner(user_id)",
        { count: "exact", head: true },
      )
      .eq("role", "user")
      .eq("status", "complete")
      .eq("chat_conversations.user_id", userId)
      .gte("created_at", start)
      .lt("created_at", end);
    if (error) throw databaseError();
    return count ?? 0;
  }

  async getOrCreateConversation(
    userId: number,
    conversationId: string | null,
    title: string,
  ): Promise<Conversation> {
    if (conversationId !== null) {
      const { data, error } = await this.scoped
        .from("chat_conversations")
        .select("id,title,created_at,updated_at")
        .eq("id", conversationId)
        .maybeSingle();
      if (error) throw databaseError();
      if (data === null) throw new ChatStoreError("FORBIDDEN");
      return data as Conversation;
    }

    const { data, error } = await this.admin
      .from("chat_conversations")
      .insert({ user_id: userId, title })
      .select("id,title,created_at,updated_at")
      .single();
    if (error || data === null) throw databaseError();
    return data as Conversation;
  }

  async findUserMessage(
    conversationId: string,
    requestId: string,
  ): Promise<ChatMessage | null> {
    const { data, error } = await this.scoped
      .from("chat_messages")
      .select("id,conversation_id,role,content,status,created_at")
      .eq("conversation_id", conversationId)
      .eq("client_request_id", requestId)
      .eq("role", "user")
      .maybeSingle();
    if (error) throw databaseError();
    return data as ChatMessage | null;
  }

  async findAssistantReply(
    userMessageId: number,
  ): Promise<ChatMessage | null> {
    const { data, error } = await this.scoped
      .from("chat_messages")
      .select("id,conversation_id,role,content,status,created_at")
      .eq("reply_to_message_id", userMessageId)
      .eq("role", "assistant")
      .maybeSingle();
    if (error) throw databaseError();
    return data as ChatMessage | null;
  }

  async claimUserMessage(
    conversationId: string,
    requestId: string,
    content: string,
  ): Promise<{ message: ChatMessage; claimed: boolean }> {
    const existing = await this.findUserMessage(conversationId, requestId);
    if (existing !== null) {
      if (existing.status !== "failed") {
        return { message: existing, claimed: false };
      }
      const { data, error } = await this.admin
        .from("chat_messages")
        .update({ status: "pending" })
        .eq("id", existing.id)
        .eq("conversation_id", conversationId)
        .eq("status", "failed")
        .select("id,conversation_id,role,content,status,created_at")
        .maybeSingle();
      if (error) throw databaseError();
      if (data !== null) {
        return { message: data as ChatMessage, claimed: true };
      }
      const current = await this.findUserMessage(conversationId, requestId);
      if (current === null) throw databaseError();
      return { message: current, claimed: false };
    }

    const { data, error } = await this.admin
      .from("chat_messages")
      .insert({
        conversation_id: conversationId,
        client_request_id: requestId,
        role: "user",
        content,
        status: "pending",
      })
      .select("id,conversation_id,role,content,status,created_at")
      .maybeSingle();
    if (!error && data !== null) {
      return { message: data as ChatMessage, claimed: true };
    }
    if (error?.code !== "23505") throw databaseError();
    const concurrent = await this.findUserMessage(conversationId, requestId);
    if (concurrent === null) throw databaseError();
    return { message: concurrent, claimed: false };
  }

  async markUserMessage(
    id: number,
    status: "pending" | "complete" | "failed",
  ): Promise<ChatMessage> {
    const { data: visible, error: readError } = await this.scoped
      .from("chat_messages")
      .select("conversation_id")
      .eq("id", id)
      .eq("role", "user")
      .single();
    if (readError || visible === null) throw databaseError();
    const { data, error } = await this.admin
      .from("chat_messages")
      .update({ status })
      .eq("id", id)
      .eq("conversation_id", visible.conversation_id)
      .eq("role", "user")
      .select("id,conversation_id,role,content,status,created_at")
      .single();
    if (error || data === null) throw databaseError();
    return data as ChatMessage;
  }

  async createAssistantMessage(
    conversationId: string,
    userMessageId: number,
    content: string,
  ): Promise<ChatMessage> {
    const { data: conversation, error: readError } = await this.scoped
      .from("chat_conversations")
      .select("id")
      .eq("id", conversationId)
      .single();
    if (readError || conversation === null) {
      throw new ChatStoreError("FORBIDDEN");
    }
    const { data, error } = await this.admin
      .from("chat_messages")
      .insert({
        conversation_id: conversationId,
        reply_to_message_id: userMessageId,
        role: "assistant",
        content,
        status: "complete",
      })
      .select("id,conversation_id,role,content,status,created_at")
      .single();
    if (error || data === null) throw databaseError();
    return data as ChatMessage;
  }

  async getRecentMessages(
    conversationId: string,
    limit: number,
  ): Promise<ContextMessage[]> {
    const { data, error } = await this.scoped
      .from("chat_messages")
      .select("role,content")
      .eq("conversation_id", conversationId)
      .eq("status", "complete")
      .order("created_at", { ascending: false })
      .order("id", { ascending: false })
      .limit(limit);
    if (error) throw databaseError();
    return ((data ?? []) as ContextMessage[]).reverse();
  }

  async getMenuContext(
    _question: string,
    limit: number,
  ): Promise<MenuContext[]> {
    const { data, error } = await this.scoped
      .from("menus")
      .select(
        "id,item_name,description,price,rating,restaurants!inner(name,is_open)",
      )
      .eq("status", "active")
      .limit(limit);
    if (error) throw databaseError();
    return (data ?? []).map((row) => {
      const restaurantValue = row.restaurants;
      const restaurant = Array.isArray(restaurantValue)
        ? restaurantValue[0]
        : restaurantValue;
      return {
        id: Number(row.id),
        item_name: String(row.item_name),
        description: row.description === null ? null : String(row.description),
        price: Number(row.price),
        rating: Number(row.rating),
        restaurant_name: String(restaurant?.name ?? ""),
        is_open: Boolean(restaurant?.is_open),
      };
    });
  }

  async getOrderContext(
    userId: number,
    limit: number,
  ): Promise<OrderContext[]> {
    const { data, error } = await this.scoped
      .from("orders")
      .select("id,status,total_amount,created_at")
      .eq("user_id", userId)
      .order("created_at", { ascending: false })
      .limit(limit);
    if (error) throw databaseError();
    return (data ?? []).map((row) => ({
      id: Number(row.id),
      status: String(row.status),
      total_amount: Number(row.total_amount),
      created_at: String(row.created_at),
    }));
  }

  async touchConversation(conversationId: string): Promise<void> {
    const { data: visible, error: readError } = await this.scoped
      .from("chat_conversations")
      .select("id")
      .eq("id", conversationId)
      .single();
    if (readError || visible === null) {
      throw new ChatStoreError("FORBIDDEN");
    }
    const { error } = await this.admin
      .from("chat_conversations")
      .update({ updated_at: new Date().toISOString() })
      .eq("id", conversationId);
    if (error) throw databaseError();
  }

  async setFeedback(
    userId: number,
    messageId: number,
    value: -1 | 1,
  ): Promise<{
    id: number;
    message_id: number;
    user_id: number;
    value: -1 | 1;
    created_at: string;
    updated_at: string;
  }> {
    const { data: visible, error: readError } = await this.scoped
      .from("chat_messages")
      .select("id,role,conversation_id")
      .eq("id", messageId)
      .eq("role", "assistant")
      .maybeSingle();
    if (readError) throw databaseError();
    if (visible === null) throw new ChatStoreError("FORBIDDEN");

    const { data, error } = await this.admin
      .from("chat_feedback")
      .upsert({
        message_id: messageId,
        user_id: userId,
        value,
        updated_at: new Date().toISOString(),
      }, { onConflict: "message_id,user_id" })
      .select("id,message_id,user_id,value,created_at,updated_at")
      .single();
    if (error || data === null) throw databaseError();
    return data as {
      id: number;
      message_id: number;
      user_id: number;
      value: -1 | 1;
      created_at: string;
      updated_at: string;
    };
  }
}
