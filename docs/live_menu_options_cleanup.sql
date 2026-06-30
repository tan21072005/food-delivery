-- Cleanup after docs/live_menu_options_patch.sql.
-- Run in Supabase SQL Editor if the UI shows duplicate-ish option choices.
--
-- This does not delete rows. It marks old live aliases unavailable so
-- get_menu_item_detail_v3 stops returning them, while historical cart/order
-- references can still resolve the old option_choice_id.

begin;

with old_option_aliases (
  restaurant_name,
  menu_name,
  group_name,
  choice_name
) as (
  values
    ('Bun Bo Hue Dong Ba', 'Bun bo dac biet', 'Size', 'To thuong'),
    ('Bun Bo Hue Dong Ba', 'Bun bo dac biet', 'Size', 'To lon'),
    ('Bun Bo Hue Dong Ba', 'Bun bo dac biet', 'Topping', 'Them bo'),
    ('Bun Bo Hue Dong Ba', 'Bun bo tai', 'Size', 'To thuong'),
    ('Bun Bo Hue Dong Ba', 'Bun bo tai', 'Size', 'To lon'),
    ('Pizza 4Ps Le Loi', 'Pizza hai san size M', 'Vien pizza', 'Vien pho mai')
)
update public.menu_option_choices moc
set is_available = false
from old_option_aliases old
join public.restaurants r on r.name = old.restaurant_name
join public.menu_items mi on mi.restaurant_id = r.id and mi.name = old.menu_name
join public.menu_option_groups mog on mog.menu_item_id = mi.id and mog.name = old.group_name
where moc.option_group_id = mog.id
  and moc.name = old.choice_name;

commit;

-- Verification: these rows should still exist but be unavailable.
select
  mi.id,
  mi.name as menu_name,
  mog.name as group_name,
  moc.name as choice_name,
  moc.is_available
from public.menu_option_choices moc
join public.menu_option_groups mog on mog.id = moc.option_group_id
join public.menu_items mi on mi.id = mog.menu_item_id
where (mi.name, mog.name, moc.name) in (
  ('Bun bo dac biet', 'Size', 'To thuong'),
  ('Bun bo dac biet', 'Size', 'To lon'),
  ('Bun bo dac biet', 'Topping', 'Them bo'),
  ('Bun bo tai', 'Size', 'To thuong'),
  ('Bun bo tai', 'Size', 'To lon'),
  ('Pizza hai san size M', 'Vien pizza', 'Vien pho mai')
)
order by mi.id, mog.sort_order, moc.sort_order, moc.id;

-- Verification: Android-facing RPC should no longer include unavailable aliases.
select public.get_menu_item_detail_v3(1) as bun_bo_dac_biet_detail;
