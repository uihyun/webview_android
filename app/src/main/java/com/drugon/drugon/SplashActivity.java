package com.drugon.drugon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Uihyun on 2018. 5. 27.
 */
public class SplashActivity extends AppCompatActivity {

    public static final int SEARCHED_LIST_SIZE = 10;

    public static final List<String> searchedNameList = new ArrayList<>();
    public static final List<String> searchedIndgList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView imageView = (ImageView) findViewById(R.id.splash);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.splash));

        Handler hd = new Handler();
        hd.postDelayed(new splashHandler(), 2000); // 2초 후에 Handler 실행

        // favorite
        SharedPreferences prefs = getSharedPreferences("favorite", MODE_PRIVATE);

        // recently searched text - name
        prefs = getSharedPreferences("searchedNameList", MODE_PRIVATE);

        if (searchedNameList.size() > 0)
            searchedNameList.clear();
        for (int i = SEARCHED_LIST_SIZE - 1; i > -1; i--) {
            String text = prefs.getString("name." + i, null);
            if (text != null) {
                searchedNameList.add(text);
            }
        }

        // recently searched text - indg
        prefs = getSharedPreferences("searchedIndgList", MODE_PRIVATE);

        if (searchedIndgList.size() > 0)
            searchedIndgList.clear();
        for (int i = SEARCHED_LIST_SIZE - 1; i > -1; i--) {
            String text = prefs.getString("indg." + i, null);
            if (text != null) {
                searchedIndgList.add(text);
            }
        }
    }

    private class splashHandler implements Runnable {
        public void run() {
            startActivity(new Intent(getApplication(), MainActivity.class)); // 로딩이 끝난후 이동할 Activity
            SplashActivity.this.finish(); // 로딩페이지 Activity Stack에서 제거
        }
    }
}
