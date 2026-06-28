# Thiết kế bộ sưu tập Restaurant yêu thích

## Mục tiêu

Xây dựng luồng giao diện “Góc khoái khẩu” trong tab yêu thích. Customer có thể tạo bộ sưu tập, chọn các Restaurant mẫu và xem lại dữ liệu sau khi chuyển màn hình hoặc khởi động lại ứng dụng. Tính năng chỉ dùng dữ liệu cục bộ, không gọi Supabase hay API.

## Phạm vi

- Màn danh sách bộ sưu tập theo dạng lưới hai cột.
- Màn nhập hoặc chọn tên bộ sưu tập.
- Màn chọn Restaurant từ danh sách mẫu.
- Mở lại bộ sưu tập đã tạo để xem và chỉnh sửa lựa chọn.
- Lưu bền vững bộ sưu tập sau khi tắt ứng dụng.
- Điều hướng tiến, lùi và trạng thái nút bám theo dữ liệu hiện tại.

Không bao gồm đồng bộ tài khoản, tải dữ liệu mạng, tìm kiếm Restaurant, xóa bộ sưu tập hoặc tích hợp Supabase.

## Kiến trúc

Tính năng nằm trong `ui/favorites` và tuân theo cấu trúc Java/XML hiện có:

- `FavoritesFragment`: màn “Góc khoái khẩu”.
- `CollectionNameFragment`: nhập hoặc chọn tên.
- `CollectionRestaurantsFragment`: thêm/bỏ Restaurant và hoàn thành.
- `FavoriteCollectionAdapter`: hiển thị lưới bộ sưu tập.
- `FavoriteRestaurantAdapter`: hiển thị danh sách Restaurant mẫu.
- `FavoriteCollection`, `FavoriteRestaurant`: mô hình dữ liệu cục bộ.
- `FavoriteCollectionStore`: lớp duy nhất đọc/ghi `SharedPreferences`, tuần tự hóa JSON bằng Gson.
- `FavoriteCollectionDraftViewModel`: giữ bản nháp xuyên suốt hai màn hình tạo/chỉnh sửa và qua thay đổi cấu hình.

Navigation Component chịu trách nhiệm chuyển màn hình. ID bộ sưu tập được truyền bằng argument khi chỉnh sửa; dữ liệu đầy đủ được đọc từ store thay vì truyền object lớn trong Bundle.

## Luồng giao diện

### Góc khoái khẩu

- Tiêu đề lớn “Góc khoái khẩu”.
- RecyclerView dạng lưới hai cột hiển thị ảnh đại diện, tên rút gọn và số Restaurant.
- Một ô viền xanh nhạt “+ Thêm bộ sưu tập mới” luôn nằm cuối danh sách.
- Nhấn ô thêm mới mở màn nhập tên với bản nháp rỗng.
- Nhấn bộ sưu tập hiện có mở màn chọn Restaurant để xem/chỉnh sửa trực tiếp.
- Bottom navigation vẫn hiển thị tại màn này.

### Thêm bộ sưu tập mới

- Thanh đầu trang có nút quay lại và tiêu đề.
- Ô nhập tên có nút xóa khi đã có nội dung.
- Các chip gợi ý điền tên vào ô nhập; Customer vẫn có thể sửa tự do.
- Nút “Quán yêu thích” bị vô hiệu hóa khi tên chỉ chứa khoảng trắng và được bật khi tên hợp lệ.
- Nhấn nút tiếp tục chuyển sang màn chọn Restaurant, giữ nguyên tên trong bản nháp.

### Thêm Restaurant vào bộ sưu tập

- Thanh đầu trang có nút quay lại và tiêu đề.
- Khối chú thích về danh sách gợi ý.
- Danh sách Restaurant mẫu gồm ảnh local, tên, đánh giá, lượt bán và khoảng cách.
- Mỗi dòng có trạng thái “Thêm vào bộ sưu tập” hoặc “Đã thêm vào bộ sưu tập”. Nhấn vào hành động sẽ chuyển đổi trạng thái ngay lập tức.
- Nút “Hoàn thành” lưu mới hoặc cập nhật bộ sưu tập, sau đó quay về “Góc khoái khẩu”.
- Khi quay lại màn nhập tên rồi trở lại, các lựa chọn Restaurant trong bản nháp không bị mất.
- Bottom navigation bị ẩn ở hai màn hình tạo/chỉnh sửa.

