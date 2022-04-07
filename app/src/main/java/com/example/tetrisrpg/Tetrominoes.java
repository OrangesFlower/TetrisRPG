package com.example.tetrisrpg;

import android.graphics.Paint;
import android.graphics.Point;

import java.util.Arrays;
import java.util.Random;

public class Tetrominoes {
    //重心位置
    int xLocation = 5;
    int yLocation = -2;
    //是否到底了
    boolean isBottom = false;
    //是否固定不能动了
    boolean isFix = false;
    //一个tetrominoes只能hold一次
    boolean canHold = true;



    //辅助线画笔、方块画笔
    Paint boxPaint;
    //Tetominoes
    Point[] boxs;
    //Tetominoes的影子
    Point[] shadow;
    //方块大小
    int boxSize;
    //tetrominoes类型
    int type = 0;
    //颜色
    int[] RGB = new int[3];

    //主进程中传过来的maps
    boolean[][] maps;

    //敌人攻击时传过来的新底部参数
    int bottom = 19;

    Tetrominoes(int boxSize, boolean maps[][]){
        this.maps = maps;
        Random random = new Random();
        type = random.nextInt(7);
        this.boxSize = boxSize;
        chooseType(type);
        //refreshShadow();
    }

    //移动
    void move(int x, int y){
        checkBottom();
        boolean moveVertical = true;
        boolean moveHorizon = true;

        if(moveValid(x, y) == 1 && !isBottom &&!isFix){//若左右能动，也没到底，也能动
            xLocation = xLocation + x;
            yLocation = yLocation + y;
            for (Point box : boxs) {
                box.x = box.x + x;
            }
            for (Point box : boxs) {
                box.y = box.y + y;
            }
        }else if(moveValid(x, y) == 1 && isBottom &&!isFix){//若左右能动，到底了，等个半秒再固定
            xLocation = xLocation + x;
            for (Point box : boxs) {
                box.x = box.x + x;
            }

        }
    }

    //旋转
    //也需要判断边界
    void rotate(){//理论上来说田字形应该特殊处理，因为其田字形旋转不应该动
        if(checkRotate()){
            if (type != 3){
                //遍历，使每个单元都按照xLocation与yLocation旋转90°
                for (Point box : boxs){//笛卡尔旋转公式，计算机图形学里有学
                    int tempX = -box.y + xLocation + yLocation;
                    int tempY = box.x - xLocation + yLocation;
                    box.x = tempX;
                    box.y = tempY;
                }
            }
        }

    }

    private boolean checkRotate() {
        boolean res = true;
        int counter = 0;
        for (Point box : boxs){//笛卡尔旋转公式，计算机图形学里有学
            int tempX = -box.y + xLocation + yLocation;
            int tempY = box.x - xLocation + yLocation;
            if(tempX > 9 || tempX < 0 || tempY > 19 || tempY < 0) res = false;
            else{
                if(maps[tempX][tempY]) res = false;
            }

        }
        return  res;
    }

    void chooseType(int type){
        switch(type){
            case 0://一字型
                boxs=new Point[]{new Point(xLocation,yLocation),
                        new Point(xLocation - 1,yLocation),
                        new Point(xLocation - 2,yLocation),
                        new Point(xLocation + 1,yLocation),};
                RGB[0] = 0;
                RGB[1] = 240;
                RGB[2] = 240;
                break;
            case 1://J字刑
                boxs=new Point[]{new Point(xLocation,yLocation),
                        new Point(xLocation - 1,yLocation),
                        new Point(xLocation - 1,yLocation - 1),
                        new Point(xLocation + 1,yLocation),};
                RGB[0] = 0;
                RGB[1] = 0;
                RGB[2] = 240;
                break;
            case 2://L字型
                boxs=new Point[]{new Point(xLocation,yLocation),
                        new Point(xLocation - 1,yLocation),
                        new Point(xLocation + 1,yLocation - 1),
                        new Point(xLocation + 1,yLocation),};
                RGB[0] = 240;
                RGB[1] = 160;
                RGB[2] = 0;
                break;
            case 3://田字型
                boxs=new Point[]{new Point(xLocation,yLocation),
                        new Point(xLocation - 1,yLocation),
                        new Point(xLocation,yLocation + 1),
                        new Point(xLocation - 1,yLocation + 1),};
                RGB[0] = 240;
                RGB[1] = 240;
                RGB[2] = 0;
                break;
            case 4://S字型
                boxs=new Point[]{new Point(xLocation,yLocation),
                        new Point(xLocation,yLocation - 1),
                        new Point(xLocation + 1,yLocation - 1),
                        new Point(xLocation - 1,yLocation)};
                RGB[0] = 0;
                RGB[1] = 240;
                RGB[2] = 0;
                break;
            case 5://T字型
                boxs=new Point[]{new Point(xLocation,yLocation),
                        new Point(xLocation,yLocation - 1),
                        new Point(xLocation - 1,yLocation),
                        new Point(xLocation + 1,yLocation)};
                RGB[0] = 160;
                RGB[1] = 0;
                RGB[2] = 240;
                break;
            case 6://Z字型
                boxs=new Point[]{new Point(xLocation,yLocation),
                        new Point(xLocation,yLocation - 1),
                        new Point(xLocation - 1,yLocation - 1),
                        new Point(xLocation + 1,yLocation)};
                RGB[0] = 240;
                RGB[1] = 0;
                RGB[2] = 0;
                break;
        }
    }

    int moveValid(int x, int y){//左右移动判定
        int validation = 1;
        for(Point box : boxs){
            if(box.x >= 9 && x > 0) validation = 0;
            else if(box.x <=0 && x < 0) validation = -1;
            else if (box.y >= 0){
                if(maps[box.x + x][box.y]) validation = -3;
            }
        }
        return validation;
    }

    public void checkBottom(){//到底了吗
        isBottom = false;
        for(Point box : boxs){
            if(box.y >= bottom) isBottom = true;
            else if(box.y >= 0){
                if(maps[box.x][box.y + 1]) isBottom = true;
            }
        }
    }

    void setFix(){
        checkBottom();
        if(isBottom) isFix = true;
    }

    void refreshMaps(boolean[][] maps){//将主线程的maps与内部同步以保证判断碰撞
        this.maps = maps;
    }



    void refreshShadow(){//更新影子坐标
        shadow = Arrays.copyOf(boxs,4);
        int highest=20;
        int distance = 0;

        for(Point box : shadow){
            for (int i = 0; i < maps[0].length; i++){
                if(maps[box.x][i] && i <= highest){
                    highest = i;
                    distance = highest - box.y - 1;
                }
            }
        }
        for(Point box : shadow){
            box.y = box.y + distance;
        }
    }


}
