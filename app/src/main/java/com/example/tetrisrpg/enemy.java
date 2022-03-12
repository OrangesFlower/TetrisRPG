package com.example.tetrisrpg;

import java.util.Random;

public class enemy {
    public String name;
    public int lifeValue;
    public int type;
    boolean[][] maps;
    int level;

    void attack(){}

    enemy(int level, boolean[][] maps){
        callMap(maps);
        this.level = level;
        Random random = new Random();
        type = random.nextInt(6);
        setName(type);
    }

    String getName(){
        return name;
    }

    void setName(int type){
        switch (type){
            case 0:
                name = "MR";
                break;
            case 1:
                name = "ZCC";
                break;
            case 2:
                name = "YZX";
                break;
            case 3:
                name = "LYB";
                break;
            case 4:
                name = "LY";
                break;
        }
    }

    void callMap(boolean[][] maps){
        this.maps = maps;
    }

    void wounded(int value){
        lifeValue = lifeValue - value;
    }



}
