# FoodGo Account Frontend Flow — Team Prompt Design

**Ngày:** 2026-06-30
**Trạng thái:** Đã duyệt thiết kế, chờ người dùng duyệt tài liệu
**Nhánh đích:** `dev`

## 1. Mục tiêu

Chia công việc nâng cấp frontend FoodGo cho ba thành viên, mỗi người dùng một prompt AI riêng và làm trên một nhánh độc lập. Phạm vi gồm:

- Splash Screen kiểm tra phiên đăng nhập rồi điều hướng đến Login hoặc màn hình chính.
- Mặc định Tiếng Việt; người dùng có thể chọn Tiếng Việt hoặc English trong Profile.
- Lưu lựa chọn ngôn ngữ và áp dụng lại ở lần mở sau.
- Design system thống nhất với màu cam san hô và đỏ gạch.
- App icon adaptive dạng monogram `FG` cho thương hiệu FoodGo.
- Khóa toàn bộ ứng dụng ở chế độ portrait.
- Tablet dùng bố cục một cột như điện thoại, căn giữa với chiều rộng tối đa hợp lý.
- Hoàn thiện UI/UX cho Splash, Login, Home và Profile mà không thay đổi nghiệp vụ đặt món.

## 2. Chiến lược nhánh và quyền sở hữu

Trưởng nhóm tạo `feature/foodgo-frontend` từ `dev`. Ba nhánh nhiệm vụ được tạo từ commit đầu của nhánh tích hợp:

| Thành viên | Nhánh | Phạm vi sở hữu |
|---|---|---|
| 1 — Foundation | `feature/foodgo-foundation` | Splash, session routing, locale, Profile language selector, portrait, navigation |
| 2 — UI/UX | `feature/foodgo-ui` | Design tokens, theme, Login/Home/Profile layouts, tablet sizing |
| 3 — Branding & QA | `feature/foodgo-branding-qa` | FoodGo icon/artwork, app label, accessibility audit, tests, regression review |

Thứ tự tích hợp bắt buộc: Foundation → UI/UX → Branding & QA → `feature/foodgo-frontend` → `dev`.

Mỗi thành viên không được sửa phần thuộc quyền sở hữu của người khác nếu chưa trao đổi. Nếu một file dùng chung bắt buộc phải sửa, thành viên phải ghi rõ thay đổi trong bàn giao để trưởng nhóm xử lý khi tích hợp.

## 3. Luồng sản phẩm

1. Android SplashScreen API hiển thị nhận diện FoodGo khi app khởi động.
2. Ứng dụng đọc trạng thái phiên hiện có:
   - Phiên hợp lệ → `MainActivity`.
   - Không có phiên hoặc phiên hết hạn → `AuthActivity`/Login.
   - Lỗi mạng không làm treo app; ưu tiên trạng thái phiên cục bộ an toàn hoặc Login với thông báo phù hợp.
3. Ngôn ngữ mặc định là Tiếng Việt.
4. Profile cho phép chọn Tiếng Việt hoặc English. Lựa chọn được lưu cục bộ và áp dụng sau khi mở lại app.
5. Mọi Activity bị khóa portrait. Việc đổi locale/configuration không được tạo vòng lặp điều hướng.

## 4. Nguyên tắc UI/UX

- Thương hiệu: **FoodGo**, biểu tượng **FG**.
- Màu chủ đạo: cam san hô và đỏ gạch, có màu nền/trạng thái tương phản đạt chuẩn đọc.
- Splash, Login, Home và Profile phải dùng chung color, typography, spacing, shape và component states.
- Không hard-code chuỗi hiển thị trong Java/XML thuộc phạm vi triển khai.
- Vùng chạm tương tác tối thiểu 48dp; hỗ trợ font scaling và content description phù hợp.
- Tablet 7–10 inch giữ bố cục một cột, căn giữa bằng container có `maxWidth`; tăng khoảng trắng nhưng không phóng giãn nội dung vô hạn.
- Không thêm onboarding hoặc thay đổi nghiệp vụ đặt món, database, schema hay Supabase ngoài việc đọc trạng thái phiên đã có.

