package com.example.tetrisrpg;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    Button button;
    Button button2;
    Button button3;
    MediaPlayer menuPlayer;

    //按钮音效池
    private SoundPool mSoundPool ;
    private HashMap<Integer,Integer> mSoundMap;//声音编号映射

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSound();

        button = findViewById(R.id.button);//start
        button2 = findViewById(R.id.button2);//setting
        button3 = findViewById(R.id.button3);//score

        //获得用户设置
        SharedPreferences sp =
                PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        Boolean menuBGM = sp.getBoolean("menuBGM", true);

        //BGM
        menuPlayer = MediaPlayer.create(this, R.raw.awake_circle_of_the_moon);

        if(!menuPlayer.isPlaying() && menuBGM){
            System.out.println("主界面音乐播了");
            menuPlayer.start();
            menuPlayer.setLooping(true);
        }


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(1);
                Intent intent = new Intent(MainActivity.this,GameActivity.class);
                startActivity(intent);
                finish();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(2);
                Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(intent);
                finish();
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(2);
                Intent intent = new Intent(MainActivity.this,ScoreActivity.class);
                startActivity(intent);

            }
        });

    }

    private void initSound() {
        mSoundMap = new HashMap<>();
        mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC,100);
        loadSound(1,R.raw.menu_start);
        loadSound(2,R.raw.menu_others);
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
        System.out.println("主界面音乐停止");
        menuPlayer.stop();
        menuPlayer.release();
        menuPlayer = null;
        super.onDestroy();
    }
}