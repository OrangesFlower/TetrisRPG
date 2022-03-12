package com.example.tetrisrpg;

import java.util.Random;

public class enemy {
    public String name;
    public int lifeValue;
    public int maxLife;
    public int type;
    boolean[][] maps;
    int level;
    int avatar;

    void attack(){}

    enemy(int level, boolean[][] maps){
        callMap(maps);
        this.level = level;
        maxLife = 500;
        lifeValue = maxLife;
        Random random = new Random();
        type = random.nextInt(6);
        setType(type);
    }

    String getName(){
        return name;
    }

    void setType(int type){
        switch (type){
            case 0:
                name = "MR";
                avatar = R.drawable.mr;
                break;
            case 1:
                name = "ZCC";
                avatar = R.drawable.zcc;
                break;
            case 2:
                name = "YZX";
                avatar = R.drawable.yzx;
                break;
            case 3:
                name = "LYB";
                avatar = R.drawable.lyb;
                break;
            case 4:
                name = "LY";
                avatar = R.drawable.ly;
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
