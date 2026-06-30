# Thiết kế đổi icon các ô mật khẩu mới

## Mục tiêu

Trong biểu mẫu đặt lại mật khẩu thuộc luồng quên mật khẩu, thay icon trái tim tại các ô “Nhập mật khẩu mới” và “Nhập lại mật khẩu mới” bằng icon con mắt để giao diện phù hợp với trường nhập mật khẩu.

## Phạm vi

- Thay thuộc tính `android:src` của `ivToggleNew` và `ivToggleConfirm` trong `app/src/main/res/layout/fragment_password_form.xml`.
- Dùng drawable `@drawable/ic_eye_off` đã có sẵn trong dự án.
- Giữ nguyên kích thước, căn chỉnh, mô tả trợ năng và hành vi hiện tại.
- Không thay icon của ô mật khẩu cũ.
- Không bổ sung chức năng hiện/ẩn mật khẩu trong thay đổi này.

## Kiểm tra

- Xác nhận `ivToggleNew` và `ivToggleConfirm` tham chiếu `@drawable/ic_eye_off`.
- Xác nhận icon `ivToggleOld` không bị thay đổi.
- Chạy kiểm thử đơn vị hiện có của ứng dụng để phát hiện lỗi tài nguyên hoặc hồi quy.
