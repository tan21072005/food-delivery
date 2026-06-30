import {
  AuthenticationError,
  type ChatDependencies,
  createChatHandler,
} from "./handler.ts";
import type {
  ChatMessage,
  ContextMessage,
  Conversation,
  MenuContext,
  OrderContext,
} from "./domain.ts";
import { OpenAiError } from "./openai.ts";
import { type ChatStore, ChatStoreError } from "./store.ts";

function assertEquals(actual: unknown, expected: unknown): void {
  if (JSON.stringify(actual) !== JSON.stringify(expected)) {
    throw new Error(
      `Expected ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}`,
    );
  }
}

class FakeStore implements ChatStore {
  userId: number | null = null;
  completedToday = 0;
  conversationError: Error | null = null;
  menuLimit = 0;
  orderLimit = 0;
  orderUserId = 0;
  markedStatuses: string[] = [];
  touched = false;
  existingUserMessage: ChatMessage | null = null;
  existingAssistantMessage: ChatMessage | null = null;
  claimCalls = 0;
  conversation: Conversation = {
    id: "f083ea9f-4621-4d97-bae0-5676c4fcecd9",
    title: "Gợi ý Món trưa",
    created_at: "2026-06-28T10:00:00Z",
    updated_at: "2026-06-28T10:00:00Z",
  };
  userMessage: ChatMessage = {
    id: 101,
    conversation_id: "f083ea9f-4621-4d97-bae0-5676c4fcecd9",
    role: "user",
    content: "Gợi ý Món trưa",
    status: "pending",
    created_at: "2026-06-28T10:00:00Z",
  };

  findUserId(_authUid: string): Promise<number | null> {
    return Promise.resolve(this.userId);
  }

  countCompletedToday(_userId: number, _now: Date): Promise<number> {
    return Promise.resolve(this.completedToday);
  }

  getOrCreateConversation(
    _userId: number,
    _conversationId: string | null,
    _title: string,
  ): Promise<Conversation> {
    if (this.conversationError !== null) {
      return Promise.reject(this.conversationError);
    }
    return Promise.resolve(this.conversation);
  }

  findUserMessage(
    _conversationId: string,
    _requestId: string,
  ): Promise<ChatMessage | null> {
    return Promise.resolve(this.existingUserMessage);
  }

  findAssistantReply(_userMessageId: number): Promise<ChatMessage | null> {
    return Promise.resolve(this.existingAssistantMessage);
  }

  claimUserMessage(
    _conversationId: string,
    _requestId: string,
    _content: string,
  ): Promise<{ message: ChatMessage; claimed: boolean }> {
    this.claimCalls++;
    return Promise.resolve({ message: this.userMessage, claimed: true });
  }

  markUserMessage(
    _id: number,
    _status: "pending" | "complete" | "failed",
  ): Promise<ChatMessage> {
    this.markedStatuses.push(_status);
    this.userMessage = { ...this.userMessage, status: _status };
    return Promise.resolve(this.userMessage);
  }

  createAssistantMessage(
    _conversationId: string,
    _userMessageId: number,
    _content: string,
  ): Promise<ChatMessage> {
    return Promise.resolve({
      id: 102,
      conversation_id: _conversationId,
      role: "assistant",
      content: _content,
      status: "complete",
      created_at: "2026-06-28T10:00:02Z",
    });
  }

  getRecentMessages(
    _conversationId: string,
    _limit: number,
  ): Promise<ContextMessage[]> {
    return Promise.resolve([]);
  }

  getMenuContext(_question: string, _limit: number): Promise<MenuContext[]> {
    this.menuLimit = _limit;
    return Promise.resolve([]);
  }

  getOrderContext(_userId: number, _limit: number): Promise<OrderContext[]> {
    this.orderUserId = _userId;
    this.orderLimit = _limit;
    return Promise.resolve([]);
  }

  touchConversation(_conversationId: string): Promise<void> {
    this.touched = true;
    return Promise.resolve();
  }

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
  }> {
    return Promise.resolve({
      id: 1,
      message_id: messageId,
      user_id: userId,
      value,
      created_at: "2026-06-28T10:00:00Z",
      updated_at: "2026-06-28T10:00:00Z",
    });
  }
}

