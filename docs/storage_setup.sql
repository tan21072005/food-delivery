-- ==========================================
-- QUẢN LÝ FILE & HÌNH ẢNH (Supabase Storage)
-- Chạy script này trong SQL Editor của Supabase
-- ==========================================

-- 1. Thêm bucket mới có tên là 'avatars'
-- public = true giúp bất kỳ ai cũng có thể xem ảnh (rất phù hợp làm Avatar)
INSERT INTO storage.buckets (id, name, public) 
VALUES ('avatars', 'avatars', true)
ON CONFLICT (id) DO NOTHING;

-- 2. Cấu hình bảo mật (RLS Policies) cho bucket 'avatars'

-- Xóa các policy cũ (nếu có) để tránh lỗi khi chạy lại nhiều lần
DROP POLICY IF EXISTS "Public View Avatar" ON storage.objects;
DROP POLICY IF EXISTS "User Can Upload Avatar" ON storage.objects;
DROP POLICY IF EXISTS "User Can Update Own Avatar" ON storage.objects;

-- Policy 1: Ai cũng có thể tải và xem ảnh trong bucket avatars (Public Read)
CREATE POLICY "Public View Avatar"
ON storage.objects FOR SELECT
USING ( bucket_id = 'avatars' );

-- Policy 2: User đã đăng nhập mới được Upload file mới (Authenticated Insert)
CREATE POLICY "User Can Upload Avatar"
ON storage.objects FOR INSERT
WITH CHECK (
    bucket_id = 'avatars' 
    AND auth.role() = 'authenticated'
);

-- Policy 3: User chỉ được update hoặc xóa cái ảnh do chính mình tạo ra (Dựa vào auth.uid())
CREATE POLICY "User Can Update Own Avatar"
ON storage.objects FOR UPDATE
WITH CHECK (
    bucket_id = 'avatars' 
    AND owner = auth.uid()
);

-- ==========================================
-- CHÚC MỪNG BẠN ĐÃ CẤU HÌNH THÀNH CÔNG STORAGE!
-- ==========================================
