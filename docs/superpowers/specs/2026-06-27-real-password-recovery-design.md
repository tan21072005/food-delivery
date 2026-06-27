# Thiết kế luồng quên mật khẩu thật

## Mục tiêu

Thay luồng quên mật khẩu mô phỏng bằng luồng khôi phục mật khẩu thật qua Supabase Auth:

1. Người dùng nhập email.
2. Supabase gửi OTP khôi phục gồm 6 chữ số qua email.
3. Ứng dụng xác minh OTP và nhận recovery access token.
4. Người dùng đặt mật khẩu mới.
5. Ứng dụng cập nhật mật khẩu, xóa trạng thái khôi phục và quay về đăng nhập.

## Phạm vi và ranh giới

- Chỉ thay đổi mã nguồn thuộc luồng Auth và các model/API dành riêng cho password recovery.
- Giữ nguyên hành vi đăng nhập, đăng ký, hồ sơ, giỏ hàng, đơn hàng và các module khác.
- Tiếp tục dùng ba Fragment hiện có:
  - `ForgetPassFragment`
  - `VerifyOtpFragment`
  - `PasswordFormFragment` ở chế độ `MODE_RESET`
- Dùng MVVM: Fragment chỉ xử lý hiển thị và thao tác UI; ViewModel giữ trạng thái; Repository gọi API.
- Dùng Retrofit REST API để nhất quán với kiến trúc mạng hiện có.
- Không dùng RecyclerView vì ba màn hình là form cố định, không phải danh sách dữ liệu.
- Không lưu recovery access token vào `SessionManager` hoặc bộ nhớ bền vững.

## Kiến trúc

### Presentation

Tạo `PasswordRecoveryViewModel` dùng chung theo phạm vi `AuthActivity` bằng `ViewModelProvider(requireActivity())`.

ViewModel giữ:

- Email đã chuẩn hóa.
- Recovery access token trong bộ nhớ.
- Trạng thái thao tác: idle, loading, success hoặc error.
- Mốc thời gian được phép gửi lại OTP.

Mỗi Fragment quan sát state chỉ liên quan đến bước của mình:

- `ForgetPassFragment`: gửi OTP và điều hướng khi API thành công.
- `VerifyOtpFragment`: xác minh OTP, gửi lại OTP và điều hướng khi có recovery token.
- `PasswordFormFragment`: trong `MODE_RESET`, cập nhật mật khẩu bằng recovery token; các mode khác giữ nguyên.

Nếu Fragment được mở nhưng ViewModel thiếu dữ liệu bắt buộc do tiến trình đã bị hủy, điều hướng an toàn về bước nhập email.

### Data

Tạo API client riêng cho password recovery để không thay đổi interceptor hoặc session header đang dùng bởi các API khác.

Các thành phần:

- `PasswordRecoveryApiService`: khai báo ba endpoint Supabase Auth.
- `PasswordRecoveryRepository`: bao bọc Retrofit callback và không chứa UI logic.
- Các request model riêng: recovery email, recovery OTP và mật khẩu mới.
- Response xác minh OTP dùng session response có `access_token`; không ghi session này thành phiên đăng nhập thông thường.

Client recovery luôn gửi:

- `apikey: <Supabase public/anon key>`
- `Content-Type: application/json`
- Authorization bằng anon key cho gửi/xác minh OTP.
- `Authorization: Bearer <recovery access token>` chỉ cho cập nhật mật khẩu.

## API contract

### Gửi hoặc gửi lại OTP

```http
POST /auth/v1/recover
Content-Type: application/json

{
  "email": "user@example.com"
}
```

Thành công HTTP 200. UI luôn dùng thông báo trung tính: nếu email khớp tài khoản, mã xác minh đã được gửi.

### Xác minh OTP recovery

```http
POST /auth/v1/verify
Content-Type: application/json

{
  "email": "user@example.com",
  "token": "123456",
  "type": "recovery"
}
```

Thành công trả về session chứa `access_token`. Token chỉ tồn tại trong `PasswordRecoveryViewModel`.

### Cập nhật mật khẩu

```http
PUT /auth/v1/user
Authorization: Bearer <recovery access token>
Content-Type: application/json

{
  "password": "NewPassword1!"
}
```

Thành công thì xóa email, token và cooldown; sau đó điều hướng về `loginFragment`.

## Hành vi UI

### Bước nhập email

- Chỉ nhận email; bỏ nội dung gợi ý số điện thoại vì luồng được chọn là OTP email.
- Kiểm tra rỗng và định dạng email trước khi gọi API.
- Khi loading: khóa ô nhập và nút tiếp tục, tránh gửi trùng.
- Chỉ chuyển sang OTP sau HTTP 200.
- Nút quay lại giữ nguyên.

### Bước OTP

