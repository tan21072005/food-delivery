# AI Chatbot Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the authenticated Supabase database and Edge Function that persist NGBT chat history, read bounded food/order context, call OpenAI, and enforce student-project cost limits.

**Architecture:** Android calls one authenticated `chat-assistant` Edge Function to send or retry a message. The function derives identity from the JWT, uses an RLS-scoped Supabase client for reads, uses a server-only admin client for chat writes, and calls the OpenAI Responses API through a small injected client. Android reads history through PostgREST under RLS and may only rename/delete its own conversations or upsert feedback.

**Tech Stack:** PostgreSQL/Supabase RLS, Supabase Edge Functions, TypeScript/Deno, `@supabase/supabase-js`, OpenAI Responses API, Deno tests with injected fakes.

## Global Constraints

- The screen title is exactly `Chat bot trợ lý NGBT`.
- The AI is read-only with respect to carts, orders, menus, restaurants, users, and payments.
- The model default is `gpt-4o-mini`, configured through `OPENAI_MODEL`.
- Input is plain text, trimmed, and limited to 1,000 characters.
- Output is limited to 500 tokens.
- Context contains at most 8 recent messages, 10 menu/restaurant rows, and 5 recent orders.
- Each authenticated user may complete at most 15 questions per UTC day.
- Never send address, phone, email, payment data, JWTs, or secrets to OpenAI.
- Never expose the OpenAI key or Supabase service-role key to Android.
- Do not commit or push unless the user explicitly requests it.

---

## File Map

- Create through `supabase migration new ai_chatbot`: `supabase/migrations/20260628000100_ai_chatbot.sql` — tables, indexes, grants, RLS, ownership policies, and feedback integrity policies. If the CLI emits a different timestamp, keep its generated filename and place the exact SQL from Task 1 there.
- Create: `supabase/config.toml` — local function JWT configuration if Supabase initialization creates no config.
- Create: `supabase/functions/chat-assistant/domain.ts` — request/response types, validation, daily-limit calculation, prompt/context shaping.
- Create: `supabase/functions/chat-assistant/store.ts` — narrow `ChatStore` interface and Supabase implementation.
- Create: `supabase/functions/chat-assistant/openai.ts` — OpenAI request/response mapping.
- Create: `supabase/functions/chat-assistant/handler.ts` — transport-independent orchestration with injected dependencies.
- Create: `supabase/functions/chat-assistant/index.ts` — Deno/Supabase wiring only.
- Create: `supabase/functions/chat-assistant/domain_test.ts` — pure validation and prompt tests.
- Create: `supabase/functions/chat-assistant/handler_test.ts` — success, authorization, retry, limit, and upstream failure tests.
- Create: `supabase/functions/chat-assistant/openai_test.ts` — OpenAI payload and response parsing tests.
- Create: `supabase/functions/.env.example` — secret names only, never values.
- Create: `docs/chatbot-backend-setup.md` — local test, deployment, secret, and manual verification commands.

### Shared API contract

Request:

```json
{
  "conversation_id": null,
  "message": "Hôm nay tôi nên ăn gì?",
  "client_request_id": "a5d8d9b4-70d7-47a3-b856-b5cbbbd694dd"
}
```

Success response:

```json
{
  "conversation": {
    "id": "f083ea9f-4621-4d97-bae0-5676c4fcecd9",
    "title": "Hôm nay tôi nên ăn gì?",
    "created_at": "2026-06-28T10:00:00Z",
    "updated_at": "2026-06-28T10:00:02Z"
  },
  "user_message": {
    "id": 101,
    "conversation_id": "f083ea9f-4621-4d97-bae0-5676c4fcecd9",
    "role": "user",
    "content": "Hôm nay tôi nên ăn gì?",
    "status": "complete",
    "created_at": "2026-06-28T10:00:00Z"
  },
  "assistant_message": {
    "id": 102,
    "conversation_id": "f083ea9f-4621-4d97-bae0-5676c4fcecd9",
    "role": "assistant",
    "content": "Bạn có thể thử phở bò của Nhà hàng NGBT.",
    "status": "complete",
    "created_at": "2026-06-28T10:00:02Z"
  },
  "usage": {
    "input_tokens": 1800,
    "output_tokens": 180
  }
}
```

