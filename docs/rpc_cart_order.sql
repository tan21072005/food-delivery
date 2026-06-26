-- Function 1: get_cart_summary()
-- Trả về danh sách giỏ hàng kèm tính toán tổng tiền
CREATE OR REPLACE FUNCTION get_cart_summary()
RETURNS json AS $$
DECLARE
  v_user_id BIGINT;
  result json;
BEGIN
  -- Lấy user_id dựa trên auth.uid() của session hiện tại
  SELECT id INTO v_user_id FROM users WHERE auth_uid = auth.uid();

  IF v_user_id IS NULL THEN
     RETURN '{"items": [], "subtotal": 0, "delivery_fee": 0, "net_total": 0}'::json;
  END IF;

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
    'delivery_fee', 15000,
    'net_total', COALESCE((
        SELECT SUM(c.quantity * m.price)
        FROM carts c
        JOIN menus m ON c.menu_id = m.id
        WHERE c.user_id = v_user_id
    ), 0) + 15000
  ) INTO result;

  RETURN result;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;


-- Function 2: checkout_cart()
-- Gom dữ liệu giỏ hàng thành Đơn hàng (Order) và Xóa giỏ hàng
CREATE OR REPLACE FUNCTION checkout_cart(p_delivery_address TEXT, p_note TEXT)
RETURNS BIGINT AS $$
DECLARE
  v_user_id BIGINT;
  v_restaurant_id BIGINT;
  v_order_id BIGINT;
  v_subtotal DECIMAL(12,2);
  v_delivery_fee DECIMAL(12,2) := 15000;
  v_net_total DECIMAL(12,2);
BEGIN
  -- 1. Lấy user_id
  SELECT id INTO v_user_id FROM users WHERE auth_uid = auth.uid();
  IF v_user_id IS NULL THEN
     RAISE EXCEPTION 'User not found';
  END IF;

  -- 2. Kiểm tra giỏ hàng có rỗng không và lấy restaurant_id
  SELECT m.restaurant_id INTO v_restaurant_id
  FROM carts c
  JOIN menus m ON c.menu_id = m.id
  WHERE c.user_id = v_user_id
  LIMIT 1;

  IF v_restaurant_id IS NULL THEN
     RAISE EXCEPTION 'Cart is empty';
  END IF;

  -- 3. Tính toán tổng tiền
  SELECT COALESCE(SUM(c.quantity * m.price), 0) INTO v_subtotal
  FROM carts c
  JOIN menus m ON c.menu_id = m.id
  WHERE c.user_id = v_user_id;

  v_net_total := v_subtotal + v_delivery_fee;

  -- 4. Tạo Order mới
  INSERT INTO orders (
    user_id, restaurant_id, status, total_amount, delivery_fee, net_amount, delivery_address, note
  ) VALUES (
    v_user_id, v_restaurant_id, 'pending', v_subtotal, v_delivery_fee, v_net_total, p_delivery_address, p_note
  ) RETURNING id INTO v_order_id;

  -- 5. Chuyển Item từ Cart sang Order Items
  INSERT INTO order_items (order_id, menu_id, quantity, unit_price)
  SELECT v_order_id, c.menu_id, c.quantity, m.price
  FROM carts c
  JOIN menus m ON c.menu_id = m.id
  WHERE c.user_id = v_user_id;

  -- 6. Xóa giỏ hàng
  DELETE FROM carts WHERE user_id = v_user_id;

  RETURN v_order_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