function createDependencies(store: ChatStore): ChatDependencies {
  return {
    authenticate: () =>
      Promise.resolve({
        authUid: "f55d230d-e324-4eec-9fb7-6b6e6aa0926b",
        email: "customer@example.com",
      }),
    storeForRequest: () => store,
    ai: {
      respond: () => {
        throw new Error("Not implemented");
      },
    },
    now: () => new Date("2026-06-28T12:00:00.000Z"),
  };
}

Deno.test("unknown Customer identity requires authentication", async () => {
  const response = await createChatHandler(createDependencies(new FakeStore()))(
    new Request("http://localhost/functions/v1/chat-assistant", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        conversation_id: null,
        message: "Gợi ý Món trưa",
        client_request_id: "959c5461-ed11-42e6-aa5f-18152727d02c",
      }),
    }),
  );

  assertEquals(response.status, 401);
  assertEquals((await response.json()).error.code, "AUTH_REQUIRED");
});

Deno.test("a conversation owned by another Customer is forbidden", async () => {
  const store = new FakeStore();
  store.userId = 42;
  store.conversationError = new ChatStoreError("FORBIDDEN");
  const response = await createChatHandler(createDependencies(store))(
    new Request("http://localhost/functions/v1/chat-assistant", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        conversation_id: "a77293fb-864b-494f-a317-b3fa00c7189c",
        message: "Order gần nhất?",
        client_request_id: "bf006a5f-0818-41d4-a576-b6f359cd1256",
      }),
    }),
  );

  assertEquals(response.status, 403);
  assertEquals((await response.json()).error.code, "FORBIDDEN");
});

Deno.test("database failures return a stable database error code", async () => {
  const store = new FakeStore();
  store.findUserId = () => Promise.reject(new ChatStoreError("DATABASE_ERROR"));
  const dependencies = createDependencies(store);
  const response = await createChatHandler(dependencies)(
    new Request("http://localhost/functions/v1/chat-assistant", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        conversation_id: null,
        message: "Gợi ý Món trưa",
        client_request_id: "7cbf4fe7-3a58-4d80-9dfa-495292c6d2a0",
      }),
    }),
  );

  assertEquals(response.status, 502);
  const body = await response.json();
  assertEquals(body.error.code, "DATABASE_ERROR");
  assertEquals(body.error.message, "Không thể truy cập dữ liệu chat lúc này");
});

Deno.test("the sixteenth completed question reaches the daily limit", async () => {
  const store = new FakeStore();
  store.userId = 42;
  store.completedToday = 15;
  const response = await createChatHandler(createDependencies(store))(
    new Request("http://localhost/functions/v1/chat-assistant", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        conversation_id: null,
        message: "Gợi ý Món trưa",
        client_request_id: "08ced42b-6fe6-4743-8365-4d9b5e860167",
      }),
    }),
  );

  assertEquals(response.status, 429);
  assertEquals((await response.json()).error.code, "DAILY_LIMIT_REACHED");
});

Deno.test("a new question persists a bounded-context assistant reply", async () => {
  const store = new FakeStore();
  store.userId = 42;
  const dependencies = createDependencies(store);
  dependencies.ai = {
    respond: () =>
      Promise.resolve({
        text: "Bạn có thể thử phở bò.",
        inputTokens: 180,
        outputTokens: 18,
      }),
  };

  const response = await createChatHandler(dependencies)(
    new Request("http://localhost/functions/v1/chat-assistant", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        conversation_id: null,
        message: "Gợi ý Món trưa",
        client_request_id: "8ba37ad1-9036-4b5a-ad80-d9bd42b52e4a",
      }),
    }),
  );
  const body = await response.json();

  assertEquals(response.status, 200);
  assertEquals(body.assistant_message.content, "Bạn có thể thử phở bò.");
  assertEquals(body.user_message.status, "complete");
  assertEquals(body.usage, { input_tokens: 180, output_tokens: 18 });
  assertEquals(store.menuLimit, 10);
  assertEquals(store.orderLimit, 5);
  assertEquals(store.orderUserId, 42);
  assertEquals(store.markedStatuses, ["complete"]);
  assertEquals(store.touched, true);
});

