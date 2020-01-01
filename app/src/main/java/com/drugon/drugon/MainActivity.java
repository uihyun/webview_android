package com.drugon.drugon;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

/**
 * Created by Uihyun on 2019. 6. 10.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActiviry";
    private static final String NOTIFICATION_CHANNEL_ID = "10001";

    private WebView mWebView;
    private final Handler handler = new Handler();
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    private SharedPreferences prefs;
    private Intent notiIntent;
    private String token;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (WebView) findViewById(R.id.activity_main_webview);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setAllowFileAccess(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSaveFormData(false);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setSupportMultipleWindows(false);

        mWebView.setWebViewClient(new WebViewClient() { // 새창이 뜨지 않게 하기 위함
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });

        mWebView.loadUrl("https://drugon-seller.firebaseapp.com/app/home/default");

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String userId = prefs.getString("userId", null);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        token = task.getResult().getToken();
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("token", token);
                        editor.apply();
                        editor.commit();
                        updateToken(userId);

                        // Log and toast
                        String msg = "InstanceID Token: " + token;
                        Log.d(TAG, msg);
//                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });


//        JavaScriptInterface javaScriptInterface = new JavaScriptInterface();
//        if (userId != null) {
//            javaScriptInterface.checkNotification(userId);
//        } else {
//            mWebView.addJavascriptInterface(javaScriptInterface, "android");
//        }
    }

    public void updateToken(String userId) {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        if (userId != null) {
            mDatabaseReference.child("account").child(userId).child("deviceToken").setValue(token);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

//        if (mChildEventListener != null) {
//            mDatabaseReference.removeEventListener(mChildEventListener);
//        }
    }

    private class JavaScriptInterface {
        @JavascriptInterface
        public void onData(final String userId) {
            handler.post(new Runnable() {
                public void run() {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("userId", userId);
                    editor.apply();
                    editor.commit();
                    updateToken(userId);
//                    checkNotification(userId);
                }
            });
        }

        @JavascriptInterface
        public void outData() {
            handler.post(new Runnable() {
                public void run() {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove("userId");
                    editor.apply();
                    editor.commit();
                }
            });
        }

        public void checkNotification(String userId) {
            mDatabaseReference = FirebaseDatabase.getInstance().getReference("/notification/" + userId + "/");
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                    String value = dataSnapshot.getValue().toString();
                    if (value.contains("read=true"))
                        return;
                    String type = value.substring(value.indexOf(", type=") + 7, value.lastIndexOf("}"));
//                    createNotification("새 알림", type);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mDatabaseReference.limitToLast(1).addChildEventListener(mChildEventListener);
        }
    }

    public void createNotification(String title, String content) {
        NotificationManager notificationManager;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("notificationId", ""); //전달할 값
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground)) //BitMap 이미지 요구
                .setContentTitle(title)
                .setContentText(content)
                // 더 많은 내용이라서 일부만 보여줘야 하는 경우 아래 주석을 제거하면 setContentText에 있는 문자열 대신 아래 문자열을 보여줌
                //.setStyle(new NotificationCompat.BigTextStyle().bigText("더 많은 내용을 보여줘야 하는 경우..."))
                // icon
                //.setSmallIcon()
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 사용자가 노티피케이션을 탭시 ResultActivity로 이동하도록 설정
                .setAutoCancel(true);

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            CharSequence channelName = "노티페케이션 채널";
            String description = "오레오 이상을 위한 것임";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
            channel.setDescription(description);

            // 노티피케이션 채널을 시스템에 등록
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        } else {
            builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남
        }

        assert notificationManager != null;
        notificationManager.notify(1234, builder.build()); // 고유숫자로 노티피케이션 동작시킴
    }

}