Error response:

```json
{
  "error": {
    "code": "DAILY_LIMIT_REACHED",
    "message": "Bạn đã dùng hết 15 lượt hỏi hôm nay"
  }
}
```

### Task 1: Create the chat schema and RLS boundary

**Files:**

- Create: `supabase/config.toml`
- Create via CLI: `supabase/migrations/20260628000100_ai_chatbot.sql`

**Interfaces:**

- Consumes: existing `public.users(id bigint, auth_uid uuid)`, `menus`, `restaurants`, `orders`, and `order_items`.
- Produces: `chat_conversations`, `chat_messages`, and `chat_feedback`, readable through PostgREST by their owner.

- [ ] **Step 1: Discover the installed CLI and create the migration**

Run:

```powershell
supabase --version
supabase migration new ai_chatbot
```

Expected: the version prints, and the CLI creates one file ending in `_ai_chatbot.sql`. Use that generated path instead of inventing another migration-history entry.

- [ ] **Step 2: Write the schema migration**

Put the following SQL in the generated migration:

```sql
create table public.chat_conversations (
  id uuid primary key default gen_random_uuid(),
  user_id bigint not null references public.users(id) on delete cascade,
  title text not null check (char_length(title) between 1 and 120),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.chat_messages (
  id bigint generated always as identity primary key,
  conversation_id uuid not null
    references public.chat_conversations(id) on delete cascade,
  client_request_id uuid,
  reply_to_message_id bigint references public.chat_messages(id) on delete cascade,
  role text not null check (role in ('user', 'assistant')),
  content text not null check (char_length(content) between 1 and 12000),
  status text not null check (status in ('pending', 'complete', 'failed')),
  created_at timestamptz not null default now()
);

create table public.chat_feedback (
  id bigint generated always as identity primary key,
  message_id bigint not null references public.chat_messages(id) on delete cascade,
  user_id bigint not null references public.users(id) on delete cascade,
  value smallint not null check (value in (-1, 1)),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (message_id, user_id)
);

create index chat_conversations_user_updated_idx
  on public.chat_conversations (user_id, updated_at desc);
create index chat_messages_conversation_created_idx
  on public.chat_messages (conversation_id, created_at, id);
create unique index chat_messages_request_idx
  on public.chat_messages (conversation_id, client_request_id)
  where role = 'user' and client_request_id is not null;
create unique index chat_messages_reply_idx
  on public.chat_messages (reply_to_message_id)
  where reply_to_message_id is not null;
create index chat_feedback_user_idx on public.chat_feedback (user_id);

alter table public.chat_conversations enable row level security;
alter table public.chat_messages enable row level security;
alter table public.chat_feedback enable row level security;

revoke all on public.chat_conversations from anon, authenticated;
revoke all on public.chat_messages from anon, authenticated;
revoke all on public.chat_feedback from anon, authenticated;

grant select, delete on public.chat_conversations to authenticated;
grant update (title) on public.chat_conversations to authenticated;
grant select on public.chat_messages to authenticated;
grant select, insert, update, delete on public.chat_feedback to authenticated;
grant usage, select on sequence public.chat_feedback_id_seq to authenticated;

create policy chat_conversations_select_own
on public.chat_conversations for select to authenticated
using (
  exists (
    select 1 from public.users u
    where u.id = chat_conversations.user_id
      and u.auth_uid = (select auth.uid())
  )
);

create policy chat_conversations_update_own
on public.chat_conversations for update to authenticated
using (
  exists (
    select 1 from public.users u
    where u.id = chat_conversations.user_id
      and u.auth_uid = (select auth.uid())
  )
)
with check (
  exists (
    select 1 from public.users u
    where u.id = chat_conversations.user_id
      and u.auth_uid = (select auth.uid())
  )
);

create policy chat_conversations_delete_own
on public.chat_conversations for delete to authenticated
using (
  exists (
    select 1 from public.users u
    where u.id = chat_conversations.user_id
      and u.auth_uid = (select auth.uid())
  )
);

create policy chat_messages_select_own
on public.chat_messages for select to authenticated
using (
  exists (
    select 1
    from public.chat_conversations c
    join public.users u on u.id = c.user_id
    where c.id = chat_messages.conversation_id
      and u.auth_uid = (select auth.uid())
  )
);

create policy chat_feedback_select_own
on public.chat_feedback for select to authenticated
using (
  exists (
    select 1 from public.users u
    where u.id = chat_feedback.user_id
      and u.auth_uid = (select auth.uid())
  )
);

create policy chat_feedback_insert_own_assistant
on public.chat_feedback for insert to authenticated
with check (
  exists (
    select 1
    from public.users u
    join public.chat_conversations c on c.user_id = u.id
    join public.chat_messages m on m.conversation_id = c.id
    where u.id = chat_feedback.user_id
      and u.auth_uid = (select auth.uid())
      and m.id = chat_feedback.message_id
      and m.role = 'assistant'
  )
);

create policy chat_feedback_update_own_assistant
on public.chat_feedback for update to authenticated
using (
  exists (
    select 1 from public.users u
    where u.id = chat_feedback.user_id
      and u.auth_uid = (select auth.uid())
  )
)
with check (
  exists (
    select 1
    from public.users u
    join public.chat_conversations c on c.user_id = u.id
    join public.chat_messages m on m.conversation_id = c.id
    where u.id = chat_feedback.user_id
      and u.auth_uid = (select auth.uid())
      and m.id = chat_feedback.message_id
      and m.role = 'assistant'
  )
);

create policy chat_feedback_delete_own
on public.chat_feedback for delete to authenticated
using (
  exists (
    select 1 from public.users u
    where u.id = chat_feedback.user_id
      and u.auth_uid = (select auth.uid())
  )
);
```

