# Overall available Test Suite

Bộ kiểm thử hiện gồm **10 lớp kiểm thử (test classes) trong 10 file Java, tổng cộng
39 ca kiểm thử (test cases)**, dùng JUnit Jupiter. Mỗi phương thức có `@Test` là
một ca kiểm thử; `@DisplayName` mô tả mục đích của ca đó bằng tiếng Việt.

Mục tiêu là phát hiện lỗi trong dữ liệu học, quy tắc quiz và lưu tiến độ khi sửa
ứng dụng, đồng thời tạo nền tảng để chạy tự động trên GitHub Actions.

## Bộ test hoạt động như thế nào?

1. **Maven chuẩn bị ứng dụng:** biên dịch mã chính, chép tài nguyên vào
   `target/classes`, rồi biên dịch mã test vào `target/test-classes`.
2. **Surefire khởi chạy JUnit:** plugin đọc cấu hình trong [pom.xml](../../pom.xml),
   tìm các lớp có tên kết thúc bằng `Test` trong bộ hiện tại và giao JUnit chạy
   các phương thức `@Test`.
3. **Mỗi ca chuẩn bị dữ liệu và thực hiện thao tác:** chẳng hạn tạo một đề quiz,
   chọn đáp án, nộp bài hoặc lưu rồi đọc lại tiến độ.
4. **Đối chiếu kết quả:** các lệnh như `assertEquals`, `assertTrue` và `assertThrows`
   kiểm tra kết quả thực tế với điều mong đợi. Ví dụ, `GrammarFlowTest` tạo đề
   10 câu để xác nhận đúng 6 câu thì chưa đạt, đúng 7 câu thì đạt, và lần làm kém
   hơn không ghi đè kết quả tốt nhất.
5. **Dọn tài nguyên và ghi báo cáo:** JUnit dọn thư mục `@TempDir`; dịch vụ có tài
   nguyên được đóng sau khi dùng. Surefire ghi kết quả vào `target/surefire-reports`.
   Ca kiểm tra thất bại hoặc lỗi thực thi làm lệnh Maven trả về trạng thái thất bại.

Bộ này kết hợp kiểm tra logic với dữ liệu mẫu nhỏ và kiểm tra tích hợp cục bộ:
đọc JSON/ảnh thật từ classpath, ghi và đọc lại file trong thư mục tạm. Các test
controller dùng `FakeView` để lưu kết quả hiển thị và gọi hành động chọn/nộp đáp án,
nên kiểm tra được luồng xử lý mà không mở cửa sổ Swing.

## Cách chạy

Chạy từ thư mục chứa `pom.xml`, với Maven và JDK 24 trở lên:

```sh
mvn -B -ntp clean test
```

`clean` xóa kết quả biên dịch cũ, hữu ích sau khi đổi hoặc xóa file test.
Khi sửa một chức năng, có thể chạy riêng, ví dụ:

```sh
mvn -B -ntp "-Dtest=VocabRepositoryTest" test
```

## Phạm vi của 10 lớp kiểm thử

