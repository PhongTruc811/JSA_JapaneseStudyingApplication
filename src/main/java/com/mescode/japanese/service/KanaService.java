package com.mescode.japanese.service;

import com.mescode.japanese.model.kana.Kana;
import com.mescode.japanese.model.kana.KanaType;
import com.mescode.japanese.model.kana.KanaQuizGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class KanaService {

    private final List<Kana> kanaList;
    private final List<Kana> goujuonList;
    private List<Kana> dakuonList;
    private List<Kana> youyonList;
    private final Random random;

    public KanaService(List<Kana> kanaList) {
        this(kanaList, new Random(), true);
    }

    public KanaService(List<Kana> kanaList, KanaQuizGroup group) {
        this(filterByGroup(kanaList, group), new Random(), true);
    }

    public KanaService(List<Kana> kanaList, Random random) {
        this(kanaList, random, true);
    }

    public KanaService(List<Kana> kanaList, Random random, boolean shuffleInput) {
        this.kanaList = new ArrayList<>(kanaList);
        this.random = random == null ? new Random() : random;
        if (shuffleInput) {
            Collections.shuffle(this.kanaList, this.random);
        }
        loadKana();
        goujuonList = this.kanaList.stream().filter(kana -> KanaType.gojuuon.equals(kana.getType())).toList();
        youyonList = this.kanaList.stream().filter(kana -> KanaType.youon.equals(kana.getType())).toList();
        dakuonList = this.kanaList.stream().filter(kana -> KanaType.dakuon.equals(kana.getType())).toList();
    }

    public Kana getRandomKana(Kana previousKana){
        if (kanaList.size() == 1) {
            return kanaList.getFirst();
        }
        Kana currentKana;
        if(previousKana == null) {
            return kanaList.get((random.nextInt(kanaList.size())));
        }
        do{
            currentKana = kanaList.get((random.nextInt(kanaList.size())));
        } while(currentKana.equals(previousKana));

        System.out.println("---------------------------------------------------");
        System.out.println("current kana: " + currentKana);
        System.out.println("---------------------------------------------------");

        return currentKana;
    }

    private static List<Kana> filterByGroup(List<Kana> kanaList, KanaQuizGroup group) {
        if (group == null) {
            throw new IllegalArgumentException("Kana quiz group is required");
        }
        List<Kana> filtered = kanaList.stream()
                .filter(kana -> kana != null && group.matches(kana.getType()))
                .toList();
        if (filtered.isEmpty()) {
            throw new IllegalArgumentException("No kana available for group " + group.getDisplayName());
        }
        return filtered;
    }

    public Kana getRandomGojuon(Kana previousGojuuon){
        Kana currentGojuon;
        do{
            currentGojuon = goujuonList.get(random.nextInt(goujuonList.size()));
        } while(currentGojuon.equals(previousGojuuon));
        return currentGojuon;
    }

    private void loadKana(){
        System.out.println("Kana list List From Hiragana.json");
        System.out.println("-----------------------------------------");
        for (Kana hira : kanaList){
            System.out.println(hira.toString());
        }
        System.out.println("-----------------------------------------");
    }

    private void loadGoujuon(){
        if(goujuonList.isEmpty()){
            System.out.println("Goujuon list is empty");
        }
        else{
            System.out.println("Goujuon list: " + goujuonList.size() + " / 46   ");
            for (Kana hira : goujuonList){
                System.out.println(hira);
            }
        }
    }
}