- [ ] **Step 3: Apply locally and verify ownership isolation**

Run:

```powershell
supabase db reset
supabase db advisors
supabase migration list --local
```

Expected: migration is applied once; advisors report no exposed-table/RLS errors; the migration appears in the local list.

Use two Supabase test users and run PostgREST requests with each JWT. Expected:

- each user selects only their own conversations/messages/feedback;
- direct conversation/message insert returns permission denied;
- feedback on a user message fails;
- feedback on the caller's assistant message succeeds;
- deleting a conversation cascades its messages and feedback.

### Task 2: Implement pure validation, context shaping, and prompt rules

**Files:**

- Create: `supabase/functions/chat-assistant/domain.ts`
- Test: `supabase/functions/chat-assistant/domain_test.ts`

**Interfaces:**

- Produces: `parseChatRequest(value): ChatRequest`, `buildPrompt(input): OpenAiInput`, `toHttpError(error): ErrorBody`.
- Consumes: no network or Supabase dependencies.

- [ ] **Step 1: Write failing domain tests**

Cover these exact cases in `domain_test.ts`:

```ts
Deno.test("trims a valid request", () => {
  const parsed = parseChatRequest({
    conversation_id: null,
    message: "  Gợi ý món trưa  ",
    client_request_id: "a5d8d9b4-70d7-47a3-b856-b5cbbbd694dd",
  });
  assertEquals(parsed.message, "Gợi ý món trưa");
});

Deno.test("rejects messages longer than 1000 characters", () => {
  assertThrows(
    () => parseChatRequest({
      conversation_id: null,
      message: "a".repeat(1001),
      client_request_id: crypto.randomUUID(),
    }),
    ValidationError,
  );
});

Deno.test("prompt excludes private order fields", () => {
  const prompt = buildPrompt({
    question: "Đơn gần nhất?",
    history: [],
    menus: [],
    orders: [{ id: 7, status: "pending", total_amount: 90000, created_at: "2026-06-28" }],
  });
  const serialized = JSON.stringify(prompt);
  assertEquals(serialized.includes("delivery_address"), false);
  assertEquals(serialized.includes("payment"), false);
});
```

