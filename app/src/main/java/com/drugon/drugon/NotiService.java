package com.drugon.drugon;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class NotiService extends Service {
    private static final String TAG = "NotiService";
    private static final String NOTIFICATION_CHANNEL_ID = "10001";

    private NotificationManager notificationManager;
    private ServiceThread thread;
    private ServiceHandler myServiceHandler;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myServiceHandler = new ServiceHandler(intent);
        thread = new ServiceThread(myServiceHandler);
        thread.start();
        return START_REDELIVER_INTENT;
    }

    //서비스가 종료될 때 할 작업
    public void onDestroy() {
        thread.stopForever();
        thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
    }

    class ServiceHandler extends Handler {
        private Intent intent;
        private String content = "";

        public ServiceHandler(Intent intent) {
            this.intent = intent;
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            content = intent.getStringExtra("content");
            if (content == null)
                return;
            if (content.length() == 0)
                return;

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            Intent notificationIntent = new Intent(NotiService.this, MainActivity.class);
            notificationIntent.putExtra("notificationId", ""); //전달할 값
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(NotiService.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(NotiService.this, NOTIFICATION_CHANNEL_ID)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground)) //BitMap 이미지 요구
                    .setContentTitle("새 알림")
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

            content = "";
        }
    }
}