## Dữ liệu và lưu trữ

`FavoriteCollection` gồm ID ổn định, tên và danh sách ID Restaurant. `FavoriteRestaurant` là danh mục mẫu bất biến được khai báo cục bộ; mỗi phần tử có ID, nội dung hiển thị và drawable ảnh.

`FavoriteCollectionStore` lưu danh sách bộ sưu tập thành JSON trong một file `SharedPreferences` riêng. Việc lưu chỉ xảy ra khi Customer nhấn “Hoàn thành”. Bản nháp chưa hoàn thành được giữ trong `FavoriteCollectionDraftViewModel`, vì vậy không tạo dữ liệu rác sau khi Customer hủy luồng.

Khi JSON trống hoặc không đọc được, store trả về danh sách mặc định an toàn thay vì làm ứng dụng crash. Tên được trim trước khi lưu; ID mới được tạo cục bộ và không phụ thuộc máy chủ.

## Trạng thái mặc định

Lần chạy đầu hiển thị một bộ sưu tập mẫu “Yêu thích” có các Restaurant mẫu và ô thêm mới. Những bộ sưu tập Customer tạo sau đó được thêm vào lưới. Ảnh bìa dùng ảnh của tối đa ba Restaurant đầu tiên; bộ sưu tập trống dùng ảnh placeholder local.

## Thiết kế hình ảnh

- Nền trắng, chữ gần đen và màu nhấn cyan/teal đồng bộ ảnh tham chiếu.
- Góc bo lớn cho ô nhập, nút và thẻ; khoảng trắng rộng, ưu tiên khả năng đọc trên màn hình nhỏ.
- Typography dùng font hệ thống với phân cấp đậm rõ ràng để không thêm dependency font.
- Dấu hiệu nhận diện là ảnh bìa dạng ghép từ Restaurant đã chọn, giúp mỗi bộ sưu tập phản ánh đúng nội dung thay vì dùng ảnh trang trí cố định.
- Các trạng thái disabled, selected và focus có độ tương phản rõ ràng; vùng chạm tối thiểu 48dp.

## Xử lý lỗi và trường hợp biên

- Không cho tiếp tục với tên rỗng hoặc toàn khoảng trắng.
- Không thêm trùng cùng một Restaurant vào một bộ sưu tập.
- Bộ sưu tập được phép có 0 Restaurant và hiển thị placeholder cùng nhãn “0 quán”.
- Nếu bộ sưu tập cần chỉnh sửa không còn tồn tại, màn hình quay về danh sách thay vì crash.
- Nếu dữ liệu lưu cục bộ bị hỏng, ứng dụng phục hồi về trạng thái mặc định.

## Kiểm thử và xác minh

- Unit test cho store: lưu/đọc, cập nhật, dữ liệu lỗi và chống trùng ID Restaurant.
- Unit test cho quy tắc hợp lệ của tên và trạng thái bản nháp.
- Build debug để phát hiện lỗi resource/navigation.
- Kiểm tra thủ công: tạo bộ sưu tập, back/forward, chỉnh sửa lựa chọn, đổi tab, xoay màn hình và tắt/mở lại ứng dụng.

## Tiêu chí hoàn thành

- Ba màn hình bám sát bố cục và trạng thái trong ảnh tham chiếu.
- Mọi nút/icon liên quan chuyển đúng màn hình.
- Tên và lựa chọn Restaurant không mất khi chuyển qua lại trong luồng.
- Bộ sưu tập đã hoàn thành vẫn tồn tại sau khi tắt/mở ứng dụng.
- Không có lời gọi Supabase hoặc phụ thuộc mạng trong tính năng.
- Dự án build debug thành công và các kiểm thử liên quan chạy đạt.