- [ ] **Step 2: Run tests and confirm red**

Run:

```powershell
deno test supabase/functions/chat-assistant/domain_test.ts
```

Expected: FAIL because `domain.ts` exports do not exist.

- [ ] **Step 3: Implement the domain boundary**

Define these exact types and constants:

```ts
export const MAX_MESSAGE_CHARS = 1000;
export const MAX_HISTORY_MESSAGES = 8;
export const MAX_MENU_ROWS = 10;
export const MAX_ORDER_ROWS = 5;
export const MAX_OUTPUT_TOKENS = 500;
export const DAILY_LIMIT = 15;

export type ChatRequest = {
  conversation_id: string | null;
  message: string;
  client_request_id: string;
};

export type ContextMessage = { role: "user" | "assistant"; content: string };
export type Conversation = {
  id: string; title: string; created_at: string; updated_at: string;
};
export type ChatMessage = {
  id: number; conversation_id: string; role: "user" | "assistant";
  content: string; status: "pending" | "complete" | "failed";
  created_at: string;
};
export type MenuContext = {
  id: number; item_name: string; description: string | null;
  price: number; rating: number; restaurant_name: string; is_open: boolean;
};
export type OrderContext = {
  id: number; status: string; total_amount: number; created_at: string;
};
export type OpenAiInput = {
  system: string;
  user: string;
};

export class ValidationError extends Error {
  constructor(public readonly code: string, message: string) {
    super(message);
  }
}
```

`parseChatRequest` must require an object, valid UUID request id, nullable valid UUID conversation id, non-empty trimmed text, and at most 1,000 characters. `buildPrompt` must return a Vietnamese system instruction plus delimited JSON containing only the declared context fields, slicing arrays to the constants above.

- [ ] **Step 4: Run domain tests**

Run:

```powershell
deno test supabase/functions/chat-assistant/domain_test.ts
```

Expected: all domain tests PASS.

### Task 3: Implement the Supabase store and bounded context queries

**Files:**

- Create: `supabase/functions/chat-assistant/store.ts`
- Modify: `supabase/functions/chat-assistant/domain.ts`
- Test: `supabase/functions/chat-assistant/handler_test.ts`

**Interfaces:**

- Produces `ChatStore`:

```ts
export interface ChatStore {
  findUserId(authUid: string): Promise<number | null>;
  countCompletedToday(userId: number, now: Date): Promise<number>;
  getOrCreateConversation(userId: number, conversationId: string | null, title: string): Promise<Conversation>;
  findUserMessage(conversationId: string, requestId: string): Promise<ChatMessage | null>;
  findAssistantReply(userMessageId: number): Promise<ChatMessage | null>;
  claimUserMessage(conversationId: string, requestId: string, content: string):
    Promise<{ message: ChatMessage; claimed: boolean }>;
  markUserMessage(id: number, status: "pending" | "complete" | "failed"): Promise<ChatMessage>;
  createAssistantMessage(conversationId: string, userMessageId: number, content: string): Promise<ChatMessage>;
  getRecentMessages(conversationId: string, limit: number): Promise<ContextMessage[]>;
  getMenuContext(question: string, limit: number): Promise<MenuContext[]>;
  getOrderContext(userId: number, limit: number): Promise<OrderContext[]>;
  touchConversation(conversationId: string): Promise<void>;
}
```

- [ ] **Step 1: Add handler-facing fake-store tests**

Add tests proving:

- an unknown `auth_uid` returns 401/`AUTH_REQUIRED`;
- a supplied conversation owned by another user returns 403/`FORBIDDEN`;
- menu context is limited to 10 rows;
- order context is filtered by the mapped numeric `users.id`;
- the 16th completed message in a UTC day returns 429/`DAILY_LIMIT_REACHED`.

- [ ] **Step 2: Run the tests and confirm red**

Run:

```powershell
deno test supabase/functions/chat-assistant/handler_test.ts
```

Expected: FAIL because `ChatStore` and the handler do not exist.

- [ ] **Step 3: Implement `SupabaseChatStore`**

Use two injected clients:

```ts
export class SupabaseChatStore implements ChatStore {
  constructor(
    private readonly scoped: SupabaseClient,
    private readonly admin: SupabaseClient,
  ) {}
}
```

Rules for each query:

- `scoped` reads the caller-visible conversation/messages/orders under RLS.
- `admin` maps `users.auth_uid`, creates chat rows, and changes message status.
- every admin mutation includes both the mapped `userId` and conversation ownership check;
- `claimUserMessage` inserts a pending row, catches unique-constraint conflicts,
  and conditionally changes `failed` to `pending`; it returns `claimed=false`
  for rows already `pending` or `complete`, so simultaneous retries cannot make
  two OpenAI calls;
- menu query selects only `id,item_name,description,price,rating,restaurants(name,is_open)`, filters `status=eq.active`, and limits 10;
- order query selects only `id,status,total_amount,created_at`, filters by the
  numeric id returned from `findUserId`, sorts descending, and limits 5;
- daily count joins messages to conversations, filters `role=user`, `status=complete`, owner, and UTC day bounds.

- [ ] **Step 4: Run handler/store tests**

Run:

```powershell
deno test supabase/functions/chat-assistant/handler_test.ts
```

Expected: ownership, context-bound, and daily-limit tests PASS.

### Task 4: Implement the OpenAI adapter

**Files:**

- Create: `supabase/functions/chat-assistant/openai.ts`
- Test: `supabase/functions/chat-assistant/openai_test.ts`

**Interfaces:**

- Produces:

```ts
export interface AiClient {
  respond(input: OpenAiInput): Promise<AiResult>;
}

export type AiResult = {
  text: string;
  inputTokens: number;
  outputTokens: number;
};
```

- [ ] **Step 1: Write the failing adapter tests**

Use an injected `fetch` fake and assert:

- URL is `https://api.openai.com/v1/responses`;
- authorization uses `Bearer test-openai-key` in the fake-fetch assertion;
- body uses configured model and `max_output_tokens: 500`;
- empty/missing `output_text` becomes `UPSTREAM_INVALID_RESPONSE`;
- HTTP 429 becomes `UPSTREAM_RATE_LIMIT`;
- token usage maps to `AiResult`.

- [ ] **Step 2: Run and confirm red**

Run:

```powershell
deno test supabase/functions/chat-assistant/openai_test.ts
```

Expected: FAIL because `OpenAiClient` does not exist.

- [ ] **Step 3: Implement `OpenAiClient`**

Constructor:

```ts
export class OpenAiClient implements AiClient {
  constructor(
    private readonly apiKey: string,
    private readonly model = "gpt-4o-mini",
    private readonly fetchFn: typeof fetch = fetch,
  ) {}
}
```

Send:

```json
{
  "model": "gpt-4o-mini",
  "input": [
    { "role": "system", "content": [{ "type": "input_text", "text": "Bạn là trợ lý giao đồ ăn chỉ đọc dữ liệu được cung cấp." }] },
    { "role": "user", "content": [{ "type": "input_text", "text": "Gợi ý món trưa từ dữ liệu thực đơn." }] }
  ],
  "max_output_tokens": 500
}
```

Read `output_text`, `usage.input_tokens`, and `usage.output_tokens`. Never log the request body or authorization header.

- [ ] **Step 4: Run OpenAI adapter tests**

