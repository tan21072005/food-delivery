-- Focused live-data patch for menu option groups/choices.
-- Run this in Supabase SQL Editor for project eiioaiyxlsfpoptmsbsm.
--
-- Why this exists:
-- docs/supabase_v3_food_delivery_seed.sql has expanded option data locally, but
-- the live database only has option groups for a few menu_items. This patch is
-- intentionally narrower than the full seed: it only upserts option groups and
-- choices, and it uses the current live naming style to avoid duplicate groups.

begin;

with seed_option_groups (
  restaurant_name,
  menu_name,
  group_name,
  selection_type,
  min_select,
  max_select,
  is_required,
  sort_order
) as (
  values
    ('Bun Bo Hue Dong Ba', 'Bun bo dac biet', 'Size', 'single', 1, 1, true, 1),
    ('Bun Bo Hue Dong Ba', 'Bun bo dac biet', 'Topping', 'multiple', 0, 3, false, 2),
    ('Bun Bo Hue Dong Ba', 'Bun bo tai', 'Size', 'single', 1, 1, true, 1),
    ('Bun Bo Hue Dong Ba', 'Bun bo tai', 'Topping', 'multiple', 0, 3, false, 2),
    ('Bun Bo Hue Dong Ba', 'Bun bo gio heo', 'Size', 'single', 1, 1, true, 1),
    ('Bun Bo Hue Dong Ba', 'Bun bo gio heo', 'Topping', 'multiple', 0, 3, false, 2),
    ('Bun Bo Hue Dong Ba', 'Cha cua them', 'Khau phan', 'single', 1, 1, true, 1),
    ('Bun Bo Hue Dong Ba', 'Tra dao cam sa', 'Muc duong', 'single', 1, 1, true, 1),
    ('Bun Bo Hue Dong Ba', 'Tra dao cam sa', 'Da', 'single', 1, 1, true, 2),
    ('Bobapop CMT8', 'Tra sua tran chau duong den', 'Muc duong', 'single', 1, 1, true, 1),
    ('Bobapop CMT8', 'Tra sua tran chau duong den', 'Da', 'single', 1, 1, true, 2),
    ('Bobapop CMT8', 'Tra sua tran chau duong den', 'Topping', 'multiple', 0, 3, false, 3),
    ('Bobapop CMT8', 'Tra vai hoa hong', 'Muc duong', 'single', 1, 1, true, 1),
    ('Bobapop CMT8', 'Tra vai hoa hong', 'Da', 'single', 1, 1, true, 2),
    ('Bobapop CMT8', 'Tra vai hoa hong', 'Topping', 'multiple', 0, 3, false, 3),
    ('Bobapop CMT8', 'Tra sua oolong nuong', 'Muc duong', 'single', 1, 1, true, 1),
    ('Bobapop CMT8', 'Tra sua oolong nuong', 'Da', 'single', 1, 1, true, 2),
    ('Bobapop CMT8', 'Tra sua oolong nuong', 'Topping', 'multiple', 0, 3, false, 3),
    ('Bobapop CMT8', 'Tra dao cam sa', 'Muc duong', 'single', 1, 1, true, 1),
    ('Bobapop CMT8', 'Tra dao cam sa', 'Da', 'single', 1, 1, true, 2),
    ('Bobapop CMT8', 'Tra dao cam sa', 'Topping', 'multiple', 0, 3, false, 3),
    ('Pizza 4Ps Le Loi', 'Pizza hai san size M', 'Size', 'single', 1, 1, true, 1),
    ('Pizza 4Ps Le Loi', 'Pizza hai san size M', 'Vien pizza', 'single', 0, 1, false, 2),
    ('Pizza 4Ps Le Loi', 'Pizza pepperoni size M', 'Size', 'single', 1, 1, true, 1),
    ('Pizza 4Ps Le Loi', 'Pizza pepperoni size M', 'Vien pizza', 'single', 0, 1, false, 2),
    ('Pizza 4Ps Le Loi', 'Pizza margherita size M', 'Size', 'single', 1, 1, true, 1),
    ('Pizza 4Ps Le Loi', 'Pizza margherita size M', 'Vien pizza', 'single', 0, 1, false, 2),
    ('Pizza 4Ps Le Loi', 'Spaghetti bo bam', 'Khau phan', 'single', 1, 1, true, 1),
    ('Pizza 4Ps Le Loi', 'Spaghetti bo bam', 'Topping', 'multiple', 0, 3, false, 2),
    ('Sushi Tei Pasteur', 'Set sushi ca hoi 8 mieng', 'Nuoc cham', 'single', 1, 1, true, 1),
    ('Sushi Tei Pasteur', 'Set sushi ca hoi 8 mieng', 'Mon kem', 'multiple', 0, 3, false, 2),
    ('Sushi Tei Pasteur', 'Sashimi ca hoi', 'Nuoc cham', 'single', 1, 1, true, 1),
    ('Sushi Tei Pasteur', 'Sashimi ca hoi', 'Mon kem', 'multiple', 0, 3, false, 2),
    ('Sushi Tei Pasteur', 'Set sushi tong hop 12 mieng', 'Nuoc cham', 'single', 1, 1, true, 1),
    ('Sushi Tei Pasteur', 'Set sushi tong hop 12 mieng', 'Mon kem', 'multiple', 0, 3, false, 2)
)
insert into public.menu_option_groups (
  menu_item_id,
  name,
  selection_type,
  min_select,
  max_select,
  is_required,
  sort_order
)
select
  mi.id,
  sog.group_name,
  sog.selection_type::public.option_selection_type,
  sog.min_select,
  sog.max_select,
  sog.is_required,
  sog.sort_order
