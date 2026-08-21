package com.mescode.japanese.repository;

import com.mescode.japanese.model.kana.Kana;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/* Note: mục tieu test mà file test này đc dùng
    -Test objective: KanaRepository
      1. readHiraFromJson()
      2. readKataFromJson()
  - kiểm tra repo đọc được file JSON
  - list không rỗng
  - dữ liệu cơ bản hợp lệ

 */

// Mọi test trong class này đc dùng để test các method của KanaRepository
class KanaRepositoryTest {

    // khởi tạo đối tượng cần test
    private final KanaRepository kanaRepositoy = new KanaRepository();
    // -> được gọi là System Under Test (SUT)

    @Test
    void readHira_shouldReturnValidKanaList() {
        // Gọi method cần được test
        List<Kana> hiraList = kanaRepositoy.readHira();
        assertAll(
                () -> assertNotNull(hiraList, "---method readHira() should not return null---"), // đảm bảo không trả về null -> return null thì test fail
                () -> assertFalse(hiraList.isEmpty(), "hiraList should not be empty")
        );
        // cách 3: tối ưu nhất
        // Assert chi tiết từng phần tử: Trả về toàn bộ lỗi nếu có nhiều phần tử sai cùng lúc
        Stream<Executable> assertions = hiraList.stream()
                .map(kana -> () -> assertTrue(isValidKana(kana), "Hiragana is not valid: " + kana.toString()));

        assertAll("check validation of each hira in hiraList", assertions);

        // cách 1: nếu fail không biết phần tử nào sai
        // assertTrue(hiraList.stream().allMatch(this::isValidKana));
        // duyệt hiraList qua isValidKana() -> tất cả hợp lệ -> test success
        // hiraList.stream() -> Java tạo luồng (stream) để duyệt qua từng phần tử trong list -> giống vòng lặp for-each
        // allMatch() - tất cả các phần tử của hiraList thỏa mãn điều kiện
        // (this::isValidKana)) tương đương : kana -> isValidKana(kana)
    }

    @Test
    void readKata_shouldReturnValidKataList() {
        List<Kana> kataList = kanaRepositoy.readKata();

        assertNotNull(kataList);
        assertFalse(kataList.isEmpty());
        assertTrue(kataList.stream().allMatch(this::isValidKana));
    }


    private boolean isValidKana(Kana kana) {
        return kana != null
                && kana.getKana() != null && !kana.getKana().isBlank()
                && kana.getRomaji() != null && !kana.getRomaji().isBlank()
                && kana.getType() != null;
    }
}