Run:

```powershell
deno test supabase/functions/chat-assistant/openai_test.ts
```

Expected: all adapter tests PASS.

### Task 5: Orchestrate idempotent chat requests

**Files:**

- Create: `supabase/functions/chat-assistant/handler.ts`
- Create: `supabase/functions/chat-assistant/index.ts`
- Modify: `supabase/functions/chat-assistant/handler_test.ts`
- Create: `supabase/functions/.env.example`

**Interfaces:**

- Consumes: `ChatStore`, `AiClient`, `parseChatRequest`, and `buildPrompt`.
- Produces: `createChatHandler(deps): (request: Request) => Promise<Response>`.

- [ ] **Step 1: Complete failing orchestration tests**

Test these transitions:

1. new request creates conversation → pending user message → assistant message → user message complete;
2. OpenAI failure marks the user message failed and returns mapped 5xx;
3. retry with the same request id reuses the failed row and does not insert another user message;
4. duplicate completed request returns the existing result without another OpenAI call;
5. missing/invalid bearer token returns 401;
6. invalid input returns 400;
7. upstream 429 and daily 429 use different error codes.

- [ ] **Step 2: Run and confirm red**

Run:

```powershell
deno test supabase/functions/chat-assistant/handler_test.ts
```

Expected: the new orchestration tests FAIL.

- [ ] **Step 3: Implement the handler**

Use injected dependencies:

```ts
export type ChatDependencies = {
  authenticate(request: Request): Promise<{ authUid: string }>;
  store: ChatStore;
  ai: AiClient;
  now(): Date;
};

export function createChatHandler(deps: ChatDependencies) {
  return async (request: Request): Promise<Response> => {
    try {
      if (request.method !== "POST") {
        return jsonError(405, "METHOD_NOT_ALLOWED", "Phương thức không được hỗ trợ");
      }
      const identity = await deps.authenticate(request);
      const userId = await deps.store.findUserId(identity.authUid);
      if (userId === null) {
        return jsonError(401, "AUTH_REQUIRED", "Phiên đăng nhập đã hết hạn");
      }
      const input = parseChatRequest(await request.json());
      let conversation: Conversation | null = null;
      let existing: ChatMessage | null = null;
      if (input.conversation_id !== null) {
        conversation = await deps.store.getOrCreateConversation(
          userId,
          input.conversation_id,
          input.message.slice(0, 80),
        );
        existing = await deps.store.findUserMessage(
          conversation.id,
          input.client_request_id,
        );
        if (existing?.status === "complete") {
          const reply = await deps.store.findAssistantReply(existing.id);
          if (reply !== null) {
            return jsonSuccess(
              conversation,
              existing,
              reply,
              { input_tokens: 0, output_tokens: 0 },
            );
          }
        }
      }
      if (await deps.store.countCompletedToday(userId, deps.now()) >= DAILY_LIMIT) {
        return jsonError(429, "DAILY_LIMIT_REACHED", "Bạn đã dùng hết 15 lượt hỏi hôm nay");
      }
      conversation ??= await deps.store.getOrCreateConversation(
        userId,
        null,
        input.message.slice(0, 80),
      );
      const claim = await deps.store.claimUserMessage(
        conversation.id,
        input.client_request_id,
        input.message,
      );
      if (!claim.claimed) {
        if (claim.message.status === "complete") {
          const reply = await deps.store.findAssistantReply(claim.message.id);
          if (reply !== null) {
            return jsonSuccess(
              conversation,
              claim.message,
              reply,
              { input_tokens: 0, output_tokens: 0 },
            );
          }
        }
        return jsonError(
          409,
          "REQUEST_IN_PROGRESS",
          "Câu hỏi này đang được xử lý",
        );
      }
      const userMessage = claim.message;
      const [history, menus, orders] = await Promise.all([
        deps.store.getRecentMessages(conversation.id, MAX_HISTORY_MESSAGES),
        deps.store.getMenuContext(input.message, MAX_MENU_ROWS),
        deps.store.getOrderContext(userId, MAX_ORDER_ROWS),
      ]);
      try {
        const ai = await deps.ai.respond(buildPrompt({
          question: input.message,
          history,
          menus,
          orders,
        }));
        const reply = await deps.store.createAssistantMessage(
          conversation.id,
          userMessage.id,
          ai.text,
        );
        const completedUser = await deps.store.markUserMessage(userMessage.id, "complete");
        await deps.store.touchConversation(conversation.id);
        return jsonSuccess(conversation, completedUser, reply, {
          input_tokens: ai.inputTokens,
          output_tokens: ai.outputTokens,
        });
      } catch (error) {
        await deps.store.markUserMessage(userMessage.id, "failed");
        throw error;
      }
    } catch (error) {
      return mapException(error);
    }
  };
}
```

