-- Function 0: add_to_cart()
-- Them mon vao Cart dua tren JWT hien tai, khong tin vao user_id tu client.
CREATE OR REPLACE FUNCTION public.add_to_cart(p_menu_id BIGINT, p_quantity INT DEFAULT 1)
RETURNS VOID AS $$
DECLARE
  v_user_id BIGINT;
BEGIN
  IF p_quantity IS NULL OR p_quantity <= 0 THEN
    RAISE EXCEPTION 'Quantity must be greater than 0';
  END IF;

  SELECT id INTO v_user_id
  FROM public.users
  WHERE auth_uid = auth.uid();

  IF v_user_id IS NULL THEN
    RAISE EXCEPTION 'User not found';
  END IF;

  INSERT INTO public.carts (user_id, menu_id, quantity)
  VALUES (v_user_id, p_menu_id, p_quantity)
  ON CONFLICT (user_id, menu_id)
  DO UPDATE SET
    quantity = public.carts.quantity + EXCLUDED.quantity,
    updated_at = NOW();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

REVOKE ALL ON FUNCTION public.add_to_cart(BIGINT, INT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.add_to_cart(BIGINT, INT) TO authenticated;

-- Function 1a: set_default_delivery_address()
-- Doi DeliveryAddress mac dinh dua tren JWT hien tai, khong tin user_id tu Android.
CREATE OR REPLACE FUNCTION set_default_delivery_address(p_delivery_address_id BIGINT)
RETURNS VOID AS $$
DECLARE
  v_user_id BIGINT;
BEGIN
  SELECT id INTO v_user_id
  FROM users
  WHERE auth_uid = auth.uid();

  IF v_user_id IS NULL THEN
     RAISE EXCEPTION 'User not found';
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM user_addresses ua
    WHERE ua.id = p_delivery_address_id
      AND ua.user_id = v_user_id
      AND ua.deleted_at IS NULL
  ) THEN
     RAISE EXCEPTION 'DeliveryAddress not found';
  END IF;

  UPDATE user_addresses
  SET is_default = FALSE,
      updated_at = NOW()
  WHERE user_id = v_user_id
    AND deleted_at IS NULL;

  UPDATE user_addresses
  SET is_default = TRUE,
      updated_at = NOW()
  WHERE id = p_delivery_address_id
    AND user_id = v_user_id
    AND deleted_at IS NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

