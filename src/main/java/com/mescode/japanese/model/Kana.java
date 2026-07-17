package com.mescode.japanese.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
// Là kết hợp giữa Katakana và Hiragana
@Getter @Setter
public class Kana {

    private String kana;    // あ
    private String romaji;  // a
    private KanaType type;   // gojuuon, dakuten, youon

    public Kana(String kana, String romaji, KanaType type) {
        this.romaji = romaji;
        this.kana = kana;
        this.type = type;
    }

    public void setType(KanaType type) {
        this.type = type;
    }
    public KanaType getType(){
        return  this.type;
    }

    public void setKana(String kana) {
        this.kana = kana;
    }
    public String getKana() {
        return kana;
    }

    public void setRomaji(String romaji) {
        this.romaji = romaji;
    }
    public String getRomaji() {
        return romaji;
    }

    @Override
    public String toString() {
        return "{ kana: " + kana + ", romaji: " + romaji + ", type: " + type + " }";
    }
}
