package com.example.tetrisrpg;

public class gameData implements Comparable<gameData> {
    private String name;
    private int score;
    private int defeated;

    public gameData(){

    }

    public gameData(String name, int score, int deafeated){
        this.name = name;
        this.score = score;
        this.defeated = deafeated;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setDefeated(int defeated) {
        this.defeated = defeated;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public int getDefeated() {
        return defeated;
    }

    @Override
    public String toString() {
        return "gameData{" +
                "name='" + name + '\'' +
                ", score=" + score +
                ", defeated=" + defeated +
                '}';
    }

    @Override
    public int compareTo(gameData gameData) {
        if (this.getDefeated() > gameData.getDefeated()){
            return 1;
        } else if (this.getDefeated() < gameData.getDefeated()){
            return -1;
        } else {
            if (this.getScore() > gameData.getScore()){
                return 1;
            } else if (this.getScore() < gameData.getScore()){
                return -1;
            } else {
                return 0;
            }
        }
    }
}