## 5. Cách dùng các prompt

Mỗi thành viên dán prompt tương ứng vào một phiên AI riêng. “Sử dụng tất cả skill” được hiểu là sử dụng **tất cả skill phù hợp với nhiệm vụ**, không kích hoạt hàng loạt skill không liên quan.

AI phải đọc `AGENTS.md`, `CONTEXT.md`, tài liệu thiết kế này và code hiện tại trước khi hành động. AI không được push, merge hoặc thay đổi nhánh ngoài phạm vi nếu chưa được người dùng cho phép.

## 6. Prompt cho thành viên 1 — Foundation

```text
Bạn đang làm việc trong repository food-delivery, một Android Customer app dùng Java/XML Android Views.

Mục tiêu: triển khai nền tảng Splash, session routing, song ngữ Việt–Anh trong Profile và khóa portrait cho FoodGo.

Quy trình bắt buộc:
1. Đọc AGENTS.md, CONTEXT.md và docs/superpowers/specs/2026-06-30-account-frontend-flow-design.md. Kiểm tra git status, cấu trúc app, AuthActivity, MainActivity, Profile, navigation, session storage và resources hiện có. Giữ nguyên mọi thay đổi không thuộc nhiệm vụ.
2. Dùng using-superpowers để định tuyến skill. Trước khi sửa code, dùng brainstorming để xác nhận thiết kế và writing-plans để lập kế hoạch. Khi triển khai dùng test-driven-development; trước khi tuyên bố xong dùng verification-before-completion và requesting-code-review. Dùng systematic-debugging nếu gặp lỗi. Không kích hoạt skill không liên quan.
3. Xác nhận đang ở nhánh tích hợp feature/foodgo-frontend hoặc commit nền đã được trưởng nhóm chỉ định. Tạo nhánh feature/foodgo-foundation. Không tạo nhánh từ main. Không push/merge nếu chưa được phép.

Phạm vi sở hữu:
- Tạo Splash bằng Android SplashScreen API phù hợp minSdk 24.
- Launcher phải đi qua Splash/session routing: phiên hợp lệ vào MainActivity; không có/hết hạn phiên vào AuthActivity/Login.
- Không dùng delay giả tạo dài. Lỗi mạng không được treo app hoặc tạo màn hình trắng.
- Mặc định Tiếng Việt. Hỗ trợ Tiếng Việt và English bằng Android string resources/AppCompat locale API phù hợp.
- Thêm lựa chọn ngôn ngữ rõ ràng trong Profile, lưu lựa chọn cục bộ và áp dụng sau khi mở lại app.
- Loại bỏ hard-coded user-facing strings trong các file bạn chạm tới thuộc Splash/Profile/language flow.
- Khóa portrait cho toàn bộ Activity; bảo đảm đổi locale không tạo navigation loop hoặc duplicate Activity.
- Viết unit/instrumented regression tests khả thi cho quyết định routing và lưu/khôi phục locale.

Ranh giới:
- Không redesign Login/Home/Profile ngoài phần selector cần thiết.
- Không thay đổi database/schema hoặc nghiệp vụ đặt món.
- Hạn chế sửa theme, colors và dimens vì thuộc thành viên UI; nếu bắt buộc, ghi rõ trong bàn giao.
- Không tạo icon cuối cùng; dùng tài nguyên tạm có tên rõ ràng nếu Splash cần placeholder.

Tiêu chí hoàn thành:
- Các trạng thái phiên hợp lệ, không có phiên và hết hạn điều hướng đúng.
- App khởi động mặc định bằng Tiếng Việt; đổi sang English và mở lại vẫn giữ English; đổi lại Tiếng Việt hoạt động.
- App không xoay ngang trên các Activity đã khai báo.
- Test liên quan pass và .\gradlew.bat :app:assembleDebug thành công.

Bàn giao gồm: tóm tắt thiết kế, commit SHA, danh sách file thay đổi, kết quả test/assemble, các file dùng chung đã chạm và rủi ro còn lại. Dừng để trưởng nhóm review; không tự merge.
```

