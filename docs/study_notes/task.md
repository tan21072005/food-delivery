# Cart & Order Integration Tasks

- `[x]` **Bước 1: Viết mã SQL (RPC) trên Supabase**
  - `[x]` Tạo hàm `get_cart_summary(user_id)`.
  - `[x]` Tạo hàm `checkout_cart(user_id, delivery_address, note)`.
- `[x]` **Bước 2: Tạo Giao diện (UI) cơ bản**
  - `[x]` Code giao diện `item_cart.xml` cho từng món trong giỏ.
- `[x]` **Bước 3: Code Models và API Service**
  - `[x]` Tạo Model `CartSummaryResponse.java`.
  - `[x]` Tạo Model `CartItem.java`.
  - `[x]` Cập nhật `ApiService.java` với các endpoint GET, POST (add to cart), PATCH (update quantity), và DELETE.
- `[x]` **Bước 4: Nâng cấp "Thêm vào giỏ" ở Home / Menu**
  - `[x]` Đấu nối hàm `addToCart` trong `HomeViewModel` (hoặc tạo `CartRepository`) vào API thật.
- `[x]` **Bước 5: Hoàn thiện `Checkout.java`**
  - `[x]` Viết `CartAdapter` để load dữ liệu `item_cart.xml`.
  - `[x]` Gọi API `get_cart_summary` hiển thị danh sách và tổng tiền.
  - `[x]` Bấm nút Order -> Gọi API `checkout_cart`.
