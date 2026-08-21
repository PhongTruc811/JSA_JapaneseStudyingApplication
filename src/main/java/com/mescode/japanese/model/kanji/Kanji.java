package com.mescode.japanese.model.kanji;

public class Kanji {
    private int unit;
    private String hanViet;
    private String kanji;
    private String hiragana;
    private String meaning;

    public Kanji() {
    }

    public Kanji(int unit, String hanViet, String kanji, String hiragana, String meaning) {
        this.unit = unit;
        this.hanViet = hanViet;
        this.kanji = kanji;
        this.hiragana = hiragana;
        this.meaning = meaning;
    }

    public int getUnit() { return unit; }
    public String getHanViet() { return hanViet; }
    public String getKanji() { return kanji; }
    public String getHiragana() { return hiragana; }
    public String getMeaning() { return meaning; }
}