## 7. Prompt cho thành viên 2 — UI/UX

```text
Bạn đang làm việc trong repository food-delivery, Android Java/XML Views. Nền tảng Splash/locale/navigation do thành viên 1 sở hữu.

Mục tiêu: tạo design system FoodGo đồng nhất và áp dụng cho Splash, Login, Home, Profile trên phone/tablet portrait.

Quy trình bắt buộc:
1. Đọc AGENTS.md, CONTEXT.md và docs/superpowers/specs/2026-06-30-account-frontend-flow-design.md. Khảo sát theme, colors, dimens, styles và các layout hiện có; xem git status và giữ nguyên thay đổi ngoài phạm vi.
2. Dùng using-superpowers, brainstorming và frontend-design để đề xuất 2–3 hướng giao diện, ưu tiên hướng cam san hô + đỏ gạch đã duyệt. Chỉ sau khi thiết kế được duyệt mới dùng writing-plans. Triển khai bằng test-driven-development khi có logic/regression test phù hợp. Trước khi hoàn thành dùng verification-before-completion và requesting-code-review.
3. Tạo feature/foodgo-ui từ commit nền do trưởng nhóm chỉ định. Nếu Foundation đã được tích hợp, cập nhật nhánh theo hướng dẫn trưởng nhóm trước khi sửa layout. Không push/merge nếu chưa được phép.

Phạm vi sở hữu:
- Tạo token semantic cho color, typography, spacing, shape, elevation và component states; tránh lặp literal trong layout.
- Dùng nhận diện FoodGo: cam san hô + đỏ gạch, nền trung tính ấm và tương phản dễ đọc.
- Đồng bộ Splash, Login, Home và Profile; giữ nguyên chức năng và navigation hiện có.
- Thiết kế tablet 7–10 inch theo một cột căn giữa, có maxWidth hợp lý; không kéo form/card hết chiều ngang.
- Bảo đảm vùng chạm tối thiểu 48dp, font scaling, trạng thái focus/pressed/disabled, keyboard và lỗi biểu mẫu dễ hiểu.
- Kiểm tra Tiếng Việt và English: text không bị cắt/tràn; layout không phụ thuộc độ dài cố định.
- Nếu dark theme vẫn được app công bố hỗ trợ, cung cấp token tương ứng; nếu chưa đủ chất lượng, ghi rõ quyết định và phạm vi thay vì để màu hỏng.

Ranh giới:
- Không thay đổi session routing, locale persistence hoặc navigation contract của thành viên 1.
- Không tạo app icon cuối cùng; để thành viên 3 sở hữu.
- Không đổi database, API hoặc nghiệp vụ đặt món.
- Nếu cần sửa file thuộc Foundation, mô tả patch đề nghị thay vì tự ý thay đổi lớn.

Tiêu chí hoàn thành:
- Splash/Login/Home/Profile có một ngôn ngữ thiết kế thống nhất.
- Phone nhỏ, phone chuẩn và tablet portrait không overflow hoặc tạo khoảng trống vô lý.
- Tiếng Việt và English đều hiển thị đúng.
- Accessibility cơ bản đạt: contrast hợp lý, touch target 48dp, content description cho control không có text.
- Test liên quan pass và .\gradlew.bat :app:assembleDebug thành công.

Bàn giao gồm: design tokens, ảnh chụp/mô tả các kích thước đã kiểm tra, commit SHA, file thay đổi, kết quả test/assemble, xung đột tiềm năng và rủi ro. Dừng để trưởng nhóm review; không tự merge.
```

