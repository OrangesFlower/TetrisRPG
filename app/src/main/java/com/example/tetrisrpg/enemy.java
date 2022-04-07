package com.example.tetrisrpg;

import java.util.Arrays;
import java.util.Random;

public class enemy {
    public String name;
    public String ability;
    public int lifeValue;
    public int maxLife;
    public int type;
    boolean[][] maps;
    boolean[][] block = new boolean[10][20];//防御的坐标
    int target = 20;
    int speedUp = 0;//加速时长
    int confuse = 0;//迷惑时常
    int level;
    int avatar;
    int interval; //攻击时间间隔

    void attack(){
        switch(type) {
            case 0:
                rising();
                break;
            case 1:
                diging();
                break;
            case 2:
                confuse();
                break;
            case 3:
                speedUp();
                break;
            case 4:
                setBlock();
                break;
        }
    }

    enemy(int level, boolean[][] maps){
        callMap(maps);
        this.level = level;
        maxLife = 500;
        lifeValue = maxLife;
        Random random = new Random();
        type = random.nextInt(5);
        //type = 4;
        setType(type);

        interval = 50000 + random.nextInt(4) * 10000;
    }

    void refreshMaps(boolean[][] maps){
        this.maps = maps;
    }

    String getName(){
        return name;
    }

    void setType(int type){
        switch (type){
            case 0:
                name = "MR";
                avatar = R.drawable.mr;
                ability = "RISING\nRising! Redundant line!";
                break;
            case 1:
                name = "ZCC";
                avatar = R.drawable.zcc;
                ability = "DIGGING\nIt will break some cube!";
                break;
            case 2:
                name = "YZX";
                avatar = R.drawable.yzx;
                ability = "CONFUSE\nNow your control keys are reversed!";
                break;
            case 3:
                name = "LYB";
                avatar = R.drawable.lyb;
                ability = "ACCELERATE\nNow the falling speed is twice!";
                break;
            case 4:
                name = "LY";
                avatar = R.drawable.ly;
                ability = "BLOCK\nYou can't remove yellow block!";
                break;
        }
    }

    void callMap(boolean[][] maps){
        this.maps = maps;
    }

    void wounded(int value){
        lifeValue = lifeValue - value;
    }

    public boolean[][] getMaps() {
        return maps;
    }

    void speedUp(){//设定加速时长
        speedUp = 10000;
    }

    void confuse(){//设定迷惑市场
        confuse = 7000;
    }

    void rising(){//上升技能
        int x;
        Random random = new Random();
        x = random.nextInt(4);
        //使所有行全部上升x层
        for (int j = 0; j <= 19 - x ; j++){
            for(int i = 0; i <= 9 ;i++){
                maps[i][j] = maps [i][j + x];
            }
        }
        int y;
        y = random.nextInt(10);
        //将剩下几层全用冗余行堆满
        for (int j = 20 - x; j <= 19 ; j++){
            for(int i = 0; i <= 9 ; i++){
                if (i == y) maps[i][j] = false;
                else maps[i][j] = true;
            }
        }
    }

    void diging(){//打洞技能
        int x;//打洞的横坐标
        int y;//打洞的纵坐标
        Random random = new Random();
        for (int i = 0; i < 3; i++){//暂且定位打三个洞
            x = random.nextInt(10);
            y = random.nextInt(20);
            while(!maps[x][y]){//若要打孔位置本来就是空的，则重新选择孔位。
                x = random.nextInt(10);
                y = random.nextInt(20);
            }
            maps[x][y] = false;
        }
    }

    void setBlock(){//封锁技能
        int max = 0;
        Random random = new Random();

        OUT:
        for (int i = 0; i < 20; i++){//找到最高行
            for(int j = 0; j < 10; j++){
                if (maps[j][i]){
                   max = i;
                   break OUT;//跳出最外层循环
                }
            }
        }

        target = max + (20 - max) / 2;
    }
}
