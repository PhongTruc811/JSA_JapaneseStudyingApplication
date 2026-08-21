# Japanese Alphabet Quiz

## Mục đích

- Ứng dụng desktop Java hỗ trợ người dùng học và ôn tập kiến thức về tiếng Nhật cơ bản thông qua các màn hình học, tra cứu và quiz bài tập.
- 

## Kỹ thuật chính

- Java 24, Maven và giao diện Swing.
- Gson cho dữ liệu JSON; Microsoft SQL Server JDBC cho dữ liệu cơ sở dữ liệu.
- ScribeJava và Jackson cho luồng đăng nhập OAuth.
- JUnit Jupiter cho kiểm thử.

## Cấu trúc mã nguồn

- `app/`: khởi động ứng dụng, điều hướng và ngữ cảnh chung.
- `controller/`, `service/`, `repo/`, `model/`: xử lý nghiệp vụ và dữ liệu.
- `view/`: các màn hình Swing cho quiz, từ vựng và cài đặt.
- `resources/data/`: dữ liệu học JSON; `resources/sql/`: script khởi tạo dữ liệu.
- `src/test/`: test cho service và repository.

## Cách phối hợp

Mỗi thay đổi đáng kể cần có task trong `Tasks.md`; sau khi hoàn thành, cập nhật kết quả và ghi quyết định lâu dài vào `Decisions.md`.

> Không lưu mật khẩu, OAuth secret hoặc dữ liệu tiến độ cá nhân trong các ghi chú này.
