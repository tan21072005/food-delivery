-- Focused live patch: include topping/option price deltas in cart and order money.
-- Run this in Supabase SQL Editor after menu option data is present.
--
-- Tables involved:
-- - carts/cart_items/cart_item_options hold draft carts.
-- - orders/order_lines hold checked-out orders.
--
-- Important pricing rule:
-- line unit price = menu_items.base_price + sum(cart_item_options.price_delta_snapshot)
-- line subtotal   = line unit price * quantity

begin;

create or replace function public.get_draft_carts_v3()
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_customer_id bigint;
  v_delivery_fee numeric(12, 2) := 15000;
  v_result jsonb;
begin
  select public.current_app_user_id() into v_customer_id;
  if v_customer_id is null then
    raise exception 'Authenticated user profile not found';
  end if;

  select coalesce(
    jsonb_agg(
      jsonb_build_object(
        'cart_id', c.id,
        'restaurant_id', r.id,
        'restaurant_name', r.name,
        'restaurant_logo_url', r.logo_url,
        'restaurant_cover_url', r.cover_url,
        'restaurant_rating', r.avg_rating,
        'restaurant_is_open', r.is_open,
        'item_count', totals.item_count,
        'line_count', totals.line_count,
        'subtotal', totals.subtotal,
        'delivery_fee', v_delivery_fee,
        'discount_amount', 0,
        'total_amount', totals.subtotal + v_delivery_fee,
        'updated_at', c.updated_at,
        'preview_items', coalesce(preview.items, '[]'::jsonb)
      )
      order by c.updated_at desc
    ),
    '[]'::jsonb
  )
  into v_result
  from public.carts c
  join public.restaurants r on r.id = c.restaurant_id
  join lateral (
    select
      count(*)::int as line_count,
      coalesce(sum(ci.quantity), 0)::int as item_count,
      coalesce(sum((coalesce(ci.last_known_unit_price, mi.base_price) + coalesce(opt.option_total, 0)) * ci.quantity), 0) as subtotal
    from public.cart_items ci
    join public.menu_items mi on mi.id = ci.menu_item_id
    left join lateral (
      select sum(cio.price_delta_snapshot) as option_total
      from public.cart_item_options cio
      where cio.cart_item_id = ci.id
    ) opt on true
    where ci.cart_id = c.id
  ) totals on true
  left join lateral (
    select jsonb_agg(
      jsonb_build_object(
        'cart_item_id', pi.cart_item_id,
        'menu_item_id', pi.menu_item_id,
        'item_name', pi.item_name,
        'image_url', pi.image_url,
        'quantity', pi.quantity,
        'unit_price', pi.unit_price,
        'options', pi.options
      )
      order by pi.created_at, pi.cart_item_id
    ) as items
    from (
      select
        ci.id as cart_item_id,
        mi.id as menu_item_id,
        mi.name as item_name,
        mi.image_url,
        ci.quantity,
        coalesce(ci.last_known_unit_price, mi.base_price) + coalesce(opt.option_total, 0) as unit_price,
        ci.created_at,
        coalesce(opt.options_json, '[]'::jsonb) as options
      from public.cart_items ci
      join public.menu_items mi on mi.id = ci.menu_item_id
      left join lateral (
        select
          sum(cio.price_delta_snapshot) as option_total,
          jsonb_agg(
            jsonb_build_object(
              'option_choice_id', cio.option_choice_id,
              'name', cio.option_name_snapshot,
              'price_delta', cio.price_delta_snapshot
            )
            order by cio.id
          ) as options_json
        from public.cart_item_options cio
        where cio.cart_item_id = ci.id
      ) opt on true
      where ci.cart_id = c.id
      order by ci.created_at, ci.id
      limit 3
    ) pi
  ) preview on true
  where c.customer_id = v_customer_id
    and c.status = 'draft'
    and totals.line_count > 0
    and r.deleted_at is null;

  return v_result;
end;
$$;

