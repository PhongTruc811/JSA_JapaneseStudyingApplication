# UI Component Refactor Plan

## Tóm tắt

Qua rà soát toàn bộ UI, project đang có một số pattern được lặp lại giữa nhiều frame. Việc tách các pattern này thành component dùng chung sẽ giúp code gọn hơn, giảm lỗi khi chỉnh giao diện và giữ UI nhất quán giữa các màn hình.

Phạm vi của note này chỉ là kế hoạch refactor UI. Không thay đổi logic nghiệp vụ hoặc code Java trong bước ghi chú này.

## Các component nên tách

### 1. `MenuActionCard` — ưu tiên cao nhất

`RoundedActionCard` đang được copy gần như toàn bộ ở ba màn hình menu:

- `src/main/java/com/mescode/japanese/view/menu/AppMenuFrame.java:341`
- `src/main/java/com/mescode/japanese/view/kana/KanaMenuFrame.java:344`
- `src/main/java/com/mescode/japanese/view/vocabulary/VocabMenuFrame.java:347`

Nên tạo component dùng chung, ví dụ `MenuActionCard`, nhận các dữ liệu:

```java
String title
String description
String mark
Runnable action
```

Component sẽ chịu trách nhiệm cho layout, rounded background, border, hover/pressed state, arrow và click handling. Đây là hạng mục có lợi ích lớn nhất vì hiện có khoảng 300 dòng code tương tự nhau.

### 2. `SectionHeader` — ưu tiên cao

Các hàm sau có cấu trúc gần như giống hệt nhau:

- `AppMenuFrame.createSectionHeader(...)` — dòng 192
- `KanaMenuFrame.createSectionHeader(...)` — dòng 190
- `VocabMenuFrame.createSectionHeader(...)` — dòng 186

Nên tạo `SectionHeader extends JPanel`, nhận `String text`. Component tự xử lý uppercase, font, màu accent, padding và separator.

### 3. `HeroHeader` — ưu tiên cao

Các menu đều có cùng cấu trúc:

- badge `Nihongo Practice`
- title
- subtitle
- icon/mark ở bên phải

Các vị trí hiện tại:

- `AppMenuFrame.createHeroHeader(...)` — dòng 134
- `KanaMenuFrame.createHeroHeader(...)` — dòng 139
- `VocabMenuFrame.createHeroHeader(...)` — dòng 128

Nên tạo `HeroHeader` với API tương tự:

```java
new HeroHeader(
    "Kana",
    "Chọn hệ chữ để luyện...",
    new KanaMark(),
    darkMode
)
```

Nội dung title, subtitle và trailing mark nên được truyền từ ngoài để component chỉ xử lý layout và styling chung.

### 4. `RoundedPanel` / `ShadowPanel` — ưu tiên trung bình-cao

`RoundedPanel` đang bị lặp ở:

- `src/main/java/com/mescode/japanese/view/activate/ActivateFrame.java:327`
- `src/main/java/com/mescode/japanese/view/activate/AccessKeyFrame.java:245`

Ngoài ra project còn có các biến thể như `ShadowCard`, `DialogCard`, `SectionCard` và `ResultCard`.

Nên tạo một component nền dùng chung, ví dụ:

```java
RoundedPanel(Color background, int arc, boolean shadow)
```

Component chỉ nên xử lý rounded background và shadow. Nội dung bên trong vẫn để từng màn hình tự quyết định, tránh tạo một component quá nhiều điều kiện.

### 5. `AppButton` theo variant — ưu tiên trung bình

Project có nhiều hàm/class tạo button riêng:

- `createPrimaryButton`
- `createSecondaryButton`
- `createTextButton`
- `createGhostButton`
- `PillButton`
- `OptionButton`
- `RoundedButton`

Có thể gom các style dùng chung thành `AppButton` với variant:

```java
AppButton("Submit", ButtonVariant.PRIMARY, action)
```

Các variant nên bắt đầu với:

- `PRIMARY`
- `SECONDARY`
- `TEXT`
- `GHOST`
- `OPTION`

Không nên gom button login OAuth hoặc các button có hành vi đặc thù vào một class quá lớn nếu visual và interaction của chúng khác biệt đáng kể.

### 6. `GradientBackgroundPanel` — ưu tiên trung bình

Project có nhiều class tên `GradientPanel` trong các frame khác nhau. Các class này thường lặp phần:

- antialiasing
- `GradientPaint`
- blend màu
- vẽ đường/shape trang trí
- xử lý background animation

Nên tạo `GradientBackgroundPanel`, cho phép truyền màu nền, màu accent và bật/tắt animation. Các màn hình vẫn có thể cấu hình mức độ animation hoặc style riêng thông qua constructor/options.

### 7. Các icon/mark dùng chung — ưu tiên trung bình

Các class như `MenuMark`, `KanaMark`, `VocabMark` và một số mark trong `SettingsFrame` đang nằm bên trong từng frame.

Nên đưa phần vẽ icon chung vào package `view.components` hoặc `view.icons`. Những phần có hình dạng chung nên dùng component/abstract base chung; màu sắc và ký hiệu đặc trưng truyền qua constructor.

## Những phần chưa nên tách ngay

Các phần sau hiện còn gắn chặt với một màn hình hoặc một domain cụ thể, nên chưa cần đưa vào component dùng chung:

- `DifficultyOption` và `GroupOption`: đặc thù cho dialog chọn độ khó.
- Table cell renderer/editor: gắn với dữ liệu vocabulary/kanji.
- `ResultCard`, `MetricCard`, `StatChip`: hiện chủ yếu phục vụ riêng từng màn hình.
- Các button OAuth login: có visual và hành vi riêng.
- `BackButton`: đã được tách thành component dùng chung đúng hướng.

## Thứ tự refactor đề xuất

1. Tách `MenuActionCard`.
2. Tách `SectionHeader`.
3. Tách `HeroHeader`.
4. Tách `RoundedPanel` / `ShadowPanel`.
5. Chuẩn hóa `AppButton` theo variant.
6. Tách `GradientBackgroundPanel`.
7. Di chuyển và chuẩn hóa các icon/mark dùng chung.
8. Sau mỗi bước, kiểm tra lại light mode, dark mode, hover/pressed state và kích thước layout.

## Tiêu chí hoàn thành

- Không còn ba bản copy của `RoundedActionCard`, `SectionHeader` và `HeroHeader` trong các menu.
- Style light/dark mode không bị thay đổi ngoài ý muốn.
- Hover, pressed state và click action vẫn hoạt động như trước.
- Các component dùng chung nhận dữ liệu qua constructor hoặc method rõ ràng, không phụ thuộc trực tiếp vào logic riêng của từng frame.
- Các frame chỉ còn chịu trách nhiệm lắp ráp UI và xử lý nghiệp vụ.

