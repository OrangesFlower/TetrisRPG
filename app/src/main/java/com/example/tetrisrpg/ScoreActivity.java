package com.example.tetrisrpg;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lingber.mycontrol.datagridview.DataGridView;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ScoreActivity extends AppCompatActivity {
    ArrayList<gameData> list = new ArrayList<>();
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        DataGridView mDataGridView = findViewById(R.id.datagridview);
        ImageButton backButton = findViewById(R.id.backButton);

        readData();

        mDataGridView.setColunms(3);
        mDataGridView.setHeaderContentByStringId(new int[]{R.string.str_name, R.string.str_score
                , R.string.defeated});
        mDataGridView.setFieldNames(new String[]{"name","score","defeated"});
        mDataGridView.setColunmWeight(new float[]{1,1,1});
        mDataGridView.setCellContentView(new Class[]{TextView.class, TextView.class, TextView.class});
        mDataGridView.setFlipOverEnable(true, 9, getFragmentManager());
        mDataGridView.setDataSource(list);
        mDataGridView.setRowHeight(100); // 设置行高
        mDataGridView.setHeaderHeight(100);  // 设置表头高度
        mDataGridView.setSortIsEnabled(new int[]{2} , true);
        mDataGridView.setFlipOverEnable(false, 6, null);
        mDataGridView.setSlidable(true);
        mDataGridView.initDataGridView();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ScoreActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(ScoreActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else {// 如果不是back键正常响应
            return super.onKeyDown(keyCode, event);
        }
    }

    private void readData() {
        String fileName = "mydb.txt";
        String gdJson = "";
        FileInputStream fis = null;

        try {
            fis = openFileInput(fileName); //输入流
            byte[] buffer = new byte[fis.available()]; //读取的缓存
            fis.read(buffer);
            gdJson = new String(buffer);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        String[] arrString = gdJson.split(";");

        for (String splitString : arrString) {
            Log.d("读出来的数据", splitString);
            gameData gd2 = gson.fromJson(splitString, gameData.class);
            //Log.d("test---", "-json转类-" + "\n姓名: " + gd2.getName() + "\n分数:" + gd2.getScore() + "\n杀敌数:" + gd2.getDefeated());
            list.add(gd2);
        }
    }


}