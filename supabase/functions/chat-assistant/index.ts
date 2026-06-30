import {
  createClient,
  type SupabaseClient,
} from "npm:@supabase/supabase-js@2.108.2";
import { AuthenticationError, createChatHandler } from "./handler.ts";
import { OpenAiClient } from "./openai.ts";
import { SupabaseChatStore } from "./store.ts";

function requiredEnvironment(name: string): string {
  const value = Deno.env.get(name)?.trim();
  if (!value) {
    throw new Error("Required server configuration is missing");
  }
  return value;
}

function bearerToken(request: Request): string {
  const value = request.headers.get("authorization") ?? "";
  const match = /^Bearer\s+(.+)$/i.exec(value);
  if (!match?.[1]) throw new AuthenticationError();
  return match[1];
}

const supabaseUrl = requiredEnvironment("SUPABASE_URL");
const supabaseAnonKey = requiredEnvironment("SUPABASE_ANON_KEY");
const supabaseServiceRoleKey = requiredEnvironment(
  "SUPABASE_SERVICE_ROLE_KEY",
);
const openAiKey = requiredEnvironment("OPENAI_API_KEY");
const openAiModel = Deno.env.get("OPENAI_MODEL")?.trim() || "gpt-4o-mini";

const admin: SupabaseClient = createClient(
  supabaseUrl,
  supabaseServiceRoleKey,
  {
    auth: { autoRefreshToken: false, persistSession: false },
  },
);

const handler = createChatHandler({
  authenticate: async (request) => {
    const token = bearerToken(request);
    const { data, error } = await admin.auth.getUser(token);
    if (error || data.user === null) throw new AuthenticationError();
    return { authUid: data.user.id, email: data.user.email ?? null };
  },
  storeForRequest: (request) => {
    const token = bearerToken(request);
    const scoped = createClient(supabaseUrl, supabaseAnonKey, {
      global: { headers: { authorization: `Bearer ${token}` } },
      auth: { autoRefreshToken: false, persistSession: false },
    });
    return new SupabaseChatStore(scoped, admin);
  },
  ai: new OpenAiClient(openAiKey, openAiModel),
  now: () => new Date(),
});

Deno.serve(handler);
