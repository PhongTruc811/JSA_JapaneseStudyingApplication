package com.mescode.japanese.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mescode.japanese.model.kana.Kana;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Class dùng để đọc nơi chứa dữ liệu của Kana như Json, Database, API
// Lưu ý : 👉 Repository KHÔNG NÊN static data
public class KanaRepository {

    // Báo cho Gson biết
    // 1. kiểu dữ liệu mà mình muốn chuyển từ Json sang là List<Kana>
    // 2. Parse Json thành List và mỗi phần tử bên trong List đó thuộc kiểu Hiragana
    private final Type listType = new TypeToken<List<Kana>>(){}.getType();
    private final Gson gson = new Gson();

    // đọc bang chu hirgana từ file Hiragana.json trong package resources
    public List<Kana> readHira() {
        return readFromJson("/data/hiragana.json");
    }

    public List<Kana> readKata() {
        return readFromJson("/data/katakana.json");
    }

    public List<Kana> readHiraFromJson() {
        return readHira();
    }

    public List<Kana> readKataFromJson() {
        return readKata();
    }

    private List<Kana> readFromJson(String filePath) {
        // Java tạo datastream cho file cần được đọc
        try(InputStream ipS = KanaRepository.class.getResourceAsStream(filePath)){
            // xử lý khi sai filePath hoặc file k tồn tại
            if(ipS == null) {
                throw new RuntimeException("file not found: " + filePath);
            }
            // Dùng BufferedReader giúp đọc dữ liệu text(json) nhanh hơn, phù hợp với Gson
            try (BufferedReader bfReader = new BufferedReader(new InputStreamReader(ipS, StandardCharsets.UTF_8))) {
                return gson.fromJson(bfReader, listType);
            }

        } catch (Exception e){
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }
}
