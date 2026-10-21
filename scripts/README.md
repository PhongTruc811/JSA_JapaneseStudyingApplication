# Phát hành JSA cho Windows

CD của JSA dùng `jpackage` tạo bộ cài **MSI Windows x64**, kèm Java runtime, icon,
shortcut Desktop và Start Menu. Người dùng tải MSI, cài và mở JSA mà không cần cài Java/Maven.
Bộ cài dùng chế độ theo tài khoản Windows hiện tại (per-user).

## CI và CD

- [CI](../.github/workflows/ci.yml): kiểm tra push/PR, chạy Maven và lưu JAR + JUnit reports.
- [CD](../.github/workflows/release.yml): kiểm tra lại bằng JDK 24 trên Windows,
  tạo MSI và checksum SHA-256. Khi push tag phiên bản, workflow tự tạo GitHub Release.
- PR vào `master` thay đổi workflow CD hoặc script đóng gói cũng chạy tạo MSI để kiểm tra.
- Chạy CD qua **Actions → CD (Windows Release) → Run workflow** chỉ tạo artifacts;
  không phát hành GitHub Release. Merge workflow vào `master` để dùng nút này.
- Test hoặc đóng gói thất bại thì không phát hành. Báo cáo Surefire được lưu kể cả khi test lỗi.
- Allure chưa tích hợp; đây là bước tiếp theo sau khi kiểm chứng CD.

## Kiểm tra đóng gói ở local

Cần Windows x64, PowerShell 7, Maven và JDK có `jpackage`.
Dùng JDK 24 để khớp môi trường GitHub Actions; script ưu tiên `JAVA_HOME/bin/jpackage.exe`.
Tạo MSI cần WiX 3.14 (có `candle.exe`/`light.exe` trên PATH hoặc `WIX` trỏ thư mục cài),
hoặc WiX 4/5 (`wix.exe` trên PATH). Runner `windows-2022` đã có WiX 3.14.

Tại thư mục gốc dự án:

```powershell
mvn -B -ntp clean verify
./scripts/Package-Windows.ps1
```

Phiên bản tự đọc từ [pom.xml](../pom.xml); ví dụ với `1.0.1`:

- `target/windows-release/JSA-1.0.1-windows-x64.msi`
- `target/windows-release/JSA-1.0.1-windows-x64.msi.sha256`

Script chỉ đưa fat JAR `target/JSA-<version>-all.jar` vào input của `jpackage`.
Không cần chép dependencies riêng. Mỗi lần đóng gói có staging riêng dưới
`target/jpackage/<id>/`; `mvn clean` dọn kết quả cũ trước lần build tiếp theo.

Nếu chưa có WiX, vẫn kiểm tra được application image và Java runtime:

```powershell
./scripts/Package-Windows.ps1 -PackageType app-image
```

Script in đường dẫn thư mục JSA chứa `JSA.exe`, `app` và `runtime`.
Đây là thư mục ứng dụng portable, chưa phải bộ cài MSI. Muốn chia sẻ dạng portable phải
gửi cả thư mục; không gửi riêng `JSA.exe`.

## Phát hành một phiên bản

1. Hoàn tất thay đổi và đặt `<version>` trong `pom.xml` thành `X.Y.Z`, ví dụ `1.0.2`.
   Commit thay đổi phiên bản trước khi tạo tag; không dùng `SNAPSHOT` hoặc hậu tố beta.
2. Merge code và cấu hình CD vào `master`; kiểm tra CI thành công.
3. Tại commit đã merge, tạo tag đúng phiên bản trong POM và push tag:

```powershell
git switch master
git pull --ff-only origin master
git tag -a v1.0.2 -m "JSA 1.0.2"
git push origin v1.0.2
```

Ví dụ trên chỉ dùng khi POM đã là `1.0.2`. Với POM hiện tại `1.0.1`, tag tương ứng là
`v1.0.1`. Tag sai phiên bản hoặc trỏ tới commit chưa có trên `master` bị từ chối.
Windows MSI giới hạn phiên bản tối đa `255.255.65535`.

4. Xem **Actions → CD (Windows Release)**. Job đầu chạy test và tạo bộ cài; job sau
   tạo Release và đính kèm MSI/checksum bằng `GITHUB_TOKEN`, không cần personal access token.
5. Tải MSI từ **Releases**, cài trên Windows x64 và kiểm tra mở app, dữ liệu học, làm quiz,
   lưu rồi đọc lại tiến độ. Kiểm tra thêm trên máy không cài Java.
6. Với lần phát hành sau, tăng version và dùng tag mới. Workflow không ghi đè Release
   đã tồn tại. Giữ nguyên `--win-upgrade-uuid` trong script để Windows nhận diện các bản nâng cấp.

CD không tự cài hay tự cập nhật ứng dụng trên máy người dùng; người dùng chủ động tải bộ cài.
Bước đóng gói kiểm tra cấu trúc application image và mã thoát công cụ; vẫn cần thử cài/chạy MSI thực tế.
Bộ cài hiện chưa được ký mã (code signing).

## Tài liệu công cụ

- [Oracle: jpackage JDK 24](https://docs.oracle.com/en/java/javase/24/docs/specs/man/jpackage.html)
- [GitHub: phần mềm có sẵn trên Windows 2022 runner](https://github.com/actions/runner-images/blob/main/images/windows/Windows2022-Readme.md)
- [GitHub CLI: tạo Release](https://cli.github.com/manual/gh_release_create)
