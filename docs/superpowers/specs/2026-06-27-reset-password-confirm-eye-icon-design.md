# Thiết kế đổi icon ô nhập lại mật khẩu

## Mục tiêu

Trong biểu mẫu đặt lại mật khẩu thuộc luồng quên mật khẩu, thay icon trái tim tại ô “Nhập lại mật khẩu mới” bằng icon con mắt để giao diện phù hợp với trường nhập mật khẩu.

## Phạm vi

- Chỉ thay thuộc tính `android:src` của `ivToggleConfirm` trong `app/src/main/res/layout/fragment_password_form.xml`.
- Dùng drawable `@drawable/ic_eye_off` đã có sẵn trong dự án.
- Giữ nguyên kích thước, căn chỉnh, mô tả trợ năng và hành vi hiện tại.
- Không thay icon của ô mật khẩu cũ hoặc ô mật khẩu mới.
- Không bổ sung chức năng hiện/ẩn mật khẩu trong thay đổi này.

## Kiểm tra

- Xác nhận `ivToggleConfirm` tham chiếu `@drawable/ic_eye_off`.
- Xác nhận hai icon còn lại trong layout không bị thay đổi.
- Chạy kiểm thử đơn vị hiện có của ứng dụng để phát hiện lỗi tài nguyên hoặc hồi quy.
