package com.example.tetrisrpg;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class GameActivity extends AppCompatActivity{
    //游戏BGM
    MediaPlayer gamePlayer;

    //用户设置
    SharedPreferences sp;
    boolean gameBGM;
    String playerNamePre;

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
    private Paint borderPaint;
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
            enemyAbility.setText(curEnemy.ability);
            enemyLife.setProgress(100 * curEnemy.lifeValue / curEnemy.maxLife);
            enemyImage.setImageResource(curEnemy.avatar);
            curEnemy.refreshMaps(maps);
            curTetro.bottom = curEnemy.target - 1;

            if(curEnemy.lifeValue <= 0){
                playSound(8);
                curEnemy = new enemy(level, maps);
                defeated ++;
            }

            if (isOver) {
                playSound(9);
                setDialog(0);
            }

            if (isAttack){
                findViewById(R.id.warningGif).setVisibility(View.VISIBLE);
                findViewById(R.id.warningView).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.warningGif).setVisibility(View.INVISIBLE);
                findViewById(R.id.warningView).setVisibility(View.INVISIBLE);
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

    //玩家姓名
    TextView playerName;
    //分数显示
    TextView scoreView;
    //击败敌人数显示
    TextView defeatNum;
    //敌人是否在攻击
    boolean isAttack = false;

    //敌人信息显示
    TextView enemyName;
    TextView enemyAbility;
    ProgressBar enemyLife;

    //按钮音效池
    private SoundPool mSoundPool ;
    private HashMap<Integer,Integer> mSoundMap;//声音编号映射


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
        playerName = (TextView) findViewById(R.id.playerName);

        scoreView = (TextView) findViewById(R.id.scoreView);
        defeatNum = (TextView) findViewById(R.id.defeated);
        enemyName = (TextView) findViewById(R.id.enemyName);
        enemyAbility = (TextView) findViewById(R.id.abilityView);
        enemyLife = (ProgressBar) findViewById(R.id.enemyLife);

        //敌人系统，生成敌人
        curEnemy = new enemy(level, maps);

        //BGM
        gamePlayer = MediaPlayer.create(this, R.raw.voyage_of_promise);
        gamePlayer.setLooping(true);



        initPreference();
        initdata();
        initview();
        initSound();
        startGame();
        checkGame();
        checkEnemy();

        if(!gamePlayer.isPlaying() && gameBGM){
            gamePlayer.start();
        }


        //左监听
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isPause && !isOver){//暂停
                    playSound(1);
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
                    playSound(1);
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
                    playSound(1);
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
                    playSound(2);
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
                    playSound(3);
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
                    playSound(4);
                    changeHold();
                    setImage(holdImage,holdType);
                }
            }
        });

        //暂停按钮监听
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(5);
                changePause();
                setDialog(1);
            }
        });


    }

    private void initPreference() {//初始化用户设置
        SharedPreferences sp =
                PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        gameBGM = sp.getBoolean("GameBGM", true);
        playerNamePre = sp.getString("PlayerName", "aaa");
    }

    private void initSound() {//初始化音效池
        mSoundMap = new HashMap<>();
        mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC,100);
        loadSound(1,R.raw.control);
        loadSound(2,R.raw.rotate);
        loadSound(3,R.raw.harddrop);
        loadSound(4,R.raw.hold);
        loadSound(5,R.raw.pause);
        loadSound(6,R.raw.elimination);
        loadSound(7,R.raw.alarm);
        loadSound(8,R.raw.deathofenemy);
        loadSound(9,R.raw.gameover);
        loadSound(10,R.raw.restart);
    }

    /**
     * 把音频资源添加到声音池
     * @param seq
     * @param resid
     */
    private void loadSound(int seq,int resid){
        int soundID = mSoundPool.load(this,resid,1);
        mSoundMap.put(seq,soundID);
    }

    /**
     * 播放指定音频
     * @param seq
     */
    private void playSound(int seq){
        int soundID = mSoundMap.get(seq);
        mSoundPool.play(soundID,1.0f,1.0f,1,0,1.0f);
    }

    @Override
    protected void onDestroy() {
        //释放MediaPlayer资源，这个很占用资源
        System.out.println("游戏音乐停止");
        gamePlayer.stop();
        gamePlayer.release();
        gamePlayer = null;

        super.onDestroy();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            changePause();
            setDialog(1);
            return true;
        } else {// 如果不是back键正常响应
            return super.onKeyDown(keyCode, event);
        }
    }

    private void setDialog(int mode) {
        Dialog mDialog = new Dialog(this, R.style.BottomDialog);
        ConstraintLayout root = (ConstraintLayout)  LayoutInflater.from(this).inflate(
                R.layout.bottom_dialog, null);
        mDialog.setCanceledOnTouchOutside(false);

        //初始化视图

        //弹出弹窗时是暂停还是游戏结束
        if (mode == 0){//游戏结束
            root.findViewById(R.id.pauseText).setVisibility(View.INVISIBLE);
            root.findViewById(R.id.continueButton).setVisibility(View.INVISIBLE);
        }else{//游戏暂停
            root.findViewById(R.id.overText).setVisibility(View.INVISIBLE);
        }

        mDialog.setContentView(root);
        Window dialogWindow = mDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        //dialogWindow.setWindowAnimations(R.style.dialogstyle); // 添加动画
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width = (int) getResources().getDisplayMetrics().widthPixels; // 宽度
        root.measure(0, 0);
        lp.height = root.getMeasuredHeight();

        //lp.alpha = 9f; // 透明度，加了之后就只有白色底面了
        dialogWindow.setAttributes(lp);
        mDialog.show();

        //设置设置页面的按钮
        //继续
        root.findViewById(R.id.continueButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(5);
                mDialog.dismiss();
                changePause();
            }
        });

        //重新开始
        root.findViewById(R.id.restartButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(10);
                saveData();
                Intent intent = new Intent(GameActivity.this,GameActivity.class);
                startActivity(intent);
                mDialog.dismiss();
                finish();
            }
        });

        //退出
        root.findViewById(R.id.quitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(9);
                saveData();
                Intent intent = new Intent(GameActivity.this,MainActivity.class);
                startActivity(intent);
                mDialog.dismiss();
                finish();
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
                    while(true && !isOver){
                        try {//创建线程后等个500ms
                            sleep(curSpeed);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //下落之前判断是否暂停
                        if(isPause || isOver) {
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

                        //因为敌人死亡与生成往往在方块落到底之后，所以在之后判断敌人状态完全合理
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
                        if(isPause || isOver) {//如果正在暂停
                            continue;//跳过这次循环（不予攻击），缺点是能通过暂停躲过攻击，以后再说
                        }

                        try {
                            sleep(curEnemy.interval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if(isPause || isOver) {//如果正在暂停
                            continue;//跳过这次循环（不予攻击），缺点是能通过暂停躲过攻击，以后再说
                        }

                        //敌人进攻
                        curEnemy.attack();
                        isAttack = true;
                        playSound(7);
                        maps = curEnemy.getMaps();
                        curTetro.bottom = curEnemy.target - 1;
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
                        }else if (curEnemy.type == 4){
                            try {
                                sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            curTetro.bottom = 19;
                            curEnemy.target = 20;
                        }
                        isAttack = false;
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
        //初始化敌人数
        defeatNum.setText("Defeated:"+defeated);
        //设置用户姓名
        playerName.setText(playerNamePre);
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

        borderPaint=new Paint();
        borderPaint.setARGB(225,79,165,213);//辅助线颜色
        borderPaint.setStrokeWidth((float) 5.0);//线宽
        borderPaint.setAntiAlias(true);//抗锯齿

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
                //边框
                canvas.drawLine(0,0,10 * boxSize, 0, borderPaint);
                canvas.drawLine(0,0,0, 20 * boxSize, borderPaint);
                canvas.drawLine(10 * boxSize,20 * boxSize,10 * boxSize, 0, borderPaint);
                canvas.drawLine(10 * boxSize,20 * boxSize,0, 20 * boxSize, borderPaint);


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


                canvas.drawRect(//画矩形->存在boxs中的box类，有则在相应的xy位置画个矩形
                        0 * boxSize,
                        curEnemy.target * boxSize,
                        10 * boxSize,
                        curEnemy.target * boxSize + boxSize, blockPaint);
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
                            maps[i][y] = maps[i][y - 1];
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
            playSound(6);
            if(isTspin) goal = 800 * plus;
            else goal = 100 * plus;
        }
        else if(counter == 2) {
            playSound(6);
            if(isTspin) goal = 1200 * plus;
            else goal = 300 * plus;
        }
        else if(counter == 3) {
            playSound(6);
            goal = 500 * plus;
        }
        else if(counter == 4) {
            playSound(6);
            goal = 800 * plus;
        }

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


    private void saveData() {//游戏结束时存档
        gameData playerData = new gameData(playerNamePre, score, defeated);
        String fileName = "mydb.txt";
        FileOutputStream fos = null;
        Gson gson = new Gson();
        String jsonUser = gson.toJson(playerData)+";";

        try {
            fos = openFileOutput(fileName, MODE_APPEND);
            fos.write(jsonUser.getBytes());

            Log.d("成功保存数据","yes");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

