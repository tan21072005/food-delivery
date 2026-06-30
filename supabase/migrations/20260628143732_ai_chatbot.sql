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
