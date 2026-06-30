do $$
begin
  if exists (
    select 1
    from public.users
    where auth_uid is not null
    group by auth_uid
    having count(*) > 1
  ) then
    raise notice 'Skipped users_auth_uid_unique_idx because duplicate auth_uid rows exist';
  elsif not exists (
    select 1
    from pg_indexes
    where schemaname = 'public'
      and indexname = 'users_auth_uid_unique_idx'
  ) then
    create unique index users_auth_uid_unique_idx
      on public.users (auth_uid)
      where auth_uid is not null;
  end if;
end $$;
