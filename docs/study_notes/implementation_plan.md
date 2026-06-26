# Kế hoạch Giỏ hàng & Đặt món (Bằng RPC 1 API)

Bạn phát hiện rất chuẩn xác! 👍 Thực tế, sử dụng **PostgreSQL Function (RPC)** cho Giỏ hàng và Đặt món không chỉ giúp gom 1 API để chạy nhanh hơn, mà còn là **tiêu chuẩn bắt buộc về bảo mật và an toàn dữ liệu** (tránh việc thao tác tính tiền bị lỗi do điện thoại người dùng bị sập mạng giữa chừng hoặc cố tình can thiệp sửa giá).

Chúng ta sẽ dùng RPC cho 2 việc quan trọng nhất:

## Bước 1: Viết 2 Hàm SQL (RPC) trên Supabase

1. **Hàm `get_cart_summary(user_id)`**:
   - Chỉ gọi 1 API, Supabase sẽ tự động quét bảng `carts`, tự "join" với bảng `menus` để lấy giá tiền, tính toán sẵn `tổng tiền (subtotal)`, tính phí ship giả định, và trả về cho Android 1 file JSON chứa sẵn mọi thứ.
   - Android không cần phải cộng trừ nhân chia tính tổng tiền nữa.

2. **Hàm `checkout_cart(user_id, delivery_address, note)` (Transaction Đặt món)**:
   - Android chỉ cần bấm "Đặt món" và gọi API này.
   - Supabase sẽ tự động lấy toàn bộ đồ trong giỏ hàng (bảng `carts`), tự động tạo 1 mã hóa đơn trong bảng `orders`, chuyển các món sang `order_items`, và **tự động xóa** giỏ hàng.
   - Tất cả diễn ra trong 1 giao dịch (transaction) 1 chiều duy nhất, đảm bảo không bao giờ có chuyện "tiền đã trừ mà đơn chưa tạo".

## Bước 2: Cập nhật App Android

1. **`CartSummaryResponse.java`**: Tạo Model để hứng dữ liệu trả về từ hàm `get_cart_summary`.
2. **`ApiService.java`**: 
   - Thêm `@POST("rest/v1/rpc/get_cart_summary")`
   - Thêm `@POST("rest/v1/rpc/checkout_cart")`
   - Vẫn giữ các API nhỏ `@POST` và `@PATCH` vào thẳng bảng `carts` để làm thao tác dấu `(+)` và `(-)` khi người dùng bấm tăng giảm số lượng.

## Bước 3: Đấu nối vào Giao diện (UI)

1. Tích hợp API nhỏ `Thêm vào giỏ` vào các nút (+) ở các màn hình Home, Menu.
2. Tại màn hình `Checkout.java`: Gọi API `get_cart_summary` để tải danh sách món ăn và hiển thị lên màn hình.
3. Khi bấm nút "Order": Gọi API `checkout_cart` và hiển thị thông báo thành công.

---

> [!TIP]
> **Tóm lại:**
> - Các hành động nhỏ (Tăng 1 món, giảm 1 món) thì dùng trực tiếp API chuẩn của Supabase.
> - Các hành động cần tính toán phức tạp (Tải danh sách giỏ hàng kèm tính tổng tiền) hoặc cần tính toàn vẹn (Tạo Đơn hàng + Xóa Giỏ) sẽ dùng **1 API duy nhất (RPC)**.

> [!WARNING]
> **Câu hỏi nhỏ cho bạn:**
> Màn hình Checkout hiện tại mình thấy chỉ có vài nút trống. Bạn đã có file giao diện XML riêng (ví dụ `item_cart.xml`) để vẽ ra từng món ăn trong giỏ hàng chưa, hay bạn muốn mình code luôn 1 cái cơ bản cho bạn?

Nếu bạn ưng ý với chiến thuật này, hãy bấm **Proceed (Tiếp tục)** để mình viết Code SQL trước nhé!
