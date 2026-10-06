package com.mescode.japanese.model.vocab;

import com.google.gson.annotations.SerializedName;

public class Vocabulary {
    private String kana;
    private String kanji;
    private String meaning;
    private String romaji;

    private Integer chapter;

    public Vocabulary(String kana, String meaning, String romaji, String example) {
        this(kana, null, meaning, romaji, example, null);
    }

    public Vocabulary(String kana, String meaning, String romaji, String example, Integer chapter) {
        this(kana, null, meaning, romaji, example, chapter);
    }

    public Vocabulary(String kana, String kanji, String meaning, String romaji, String example) {
        this(kana, kanji, meaning, romaji, example, null);
    }

    public Vocabulary(String kana, String kanji, String meaning, String romaji, String example, Integer chapter) {
        this.kana = kana;
        this.kanji = kanji;
        this.meaning = meaning;
        this.romaji = romaji;
        this.chapter = chapter;
    }

    public String getRomaji() {
        return romaji;
    }

    public void setRomaji(String romaji) {
        this.romaji = romaji;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getKana() {
        return kana;
    }

    public void setKana(String kana) {
        this.kana = kana;
    }

    public String getKanji() {
        return kanji;
    }

    public void setKanji(String kanji) {
        this.kanji = kanji;
    }

    public Integer getChapter() {
        return chapter;
    }

    public void setChapter(Integer chapter) {
        this.chapter = chapter;
    }

    @Override
    public String toString() {
        return "Vocab{" +
                "kana='" + kana + '\'' +
                ", kanji='" + kanji + '\'' +
                ", meaning='" + meaning + '\'' +
                ", roumaji='" + romaji + '\'' +
                ", lesson='" + chapter + '\'' +
                '}';
    }
}