Define `jsonSuccess`, `jsonError`, and `mapException` in the same file. They
must always return the shared JSON envelopes and map `ValidationError`,
authentication errors, forbidden ownership, upstream 429, and generic
upstream failures to 400, 401, 403, 429, and 502 respectively.

Return only the shared API contract. Use `Content-Type: application/json`.
Never return raw Supabase/OpenAI errors.

- [ ] **Step 4: Wire `index.ts`**

`index.ts` must:

- read `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY`,
  `OPENAI_API_KEY`, and optional `OPENAI_MODEL`;
- fail startup with a generic message if a required value is missing;
- validate the bearer token through Supabase Auth `getUser(token)`;
- create an RLS-scoped client carrying the bearer header;
- create an admin client only inside the function runtime;
- pass both clients into `SupabaseChatStore`;
- call `Deno.serve(createChatHandler(dependencies))`.

Write only secret names in `.env.example`:

```dotenv
OPENAI_API_KEY=
OPENAI_MODEL=gpt-4o-mini
```

- [ ] **Step 5: Run all backend tests**

Run:

```powershell
deno fmt --check supabase/functions/chat-assistant
deno lint supabase/functions/chat-assistant
deno test supabase/functions/chat-assistant
```

Expected: format check, lint, and all tests PASS.

### Task 6: Document and verify local/deployed operation

**Files:**

- Create: `docs/chatbot-backend-setup.md`
- Modify: `supabase/config.toml`

**Interfaces:**

- Produces: repeatable setup for another student or evaluator without exposing credentials.

- [ ] **Step 1: Write exact setup commands**

Document:

```powershell
supabase login
$projectRef = Read-Host 'Supabase project ref'
supabase link --project-ref $projectRef
supabase secrets set OPENAI_API_KEY
supabase secrets set OPENAI_MODEL=gpt-4o-mini
supabase db push
supabase functions deploy chat-assistant
```

Explain that the project ref and key values are entered interactively and must
never be committed. Keep `verify_jwt = true` for `chat-assistant`.

- [ ] **Step 2: Verify locally**

Run:

```powershell
supabase start
supabase functions serve chat-assistant --env-file supabase/functions/.env.local
```

Invoke with a real local test-user JWT. Expected:

- a food question returns 200 and persists two messages;
- an order question returns only the caller's Supabase order;
- an anonymous request returns 401;
- a 1,001-character request returns 400;
- the 16th completed question returns 429.

- [ ] **Step 3: Run database security checks**

Run:

```powershell
supabase db advisors
supabase migration list --local
```

Expected: no unresolved security advisor findings caused by the chat migration; local migration list is consistent.

- [ ] **Step 4: Record deployment prerequisites**

End the setup doc with:

- backend code and mock tests can be completed locally;
- live deployment requires Supabase project access;
- end-to-end OpenAI testing requires an API key with billing;
- recommended development budget is USD 5 with an OpenAI project hard limit;
- no secret value belongs in `local.properties`, Git, APK resources, logs, or screenshots.
