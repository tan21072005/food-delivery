import { MAX_OUTPUT_TOKENS, type OpenAiInput } from "./domain.ts";

export type AiResult = {
  text: string;
  inputTokens: number;
  outputTokens: number;
};

export interface AiClient {
  respond(input: OpenAiInput): Promise<AiResult>;
}

export class OpenAiError extends Error {
  constructor(
    public readonly code:
      | "UPSTREAM_INVALID_RESPONSE"
      | "UPSTREAM_RATE_LIMIT"
      | "UPSTREAM_ERROR",
  ) {
    super(code);
    this.name = "OpenAiError";
  }
}

type OpenAiResponse = {
  output_text?: unknown;
  output?: Array<{ content?: Array<{ type?: string; text?: unknown }> }>;
  usage?: { input_tokens?: unknown; output_tokens?: unknown };
};

function readOutputText(body: OpenAiResponse): string {
  if (typeof body.output_text === "string" && body.output_text.trim() !== "") {
    return body.output_text.trim();
  }
  const text = body.output
    ?.flatMap((item) => item.content ?? [])
    .filter((item) => item.type === "output_text")
    .map((item) => typeof item.text === "string" ? item.text : "")
    .join("")
    .trim();
  if (!text) {
    throw new OpenAiError("UPSTREAM_INVALID_RESPONSE");
  }
  return text;
}

export class OpenAiClient implements AiClient {
  constructor(
    private readonly apiKey: string,
    private readonly model = "gpt-4o-mini",
    private readonly fetchFn: typeof fetch = fetch,
  ) {}

  async respond(input: OpenAiInput): Promise<AiResult> {
    let response: Response;
    try {
      response = await this.fetchFn(
        "https://api.openai.com/v1/responses",
        {
          method: "POST",
          headers: {
            authorization: `Bearer ${this.apiKey}`,
            "content-type": "application/json",
          },
          body: JSON.stringify({
            model: this.model,
            input: [
              {
                role: "system",
                content: [{ type: "input_text", text: input.system }],
              },
              {
                role: "user",
                content: [{ type: "input_text", text: input.user }],
              },
            ],
            max_output_tokens: MAX_OUTPUT_TOKENS,
          }),
        },
      );
    } catch (error) {
      console.error("[chat-assistant] OpenAI network failure", error);
      throw new OpenAiError("UPSTREAM_ERROR");
    }

    if (response.status === 429) {
      throw new OpenAiError("UPSTREAM_RATE_LIMIT");
    }
    if (!response.ok) {
      const errorText = await response.text().catch(() => "");
      console.error(
        "[chat-assistant] OpenAI error response",
        response.status,
        errorText.slice(0, 1000),
      );
      throw new OpenAiError("UPSTREAM_ERROR");
    }

    try {
      const body = await response.json() as OpenAiResponse;
      return {
        text: readOutputText(body),
        inputTokens: typeof body.usage?.input_tokens === "number"
          ? body.usage.input_tokens
          : 0,
        outputTokens: typeof body.usage?.output_tokens === "number"
          ? body.usage.output_tokens
          : 0,
      };
    } catch (error) {
      if (error instanceof OpenAiError) throw error;
      console.error("[chat-assistant] OpenAI invalid JSON response", error);
      throw new OpenAiError("UPSTREAM_INVALID_RESPONSE");
    }
  }
}