| Lớp kiểm thử | Số ca | Phạm vi |
| --- | ---: | --- |
| [LearningContentRepositoryTest](java/com/mescode/japanese/repository/LearningContentRepositoryTest.java) | 5 | Dữ liệu Kana, từ vựng, Kanji, ngữ pháp và ảnh/đáp án đề thi thử từ classpath |
| [VocabRepositoryTest](java/com/mescode/japanese/repository/VocabRepositoryTest.java) | 5 | Lưu từ cá nhân, yêu thích/đã học, phục hồi file hỏng và lọc chương |
| [KanaServiceTest](java/com/mescode/japanese/service/KanaServiceTest.java) | 3 | Chọn kana, tránh lặp và lọc nhóm |
| [KanaQuizTest](java/com/mescode/japanese/controller/KanaQuizTest.java) | 4 | Điểm, chuỗi đúng, xem đáp án, độ khó và đồng hồ |
| [VocabQuizTest](java/com/mescode/japanese/service/VocabQuizTest.java) | 3 | Tạo câu hỏi Kana/Kanji, bốn lựa chọn phân biệt và thiếu dữ liệu |
| [VocabControllerTest](java/com/mescode/japanese/controller/VocabControllerTest.java) | 3 | Giữ lựa chọn, chặn nộp thiếu câu và trả kết quả |
| [GrammarFlowTest](java/com/mescode/japanese/service/GrammarFlowTest.java) | 5 | Mở khóa, chấm bài, ngưỡng 70%, kết quả tốt nhất, lưu và đặt lại tiến độ |
| [PeTrialFlowTest](java/com/mescode/japanese/controller/PeTrialFlowTest.java) | 4 | Chuyển câu, xác nhận nộp, câu bỏ trống và hết giờ chỉ nộp một lần |
| [MizukiChatServiceTest](java/com/mescode/japanese/service/chatbot/MizukiChatServiceTest.java) | 4 | Tra cứu ngoại tuyến, trạng thái mini quiz và lệnh nhạc |
| [MusicServiceTest](java/com/mescode/japanese/service/music/MusicServiceTest.java) | 3 | Kiểm tra playlist và thay đổi trạng thái không phát âm thanh |

## Đọc kết quả

Sau khi chạy, xem dòng tổng kết của Maven: `Tests run` là số ca đã chạy,
`Failures` là số ca không đạt điều kiện kiểm tra, `Errors` là số lỗi thực thi,
và `Skipped` là số ca bị bỏ qua. Với bộ hiện tại chạy đầy đủ và thành công,
kết quả mong đợi là:

```text
Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Nếu có lỗi, mở file `TEST-*.xml` hoặc `*.txt` tương ứng trong
`target/surefire-reports/` để xem tên ca, kết quả mong đợi và vị trí lỗi.
Các ca cố ý tạo JSON hỏng có thể in thông báo đọc file thất bại; hãy dựa vào
kết quả tổng kết để xác định ca kiểm tra có qua hay không.

## Cách giữ bộ test ổn định

- Test ghi dữ liệu dùng `@TempDir`; Surefire đặt thư mục tạm dưới `target`, không ghi vào `user_data`.
- Controller dùng giao diện giả. Surefire bật `java.awt.headless=true`, không cần màn hình hoặc cửa sổ Swing.
- Không gọi mạng, cơ sở dữ liệu, OAuth hoặc thiết bị âm thanh. Maven vẫn cần tải thư viện khi máy chưa có bộ nhớ đệm.
- Đồng hồ được kiểm tra bằng `tick()`, không chờ thời gian thực. Test chọn ngẫu nhiên dùng hạt giống cố định hoặc kiểm tra tính hợp lệ của kết quả, không phụ thuộc thứ tự xáo trộn.
- Mỗi ca tự tạo trạng thái, đóng dịch vụ sau khi dùng và không cần chạy theo thứ tự.
- Kiểm tra dữ liệu ưu tiên nội dung hợp lệ thay vì cố định số lượng; riêng đề thi thử giữ 30 câu và đáp án đã xác nhận. Chỉ sửa đáp án kỳ vọng khi nội dung đề thay đổi có chủ đích.

## Chuẩn bị cho GitHub Actions và Allure

CI có thể dùng cùng lệnh `mvn -B -ntp clean test`. Chọn rõ JDK phù hợp với
`maven.compiler.release` trong `pom.xml` (hiện là 24); nên dùng cùng phiên bản JDK
trên máy phát triển và CI. `failIfNoTests=true` giúp build thất bại nếu không tìm thấy test.

Kết quả JUnit XML và nhật ký nằm tại `target/surefire-reports/` để lưu làm artifact.
Tên ca kiểm tra bằng tiếng Việt được khai báo qua `@DisplayName`, có thể dùng khi
tích hợp Allure sau này. Bộ hiện tại chưa thêm Allure hoặc workflow GitHub Actions.

Bộ lõi này chưa kiểm tra bố cục/hoạt ảnh Swing, phát âm thanh thật, đăng nhập hoặc
chuyển màn hình toàn ứng dụng. Các phần đó cần kiểm tra giao diện riêng.