from seed_option_groups sog
join public.restaurants r on r.name = sog.restaurant_name
join public.menu_items mi on mi.restaurant_id = r.id and mi.name = sog.menu_name
on conflict (menu_item_id, name) do update
set
  selection_type = excluded.selection_type,
  min_select = excluded.min_select,
  max_select = excluded.max_select,
  is_required = excluded.is_required,
  sort_order = excluded.sort_order;

with seed_option_choices (
  restaurant_name,
  menu_name,
  group_name,
  choice_name,
  price_delta,
  is_available,
  sort_order
) as (
  values
    ('Bun Bo Hue Dong Ba', 'Bun bo dac biet', 'Size', 'Thuong', 0, true, 1),
    ('Bun Bo Hue Dong Ba', 'Bun bo dac biet', 'Size', 'Lon', 12000, true, 2),
    ('Bun Bo Hue Dong Ba', 'Bun bo dac biet', 'Topping', 'Them bo tai', 15000, true, 1),
    ('Bun Bo Hue Dong Ba', 'Bun bo dac biet', 'Topping', 'Them cha cua', 12000, true, 2),
    ('Bun Bo Hue Dong Ba', 'Bun bo dac biet', 'Topping', 'Them gio heo', 18000, true, 3),
    ('Bun Bo Hue Dong Ba', 'Bun bo tai', 'Size', 'Thuong', 0, true, 1),
    ('Bun Bo Hue Dong Ba', 'Bun bo tai', 'Size', 'Lon', 10000, true, 2),
    ('Bun Bo Hue Dong Ba', 'Bun bo tai', 'Topping', 'Them bo tai', 15000, true, 1),
    ('Bun Bo Hue Dong Ba', 'Bun bo tai', 'Topping', 'Them cha cua', 12000, true, 2),
    ('Bun Bo Hue Dong Ba', 'Bun bo tai', 'Topping', 'Them rau song', 5000, true, 3),
    ('Bun Bo Hue Dong Ba', 'Bun bo gio heo', 'Size', 'Thuong', 0, true, 1),
    ('Bun Bo Hue Dong Ba', 'Bun bo gio heo', 'Size', 'Lon', 12000, true, 2),
    ('Bun Bo Hue Dong Ba', 'Bun bo gio heo', 'Topping', 'Them gio heo', 18000, true, 1),
    ('Bun Bo Hue Dong Ba', 'Bun bo gio heo', 'Topping', 'Them cha Hue', 10000, true, 2),
    ('Bun Bo Hue Dong Ba', 'Bun bo gio heo', 'Topping', 'Them rau song', 5000, true, 3),
    ('Bun Bo Hue Dong Ba', 'Cha cua them', 'Khau phan', 'Mot phan', 0, true, 1),
    ('Bun Bo Hue Dong Ba', 'Cha cua them', 'Khau phan', 'Hai phan', 18000, true, 2),
    ('Bun Bo Hue Dong Ba', 'Tra dao cam sa', 'Muc duong', 'It duong', 0, true, 1),
    ('Bun Bo Hue Dong Ba', 'Tra dao cam sa', 'Muc duong', 'Vua duong', 0, true, 2),
    ('Bun Bo Hue Dong Ba', 'Tra dao cam sa', 'Da', 'It da', 0, true, 1),
    ('Bun Bo Hue Dong Ba', 'Tra dao cam sa', 'Da', 'Da binh thuong', 0, true, 2),
    ('Bobapop CMT8', 'Tra sua tran chau duong den', 'Muc duong', '30% duong', 0, true, 1),
    ('Bobapop CMT8', 'Tra sua tran chau duong den', 'Muc duong', '70% duong', 0, true, 2),
    ('Bobapop CMT8', 'Tra sua tran chau duong den', 'Da', 'It da', 0, true, 1),
    ('Bobapop CMT8', 'Tra sua tran chau duong den', 'Da', 'Da binh thuong', 0, true, 2),
    ('Bobapop CMT8', 'Tra sua tran chau duong den', 'Topping', 'Tran chau den', 7000, true, 1),
    ('Bobapop CMT8', 'Tra sua tran chau duong den', 'Topping', 'Pudding trung', 9000, true, 2),
    ('Bobapop CMT8', 'Tra sua tran chau duong den', 'Topping', 'Thach ca phe', 8000, true, 3),
    ('Bobapop CMT8', 'Tra vai hoa hong', 'Muc duong', 'It duong', 0, true, 1),
    ('Bobapop CMT8', 'Tra vai hoa hong', 'Muc duong', 'Vua duong', 0, true, 2),
    ('Bobapop CMT8', 'Tra vai hoa hong', 'Da', 'It da', 0, true, 1),
    ('Bobapop CMT8', 'Tra vai hoa hong', 'Da', 'Da binh thuong', 0, true, 2),
    ('Bobapop CMT8', 'Tra vai hoa hong', 'Topping', 'Vai them', 9000, true, 1),
    ('Bobapop CMT8', 'Tra vai hoa hong', 'Topping', 'Thach trai cay', 8000, true, 2),
    ('Bobapop CMT8', 'Tra sua oolong nuong', 'Muc duong', '30% duong', 0, true, 1),
    ('Bobapop CMT8', 'Tra sua oolong nuong', 'Muc duong', '70% duong', 0, true, 2),
    ('Bobapop CMT8', 'Tra sua oolong nuong', 'Da', 'It da', 0, true, 1),
    ('Bobapop CMT8', 'Tra sua oolong nuong', 'Da', 'Da binh thuong', 0, true, 2),
    ('Bobapop CMT8', 'Tra sua oolong nuong', 'Topping', 'Tran chau den', 7000, true, 1),
    ('Bobapop CMT8', 'Tra sua oolong nuong', 'Topping', 'Kem sua', 10000, true, 2),
    ('Bobapop CMT8', 'Tra dao cam sa', 'Muc duong', 'It duong', 0, true, 1),
    ('Bobapop CMT8', 'Tra dao cam sa', 'Muc duong', 'Vua duong', 0, true, 2),
    ('Bobapop CMT8', 'Tra dao cam sa', 'Da', 'It da', 0, true, 1),
    ('Bobapop CMT8', 'Tra dao cam sa', 'Da', 'Da binh thuong', 0, true, 2),
    ('Bobapop CMT8', 'Tra dao cam sa', 'Topping', 'Dao them', 9000, true, 1),
    ('Bobapop CMT8', 'Tra dao cam sa', 'Topping', 'Thach trai cay', 8000, true, 2),
    ('Pizza 4Ps Le Loi', 'Pizza hai san size M', 'Size', 'Size M', 0, true, 1),
    ('Pizza 4Ps Le Loi', 'Pizza hai san size M', 'Size', 'Size L', 45000, true, 2),
    ('Pizza 4Ps Le Loi', 'Pizza hai san size M', 'Vien pizza', 'Khong vien', 0, true, 1),
    ('Pizza 4Ps Le Loi', 'Pizza hai san size M', 'Vien pizza', 'Them pho mai', 25000, true, 2),
    ('Pizza 4Ps Le Loi', 'Pizza pepperoni size M', 'Size', 'Size M', 0, true, 1),
    ('Pizza 4Ps Le Loi', 'Pizza pepperoni size M', 'Size', 'Size L', 40000, true, 2),
    ('Pizza 4Ps Le Loi', 'Pizza pepperoni size M', 'Vien pizza', 'Khong vien', 0, true, 1),
    ('Pizza 4Ps Le Loi', 'Pizza pepperoni size M', 'Vien pizza', 'Them pho mai', 25000, true, 2),
    ('Pizza 4Ps Le Loi', 'Pizza margherita size M', 'Size', 'Size M', 0, true, 1),
    ('Pizza 4Ps Le Loi', 'Pizza margherita size M', 'Size', 'Size L', 35000, true, 2),
    ('Pizza 4Ps Le Loi', 'Pizza margherita size M', 'Vien pizza', 'Khong vien', 0, true, 1),
    ('Pizza 4Ps Le Loi', 'Pizza margherita size M', 'Vien pizza', 'Them pho mai', 25000, true, 2),
    ('Pizza 4Ps Le Loi', 'Spaghetti bo bam', 'Khau phan', 'Phan thuong', 0, true, 1),
    ('Pizza 4Ps Le Loi', 'Spaghetti bo bam', 'Khau phan', 'Phan lon', 25000, true, 2),
    ('Pizza 4Ps Le Loi', 'Spaghetti bo bam', 'Topping', 'Them pho mai', 15000, true, 1),
    ('Pizza 4Ps Le Loi', 'Spaghetti bo bam', 'Topping', 'Them bo bam', 25000, true, 2),
    ('Sushi Tei Pasteur', 'Set sushi ca hoi 8 mieng', 'Nuoc cham', 'Nuoc tuong', 0, true, 1),
    ('Sushi Tei Pasteur', 'Set sushi ca hoi 8 mieng', 'Nuoc cham', 'Sot me rang', 5000, true, 2),
    ('Sushi Tei Pasteur', 'Set sushi ca hoi 8 mieng', 'Mon kem', 'Them gung hong', 5000, true, 1),
    ('Sushi Tei Pasteur', 'Set sushi ca hoi 8 mieng', 'Mon kem', 'Them wasabi', 3000, true, 2),
    ('Sushi Tei Pasteur', 'Sashimi ca hoi', 'Nuoc cham', 'Nuoc tuong', 0, true, 1),
    ('Sushi Tei Pasteur', 'Sashimi ca hoi', 'Nuoc cham', 'Sot ponzu', 7000, true, 2),
    ('Sushi Tei Pasteur', 'Sashimi ca hoi', 'Mon kem', 'Them cu cai bao', 5000, true, 1),
    ('Sushi Tei Pasteur', 'Sashimi ca hoi', 'Mon kem', 'Them wasabi', 3000, true, 2),
    ('Sushi Tei Pasteur', 'Set sushi tong hop 12 mieng', 'Nuoc cham', 'Nuoc tuong', 0, true, 1),
    ('Sushi Tei Pasteur', 'Set sushi tong hop 12 mieng', 'Nuoc cham', 'Sot me rang', 5000, true, 2),
    ('Sushi Tei Pasteur', 'Set sushi tong hop 12 mieng', 'Mon kem', 'Them gung hong', 5000, true, 1),
    ('Sushi Tei Pasteur', 'Set sushi tong hop 12 mieng', 'Mon kem', 'Them wasabi', 3000, true, 2)
)
insert into public.menu_option_choices (
  option_group_id,
  name,
  price_delta,
  is_available,
  sort_order
)
select
  mog.id,
  soc.choice_name,
  soc.price_delta,
  soc.is_available,
  soc.sort_order
from seed_option_choices soc
join public.restaurants r on r.name = soc.restaurant_name
join public.menu_items mi on mi.restaurant_id = r.id and mi.name = soc.menu_name
join public.menu_option_groups mog on mog.menu_item_id = mi.id and mog.name = soc.group_name
on conflict (option_group_id, name) do update
set
  price_delta = excluded.price_delta,
  is_available = excluded.is_available,
  sort_order = excluded.sort_order;

commit;

-- Verification: every live menu item should now have at least one group.
select
  mi.id,
  r.name as restaurant_name,
  mi.name as menu_name,
  count(mog.id) as option_group_count,
  count(moc.id) as option_choice_count
from public.menu_items mi
join public.restaurants r on r.id = mi.restaurant_id
left join public.menu_option_groups mog on mog.menu_item_id = mi.id
left join public.menu_option_choices moc on moc.option_group_id = mog.id
where mi.status = 'active'
  and mi.deleted_at is null
group by mi.id, r.name, mi.name
order by mi.id;

-- Verification: RPC payload should expose the same groups to Android.
select public.get_menu_item_detail_v3(mi.id) as menu_item_detail
from public.menu_items mi
where mi.status = 'active'
  and mi.deleted_at is null
order by mi.id
limit 3;
