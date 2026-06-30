import { SupabaseChatStore } from "./store.ts";

function assertEquals(actual: unknown, expected: unknown): void {
  if (JSON.stringify(actual) !== JSON.stringify(expected)) {
    throw new Error(
      `Expected ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}`,
    );
  }
}

type UserRow = {
  id: number;
  auth_uid: string;
  email: string | null;
  username: string;
  role: string;
  status: string;
};

function fakeUsersClient(
  initialRows: UserRow[],
  options: { failInsertAndAddExisting?: UserRow } = {},
) {
  const rows = [...initialRows];
  let pendingInsert: Partial<UserRow> | null = null;

  const query = {
    select: () => query,
    eq: (_column: string, value: string) => {
      query.currentAuthUid = value;
      return query;
    },
    maybeSingle: () => {
      if (pendingInsert !== null) {
        if (options.failInsertAndAddExisting !== undefined) {
          rows.push(options.failInsertAndAddExisting);
          pendingInsert = null;
          options.failInsertAndAddExisting = undefined;
          return Promise.resolve({
            data: null,
            error: { code: "23505", message: "duplicate key" },
          });
        }
        const created = { id: rows.length + 1, ...pendingInsert } as UserRow;
        rows.push(created);
        pendingInsert = null;
        return Promise.resolve({ data: { id: created.id }, error: null });
      }
      const found = rows.find((row) => row.auth_uid === query.currentAuthUid);
      return Promise.resolve({
        data: found === undefined ? null : { id: found.id },
        error: null,
      });
    },
    currentAuthUid: "",
  };

  const client = {
    from: (table: string) => {
      if (table !== "users") throw new Error(`Unexpected table ${table}`);
      return {
        select: query.select,
        eq: query.eq,
        maybeSingle: query.maybeSingle,
        insert: (value: Partial<UserRow>) => {
          pendingInsert = value;
          return query;
        },
      };
    },
  };

  return { client, rows };
}

Deno.test("findUserId creates a missing public user profile", async () => {
  const { client, rows } = fakeUsersClient([]);
  const store = new SupabaseChatStore(client as never, client as never);

  const id = await store.findUserId(
    "f55d230d-e324-4eec-9fb7-6b6e6aa0926b",
    "customer@example.com",
  );

  assertEquals(id, 1);
  assertEquals(rows, [{
    id: 1,
    auth_uid: "f55d230d-e324-4eec-9fb7-6b6e6aa0926b",
    email: "customer@example.com",
    username: "customer",
    role: "customer",
    status: "active",
  }]);
});

Deno.test("findUserId rereads after a concurrent profile insert", async () => {
  const authUid = "f55d230d-e324-4eec-9fb7-6b6e6aa0926b";
  const { client, rows } = fakeUsersClient([], {
    failInsertAndAddExisting: {
      id: 7,
      auth_uid: authUid,
      email: "customer@example.com",
      username: "customer",
      role: "customer",
      status: "active",
    },
  });
  const store = new SupabaseChatStore(client as never, client as never);

  const id = await store.findUserId(authUid, "customer@example.com");

  assertEquals(id, 7);
  assertEquals(rows.length, 1);
});
