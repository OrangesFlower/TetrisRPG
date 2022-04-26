package com.example.tetrisrpg;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.widget.TextView;

import androidx.preference.PreferenceManager;


public class SettingActivity extends PreferenceActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_preference);

        SharedPreferences sp =
                PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        Boolean menuBGM = sp.getBoolean("menuBGM", true);
        if(menuBGM) System.out.println("yyy");

        String playerName = sp.getString("PlayerName", "Player");
        System.out.println(playerName);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(SettingActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else {// 如果不是back键正常响应
            return super.onKeyDown(keyCode, event);
        }
    }
}