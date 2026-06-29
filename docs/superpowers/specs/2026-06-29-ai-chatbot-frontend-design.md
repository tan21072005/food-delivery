# Thiết kế frontend AI chatbot NGBT

**Ngày:** 2026-06-29  
**Trạng thái:** Đã được duyệt

## 1. Mục tiêu

Tạo trải nghiệm chatbot AI cho ứng dụng Android food-delivery. Chatbot là tab
chính ở giữa thanh bottom navigation, giúp Customer hỏi bằng tiếng Việt về Món,
Restaurant, Order và cách sử dụng ứng dụng.

Thiết kế lấy cảm hứng từ giao diện Moni trong ảnh tham chiếu: bố cục thoáng,
composer nổi phía dưới và lịch sử dạng bottom sheet. Màu hồng của ảnh được thay
bằng màu cam thương hiệu hiện tại để chatbot vẫn thuộc cùng một ứng dụng.

## 2. Điều hướng

Bottom navigation có đúng năm mục:

1. Trang chủ
2. Đơn hàng
3. Trợ lý AI
4. Yêu thích
5. Cá nhân

`Trợ lý AI` là destination chuẩn ở vị trí thứ ba. Khi chọn tab:

- nếu chưa đăng nhập hoặc JWT hết hạn, chuyển sang luồng đăng nhập;
- nếu đã đăng nhập, mở conversation được chọn gần nhất trong phiên;
- nếu chưa có conversation, hiển thị trạng thái chào và câu hỏi gợi ý.

Chatbot là tab cấp cao nên header không có nút quay lại hoặc nút Trang chủ.
Chuyển sang tab khác bằng bottom navigation không xóa conversation đang mở.

## 3. Hướng thị giác

### Màu

- `NGBT Orange`: `#F5A623` — tab đang chọn, nút gửi, bubble Customer.
- `Warm Orange`: `#FFD580` — điểm nhấn và trạng thái đang trả lời.
- `Assistant Wash`: `#FFF8E1` — nền gợi ý và trạng thái rỗng.
- `Paper`: `#FFFFFF` — surface, bubble trợ lý và bottom sheet.
- `Ink`: `#1A1A1A` — nội dung chính.
- `Muted`: `#888888` — thời gian, placeholder và nội dung phụ.

Không đưa hồng hoặc tím từ ảnh tham chiếu vào chatbot. Dùng typography hệ thống
Android hiện tại để nhất quán và hỗ trợ tiếng Việt; phân cấp bằng weight, size
và khoảng cách thay vì thêm font bên ngoài.

### Bố cục

```text
┌──────────────────────────────────┐
│ Chat bot trợ lý NGBT    [☷] [＋] │  Header
├──────────────────────────────────┤
│              Hôm nay             │
│                       [Customer]  │
│ [Trả lời của trợ lý]              │
│ Hữu ích?              [Có][Không] │
│                                  │
│          vùng hội thoại          │
│                                  │
├──────────────────────────────────┤
│ [ Hỏi về Món hoặc Order... ][➤]  │  Composer
├──────────────────────────────────┤
│ Home  Order   AI   Yêu thích  Tôi│  Bottom nav
└──────────────────────────────────┘
```

Điểm nhận diện chính là composer trắng bo tròn, nổi nhẹ ngay trên bottom
navigation, với nút gửi cam. Phần còn lại giữ yên tĩnh, ít decoration và ưu
tiên khả năng đọc.

## 4. Màn hình chat

### Header

- Tiêu đề chính xác: `Chat bot trợ lý NGBT`.
- Nút lịch sử mở bottom sheet lịch sử conversation.
- Nút `+` tạo conversation mới bằng cách đặt lại màn hình; chỉ tạo row database
  khi Customer gửi câu hỏi đầu tiên.
- Không có nút quay lại, yêu thích, hỗ trợ hoặc Trang chủ trong header.

### Trạng thái rỗng

Hiển thị lời chào ngắn: `Hôm nay bạn muốn ăn gì?` và tối đa ba câu hỏi nhanh:

- `Gợi ý Món trưa cho tôi`
- `Restaurant nào đang mở?`
- `Order gần nhất của tôi thế nào?`

Chạm câu hỏi gợi ý điền nội dung vào composer; Customer vẫn chủ động nhấn gửi.

### Danh sách tin nhắn