## 8. Prompt cho thành viên 3 — Branding & QA

```text
Bạn đang làm việc trong repository food-delivery. Foundation và UI/UX phải được tích hợp trước khi bạn hoàn thiện QA; không đánh giá trên code nền cũ.

Mục tiêu: hoàn thiện thương hiệu FoodGo, icon adaptive monogram FG, kiểm thử tổng thể và review chất lượng.

Quy trình bắt buộc:
1. Đọc AGENTS.md, CONTEXT.md và docs/superpowers/specs/2026-06-30-account-frontend-flow-design.md. Kiểm tra git status, manifest, launcher resources, splash resources, test hiện có và code đã tích hợp.
2. Dùng using-superpowers và brainstorming cho hướng icon. Dùng imagegen chỉ khi cần tạo raster artwork; ưu tiên Android Vector Drawable/adaptive icon cho tài sản code-native. Dùng test-driven-development cho regression tests, systematic-debugging cho lỗi, code-review-expert/requesting-code-review để audit, và verification-before-completion trước khi báo xong.
3. Tạo feature/foodgo-branding-qa từ feature/foodgo-frontend sau khi Foundation và UI đã merge. Không tạo từ dev cũ. Không push/merge nếu chưa được phép.

Phạm vi sở hữu:
- Đổi app label từ nhãn tạm thành FoodGo bằng string resource.
- Thiết kế icon adaptive cao cấp dạng monogram FG, dùng màu cam san hô/đỏ gạch, nhận diện rõ ở kích thước nhỏ và hoạt động với mask tròn/vuông.
- Đồng bộ foreground/background/round icon và Splash branding; không nhúng chữ nhỏ khó đọc.
- Audit accessibility, locale, portrait, session routing, phone/tablet và visual consistency.
- Bổ sung regression tests còn thiếu cho các acceptance criteria; không viết test chỉ để khớp implementation sai.
- Review thay đổi của hai thành viên trước theo mức nghiêm trọng, sửa lỗi trong phạm vi đã duyệt và ghi rõ mọi thay đổi chéo.

Ma trận kiểm thử bắt buộc:
- Phiên hợp lệ, không có phiên, phiên hết hạn và lỗi mạng khi khởi động.
- Tiếng Việt mặc định; đổi English; kill/relaunch; đổi lại Tiếng Việt.
- Phone nhỏ, phone chuẩn, tablet 7–10 inch ở portrait; xác nhận landscape bị chặn.
- Light theme và dark theme nếu app còn hỗ trợ.
- Text scaling, touch target, content description và không có text overflow.
- Unit tests, instrumented tests khả thi và .\gradlew.bat :app:assembleDebug.

Ranh giới:
- Không redesign lại UI đã duyệt nếu lỗi có thể sửa cục bộ.
- Không thay đổi database, Supabase schema hoặc nghiệp vụ đặt món.
- Không che giấu test fail, không xóa test để build xanh.

Bàn giao gồm: icon source/variants, báo cáo review theo severity, commit SHA, test matrix với pass/fail, lệnh và output xác minh, vấn đề còn lại. Dừng để trưởng nhóm review; không tự merge.
```

## 9. Prompt tích hợp cho trưởng nhóm