- Chỉ nhận đúng 6 chữ số.
- Hiển thị email đã che bớt trong phần mô tả.
- OTP sai hoặc hết hạn hiển thị lỗi từ API theo thông báo thân thiện.
- “Gửi lại mã” gọi lại `/recover`; khóa 60 giây giữa hai lần gửi.
- “Thay đổi email” quay về màn nhập email và xóa token cũ nếu có.
- Không còn OTP hard-code.

### Bước mật khẩu mới

- Chỉ áp dụng logic recovery mới khi `currentMode == MODE_RESET`; không thay đổi `MODE_CREATE` và `MODE_CHANGE`.
- Mật khẩu dài từ 8 đến 20 ký tự và có ít nhất một chữ cái, một chữ số, một ký tự đặc biệt.
- Hai mật khẩu phải khớp.
- Hai icon mắt chuyển đổi hiện/ẩn mật khẩu thật và giữ vị trí con trỏ.
- Khi loading: khóa các trường, icon mắt và nút xác nhận.
- Thành công quay về đăng nhập bằng navigation action, không `popBackStack()` về OTP.

## Trạng thái và sự kiện

Mỗi yêu cầu có một kết quả dùng một lần để tránh điều hướng hoặc Toast lặp lại sau khi Fragment được tạo lại. Fragment đánh dấu sự kiện đã xử lý.

Không chạy đồng thời hai request cùng loại. Response cũ không được ghi đè một thao tác mới hơn.

ViewModel xóa recovery state khi:

- Đổi mật khẩu thành công.
- Người dùng quay về đăng nhập.
- Người dùng bắt đầu lại bằng email khác.

## Xử lý lỗi

- Không có mạng hoặc timeout: giữ nguyên màn hình, mở lại nút và cho phép thử lại.
- HTTP 400/401/403 khi xác minh: thông báo OTP không đúng hoặc đã hết hạn.
- HTTP 429: thông báo thao tác quá nhanh và tôn trọng cooldown.
- Recovery token thiếu/hết hạn: quay về bước nhập email và yêu cầu thực hiện lại.
- Lỗi server khác: thông báo chung, không hiển thị response nhạy cảm.
- Parse lỗi Supabase theo model lỗi riêng; có fallback theo HTTP status.

## Bảo mật

- Không kiểm tra OTP ở client ngoài kiểm tra hình thức 6 chữ số.
- Không hard-code OTP, mật khẩu, service-role key hoặc secret key.
- Chỉ dùng public/anon key trong ứng dụng.
- Không ghi recovery token vào log, Bundle, SharedPreferences hoặc `SessionManager`.
- Không tiết lộ email có tồn tại trong hệ thống.
- Xóa token khỏi ViewModel ngay khi hoàn tất hoặc hủy luồng.
- Không tự động đăng nhập người dùng sau khi đổi mật khẩu; yêu cầu đăng nhập bằng mật khẩu mới.

## Cấu hình Supabase bắt buộc

Trong Dashboard, mẫu email “Reset Password” phải hiển thị OTP bằng biến:

```text
{{ .Token }}
```

Nếu template chỉ chứa `{{ .ConfirmationURL }}`, người dùng sẽ nhận liên kết thay vì mã 6 chữ số. SMTP tùy chỉnh được khuyến nghị cho môi trường production; dịch vụ email mặc định có giới hạn gửi thấp.

## Kiểm thử

### Unit test

- API service dùng đúng endpoint, HTTP method và authorization header.
- Repository chuyển tiếp đúng email, OTP, recovery type và mật khẩu.
- ViewModel không điều hướng trước khi request thành công.
- OTP chỉ chấp nhận 6 chữ số.
- Quy tắc mật khẩu 8–20 ký tự, độ phức tạp và xác nhận trùng khớp.
- Recovery token chỉ được đặt sau verify thành công và bị xóa sau update thành công.
- Resend cooldown chặn gửi sớm.
- `MODE_CREATE` và `MODE_CHANGE` không bị thay đổi.

### Regression/build

- Chạy toàn bộ `:app:testDebugUnitTest`.
- Chạy `:app:assembleDebug` để xác nhận resources, manifest và navigation compile.
- Kiểm tra thủ công với một tài khoản Supabase thử nghiệm:
  - Email nhận OTP 6 số.
  - OTP sai bị từ chối.
  - OTP đúng mở màn đặt mật khẩu.
  - Mật khẩu mới đăng nhập được.
  - Mật khẩu cũ không đăng nhập được.
  - Gửi lại mã bị khóa 60 giây.

## Tiêu chí hoàn thành

- Không còn OTP hard-code hoặc Toast thành công giả.
- Ba API recovery thực sự được gọi.
- Luồng thành công kết thúc ở màn đăng nhập.
- Loading, lỗi mạng, OTP sai/hết hạn và rate limit đều có phản hồi UI.
- Recovery token không được lưu bền vững.
- Chỉ các file trong phạm vi Auth/recovery và test liên quan bị thay đổi.