REVOKE ALL ON FUNCTION set_default_delivery_address(BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION set_default_delivery_address(BIGINT) TO authenticated;

-- Function 1: get_cart_summary()
-- Trả về danh sách giỏ hàng kèm tính toán tổng tiền
CREATE OR REPLACE FUNCTION get_cart_summary()
RETURNS json AS $$
DECLARE
  v_user_id BIGINT;
  v_restaurant_count INT;
  result json;
BEGIN
  -- Lấy user_id dựa trên auth.uid() của session hiện tại
  SELECT id INTO v_user_id FROM users WHERE auth_uid = auth.uid();

  IF v_user_id IS NULL THEN
     RETURN '{"items": [], "subtotal": 0, "delivery_fee": 0, "net_total": 0}'::json;
  END IF;

  -- Đếm số lượng nhà hàng khác nhau trong giỏ hàng để tính phí ship (15k/quán)
  SELECT COUNT(DISTINCT m.restaurant_id) INTO v_restaurant_count
  FROM carts c JOIN menus m ON c.menu_id = m.id WHERE c.user_id = v_user_id;

  SELECT json_build_object(
    'items', COALESCE((
        SELECT json_agg(
            json_build_object(
                'cart_id', c.id,
                'menu_id', c.menu_id,
                'quantity', c.quantity,
                'item_name', m.item_name,
                'image_url', m.image_url,
                'price', m.price,
                'restaurant_id', m.restaurant_id
            )
        ) 
        FROM carts c
        JOIN menus m ON c.menu_id = m.id
        WHERE c.user_id = v_user_id
    ), '[]'::json),
    'subtotal', COALESCE((
        SELECT SUM(c.quantity * m.price)
        FROM carts c
        JOIN menus m ON c.menu_id = m.id
        WHERE c.user_id = v_user_id
    ), 0),
    'delivery_fee', (v_restaurant_count * 15000),
    'net_total', COALESCE((
        SELECT SUM(c.quantity * m.price)
        FROM carts c
        JOIN menus m ON c.menu_id = m.id
        WHERE c.user_id = v_user_id
    ), 0) + (v_restaurant_count * 15000)
  ) INTO result;

  RETURN result;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

REVOKE ALL ON FUNCTION checkout_cart(BIGINT, TEXT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION checkout_cart(BIGINT, TEXT) TO authenticated;


-- Function 2: checkout_cart()
-- Gom dữ liệu giỏ hàng thành Đơn hàng (Order) và Xóa giỏ hàng
CREATE OR REPLACE FUNCTION checkout_cart(p_delivery_address_id BIGINT, p_note TEXT)
RETURNS BIGINT[] AS $$
DECLARE
  v_user_id BIGINT;
  v_delivery_address user_addresses%ROWTYPE;
  v_restaurant_id BIGINT;
  v_order_id BIGINT;
  v_subtotal DECIMAL(12,2);
  v_delivery_fee DECIMAL(12,2) := 15000;
  v_net_total DECIMAL(12,2);
  v_order_ids BIGINT[] := '{}';
  rec RECORD;
BEGIN
  -- 1. Lấy user_id
  SELECT id INTO v_user_id FROM users WHERE auth_uid = auth.uid();
  IF v_user_id IS NULL THEN
     RAISE EXCEPTION 'User not found';
  END IF;

  SELECT * INTO v_delivery_address
  FROM user_addresses ua
  WHERE ua.id = p_delivery_address_id
    AND ua.user_id = v_user_id
    AND ua.deleted_at IS NULL;

  IF v_delivery_address.id IS NULL THEN
     RAISE EXCEPTION 'DeliveryAddress not found';
  END IF;

  -- 2. Lặp qua từng nhà hàng có trong giỏ hàng
  FOR rec IN
    SELECT DISTINCT m.restaurant_id
    FROM carts c
    JOIN menus m ON c.menu_id = m.id
    WHERE c.user_id = v_user_id
  LOOP
      v_restaurant_id := rec.restaurant_id;

      -- Tính tổng tiền cho từng nhà hàng
      SELECT COALESCE(SUM(c.quantity * m.price), 0) INTO v_subtotal
      FROM carts c
      JOIN menus m ON c.menu_id = m.id
      WHERE c.user_id = v_user_id AND m.restaurant_id = v_restaurant_id;

      v_net_total := v_subtotal + v_delivery_fee;

      -- Tạo Order mới cho nhà hàng này
      INSERT INTO orders (
        user_id,
        restaurant_id,
        status,
        total_amount,
        delivery_fee,
        net_amount,
        delivery_address,
        delivery_address_id,
        recipient_name_snapshot,
        recipient_phone_snapshot,
        full_address_snapshot,
        latitude_snapshot,
        longitude_snapshot,
        note
      ) VALUES (
        v_user_id,
        v_restaurant_id,
        'pending',
        v_subtotal,
        v_delivery_fee,
        v_net_total,
        v_delivery_address.address_detail,
        v_delivery_address.id,
        v_delivery_address.recipient_name,
        v_delivery_address.recipient_phone,
        v_delivery_address.address_detail,
        v_delivery_address.latitude,
        v_delivery_address.longitude,
        p_note
      ) RETURNING id INTO v_order_id;

      v_order_ids := array_append(v_order_ids, v_order_id);

      -- Chuyển Item từ Cart sang Order Items
      INSERT INTO order_items (
        order_id,
        menu_id,
        quantity,
        unit_price,
        item_name_snapshot,
        item_image_snapshot
      )
      SELECT v_order_id, c.menu_id, c.quantity, m.price, m.item_name, m.image_url
      FROM carts c
      JOIN menus m ON c.menu_id = m.id
      WHERE c.user_id = v_user_id AND m.restaurant_id = v_restaurant_id;
  END LOOP;

  -- Xóa giỏ hàng
  DELETE FROM carts WHERE user_id = v_user_id;

  IF array_length(v_order_ids, 1) IS NULL THEN
     RAISE EXCEPTION 'Cart is empty';
  END IF;

  RETURN v_order_ids;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
