# Giai đoạn 2: Cấu trúc lại Giỏ hàng & Thanh toán (Multi-Vendor)

Mình đã hoàn thành việc refactor logic Giỏ hàng và Thanh toán để hỗ trợ việc đặt món từ nhiều nhà hàng cùng lúc.

## Các thay đổi chính

### 1. Cập nhật Logic Database (Supabase)
- Sửa đổi hàm `checkout_cart` trong file `docs/rpc_cart_order.sql`:
  - Thêm vòng lặp qua từng `restaurant_id` có trong giỏ hàng.
  - Mỗi nhà hàng sẽ được tạo một bản ghi `Order` riêng biệt với phí giao hàng (15,000đ/quán) và tổng tiền độc lập.
  - Hàm giờ đây trả về danh sách mã đơn hàng (`BIGINT[]`) thay vì chỉ một ID duy nhất.
- Sửa đổi hàm `get_cart_summary` trong file `docs/rpc_cart_order.sql`:
  - Logic tính phí vận chuyển được điều chỉnh để nhân với số lượng nhà hàng (`COUNT(DISTINCT restaurant_id) * 15000`).
  - Gộp chung tổng số tiền cần thanh toán một cách chính xác.

*(**Lưu ý:** Bạn cần copy toàn bộ file `docs/rpc_cart_order.sql` và dán vào Supabase SQL Editor để các Function này được cập nhật trên Cloud Database).*

### 2. Cập nhật Android Code (Retrofit & Repository)
- Đổi kiểu trả về của endpoint `checkoutCart` trong `ApiService.java` từ `Call<Long>` sang `Call<List<Long>>`.
- Cập nhật `OrderRepository.java` và `CheckoutViewModel.java` để xử lý luồng `List<Long>` khi thanh toán thành công.

### 3. Cập nhật Giao diện (UI) Giỏ Hàng
- Sửa `CartAdapter.java` để tự động sắp xếp và gom nhóm (`sort`) các món ăn theo cùng một `restaurant_id` để chúng hiển thị cạnh nhau trên giao diện.
- Sửa đổi text trong `Checkout.java` thành "Đơn hàng của bạn" thay vì cố định tên của một nhà hàng, và cập nhật thông báo thành công để hiển thị toàn bộ các mã đơn hàng vừa được tạo (VD: "Đặt món thành công! Mã đơn: #12, #13").

## Kiểm thử
- Các hàm RPC mới sẽ chia nhỏ giỏ hàng một cách mượt mà dưới backend mà không cần gửi quá nhiều request từ client.
- Giao diện Android sẵn sàng hiển thị danh sách đơn hàng đã được gom cụm theo từng quán.
