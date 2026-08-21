

## Đang làm

- [ ] 1. Tasks nhỏ: 
	- [x] 1. AccessKeyFrame: cải thiện UX, chỗ input nhập Key, khi nhập key được hiển thị dưới dạng mật khẩu, có nút bấm hình con mắt để hiện key
	- [ ] 2. MenuFrame: cải thiện và nâng cấp UI 
		- [ ] gợi ý về khoảng trống hiện ở trang Menu
		- [ ] gợi ý nâng cấp UX, mượt mà hiện đại theo phong cách Nhật Bản
- [x] 2. Module Kana:
	- [x] KanaMenuFrame: build lại UI/UX, đồng điệu với giao diện hiện tại của app
	- [x] KanaQuizFrame: nâng cấp UI/UX, thêm nút kết thúc quiz 
		- [x] Nâng cấp và sửa lại UI/UX của frame
		- [x] Thêm nút end để kết thúc quiz (bấm nút -> hiện ô điểm -> Cho user option: 1. làm lại quiz; 2. Quay về Menu)
		- [x] Cho user chọn 3 chế độ làm KanaQuiz: easy; medium; hard và cho thời gian làm quiz đếm ngược ở kế bên scoreLabel
			- [x] easy: enable nút correctButton, time = 3 min 
			- [x] medium: enable nút correctButton, time = 1min30s
			- [x] hard: không nhấn được nút correctButton, time = 1min 
		- [x] Sửa lỗi hiển thị ký tự kana khi nhấn nút correctButton ![[Pasted image 20260722103605.png]]
		- [x] Gợi ý bổ sung tính năng cần thiết hoặc gợi ý UI update cho KanaQuizFrame
			- [x] Thay E/M/H bằng Meter khi user chọn độ khó quiz
				- [x] - Easy: ▮▯▯
				- [x] - Medium: ▮▮▯
				- [x] - Hard: ▮▮▮

- [ ] 3. Module Vocabulary:
	- [ ] 1. VocabQuizFrame (frame này dùng chung cho từ vựng và kanji)
		- [ ] Hiện Vocab.meaning ở trên đầu, rồi cho user chọn 1 đáp án đúng trong A;B;C;D
	- [ ] 2. VocabMenuFrame
		- [x] 1. Đồng bộ lại UI ở frame này cho giống với ở AppMenuFrame 
	- [ ] 
- [ ] 4. [Hướng dẫn đóng gói Windows]
	- [ ] Tài liệu: [[Packaging-Release-Guide]] — quy trình tạo ZIP portable và MSI kèm Java runtime.
	- [ ] 
- [ ] 

## needFixed_Suggest
	- [ ] 

## Tiếp theo

### [P2 — Mở rộng trải nghiệm Kana Quiz]

- [x] Thống kê số câu đúng, sai và số lần xem đáp án trong result dialog.
- [x] Bổ sung streak để khuyến khích chuỗi trả lời đúng liên tiếp.
- [x] Cho phép chọn nhóm kana cần luyện: gojuuon, dakuon hoặc youon.

### [P0 — Chuẩn hoá cách chạy ứng dụng]

- [ ] Mục tiêu: Tạo hướng dẫn chạy lặp lại được từ IDE và terminal, nêu JDK 24 và chế độ database/JSON.
- [ ] Tiêu chí hoàn thành: Tài liệu nêu entry point `com.mescode.japanese.app.launch.Main`, lệnh terminal đã kiểm chứng và điều kiện cấu hình database; không có secret trong Git.
- [ ] File/khu vực có thể bị ảnh hưởng: `pom.xml`, tài liệu dự án, `src/main/resources/application.properties`.
- [ ] Test cần chạy: `mvn test`; smoke test luồng Splash → Access key/Menu.

### [P1 — Mở rộng regression test]

- [ ] Mục tiêu: Phủ các luồng điều hướng, quiz vocabulary/grammar, trạng thái đã học/yêu thích và dữ liệu người dùng.
- [ ] Tiêu chí hoàn thành: Test độc lập với dữ liệu local cho controller/service/repository còn thiếu, gồm dữ liệu grammar và file dữ liệu lỗi/thiếu.
- [ ] File/khu vực có thể bị ảnh hưởng: `src/test/`, `controller/`, `service/`, `repo/`, `app/navigation/`.
- [ ] Test cần chạy: `mvn test`.

