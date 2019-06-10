package com.drugon.drugon;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/**
 * Created by Uihyun on 2019. 6. 10.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_splash);
        setContentView(R.layout.activity_splash_full);

        ImageView imageView = (ImageView) findViewById(R.id.splash);
//        imageView.setImageDrawable(getResources().getDrawable(R.drawable.splash));
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.splash_1_a));

        Handler hd = new Handler();
        hd.postDelayed(new splashHandler(), 2000); // 2초 후에 Handler 실행
    }

    private class splashHandler implements Runnable {
        public void run() {
            startActivity(new Intent(getApplication(), MainActivity.class)); // 로딩이 끝난후 이동할 Activity
            SplashActivity.this.finish(); // 로딩페이지 Activity Stack에서 제거
        }
    }
}
