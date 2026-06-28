# food-delivery

Ứng dụng đặt món ăn dành cho Khách hàng (Customer app). Khách đặt Món từ Restaurant, thanh toán và theo dõi đơn hàng đến khi nhận hàng. Seller có app riêng biệt, không thuộc scope của repo này.

## Language

### Người dùng & Địa điểm

**Customer**:
Người dùng duy nhất của app — người đặt Món, quản lý Cart, theo dõi Order, và lưu DeliveryAddress.
_Avoid_: User, buyer, client, account

**Restaurant**:
Đơn vị kinh doanh có ID, tên, địa chỉ và danh sách Món. Được phân loại bằng Cuisine. Restaurant vận hành trên app riêng (Seller app) — Customer app chỉ đọc thông tin Restaurant.
_Avoid_: Shop, store, vendor, merchant, quán

**DeliveryAddress**:
Địa chỉ giao hàng đã lưu của Customer, gồm nhãn gợi nhớ ("Nhà", "Cơ quan"), địa chỉ đầy đủ, tọa độ GPS, và cờ `isDefault`. Một Customer có nhiều DeliveryAddress.
_Avoid_: Address, location, shipping address

### Thực đơn & Phân loại

**Món**:
Món ăn hoặc đồ uống cụ thể do Restaurant cung cấp, có id, tên, mô tả, giá, số lượng đã bán (`soldCount`) và ảnh. Một Món thuộc một Restaurant.
_Avoid_: FoodItem, food item, sản phẩm, item, dish

**DishCategory**:
Danh mục phân loại Món (ví dụ: "Cơm", "Phở", "Đồ uống", "Tráng miệng"). Dùng để filter Món trong màn hình Menu. Có `slug` để định danh trong URL/navigation.
_Avoid_: FoodCategory, category, loại món

**Cuisine**:
Loại ẩm thực phân loại Restaurant (ví dụ: "Trà sữa", "Quán ăn", "Pizza", "Sushi"). Dùng để filter/browse Restaurant. Tách biệt hoàn toàn với DishCategory.
_Avoid_: RestaurantCategory, food type, loại quán

### Yêu thích & Bộ sưu tập

**FavoriteRestaurant**:
Quan hệ thể hiện Customer đã đánh dấu yêu thích một Restaurant bằng hành động trái tim. Trạng thái này độc lập với membership của FavoriteCollection: thêm hoặc bỏ Restaurant khỏi collection không tự động thay đổi trạng thái trái tim.
_Avoid_: FavoriteItem, favorite food, bộ sưu tập

**FavoriteCollection**:
Nhóm Restaurant do Customer đặt tên để tổ chức các Restaurant muốn lưu lại. Một FavoriteCollection có thể rỗng; một Restaurant có thể xuất hiện trong nhiều FavoriteCollection. Membership trong collection không ngụ ý Restaurant đã được đánh dấu FavoriteRestaurant.
_Avoid_: Folder, tag, danh sách yêu thích

### Giỏ hàng & Đặt hàng

**Cart**:
Entity có ID lưu danh sách CartItem của Customer trước khi đặt hàng. Được persist (server/local DB) để đồng bộ giữa thiết bị và hỗ trợ "đặt lại đơn cũ".
_Avoid_: Basket, shopping cart, giỏ

**CartItem**:
Một Món kèm số lượng đang nằm trong Cart, chưa được xác nhận thành Order.
_Avoid_: OrderItem (dùng OrderLine sau khi đã đặt), cart item

**Order**:
Đơn đặt hàng đã được Customer xác nhận, gồm nhiều OrderLine, một DeliveryAddress, một PaymentMethod, và trạng thái trong vòng đời. Khác với Cart — Order là cam kết thực sự, không thể chỉnh sửa tự do.
_Avoid_: Purchase, transaction, đơn hàng (dùng "Order" trong code)

**OrderLine**:
Một Món kèm số lượng và giá tại thời điểm đặt, nằm trong Order đã được xác nhận. Khác với CartItem ở chỗ OrderLine là bất biến sau khi Order tạo ra.
_Avoid_: OrderItem, line item, CartItem (dùng CartItem trước khi đặt)

### Vòng đời Order

Order đi qua các trạng thái sau (theo thứ tự):

| Trạng thái | Ý nghĩa | Ai có thể chuyển sang `cancelled`? |
|---|---|---|
| `pending` | Customer vừa đặt, chờ Restaurant xác nhận. Có timeout tự huỷ. | Customer hoặc hệ thống (timeout) |
| `confirmed` | Restaurant đã nhận đơn và đồng ý nấu. | Restaurant (hết hàng, sự cố) |
| `preparing` | Restaurant đang chuẩn bị Món. Customer xem ETA. | Restaurant |
| `ready_for_pickup` | Món xong, chờ shipper đến lấy. Dùng để tính KPI giao hàng. | — |
| `delivering` | Shipper đang di chuyển đến Customer. GPS tracking real-time. | — (chỉ khiếu nại/refund) |
| `completed` | Customer đã nhận hàng thành công. | — |
| `cancelled` | Đơn bị huỷ. Xảy ra từ `pending`, `confirmed`, hoặc `preparing`. | Xem cột bên trái |

**Quy tắc huỷ:**
- Customer chỉ huỷ được ở `pending` (trước khi Restaurant nhận đơn).
- Restaurant huỷ được ở `confirmed` và `preparing` (lý do: hết hàng, sự cố).
- Sau `ready_for_pickup` không thể huỷ — chỉ có khiếu nại/refund riêng.

### Thanh toán

**PaymentMethod**:
Phương thức thanh toán Customer chọn khi checkout. Giá trị: `COD` (tiền mặt khi nhận), `MOMO`, `ZALOPAY`, `BANK_CARD`. Là field trong Order, không phải entity riêng.
_Avoid_: Payment, payment type, hình thức thanh toán
