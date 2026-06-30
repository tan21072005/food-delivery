# Thiết lập backend AI chatbot

Backend chatbot chạy trong Supabase Edge Function `chat-assistant`. OpenAI key
chỉ tồn tại trong Supabase secrets hoặc file môi trường local bị Git bỏ qua;
không đưa key vào Android, `local.properties`, log hay ảnh chụp màn hình.

## Kiểm thử code

Từ thư mục gốc của repo:

```powershell
deno fmt --check supabase/functions/chat-assistant
deno lint supabase/functions/chat-assistant
deno test supabase/functions/chat-assistant
```

## Chạy Supabase local

Yêu cầu Docker Desktop đang chạy. Tạo file
`supabase/functions/.env.local` (không commit) với:

```dotenv
OPENAI_API_KEY=<OpenAI project key>
OPENAI_MODEL=gpt-4o-mini
```

Sau đó chạy:

```powershell
supabase start
supabase db reset
supabase functions serve chat-assistant --env-file supabase/functions/.env.local
```

Gọi function bằng JWT của một Customer test local. Xác minh:

- câu hỏi về Món trả `200` và lưu một user message cùng một assistant message;
- câu hỏi về Order chỉ thấy Order của Customer đang đăng nhập;
- request không có JWT trả `401`;
- nội dung dài 1.001 ký tự trả `400`;
- câu hỏi hoàn tất thứ 16 trong cùng ngày UTC trả `429`.

Chạy kiểm tra database:

```powershell
supabase db advisors
supabase migration list --local
```

Không để lại security finding do migration chatbot gây ra và bảo đảm migration
local khớp với các file trong repo.

## Deploy

Project ref và secret được nhập tương tác; không ghi các giá trị này vào repo:

```powershell
supabase login
$projectRef = Read-Host 'Supabase project ref'
supabase link --project-ref $projectRef
supabase secrets set OPENAI_API_KEY
supabase secrets set OPENAI_MODEL=gpt-4o-mini
supabase db push
supabase functions deploy chat-assistant
```

Giữ `verify_jwt = true` cho `chat-assistant` trong `supabase/config.toml`.

Sau các sửa lỗi mới nhất, luôn deploy đủ cả database migration và Edge Function:

```powershell
cd C:\Project\food-delivery
& "C:\Program Files\nodejs\npx.cmd" --yes supabase@latest db push
& "C:\Program Files\nodejs\npx.cmd" --yes supabase@latest functions deploy chat-assistant
```

Nếu CLI báo `WARNING: Docker is not running` nhưng vẫn có dòng `Finished supabase db push`
hoặc `Deployed Functions ... chat-assistant` thì thao tác remote đã hoàn tất.

## Kiểm tra lỗi thường gặp

- `INVALID_CONVERSATION_ID`: app hoặc Edge Function đang chạy bản cũ. Bản mới đã xử lý
  `conversation_id` rỗng hoặc thiếu như một cuộc trò chuyện mới.
- `401` khi gọi function: app chưa có Supabase Auth session hợp lệ, hoặc token đã hết hạn.
  Đăng nhập lại trước khi mở chatbot.
- Sau khi đăng nhập vẫn bị lỗi quyền/profile: deploy lại function bản mới. Function sẽ tự tạo
  profile tối thiểu trong `public.users` nếu Supabase Auth user chưa có dòng tương ứng.
- `UPSTREAM_ERROR` / thông báo “Không thể xử lý yêu cầu”: mở Supabase Dashboard →
  Edge Functions → `chat-assistant` → Logs và tìm log có prefix `[chat-assistant]`.
- Trên Android Studio, lọc Logcat theo tag `ChatRepository` để xem dòng
  `Chat API failed. status=..., body=...`.

## Verification gần nhất

Lần kiểm tra local gần nhất: **30/06/2026 13:51 (Asia/Saigon)**.

Đã chạy:

```powershell
deno fmt --check supabase\functions\chat-assistant
deno lint supabase\functions\chat-assistant
deno test supabase\functions\chat-assistant
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
rg -n "OPENAI_API_KEY|SUPABASE_SERVICE_ROLE|sk-[A-Za-z0-9_-]{16,}" app\src\main
```

Kết quả:

- Edge Function format: `Checked 9 files`;
- Edge Function lint: `Checked 9 files`;
- Edge Function tests: `23 passed | 0 failed`;
- Android unit tests: `65 tests, 0 failures, 0 errors`;
- Debug APK: `app\build\outputs\apk\debug\app-debug.apk`;
- Android secret scan: không có match trong `app\src\main`.
- Chat tab initial load: lần load history đầu tiên tự mở conversation mới nhất; nếu Customer
  đã bấm `Trò chuyện mới` thì không tự kéo lại cuộc cũ.
- History bottom sheet: mở expanded với chiều cao mục tiêu khoảng 70% màn hình.
- History rows: `updated_at` được format thành `Hôm nay` hoặc `dd/MM/yyyy`, không hiển thị
  raw ISO timestamp.
- Retry khi gửi lỗi: màn hình giữ lại bubble câu hỏi của Customer rồi hiển thị row
  `Thử lại`, dùng lại `client_request_id` cũ để tránh tạo message trùng.
- RecyclerView safety: typing row reset click listener khi tái sử dụng holder từ retry row.
- Feedback controls: chỉ hiện cho assistant message có status `complete` / `completed`.
- Message time: giờ gửi tin nhắn được format theo timezone của thiết bị thay vì lấy raw UTC substring.
- History bottom sheet lifecycle: detach `historyList` adapter trong `onDestroyView`.
- Menu add-to-cart regression: `MenuFragment` vẫn sync `LocalCart` sau khi Supabase add cart
  thành công, để sticky cart cập nhật đúng.
- Chat history/options sheets: dùng lại `ChatViewModel` của `ChatFragment`, không tự tạo
  ViewModel bằng default factory.
- Feedback `Có` / `Không`: Android gọi `functions/v1/chat-assistant/feedback`
  và không gửi `public.users.id`; Edge Function tự map JWT sang Customer profile.
- Supabase build config: `app/build.gradle.kts` validate `SUPABASE_URL` /
  `SUPABASE_ANON_KEY`, chuẩn hóa URL có dấu `/` cuối, và repo có
  `local.properties.example` không chứa secret thật.
- Supabase client contract: request dùng `apikey`, `Authorization` từ
  `SessionManager.getBearerToken()` khi đã đăng nhập.

Phần vẫn cần xác minh trên thiết bị/emulator sau khi deploy remote:

1. đăng nhập Customer thật;
2. mở tab `Trợ lý AI`;
3. gửi `xin chào`;
4. xác nhận có trả lời từ assistant;
5. mở lịch sử, chọn lại conversation, thử feedback `Có` / `Không`;
6. nếu lỗi, lấy Logcat tag `ChatRepository` và Supabase Function Logs có prefix
   `[chat-assistant]`.

Code backend và test dùng OpenAI fake có thể hoàn tất hoàn toàn ở local. Deploy
thật cần quyền truy cập Supabase project; kiểm thử OpenAI end-to-end cần API key
có billing. Với đồ án sinh viên, nên dùng OpenAI project riêng và đặt hạn mức
chi tiêu khoảng 5 USD. Không để secret trong `local.properties`, Git, APK
resources, log hoặc ảnh chụp màn hình.
