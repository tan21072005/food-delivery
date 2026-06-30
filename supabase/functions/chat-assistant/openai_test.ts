import { OpenAiClient, OpenAiError } from "./openai.ts";

function assertEquals(actual: unknown, expected: unknown): void {
  if (JSON.stringify(actual) !== JSON.stringify(expected)) {
    throw new Error(
      `Expected ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}`,
    );
  }
}

Deno.test("sends the bounded Responses API request and maps usage", async () => {
  let capturedUrl = "";
  let capturedAuthorization = "";
  let capturedBody: Record<string, unknown> = {};
  const fakeFetch: typeof fetch = (_input, init) => {
    capturedUrl = String(_input);
    capturedAuthorization = new Headers(init?.headers).get("authorization") ??
      "";
    capturedBody = JSON.parse(String(init?.body));
    return Promise.resolve(Response.json({
      output_text: "Bạn có thể thử phở bò.",
      usage: { input_tokens: 120, output_tokens: 14 },
    }));
  };

  const result = await new OpenAiClient(
    "test-openai-key",
    "gpt-4o-mini",
    fakeFetch,
  ).respond({ system: "system prompt", user: "user prompt" });

  assertEquals(capturedUrl, "https://api.openai.com/v1/responses");
  assertEquals(capturedAuthorization, "Bearer test-openai-key");
  assertEquals(capturedBody.model, "gpt-4o-mini");
  assertEquals(capturedBody.max_output_tokens, 500);
  assertEquals(capturedBody.input, [
    {
      role: "system",
      content: [{ type: "input_text", text: "system prompt" }],
    },
    {
      role: "user",
      content: [{ type: "input_text", text: "user prompt" }],
    },
  ]);
  assertEquals(result, {
    text: "Bạn có thể thử phở bò.",
    inputTokens: 120,
    outputTokens: 14,
  });
});

Deno.test("rejects an empty Responses API output", async () => {
  const client = new OpenAiClient(
    "test-openai-key",
    "gpt-4o-mini",
    () => Promise.resolve(Response.json({ output: [], usage: {} })),
  );
  let code = "";
  try {
    await client.respond({ system: "system", user: "user" });
  } catch (error) {
    code = error instanceof OpenAiError ? error.code : "";
  }
  assertEquals(code, "UPSTREAM_INVALID_RESPONSE");
});

Deno.test("maps OpenAI rate limiting without exposing its body", async () => {
  const client = new OpenAiClient(
    "test-openai-key",
    "gpt-4o-mini",
    () =>
      Promise.resolve(
        new Response("sensitive provider detail", { status: 429 }),
      ),
  );
  let code = "";
  try {
    await client.respond({ system: "system", user: "user" });
  } catch (error) {
    code = error instanceof OpenAiError ? error.code : "";
  }
  assertEquals(code, "UPSTREAM_RATE_LIMIT");
});

Deno.test("maps OpenAI network failures to upstream error", async () => {
  const client = new OpenAiClient(
    "test-openai-key",
    "gpt-4o-mini",
    () => Promise.reject(new Error("network down")),
  );
  let code = "";
  try {
    await client.respond({ system: "system", user: "user" });
  } catch (error) {
    code = error instanceof OpenAiError ? error.code : "";
  }
  assertEquals(code, "UPSTREAM_ERROR");
});
