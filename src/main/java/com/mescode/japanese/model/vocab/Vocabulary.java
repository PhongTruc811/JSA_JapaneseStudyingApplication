package com.mescode.japanese.model.vocab;

public class Vocabulary {
    private String kana;
    private String kanji;
    private String meaning;
    private String romaji;
    private String example;
    private Integer lesson;

    public Vocabulary(String kana, String meaning, String romaji, String example) {
        this(kana, null, meaning, romaji, example, null);
    }

    public Vocabulary(String kana, String meaning, String romaji, String example, Integer lesson) {
        this(kana, null, meaning, romaji, example, lesson);
    }

    public Vocabulary(String kana, String kanji, String meaning, String romaji, String example) {
        this(kana, kanji, meaning, romaji, example, null);
    }

    public Vocabulary(String kana, String kanji, String meaning, String romaji, String example, Integer lesson) {
        this.kana = kana;
        this.kanji = kanji;
        this.meaning = meaning;
        this.romaji = romaji;
        this.example = example;
        this.lesson = lesson;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
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

    public Integer getLesson() {
        return lesson;
    }

    public void setLesson(Integer lesson) {
        this.lesson = lesson;
    }

    @Override
    public String toString() {
        return "Vocab{" +
                "kana='" + kana + '\'' +
                ", kanji='" + kanji + '\'' +
                ", meaning='" + meaning + '\'' +
                ", roumaji='" + romaji + '\'' +
                ", example='" + example + '\'' +
                ", lesson='" + lesson + '\'' +
                '}';
    }
}