revoke all on function public.get_draft_carts_v3() from public;
grant execute on function public.get_draft_carts_v3() to authenticated;

create or replace function public.get_cart_summary_v3(p_cart_id bigint)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_customer_id bigint;
  v_cart_owner_id bigint;
  v_subtotal numeric(12, 2);
  v_delivery_fee numeric(12, 2) := 15000;
  v_result jsonb;
begin
  select public.current_app_user_id() into v_customer_id;

  select c.customer_id into v_cart_owner_id
  from public.carts c
  where c.id = p_cart_id and c.status = 'draft';

  if v_customer_id is null or v_cart_owner_id is distinct from v_customer_id then
    raise exception 'Cart not found';
  end if;

  select coalesce(sum((coalesce(ci.last_known_unit_price, mi.base_price) + coalesce(opt.option_total, 0)) * ci.quantity), 0)
  into v_subtotal
  from public.cart_items ci
  join public.menu_items mi on mi.id = ci.menu_item_id
  left join (
    select cio.cart_item_id, sum(cio.price_delta_snapshot) as option_total
    from public.cart_item_options cio
    group by cio.cart_item_id
  ) opt on opt.cart_item_id = ci.id
  where ci.cart_id = p_cart_id;

  select jsonb_build_object(
    'cart_id', p_cart_id,
    'items', coalesce((
      select jsonb_agg(
        jsonb_build_object(
          'cart_item_id', ci.id,
          'menu_item_id', ci.menu_item_id,
          'item_name', mi.name,
          'image_url', mi.image_url,
          'quantity', ci.quantity,
          'base_price', coalesce(ci.last_known_unit_price, mi.base_price),
          'unit_price', coalesce(ci.last_known_unit_price, mi.base_price) + coalesce(opt.option_total, 0),
          'line_subtotal', (coalesce(ci.last_known_unit_price, mi.base_price) + coalesce(opt.option_total, 0)) * ci.quantity,
          'note', ci.note,
          'options', coalesce((
            select jsonb_agg(
              jsonb_build_object(
                'option_choice_id', cio.option_choice_id,
                'name', cio.option_name_snapshot,
                'price_delta', cio.price_delta_snapshot
              )
              order by cio.id
            )
            from public.cart_item_options cio
            where cio.cart_item_id = ci.id
          ), '[]'::jsonb)
        )
        order by ci.created_at, ci.id
      )
      from public.cart_items ci
      join public.menu_items mi on mi.id = ci.menu_item_id
      left join (
        select cio.cart_item_id, sum(cio.price_delta_snapshot) as option_total
        from public.cart_item_options cio
        group by cio.cart_item_id
      ) opt on opt.cart_item_id = ci.id
      where ci.cart_id = p_cart_id
    ), '[]'::jsonb),
    'subtotal', v_subtotal,
    'delivery_fee', v_delivery_fee,
    'discount_amount', 0,
    'total_amount', v_subtotal + v_delivery_fee
  )
  into v_result;

  return v_result;
end;
$$;

revoke all on function public.get_cart_summary_v3(bigint) from public;
grant execute on function public.get_cart_summary_v3(bigint) to authenticated;

create or replace function public.checkout_cart_v3(
  p_cart_id bigint,
  p_delivery_address_id bigint,
  p_payment_method public.app_payment_method default 'COD',
  p_note text default null
)
returns bigint
language plpgsql
security definer
set search_path = public
as $$
declare
  v_customer_id bigint;
  v_cart record;
  v_address record;
  v_order_id bigint;
  v_subtotal numeric(12, 2);
  v_delivery_fee numeric(12, 2) := 15000;
  v_total numeric(12, 2);
