package com.mescode.japanese.model.user;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Score {
    private int score = 0;

    public void increase(){
        score ++;
    }

    public void decrease(){
        score--;
    }

    public void reset(){
        score = 0;
    }

}
