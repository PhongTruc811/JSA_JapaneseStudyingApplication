# Kế hoạch code website thanh toán và cấp license

## Mục tiêu

Xây dựng một website nhỏ để bán app Japanese Alphabet Quiz cho từng cá nhân. Website này không nằm trong app Java Swing, mà đóng vai trò như cửa hàng bán sản phẩm:

- Giới thiệu app.
- Cho user bấm mua app.
- Xử lý thanh toán qua cổng thanh toán.
- Tạo license sau khi thanh toán thành công.
- Gửi license key cho user qua email.
- Cho app desktop gọi backend để kiểm tra license.

## Kiến trúc đề xuất

Nên tách thành các project riêng:

```text
JapaneseAlphabetQuiz/             Java Swing desktop app hiện tại
JapaneseAlphabetQuizWebsite/      Website giới thiệu và bán app
JapaneseAlphabetQuizBackend/      Backend license/payment sau này
```

Không nên đặt website vào `src` của project Java Swing, vì `src` hiện đang phục vụ build Maven cho app desktop. Tách project giúp deploy web, quản lý dependency frontend và phát triển backend dễ hơn.

## Giai đoạn 1: Bán thủ công, ít code nhất

Mục tiêu: có thể bán app sớm, chưa cần tự động hóa hoàn toàn.

### Việc cần làm

1. Tạo website landing page đơn giản.
2. Tạo nút `Buy now`.
3. Nút `Buy now` dẫn đến checkout của Gumroad, Lemon Squeezy, Paddle hoặc Stripe Checkout.
4. Sau khi user mua, mình tạo license key thủ công.
5. Gửi license key cho user qua email/Zalo.
6. App desktop chỉ cần màn hình nhập license key hoặc email + license key.

### Ưu điểm

- Nhanh làm.
- Ít rủi ro.
- Không cần webhook ngay.
- Phù hợp khi mới test thị trường.

### Nhược điểm

- Tạo license thủ công.
- Gửi license thủ công.
- Khó quản lý nếu có nhiều người mua.

## Giai đoạn 2: Website có checkout chuyên nghiệp

Mục tiêu: website trông đẹp hơn, thanh toán vẫn dùng checkout của bên thứ ba.

### Trang cần có

```text
/
/pricing
/download
/license-help
/payment-success
```

### Nội dung từng trang

`/`

- Giới thiệu Japanese Alphabet Quiz.
- Ảnh/screenshot app.
- Lợi ích chính.
- Nút `Buy now`.
- Nút `Download trial` nếu có.

`/pricing`

- Gói Lifetime.
- Gói Yearly nếu sau này cần.
- Số thiết bị được kích hoạt.
- Chính sách update.

`/download`

- Link tải file cài đặt.
- Hướng dẫn cài đặt.
- Hướng dẫn nhập license key trong app.

`/license-help`

- User nhập license ở đâu.
- Làm gì khi mất license.
- Làm gì khi đổi máy.
- Liên hệ hỗ trợ.

`/payment-success`

- Cảm ơn đã mua.
- Thông báo license key sẽ được gửi qua email.
- Hướng dẫn mở app và activate.

## Giai đoạn 3: Tự động tạo license bằng webhook

Mục tiêu: user thanh toán xong thì hệ thống tự tạo license và gửi email.

### Luồng xử lý

```text
User vào website
-> Bấm Buy now
-> Cổng thanh toán xử lý payment
-> Payment provider gọi webhook về backend
-> Backend xác minh webhook
-> Backend tạo license
-> Backend gửi email license key
-> User mở app
-> User nhập license key
-> App gọi backend verify license
```

### Backend cần có

```text
POST /api/payment/webhook
POST /api/licenses/activate
POST /api/licenses/verify
POST /api/licenses/deactivate-device
```

### Bảng database đề xuất

```text
licenses
- id
- email
- license_key_hash
- plan
- status
- max_devices
- expires_at
- created_at
- updated_at

license_activations
- id
- license_id
- device_id
- device_name
- activated_at
- last_seen_at
```

## Cổng thanh toán nên cân nhắc

### Lemon Squeezy

Phù hợp cho indie app/digital product.

- Có checkout sẵn.
- Hỗ trợ digital product.
- Có webhook.
- Dễ bắt đầu hơn Stripe nếu muốn bán sản phẩm số.

### Paddle

Phù hợp nếu muốn xử lý thuế/quốc tế nghiêm túc hơn.

- Có checkout sẵn.
- Có webhook.
- Lo nhiều phần merchant/tax.

### Stripe

Mạnh và linh hoạt nhất, nhưng phải tự xử lý nhiều hơn.

- Cần backend webhook tốt.
- Cần quản lý invoice/tax/customer kỹ hơn.
- Phù hợp khi sản phẩm lớn hơn.

### Gumroad

Nhanh để bán ban đầu.

- Ít code.
- Dễ tạo product page.
- Phù hợp MVP.

## Đề xuất cho project này

Nên đi theo thứ tự:

```text
Giai đoạn 1: Gumroad hoặc Lemon Squeezy checkout link
Giai đoạn 2: Website landing page riêng
Giai đoạn 3: Spring Boot backend tạo license từ webhook
Giai đoạn 4: App desktop verify license online
```

## UI ActivateFrame đề xuất

Nếu muốn đơn giản:

```text
License key
[Activate]
```

Nếu muốn gắn license với người mua rõ hơn:

```text
Email
License key
[Activate]
```

Không nên dùng password ở giai đoạn đầu, vì password kéo theo nhiều việc bảo mật và account management.

## Security cần lưu ý

- Không lưu license key plain text trong database. Nên lưu hash.
- App desktop không nên quyết định license hợp lệ một mình.
- Backend mới là nơi validate license.
- Nên giới hạn số thiết bị kích hoạt bằng `max_devices`.
- Nên có `device_id` để nhận diện máy.
- Nên có trang/admin tool để revoke license khi cần.

## Việc cần làm tiếp theo

1. Chọn cổng thanh toán: Gumroad, Lemon Squeezy, Paddle hoặc Stripe.
2. Quyết định UI app dùng `license only` hay `email + license`.
3. Tạo project website riêng.
4. Tạo landing page và nút `Buy now`.
5. Sau đó mới làm backend license/payment webhook.