begin
  select public.current_app_user_id() into v_customer_id;
  if v_customer_id is null then
    raise exception 'Authenticated user profile not found';
  end if;

  select *
  into v_cart
  from public.carts
  where id = p_cart_id
    and customer_id = v_customer_id
    and status = 'draft';

  if v_cart.id is null then
    raise exception 'Draft cart not found';
  end if;

  select *
  into v_address
  from public.delivery_addresses
  where id = p_delivery_address_id
    and customer_id = v_customer_id
    and deleted_at is null;

  if v_address.id is null then
    raise exception 'Delivery address not found';
  end if;

  select coalesce(sum((coalesce(ci.last_known_unit_price, mi.base_price) + coalesce(opt.option_total, 0)) * ci.quantity), 0)
  into v_subtotal
  from public.cart_items ci
  join public.menu_items mi on mi.id = ci.menu_item_id and mi.status = 'active' and mi.deleted_at is null
  left join (
    select cart_item_id, sum(price_delta_snapshot) as option_total
    from public.cart_item_options
    group by cart_item_id
  ) opt on opt.cart_item_id = ci.id
  where ci.cart_id = p_cart_id;

  if v_subtotal <= 0 then
    raise exception 'Cart is empty';
  end if;

  v_total := v_subtotal + v_delivery_fee;

  insert into public.orders (
    customer_id,
    restaurant_id,
    cart_id,
    delivery_address_id,
    delivery_address_snapshot_json,
    status,
    subtotal,
    delivery_fee,
    discount_amount,
    total_amount,
    payment_method,
    payment_status,
    note
  )
  values (
    v_customer_id,
    v_cart.restaurant_id,
    p_cart_id,
    p_delivery_address_id,
    jsonb_build_object(
      'label', v_address.label,
      'receiver_name', v_address.receiver_name,
      'receiver_phone', v_address.receiver_phone,
      'address_line', v_address.address_line,
      'building_name', v_address.building_name,
      'floor', v_address.floor,
      'gate_note', v_address.gate_note,
      'latitude', v_address.latitude,
      'longitude', v_address.longitude
    ),
    'pending',
    v_subtotal,
    v_delivery_fee,
    0,
    v_total,
    p_payment_method,
    'pending'::public.app_payment_status,
    p_note
  )
  returning id into v_order_id;

  insert into public.order_lines (
    order_id,
    menu_item_id,
    item_name_snapshot,
    item_image_snapshot,
    quantity,
    unit_price_snapshot,
    options_snapshot_json
  )
  select
    v_order_id,
    mi.id,
    mi.name,
    mi.image_url,
    ci.quantity,
    coalesce(ci.last_known_unit_price, mi.base_price) + coalesce(opt.option_total, 0),
    coalesce(opt.options_json, '[]'::jsonb)
  from public.cart_items ci
  join public.menu_items mi on mi.id = ci.menu_item_id
  left join (
    select
      cio.cart_item_id,
      sum(cio.price_delta_snapshot) as option_total,
      jsonb_agg(
        jsonb_build_object(
          'option_choice_id', cio.option_choice_id,
          'name', cio.option_name_snapshot,
          'price_delta', cio.price_delta_snapshot
        )
        order by cio.id
      ) as options_json
    from public.cart_item_options cio
    group by cio.cart_item_id
  ) opt on opt.cart_item_id = ci.id
  where ci.cart_id = p_cart_id;

  insert into public.order_status_history (
    order_id,
    from_status,
    to_status,
    changed_by_user_id,
    note
  )
  values (v_order_id, null, 'pending', v_customer_id, 'Order created from cart');

  insert into public.payments (order_id, provider, amount, status)
  values (v_order_id, p_payment_method, v_total, 'pending');

  update public.carts
  set status = 'checked_out', updated_at = now()
  where id = p_cart_id;

  return v_order_id;
end;
$$;

revoke all on function public.checkout_cart_v3(bigint, bigint, public.app_payment_method, text) from public;
grant execute on function public.checkout_cart_v3(bigint, bigint, public.app_payment_method, text) to authenticated;

commit;

-- Verification query for a draft cart:
-- Replace <cart_id> with a real draft cart id after adding a menu item with options.
-- select public.get_cart_summary_v3(<cart_id>);