Deno.test("an AI failure marks the persisted question failed", async () => {
  const store = new FakeStore();
  store.userId = 42;
  const dependencies = createDependencies(store);
  dependencies.ai = {
    respond: () => Promise.reject(new Error("provider details")),
  };

  const response = await createChatHandler(dependencies)(
    new Request("http://localhost/functions/v1/chat-assistant", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        conversation_id: null,
        message: "Gợi ý Món trưa",
        client_request_id: "892882e8-a0aa-4ef5-9069-3bd71fafc033",
      }),
    }),
  );

  assertEquals(response.status, 502);
  assertEquals((await response.json()).error.code, "UPSTREAM_ERROR");
  assertEquals(store.markedStatuses, ["failed"]);
});

Deno.test("a completed duplicate returns its saved reply without another AI call", async () => {
  const store = new FakeStore();
  store.userId = 42;
  store.existingUserMessage = { ...store.userMessage, status: "complete" };
  store.existingAssistantMessage = {
    id: 102,
    conversation_id: store.conversation.id,
    role: "assistant",
    content: "Câu trả lời đã lưu",
    status: "complete",
    created_at: "2026-06-28T10:00:02Z",
  };
  let aiCalls = 0;
  const dependencies = createDependencies(store);
  dependencies.ai = {
    respond: () => {
      aiCalls++;
      throw new Error("must not run");
    },
  };

  const response = await createChatHandler(dependencies)(
    new Request("http://localhost/functions/v1/chat-assistant", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        conversation_id: store.conversation.id,
        message: "Gợi ý Món trưa",
        client_request_id: "ae8d4bfd-8583-418a-9883-382e4aed5ed7",
      }),
    }),
  );
  const body = await response.json();

  assertEquals(response.status, 200);
  assertEquals(body.assistant_message.content, "Câu trả lời đã lưu");
  assertEquals(body.usage, { input_tokens: 0, output_tokens: 0 });
  assertEquals(aiCalls, 0);
  assertEquals(store.claimCalls, 0);
});

Deno.test("invalid input returns a stable client error", async () => {
  const store = new FakeStore();
  store.userId = 42;
  const response = await createChatHandler(createDependencies(store))(
    new Request("http://localhost/functions/v1/chat-assistant", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        conversation_id: null,
        message: " ",
        client_request_id: "6ed8842a-262c-453b-9ee2-1fdac82f2bc2",
      }),
    }),
  );

  assertEquals(response.status, 400);
  assertEquals((await response.json()).error.code, "EMPTY_MESSAGE");
});

Deno.test("a missing or invalid bearer token returns session expired", async () => {
  const dependencies = createDependencies(new FakeStore());
  dependencies.authenticate = () => Promise.reject(new AuthenticationError());
  const response = await createChatHandler(dependencies)(
    new Request("http://localhost/functions/v1/chat-assistant", {
      method: "POST",
      body: "{}",
    }),
  );

  assertEquals(response.status, 401);
  assertEquals((await response.json()).error.code, "AUTH_REQUIRED");
});

Deno.test("OpenAI and daily rate limits use different error codes", async () => {
  const store = new FakeStore();
  store.userId = 42;
  const dependencies = createDependencies(store);
  dependencies.ai = {
    respond: () => Promise.reject(new OpenAiError("UPSTREAM_RATE_LIMIT")),
  };
  const response = await createChatHandler(dependencies)(
    new Request("http://localhost/functions/v1/chat-assistant", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        conversation_id: null,
        message: "Gợi ý Món trưa",
        client_request_id: "42ffb759-6097-46ce-8833-a5e605b5f971",
      }),
    }),
  );

  assertEquals(response.status, 429);
  assertEquals((await response.json()).error.code, "UPSTREAM_RATE_LIMIT");
  assertEquals(store.markedStatuses, ["failed"]);
});

Deno.test("feedback uses authenticated public user id instead of client user id", async () => {
  const store = new FakeStore();
  store.userId = 42;
  const response = await createChatHandler(createDependencies(store))(
    new Request("http://localhost/functions/v1/chat-assistant/feedback", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        message_id: 102,
        user_id: -1,
        value: 1,
      }),
    }),
  );
  const body = await response.json();

  assertEquals(response.status, 200);
  assertEquals(body.message_id, 102);
  assertEquals(body.user_id, 42);
  assertEquals(body.value, 1);
});
