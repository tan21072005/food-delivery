import { buildPrompt, parseChatRequest, ValidationError } from "./domain.ts";

function assertEquals(actual: unknown, expected: unknown): void {
  if (JSON.stringify(actual) !== JSON.stringify(expected)) {
    throw new Error(
      `Expected ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}`,
    );
  }
}

Deno.test("trims a valid request", () => {
  const parsed = parseChatRequest({
    conversation_id: null,
    message: "  Gợi ý món trưa  ",
    client_request_id: "a5d8d9b4-70d7-47a3-b856-b5cbbbd694dd",
  });
  assertEquals(parsed.message, "Gợi ý món trưa");
});

Deno.test("rejects messages longer than 1000 characters", () => {
  let thrown = false;
  try {
    parseChatRequest({
      conversation_id: null,
      message: "a".repeat(1001),
      client_request_id: crypto.randomUUID(),
    });
  } catch (error) {
    thrown = error instanceof ValidationError;
  }
  assertEquals(thrown, true);
});

Deno.test("prompt excludes private order fields", () => {
  const prompt = buildPrompt({
    question: "Đơn gần nhất?",
    history: [],
    menus: [],
    orders: [{
      id: 7,
      status: "pending",
      total_amount: 90000,
      created_at: "2026-06-28",
    }],
  });
  const serialized = JSON.stringify(prompt);
  assertEquals(serialized.includes("delivery_address"), false);
  assertEquals(serialized.includes("payment"), false);
});

Deno.test("rejects invalid UUIDs and blank messages", () => {
  for (
    const value of [
      {
        conversation_id: null,
        message: " ",
        client_request_id: crypto.randomUUID(),
      },
      {
        conversation_id: "bad",
        message: "ok",
        client_request_id: crypto.randomUUID(),
      },
      { conversation_id: null, message: "ok", client_request_id: "bad" },
    ]
  ) {
    let thrown = false;
    try {
      parseChatRequest(value);
    } catch (error) {
      thrown = error instanceof ValidationError;
    }
    assertEquals(thrown, true);
  }
});

Deno.test("treats a blank conversation id as a new conversation", () => {
  const parsed = parseChatRequest({
    conversation_id: "",
    message: "xin chao",
    client_request_id: "1a94f0e4-7e1c-4f3b-bc2f-2c8f3dd2c8a1",
  });

  assertEquals(parsed.conversation_id, null);
});

Deno.test("treats a missing conversation id as a new conversation", () => {
  const parsed = parseChatRequest({
    message: "xin chao",
    client_request_id: "1a94f0e4-7e1c-4f3b-bc2f-2c8f3dd2c8a1",
  });

  assertEquals(parsed.conversation_id, null);
});
