-- ==========================================
-- 0. Cập nhật lại Schema (Sửa lỗi thiếu cột)
-- ==========================================
ALTER TABLE categories ADD COLUMN IF NOT EXISTS icon_url TEXT;
ALTER TABLE menu_categories ADD COLUMN IF NOT EXISTS icon_url TEXT;

-- Bỏ điều kiện bắt buộc phải có nhà hàng (restaurant_id) để dễ tạo data mẫu
ALTER TABLE menus ALTER COLUMN restaurant_id DROP NOT NULL;

-- ==========================================
-- 1. Xóa dữ liệu cũ (nếu có) để tránh trùng lặp
-- ==========================================
TRUNCATE TABLE order_items, orders, carts, menus, categories, menu_categories RESTART IDENTITY CASCADE;

-- ==========================================
-- 2. Bơm danh mục (Categories & Menu Categories)
-- Do app dùng cả 2 bảng nên bơm cả 2 cho an toàn
-- ==========================================
INSERT INTO categories (cat_name, icon_url) VALUES 
('Burger', 'https://cdn-icons-png.flaticon.com/512/3075/3075977.png'),
('Pizza', 'https://cdn-icons-png.flaticon.com/512/3132/3132693.png'),
('Sushi', 'https://cdn-icons-png.flaticon.com/512/2254/2254464.png'),
('Đồ uống', 'https://cdn-icons-png.flaticon.com/512/3081/3081076.png'),
('Món Việt', 'https://cdn-icons-png.flaticon.com/512/1205/1205244.png');

INSERT INTO menu_categories (name, icon_url) VALUES 
('Burger', 'https://cdn-icons-png.flaticon.com/512/3075/3075977.png'),
('Pizza', 'https://cdn-icons-png.flaticon.com/512/3132/3132693.png'),
('Sushi', 'https://cdn-icons-png.flaticon.com/512/2254/2254464.png'),
('Đồ uống', 'https://cdn-icons-png.flaticon.com/512/3081/3081076.png'),
('Món Việt', 'https://cdn-icons-png.flaticon.com/512/1205/1205244.png');

-- 3. Bơm món ăn (Menus)
-- Lưu ý: restaurant_id có thể để tạm = 1
-- ==========================================
INSERT INTO menus (category_id, item_name, description, price, image_url, status) VALUES 
-- Burger (category_id = 1)
(1, 'Burger Phô Mai 2 Tầng', 'Bò nướng lửa hồng, phô mai dẻo, rau tươi.', 85000, 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=500&q=60', 'active'),
(1, 'Burger Gà Giòn cay', 'Thịt gà chiên giòn rụm với nước sốt cay nồng.', 75000, 'https://images.unsplash.com/photo-1610440042657-612c34d95e9f?auto=format&fit=crop&w=500&q=60', 'active'),

-- Pizza (category_id = 2)
(2, 'Pizza Hải Sản (L)', 'Nhiều tôm, mực, sốt chua ngọt, phô mai ngập tràn.', 250000, 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?auto=format&fit=crop&w=500&q=60', 'active'),
(2, 'Pizza Margherita', 'Truyền thống Ý với cà chua, phô mai Mozzarella.', 150000, 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?auto=format&fit=crop&w=500&q=60', 'active'),

-- Sushi (category_id = 3)
(3, 'Set Sushi Thập Cẩm 12 miếng', 'Cá hồi, cá ngừ, bạch tuộc, tôm thanh vị tươi ngon.', 220000, 'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=500&q=60', 'active'),

-- Đồ uống (category_id = 4)
(4, 'Trà Đào Cam Sả', 'Mát lạnh, thanh lọc cơ thể mùa hè.', 45000, 'https://images.unsplash.com/photo-1497534446932-c925b458314e?auto=format&fit=crop&w=500&q=60', 'active'),
(4, 'Coca Cola (Lon)', 'Giải khát tuyệt đỉnh.', 15000, 'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=500&q=60', 'active'),

-- Món Việt (category_id = 5)
(5, 'Phở Bò Tái Nạm', 'Nước dùng đậm đà, thịt bò mềm.', 60000, 'https://images.unsplash.com/photo-1628294895950-9805252327bc?auto=format&fit=crop&w=500&q=60', 'active'),
(5, 'Bún Chả Hà Nội', 'Thịt nướng than hoa thơm lừng.', 55000, 'https://images.unsplash.com/photo-1596484552834-6a58f850e0a1?auto=format&fit=crop&w=500&q=60', 'active');

-- Đã xong! Refresh lại Database trên Supabase Dashboard để xem kết quả.
