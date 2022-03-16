package com.example.tetrisrpg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class GameActivity extends AppCompatActivity{
    //游戏区域空间
    SurfaceView gameView;

    //游戏区域的高 宽
    int xHight,xWidth;
    //游戏区域格子
    boolean[][] maps;
    //方块
    Point[] boxs;
    //方块大小
    int boxSize;
    //辅助线画笔、方块画笔
    Paint linePaint,boxPaint,mapPaint;
    //界面状态画笔（暂停/结束）
    Paint statePaint;
    //block画笔
    Paint blockPaint;
    //生成现在的tetrominoes
    Tetrominoes curTetro;
    //生成现在的tetrominoes
    Tetrominoes nextTero = new Tetrominoes(boxSize, maps);

    //生成一个敌人
    enemy curEnemy;

    //下落速度（下落一格的时间间隔）
    int curSpeed = 800;
    //分数
    int score = 0;
    //游戏难度
    int level = 1;
    //杀敌数量
    int defeated = 0;
    //暂停状态
    boolean isPause = false;
    //GameOver状态
    boolean isOver = false;
    //检查有没有触发Tspin
    boolean isTspin = false;
    //检查上一个动作是否为Tspin
    boolean isPreTspin = false;
    //有没有被攻击扰乱
    boolean isConfuse = false;
    //检查刚刚有没有line removing，并识别是消除几行
    int isPreRemv = 0;
    //hold中的类型
    int holdType = -1;



    //下落线程
    public Thread downThread;
    //检测状态线程
    public Thread checkThread;
    //敌人状态线程
    public Thread enemyThread;
    //敌人攻击线程
    public Thread enemyAttack;

    public Handler handler =new Handler(){
        @SuppressLint("HandlerLeak")
        public void handleMessage(android.os.Message msg){
            //重绘画面
            gameView.invalidate();
            setImage(nextImage, nextTero.type);
            scoreView.setText("Score:"+score);
            defeatNum.setText("Defeated:"+defeated);

            //敌人状态的替换
            enemyName.setText(curEnemy.name);
            enemyLife.setProgress(100 * curEnemy.lifeValue / curEnemy.maxLife);
            enemyImage.setImageResource(curEnemy.avatar);
            curEnemy.refreshMaps(maps);

            if(curEnemy.lifeValue <= 0){
                curEnemy = new enemy(level, maps);
                defeated ++;
            }

            lineRemvAnime();
        };
    };

    public Handler enemyHandler =new Handler(){
        @SuppressLint("HandlerLeak")
        public void handleMessage(android.os.Message msg){
            //重绘画面
            gameView.invalidate();
            setImage(nextImage, nextTero.type);
            scoreView.setText("Score:"+score);
            defeatNum.setText("Defeated:"+defeated);

            //敌人状态的替换
            enemyName.setText(curEnemy.name);
            enemyLife.setProgress(100 * curEnemy.lifeValue / curEnemy.maxLife);
            enemyImage.setImageResource(curEnemy.avatar);
            curEnemy.refreshMaps(maps);

            if(curEnemy.lifeValue <= 0){
                curEnemy = new enemy(level, maps);
                defeated ++;
            }

            lineRemvAnime();
        };
    };



    //向左按钮
    Button leftButton;
    //向右按钮
    Button rightButton;
    //向下按钮
    Button downButton;
    //旋转按钮
    Button rotateButton;
    //瞬间下落按钮
    Button hardDropButton;
    //hold按钮
    Button holdButton;

    //暂停按钮
    ImageButton pauseButton;

    //玩家图片
    GifImageView playerView;

    //next图片
    ImageView nextImage;
    //hold图片
    ImageView holdImage;
    //敌人图片
    ImageView enemyImage;

    //分数显示
    TextView scoreView;
    //击败敌人数显示
    TextView defeatNum;

    //敌人信息显示
    TextView enemyName;
    ProgressBar enemyLife;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        leftButton = (Button)findViewById(R.id.leftButton);
        rightButton = (Button)findViewById(R.id.rightButton);
        downButton = (Button)findViewById(R.id.downButton);
        rotateButton = (Button)findViewById(R.id.rotateButton);
        pauseButton = (ImageButton) findViewById(R.id.pauseButton);
        hardDropButton = (Button)findViewById(R.id.hardDrop);
        holdButton = (Button)findViewById(R.id.holdButton);

        nextImage = findViewById(R.id.nextImage);
        holdImage = findViewById(R.id.holdImage);
        enemyImage = findViewById(R.id.enemyImage);

        playerView = findViewById(R.id.playerView);

        scoreView = (TextView) findViewById(R.id.scoreView);
        defeatNum = (TextView) findViewById(R.id.defeated);
        enemyName = (TextView) findViewById(R.id.enemyName);
        enemyLife = (ProgressBar) findViewById(R.id.enemyLife);

        //敌人系统，生成敌人
        curEnemy = new enemy(level, maps);


        initdata();
        initview();
        startGame();
        checkGame();
        checkEnemy();




        //左监听
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isPause && !isOver){//暂停
                    if (isConfuse) curTetro.move(1 , 0);
                    else curTetro.move(-1 , 0);
                }
                gameView.invalidate();
            }
        });

        //右监听
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isPause && !isOver){//暂停
                    if (isConfuse) curTetro.move(-1 , 0);
                    else curTetro.move(1 , 0);
                }
                gameView.invalidate();
            }
        });

        //向下加速
        downButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!isPause && !isOver){
                    if (motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                        curSpeed = curSpeed / 2;
                        System.out.println("加速："+curSpeed);
                    }else if (motionEvent.getAction()==MotionEvent.ACTION_UP){
                        curSpeed = curSpeed * 2;
                        System.out.println("减速："+curSpeed);
                    }
                }
                return false;
            }
        });


        //要不要注释掉以后再说吧
        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isPause && !isOver){//暂停
                    curTetro.move(0 , 1);
                }
                gameView.invalidate();
            }
        });


        //旋转按钮监听
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isPause && !isOver){//暂停
                    curTetro.rotate();
                }
                gameView.invalidate();
            }
        });

        //瞬间下落按钮监听
        hardDropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isPause && !isOver){//暂停
                    while(!curTetro.isBottom){
                        curTetro.move(0,1);
                    }
                    curTetro.isFix = true;
                }
                gameView.invalidate();
            }
        });

        //hold按钮监听
        holdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isPause && !isOver){
                    changeHold();
                    setImage(holdImage,holdType);
                }
            }
        });

        //暂停按钮监听
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePause();
            }
        });
    }

    private void setImage(ImageView image, int type) {
        switch (type){
            case 0:
                image.setImageResource(R.drawable.one);
                break;
            case 1:
                image.setImageResource(R.drawable.j);
                break;
            case 2:
                image.setImageResource(R.drawable.l);
                break;
            case 3:
                image.setImageResource(R.drawable.cube);
                break;
            case 4:
                image.setImageResource(R.drawable.s);
                break;
            case 5:
                image.setImageResource(R.drawable.t);
                break;
            case 6:
                image.setImageResource(R.drawable.z);
                break;
        }
    }

    private void changeHold() {
        int tmp;

        if(holdType == -1){
            holdType = curTetro.type;
            curTetro = nextTero;
            nextTero = new Tetrominoes(boxSize, maps);
        } else {
            if(curTetro.canHold){
                tmp = curTetro.type;
                curTetro.chooseType(holdType);
                holdType = tmp;
                curTetro.canHold = false;
            }
        }
    }

    private void checkGame() {//检测tetrominoes状态的进程
        if(checkThread == null){
            checkThread = new Thread(){
                @Override
                public void run(){
                    super.run();
                    while(true){
                        if(curTetro.isBottom){//检测到底之后还给一点点时间移动
                            try {
                                sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            curTetro.setFix();
                        }

                        if(curTetro.isFix){
                            checkOver();
                        }


                    }
                }
            };
            checkThread.start();
        }
    }

    //开始游戏
    private void startGame() {
        //若线程为空——>new
        if (downThread == null){
            downThread = new Thread(){
                @Override
                public void run() {
                    super.run();
                    while(true){
                        try {//创建线程后等个500ms
                            sleep(curSpeed);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //下落之前判断是否暂停
                        if(isPause || isPause) {
                            handler.sendEmptyMessage(0);
                            continue;//跳过这次循环
                        }
                        //下落一格
                        curTetro.refreshMaps(maps);
                        curTetro.move(0, 1);
                        //检测是否到底了——>给个500ms的休息时间将tetrominoes设为固定
                        //检测是否固定了，如果固定了了则重新生成一个
                        if(curTetro.isFix){
                            checkTspin();
                            for (Point box : curTetro.boxs){
                                try{
                                    maps[box.x][box.y] = true;
                                } catch (Exception e){
                                    isOver = true;
                                }

                            }
                            removeLine();
                            isPreTspin = false;//检测上一个Tspin只在本轮有效
                            if(isTspin){
                                isTspin = false;//Tspin只在本轮有效
                                isPreTspin = true;//重新判定是否上一把有Tspin
                            }

                            curTetro = nextTero;
                            nextTero = new Tetrominoes(boxSize, maps);

                            //判断游戏是否结束
                            checkOver();
                        }

                        //
                        handler.sendEmptyMessage(0);
                    }
                }
            };
            downThread.start();
        }

        curTetro = nextTero;
        nextTero = new Tetrominoes(boxSize, maps);
    }

    private void checkEnemy() {//检测enemy状态的进程
        if(enemyThread == null){
            enemyThread = new Thread(){
                @Override
                public void run(){
                    super.run();
                    while(true){
                        if (!isPause && !isOver){
                            //敌人休息
                            try {
                                sleep(curEnemy.interval);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            //敌人进攻
                            curEnemy.attack();
                            maps = curEnemy.getMaps();
                            if (curEnemy.speedUp != 0) {
                                curSpeed = curSpeed / 2;
                                try {//持续几秒
                                    sleep(curEnemy.speedUp);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                curSpeed = curSpeed * 2;
                                curEnemy.speedUp = 0;
                            }else if (curEnemy.confuse != 0) {
                                isConfuse = true;
                                try {
                                    sleep(curEnemy.confuse);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                isConfuse = false;
                                curEnemy.confuse = 0;
                            }

                        }



                    }
                }
            };
            enemyThread.start();
        }
    }

    //判断游戏是否结束
    public void checkOver(){
        for (Point box : curTetro.boxs){//若新生成的tetrominoes满足以下条件
            if(box.y >= 0 && maps[box.x][box.y] && curTetro.xLocation <= 0) {
                isOver = true;
                System.out.println("GAME OVER");
            }
        }
    }

    //初始化画面数据
    private void initdata(){
        //获得屏幕宽度
        int width=getScreenWidth(this);
        int height=getScreenHeight(this);
        System.out.println("屏幕宽度："+width);
        System.out.println("屏幕高度："+height);
        //设置游戏区域宽度
        //设置游戏区域的高度=宽度*2
        xHight = height * 5 /6;
        xWidth = xHight / 2;
        //xWidth= width /4;
        System.out.println("宽度："+xWidth);
        //xHight=xWidth*2;
        System.out.println("高度："+xHight);
        //初始化地图
        maps=new boolean[10][20];
        //初始化方块
        boxs=new Point[]{new Point(0,0)};
        //初始化方块大小=屏幕长度/10
        boxSize=xWidth/maps.length;
    }

    //获取屏幕尺寸(宽度)
    private int getScreenWidth(Context context) {
        WindowManager wm=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);//屏幕对象
        DisplayMetrics outMetrics=new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    //获取屏幕高度
    private int getScreenHeight(Context context) {
        WindowManager wm=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);//屏幕对象
        DisplayMetrics outMetrics=new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    //初始化游戏主体部分
    private void initview(){
        //得到游戏容器
        //画面区域
        LinearLayout gameView = findViewById(R.id.gameView);
        //创建一个辅助线画笔
        linePaint=new Paint();
        linePaint.setARGB(225,60,60,60);//辅助线颜色
        linePaint.setStrokeWidth((float) 5.0);//线宽
        linePaint.setAntiAlias(true);//抗锯齿

        //创建map画笔
        mapPaint=new Paint();
        mapPaint.setColor(0xff666666);//方块颜色
        mapPaint.setAntiAlias(true);


        //画游戏区域
        this.gameView = new SurfaceView(this) {
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                //绘制已下落方块
                for (int i = 0; i < maps.length; i++) {
                    for(int j = 0; j < maps[i].length; j++){
                        if (maps[i][j]){
                            canvas.drawRect(//画矩形->存在boxs中的box类，有则在相应的xy位置画个矩形
                                    i * boxSize,
                                    j * boxSize,
                                    i * boxSize + boxSize,
                                    j * boxSize + boxSize, mapPaint);
                        }
                    }
                }

                //创建一个方块画笔(不知道什么问题要放在这里才能画出彩色方块)
                boxPaint=new Paint();
                boxPaint.setARGB(255,curTetro.RGB[0],curTetro.RGB[1],curTetro.RGB[2]);//方块颜色
                boxPaint.setAntiAlias(true);

                //方块
                for (Point box : curTetro.boxs) {
                    canvas.drawRect(//画矩形->存在boxs中的box类，有则在相应的xy位置画个矩形
                            box.x * boxSize,
                            box.y * boxSize,
                            box.x * boxSize + boxSize,
                            box.y * boxSize + boxSize, boxPaint);
                }
                //竖线
                for (int x = 0; x <= maps.length; x++) {
                    canvas.drawLine(x * boxSize, 0, x * boxSize , 20 * boxSize, linePaint);
                }
                //横线
                for (int y = 0; y <= maps[0].length; y++) {
                    canvas.drawLine(0, y * boxSize, 10 * boxSize, y * boxSize, linePaint);
                }

                //创建状态画笔
                statePaint = new Paint();
                statePaint.setARGB(200,255,0,0);//文字
                statePaint.setAntiAlias(true);
                statePaint.setTextSize(100);

                //画状态提示
                if(isPause) {
                    canvas.drawText("PAUSE",gameView.getWidth() / 2 - statePaint.measureText("PAUSE") * 3 / 4,gameView.getHeight() / 2,statePaint);
                }
                else if(isOver) {
                    canvas.drawText("GAME",gameView.getWidth() / 2 - statePaint.measureText("GAME") * 3/4,gameView.getHeight() / 2,statePaint);
                    canvas.drawText("OVER",gameView.getWidth() / 2 - statePaint.measureText("OVER") * 3/4,gameView.getHeight() / 2 + 100,statePaint);
                }


                //敌人的block技能
                //创建状态画笔
                blockPaint = new Paint();
                blockPaint.setARGB(100,255,255,0);//文字
                blockPaint.setAntiAlias(true);

                for (int i = 0; i < curEnemy.block.length; i++) {
                    for(int j = 0; j < curEnemy.block[i].length; j++){
                        if (curEnemy.block[i][j]){
                            canvas.drawRect(//画矩形->存在boxs中的box类，有则在相应的xy位置画个矩形
                                    i * boxSize,
                                    j * boxSize,
                                    i * boxSize + boxSize,
                                    j * boxSize + boxSize, blockPaint);
                        }
                    }
                }
            }
        };

        //画

        //设置游戏区域大小
        this.gameView.setLayoutParams(new FrameLayout.LayoutParams(xWidth,xHight));
        //是指背景颜色
        this.gameView.setBackgroundColor(Color.rgb(10, 10, 10));
        //添加到父容器里面
        gameView.addView(this.gameView);

    }

    void removeLine(){
        int counter = 0;
        for (int j = 19; j > 0; j--) {
            //消行
            if(checkLine(j)){
                for (int y = j; y > 0; y--) {
                    for(int i = 0; i < maps.length; i++){
                        //一行全为true才进行消行
                        if(!curEnemy.block[i][y]){
                            maps[i][y] = maps[i][y - 1];
                        }
                    }
                }
                counter++;
                j++;
            }
        }
        isPreRemv = counter;
        goal(counter);
        System.out.println(score);
    }

    //检测消除几行并播放动画
    private void lineRemvAnime() {
        if(isPreRemv != 0) playerView.setImageResource(R.drawable.remove_line);
    }

    private void goal(int counter) {
        float plus = 1;//分数加成
        float goal = 0;
        if(isPreTspin) plus = 1.5F;

        if(counter == 1) {
            if(isTspin) goal = 800 * plus;
            else goal = 100 * plus;
        }
        else if(counter == 2) {
            if(isTspin) goal = 1200 * plus;
            else goal = 300 * plus;
        }
        else if(counter == 3) goal = 500 * plus;
        else if(counter == 4) goal = 800 * plus;

        curEnemy.wounded((int) goal);
        score += goal;
    }

    private void checkTspin(){
        List<Point> list = Arrays.asList(curTetro.boxs);
        if(list.contains(new Point(curTetro.xLocation - 1, curTetro.yLocation))){
            if(curTetro.xLocation - 1 >= 0 && curTetro.yLocation - 1 >= 0 && curTetro.xLocation + 1 <= 9 && curTetro.yLocation + 1 <= 19){
                if(maps[curTetro.xLocation - 1][curTetro.yLocation - 1] && maps[curTetro.xLocation - 1][curTetro.yLocation + 1] && maps[curTetro.xLocation + 1][curTetro.yLocation + 1]){
                    isTspin = true;
                }
            }
        }

    }

    public boolean checkLine(int y){//检测一行是否满了
        for(int i = 0; i < maps.length; i++){
            if(!maps[i][y]) return false;
        }
        return true;
    }

    public void changePause(){
        isPause = !isPause;//点一下暂停，再点一下继续
    }

}

