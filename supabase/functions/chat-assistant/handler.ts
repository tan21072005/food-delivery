import { type AiClient, OpenAiError } from "./openai.ts";
import {
  buildPrompt,
  type ChatMessage,
  type Conversation,
  DAILY_LIMIT,
  MAX_HISTORY_MESSAGES,
  MAX_MENU_ROWS,
  MAX_ORDER_ROWS,
  parseChatRequest,
  parseFeedbackRequest,
  ValidationError,
} from "./domain.ts";
import { type ChatStore, ChatStoreError } from "./store.ts";

export type ChatDependencies = {
  authenticate(
    request: Request,
  ): Promise<{ authUid: string; email: string | null }>;
  storeForRequest(request: Request): ChatStore;
  ai: AiClient;
  now(): Date;
};

export class AuthenticationError extends Error {
  constructor() {
    super("AUTH_REQUIRED");
    this.name = "AuthenticationError";
  }
}

function jsonError(
  status: number,
  code: string,
  message: string,
): Response {
  const safeMessage = code === "DATABASE_ERROR"
    ? "Không thể truy cập dữ liệu chat lúc này"
    : message;
  return Response.json({ error: { code, message: safeMessage } }, { status });
}

function jsonSuccess(
  conversation: Conversation,
  userMessage: ChatMessage,
  assistantMessage: ChatMessage,
  usage: { input_tokens: number; output_tokens: number },
): Response {
  return Response.json({
    conversation,
    user_message: userMessage,
    assistant_message: assistantMessage,
    usage,
  });
}

export function createChatHandler(deps: ChatDependencies) {
  return async (request: Request): Promise<Response> => {
    try {
      if (request.method !== "POST") {
        return jsonError(
          405,
          "METHOD_NOT_ALLOWED",
          "Phương thức không được hỗ trợ",
        );
      }

      const identity = await deps.authenticate(request);
      const store = deps.storeForRequest(request);
      const userId = await store.findUserId(identity.authUid, identity.email);
      if (userId === null) {
        return jsonError(401, "AUTH_REQUIRED", "Phiên đăng nhập đã hết hạn");
      }

      if (new URL(request.url).pathname.endsWith("/feedback")) {
        const input = parseFeedbackRequest(await request.json());
        return Response.json(
          await store.setFeedback(userId, input.message_id, input.value),
        );
      }

      const input = parseChatRequest(await request.json());
      let conversation: Conversation | null = null;
      if (input.conversation_id !== null) {
        conversation = await store.getOrCreateConversation(
          userId,
          input.conversation_id,
          input.message.slice(0, 80),
        );
        const existing = await store.findUserMessage(
          conversation.id,
          input.client_request_id,
        );
        if (existing?.status === "complete") {
          const reply = await store.findAssistantReply(existing.id);
          if (reply !== null) {
            return jsonSuccess(conversation, existing, reply, {
              input_tokens: 0,
              output_tokens: 0,
            });
          }
        }
      }
      if (
        await store.countCompletedToday(userId, deps.now()) >= DAILY_LIMIT
      ) {
        return jsonError(
          429,
          "DAILY_LIMIT_REACHED",
          "Bạn đã dùng hết 15 lượt hỏi hôm nay",
        );
      }
      conversation ??= await store.getOrCreateConversation(
        userId,
        null,
        input.message.slice(0, 80),
      );
      const claim = await store.claimUserMessage(
        conversation.id,
        input.client_request_id,
        input.message,
      );
      if (!claim.claimed) {
        return jsonError(
          409,
          "REQUEST_IN_PROGRESS",
          "Câu hỏi này đang được xử lý",
        );
      }

      const [history, menus, orders] = await Promise.all([
        store.getRecentMessages(
          conversation.id,
          MAX_HISTORY_MESSAGES,
        ),
        store.getMenuContext(input.message, MAX_MENU_ROWS),
        store.getOrderContext(userId, MAX_ORDER_ROWS),
      ]);
      try {
        const ai = await deps.ai.respond(buildPrompt({
          question: input.message,
          history,
          menus,
          orders,
        }));
        const assistantMessage = await store.createAssistantMessage(
          conversation.id,
          claim.message.id,
          ai.text,
        );
        const completedUser = await store.markUserMessage(
          claim.message.id,
          "complete",
        );
        await store.touchConversation(conversation.id);

        return jsonSuccess(conversation, completedUser, assistantMessage, {
          input_tokens: ai.inputTokens,
          output_tokens: ai.outputTokens,
        });
      } catch (error) {
        await store.markUserMessage(claim.message.id, "failed");
        throw error;
      }
    } catch (error) {
      if (error instanceof AuthenticationError) {
        return jsonError(401, "AUTH_REQUIRED", "Phiên đăng nhập đã hết hạn");
      }
      if (error instanceof ValidationError) {
        return jsonError(400, error.code, error.message);
      }
      if (error instanceof SyntaxError) {
        return jsonError(
          400,
          "INVALID_REQUEST",
          "Dữ liệu yêu cầu không hợp lệ",
        );
      }
      if (error instanceof ChatStoreError && error.code === "FORBIDDEN") {
        return jsonError(
          403,
          "FORBIDDEN",
          "Bạn không có quyền truy cập cuộc trò chuyện này",
        );
      }
      if (error instanceof ChatStoreError && error.code === "DATABASE_ERROR") {
        console.error("[chat-assistant] Database error", error);
        return jsonError(
          502,
          "DATABASE_ERROR",
          "Không thể truy cập dữ liệu chat lúc này",
        );
      }
      if (
        error instanceof OpenAiError &&
        error.code === "UPSTREAM_RATE_LIMIT"
      ) {
        return jsonError(
          429,
          "UPSTREAM_RATE_LIMIT",
          "Trợ lý đang quá tải, vui lòng thử lại",
        );
      }
      if (error instanceof OpenAiError) {
        return jsonError(
          502,
          error.code,
          "Không thể nhận phản hồi từ trợ lý lúc này",
        );
      }
      console.error("[chat-assistant] Unhandled error", error);
      return jsonError(
        502,
        "UPSTREAM_ERROR",
        "Không thể xử lý yêu cầu lúc này",
      );
    }
  };
}