```text
Bạn là người tích hợp ba nhánh FoodGo vào feature/foodgo-frontend rồi chuẩn bị merge về dev.

Đọc AGENTS.md, CONTEXT.md và docs/superpowers/specs/2026-06-30-account-frontend-flow-design.md. Dùng using-superpowers để chọn skill; dùng receiving-code-review khi xử lý phản hồi, resolving-merge-conflicts nếu có conflict, systematic-debugging khi test fail, verification-before-completion trước mọi tuyên bố hoàn tất và finishing-a-development-branch khi mọi kiểm tra đã pass.

Không được dùng git reset --hard, git checkout -- để xóa thay đổi, hoặc force push. Không tự ý đụng thay đổi không liên quan trong worktree.

Quy trình:
1. Xác nhận feature/foodgo-frontend bắt đầu đúng từ dev và worktree sạch ngoại trừ thay đổi đã biết.
2. Review bàn giao và commit của feature/foodgo-foundation. Chạy test liên quan và assembleDebug rồi mới tích hợp.
3. Review và tích hợp feature/foodgo-ui. Ưu tiên contract navigation/locale của Foundation; giải quyết style/resource conflict bằng semantic token đã duyệt.
4. Tạo/cập nhật feature/foodgo-branding-qa từ trạng thái tích hợp mới nhất, sau đó review và tích hợp commit branding/QA.
5. Chạy toàn bộ ma trận nghiệm thu: session routing, Việt/Anh persistence, portrait, phone/tablet, accessibility, icon masks, unit/instrumented tests khả thi và :app:assembleDebug.
6. Thực hiện code review cuối: lỗi chức năng, regression, hard-coded strings, resource duplication, accessibility, lifecycle/navigation loop và thay đổi ngoài phạm vi.
7. Chỉ khi mọi kiểm tra cần thiết pass, trình bày lựa chọn merge/PR về dev. Không tự push hoặc merge vào dev nếu chưa được người dùng cho phép.

Definition of Done:
- Splash điều hướng đúng và không delay giả tạo dài.
- Tiếng Việt mặc định; lựa chọn Việt/Anh trong Profile được lưu qua relaunch.
- Splash/Login/Home/Profile đồng nhất theo design system FoodGo.
- Adaptive icon FG đúng trên mask tròn/vuông; app label là FoodGo.
- App khóa portrait; phone/tablet không overflow và tablet dùng cột căn giữa có maxWidth.
- Không có regression nghiệp vụ đặt món, database hoặc Supabase.
- Test và assembleDebug pass, kèm bằng chứng đầu ra mới nhất.

Báo cáo cuối gồm commit đã tích hợp theo thứ tự, conflict và cách giải quyết, test matrix, rủi ro còn lại, cùng đề xuất PR/merge. Không tuyên bố hoàn tất nếu chưa có bằng chứng xác minh.
```

## 10. Tiêu chí nghiệm thu chung

- Splash dùng API phù hợp và không hiển thị màn hình trắng.
- Session routing đúng cho phiên hợp lệ, không có phiên, hết hạn và lỗi mạng.
- Tiếng Việt là mặc định; Việt/Anh đổi được trong Profile và lưu qua relaunch.
- User-facing strings trong phạm vi không bị hard-code.
- FoodGo có app label và adaptive icon FG nhất quán với Splash.
- Splash/Login/Home/Profile dùng chung design tokens.
- Portrait được khóa; không có navigation loop khi đổi locale.
- Phone nhỏ, phone chuẩn và tablet 7–10 inch không overflow; tablet căn giữa với maxWidth.
- Touch target tối thiểu 48dp, text scaling và content descriptions phù hợp.
- Unit/instrumented tests liên quan và `:app:assembleDebug` pass.
- Không thay đổi ngoài phạm vi nghiệp vụ frontend đã duyệt.

## 11. Rủi ro và biện pháp

- **Xung đột resource:** phân quyền file và tích hợp tuần tự Foundation → UI → Branding.
- **Locale làm tái tạo Activity:** kiểm thử relaunch/configuration, giữ routing idempotent.
- **Session API không rõ:** tái sử dụng storage/repository hiện có, không dựng một nguồn chân lý thứ hai.
- **Tablet bị kéo giãn:** dùng resource theo width qualifier hoặc container maxWidth, kiểm thử nhiều kích thước.
- **Icon đẹp ở preview nhưng mờ khi chạy:** kiểm thử adaptive masks và launcher density thực tế.
- **Ba người sửa cùng Manifest:** Foundation sở hữu orientation/launcher routing; Branding chỉ đề xuất/apply label và icon sau khi Foundation đã tích hợp.