### [P1 — Rà soát khởi tạo database và login/OAuth]

- [ ] Mục tiêu: Xác nhận trải nghiệm khi SQL Server khả dụng, không khả dụng hoặc tắt; kiểm tra login/OAuth mà không lộ cấu hình nhạy cảm.
- [ ] Tiêu chí hoàn thành: Fallback JSON và thông báo lỗi nhất quán; cấu hình/secret local được hướng dẫn và không theo dõi bởi Git.
- [ ] File/khu vực có thể bị ảnh hưởng: `database/`, `auth/oauth/`, `controller/LoginController.java`, `src/main/resources/`.
- [ ] Test cần chạy: `mvn test`; smoke test với database tắt và môi trường database hợp lệ.

### [P2 — Bổ sung kiểm tra chất lượng dữ liệu học]

- [ ] Mục tiêu: Kiểm tra cấu trúc/tính đầy đủ của grammar, kana, kanji và vocabulary.
- [ ] Tiêu chí hoàn thành: Test báo rõ dữ liệu JSON sai/thiếu và không phụ thuộc `user_data/`.
- [ ] File/khu vực có thể bị ảnh hưởng: `src/main/resources/data/`, `src/test/`, `repo/`.
- [ ] Test cần chạy: `mvn test`.

## Hoàn thành

### [Khảo sát hiện trạng dự án — 2026-07-18]

- [x] Mục tiêu: Xác định cách chạy/test, module chính, rủi ro và thứ tự ưu tiên.
- [x] Cách build/test: Maven biên dịch với Java 24 (`maven.compiler.source/target`); chạy `mvn test` tại thư mục gốc.
- [x] Kết quả kiểm chứng: `mvn test` thành công — 10 tests, 0 failures, 0 errors, 0 skipped.
- [x] Phạm vi test: `KanaRepositoryTest` (2), `KanjiRepositoryTest` (1), `VocabRepositoryTest` (3) và `KanaServiceTest` (4). Chúng kiểm tra đọc JSON và chọn kana ngẫu nhiên; chưa phủ UI, controller, navigation, grammar, login/OAuth hay database integration.
- [x] Cách mở ứng dụng: chạy `com.mescode.japanese.app.launch.Main` từ IDE. Luồng là `Main` → `AppLauncher` → `DatabaseInitializer` → `AppContext`/`MenuNavigator` → Splash. `pom.xml` chưa cấu hình Maven Exec Plugin hoặc JAR có `Main-Class`, nên chưa có lệnh terminal tự mô tả.
- [x] Runtime: khởi động luôn gọi `DatabaseInitializer`. Khi `db.enabled=false`, ứng dụng dùng JSON; khi database bật, ứng dụng chạy `sql/create_tables.sql` và có fallback cho một số lỗi kết nối/đăng nhập. Kiểm tra cấu hình local trước smoke test, không ghi secret vào tài liệu.
- [x] Module chính:
  - `app/`: entry point, vòng đời, điều hướng, `AppContext` (dữ liệu kana/vocabulary, preferences, tiến độ local).
  - `view/`: Swing UI cho splash, access/activate/login, menu, kana, vocabulary, grammar, settings và theme.
  - `controller/`: kết nối UI với service và login.
  - `service/` + `repo/`: chọn câu hỏi/dữ liệu, đọc JSON, lưu trạng thái học cục bộ.
  - `model/`: Kana, Kanji, Vocabulary, GrammarQuestion, User, Score.
  - `database/` + `auth/oauth/`: SQL Server và cấu hình OAuth.
- [x] Rủi ro ưu tiên: thiếu lệnh chạy terminal; độ phủ test hẹp; database khởi tạo trước UI; OAuth/database cần smoke test với cấu hình local an toàn.
- [x] File/khu vực đã khảo sát: `pom.xml`, `src/main/java/`, `src/main/resources/`, `src/test/`.
- [x] Test đã chạy: `mvn test`.

- [x] Khởi tạo Git local và commit đầu tiên.
- [x] Thiết lập `.gitignore` cho dữ liệu local, cache Codex và cài đặt Obsidian.
## Mẫu task

```md
### [Tên task]

- [ ] Mục tiêu:
- [ ] Tiêu chí hoàn thành:
- [ ] File/khu vực có thể bị ảnh hưởng:
- [ ] Test cần chạy:
```
