# Hướng dẫn đóng gói JapaneseAlphabetQuiz cho Windows

## Mục tiêu

Tạo bản phát hành chạy được trên máy Windows khác mà không cần cài Java, Maven hoặc SQL Server. Bản nộp dùng dữ liệu JSON, vì vậy phải giữ `db.enabled=false` trong `src/main/resources/application.properties`.

Sản phẩm đầu ra gồm hai dạng:

- **ZIP portable**: giải nén rồi chạy ngay; đây là bản nên gửi giảng viên.
- **MSI installer**: cài đặt như phần mềm Windows; cần WiX Toolset trên máy đóng gói.

> Không gửi riêng file `.exe`. File này cần thư mục `app` (JAR và thư viện) cùng thư mục `runtime` (Java runtime) nằm cạnh nó. Hãy gửi nguyên file ZIP.

## Điều kiện môi trường

- Windows 10/11 64-bit.
- JDK 24, gồm lệnh `java` và `jpackage`.
- Apache Maven, gồm lệnh `mvn`.
- WiX Toolset (chỉ cần nếu tạo file `.msi`).

Kiểm tra nhanh trong PowerShell:

```powershell
java -version
mvn -version
jpackage --version
```

## Quy trình đóng gói

Mở PowerShell tại thư mục gốc dự án (`JapaneseAlphabetQuiz`) rồi chạy lần lượt các lệnh sau.

### 1. Kiểm tra cấu hình và chạy test

```powershell
Get-Content src\main\resources\application.properties | Select-String '^db.enabled=false$'
mvn clean test
```

Lệnh đầu tiên phải trả về `db.enabled=false`. Chỉ tiếp tục nếu Maven báo `BUILD SUCCESS`.

### 2. Build JAR và gom thư viện runtime

```powershell
mvn package
mvn dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target\package\lib
New-Item -ItemType Directory -Force -Path target\package | Out-Null
Copy-Item -LiteralPath target\JapaneseAlphabetQuiz-1.0-SNAPSHOT.jar -Destination target\package\JapaneseAlphabetQuiz.jar -Force
```

Thay tên JAR trong lệnh `Copy-Item` nếu version trong `pom.xml` thay đổi.

### 3. Tạo Windows app-image kèm Java runtime

```powershell
New-Item -ItemType Directory -Force -Path output\release | Out-Null
jpackage --type app-image --name JapaneseAlphabetQuiz --input target\package --main-jar JapaneseAlphabetQuiz.jar --main-class com.mescode.japanese.app.launch.Main --dest output\release --vendor "MES Code" --description "Japanese Alphabet Quiz"
```

Kết quả là thư mục `output\release\JapaneseAlphabetQuiz\`, trong đó file chạy là `JapaneseAlphabetQuiz.exe`.

> Nếu lệnh báo thư mục đích đã tồn tại, hãy đổi tên hoặc xóa **chỉ** thư mục release cũ sau khi đã sao lưu bản cần giữ, rồi chạy lại bước này.

### 4. Tạo ZIP portable để gửi giảng viên

```powershell
Compress-Archive -LiteralPath output\release\JapaneseAlphabetQuiz -DestinationPath output\JapaneseAlphabetQuiz-Windows.zip -Force
```

Gửi file `output\JapaneseAlphabetQuiz-Windows.zip`. Người nhận giải nén toàn bộ rồi mở `JapaneseAlphabetQuiz\JapaneseAlphabetQuiz.exe`.

### 5. Tạo MSI (tùy chọn)

Sau khi cài WiX Toolset và mở PowerShell mới, chạy:

```powershell
jpackage --type msi --name JapaneseAlphabetQuiz --input target\package --main-jar JapaneseAlphabetQuiz.jar --main-class com.mescode.japanese.app.launch.Main --dest output\release --vendor "MES Code" --description "Japanese Alphabet Quiz"
```

File `.msi` sẽ nằm trong `output\release\`. Nếu `jpackage` báo không tìm thấy WiX tools, dùng bản ZIP hoặc cài WiX Toolset rồi chạy lại bước này.

## Checklist trước khi nộp

- [ ] `db.enabled=false`.
- [ ] `mvn clean test` thành công.
- [ ] Đã mở thử `output\release\JapaneseAlphabetQuiz\JapaneseAlphabetQuiz.exe`.
- [ ] App vào được màn hình đầu mà không yêu cầu SQL Server.
- [ ] Đã gửi file ZIP, không gửi riêng `.exe`.
- [ ] Không đính kèm file chứa password, OAuth secret hoặc dữ liệu cá nhân.

## Thông tin kỹ thuật hiện tại

- Entry point: `com.mescode.japanese.app.launch.Main`.
- Dữ liệu quiz được đóng gói từ `src/main/resources/data/` vào JAR.
- Bản Windows đã có Java runtime riêng, nên máy người nhận không cần cài Java.