- Dùng `RecyclerView`, cuộn từ trên xuống và tự cuộn đến tin mới khi phù hợp.
- Tin Customer là bubble cam, chữ trắng, căn phải.
- Tin trợ lý là bubble trắng hoặc cam rất nhạt, chữ `Ink`, căn trái.
- Nhãn ngày nằm giữa; thời gian nằm cạnh hoặc dưới từng nhóm tin.
- Chỉ tin trợ lý hoàn tất mới có `Có` và `Không`.
- Feedback đã chọn dùng viền/màu cam; nhấn lựa chọn còn lại cập nhật feedback.
- Không hiển thị markdown phức tạp trong phiên bản đầu; render text thuần.

### Composer

- Nằm cố định phía trên bottom navigation và bàn phím.
- Placeholder: `Hỏi về Món hoặc Order...`.
- Trim nội dung; tối đa 1.000 ký tự.
- Nút gửi chỉ bật khi có nội dung hợp lệ và không có request đang chạy.
- Sau khi gửi, giữ câu hỏi trong lịch sử ngay lập tức, xóa composer và hiển thị
  `Đang trả lời…`.

## 5. Lịch sử và tùy chọn

Bottom sheet lịch sử chiếm tối đa khoảng 70% chiều cao:

- tiêu đề `Lịch sử trò chuyện`;
- conversation mới nhất ở trên;
- mỗi dòng có title, thời gian cập nhật và nút ba chấm;
- nút lớn `+ Trò chuyện mới` ở cuối sheet.

Chọn conversation đóng sheet và tải message. Nút ba chấm mở bottom sheet tùy
chọn gồm `Đổi tên` và `Xóa`. Xóa yêu cầu xác nhận rõ ràng; sau khi xóa
conversation đang mở, chuyển về trạng thái rỗng. Không có chức năng chia sẻ
trong phiên bản đầu.

## 6. Luồng trạng thái

```text
Chọn tab AI
   ├─ Chưa đăng nhập ──> Đăng nhập
   └─ Đã đăng nhập
        ├─ Chưa có chat ──> Lời chào + gợi ý
        └─ Có chat ───────> Tải lịch sử

Gửi câu hỏi
   ├─ Thành công ──> Lưu và hiển thị trả lời + feedback
   ├─ Mất mạng ────> Giữ câu hỏi failed + nút Thử lại
   ├─ Hết phiên ───> Điều hướng đăng nhập
   └─ Hết lượt ────> Thông báo giới hạn ngày, không treo composer
```

Chỉ cho phép một request gửi tại một thời điểm. Nếu Customer đổi conversation
trong lúc callback cũ chưa về, callback đó không được ghi đè conversation đang
hiển thị.

## 7. Kiến trúc Android

- `ChatFragment`: bind view, render state, xử lý keyboard và mở bottom sheet.
- `ChatViewModel`: giữ conversation đang chọn, messages, operation generation,
  trạng thái gửi/retry và các event điều hướng.
- `ChatRepository`: gửi Edge Function và đọc/ghi lịch sử qua Supabase RLS.
- `ChatMessageAdapter`: render day, Customer, assistant và typing rows.
- `ChatHistoryBottomSheet`: chọn/tạo mới và mở tùy chọn conversation.
- `ChatOptionsBottomSheet`: đổi tên/xóa conversation.

Fragment không gọi Retrofit trực tiếp. ViewModel không giữ `Context` hoặc View.
Repository không quyết định cách hiển thị lỗi.

## 8. Lỗi và khả năng phục hồi

- `401`: phát event hết phiên và chuyển về đăng nhập.
- `DAILY_LIMIT_REACHED`: `Bạn đã dùng hết 15 lượt hỏi hôm nay`.
- `UPSTREAM_RATE_LIMIT`: `Trợ lý đang bận. Vui lòng thử lại sau`.
- Mất mạng: `Không thể kết nối. Vui lòng kiểm tra mạng`.
- Lỗi khác: `Không thể xử lý yêu cầu. Vui lòng thử lại`.

Message gửi thất bại giữ nguyên nội dung và `client_request_id`; nút `Thử lại`
dùng lại request id để backend không tạo message trùng.

## 9. Kiểm thử và tiêu chí hoàn tất

- Resource contract xác nhận AI là mục thứ ba trong bottom navigation.
- ViewModel tests bao phủ gửi thành công, retry, hết phiên và callback cũ.
- Adapter tests bao phủ day row, bubble hai phía, typing và feedback.
- Repository contract tests tiếp tục xác nhận endpoint/query/error mapping.
- Build debug thành công và không chứa OpenAI key hoặc Supabase service-role key.
- Kiểm thử thủ công xác nhận keyboard không che composer, bottom nav vẫn hiện,
  lịch sử hoạt động và xoay màn hình không mất conversation.
