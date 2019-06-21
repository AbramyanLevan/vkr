package com.example.myapplication;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;

import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class Watcher extends Service {
    List<Table> tableList;
    boolean active = true;
    private int counter = 0;
    TimerCounter tc;
    Context mBaseConext;
    String[] args = new String[]{"com.vkontakte.android", "com.instagram.android", "com.facebook.android", "org.telegram.messenger",
            "com.viber.voip", "ru.ok.android", "com.google.android.youtube", "com.whatsapp", "com.snapchat.android", "tv.twitch.android.app"
            , "com.discord", "com.skype.raider", "com.tumblr", "com.twitter.android", "com.pinterest"};

    Watcher() {}
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        tc = new TimerCounter();
        int NOTIFICATION_ID = (int) (System.currentTimeMillis()%10000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, new Notification.Builder(this).build());
        }
    }

    @Override
    public void onDestroy() {
        synchronized (getApplicationContext()) {
            active = false;
        }
        super.onDestroy();

//        Intent broadcastIntent = new Intent("com.example.action.CAT");
//        broadcastIntent.setClass(this, SensorRestartBroadcastReceiver.class);
//        sendBroadcast(broadcastIntent);
        tc.stopTimerTask();
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent intent = new Intent(getApplicationContext(), Watcher.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000, pendingIntent);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void sendNotification(String str) {
        Log.i("in timer", "NOTIFICATION");
        Context mContext = this;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext.getApplicationContext(), "notify_001");
        Intent ii = new Intent(mContext.getApplicationContext(), Watcher.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, ii, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText("Слышьщь закрой");
        bigText.setBigContentTitle("Закрой блять "+str);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle("Your Title");
        mBuilder.setContentText("Your text");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);

        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        mNotificationManager.notify(0, mBuilder.build());


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Gson gson = new Gson();
        mBaseConext = getBaseContext();
        long DayInMillis = 24 * 3600 * 1000;
        tc.startTimer(counter);
        try {
            tableList = gson.fromJson(intent.getStringExtra("inf"), new TypeToken<List<Table>>(){}.getType());
        } catch (Exception e) {
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        Thread thread = new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                while(true) {
                    try {
                        synchronized (getApplicationContext()) {
                            if (!active)
                                break;
                        }
                        UsageStatsManager usageStatsManager;
                        usageStatsManager = (UsageStatsManager) mBaseConext.getSystemService(mBaseConext.USAGE_STATS_SERVICE);
                        ZoneId z = ZoneId.of("Europe/Moscow");
                        ZonedDateTime zdt = ZonedDateTime.now(z);
                        LocalDate today = zdt.toLocalDate();
                        ZonedDateTime zdtTodayStart = today.atStartOfDay(z);
                        ZonedDateTime zdtTodayEnd = today.atStartOfDay(z).plusDays(1);
                        long startMillis = zdtTodayStart.toEpochSecond() * 1000;
                        long endMillis = zdtTodayEnd.toEpochSecond() * 1000;

                        Map<String, UsageStats> queryUsageStats = usageStatsManager.queryAndAggregateUsageStats(startMillis, endMillis);
                        List<String> list = Arrays.asList(args);
                        List<String> appsToShow = new ArrayList<>(list);
                        queryUsageStats.forEach((String key, UsageStats usage) ->
                        {
                            if (appsToShow.contains(key)) {
                                long t = usage.getTotalTimeInForeground();
                                for (Table table: tableList) {
                                    if (table.getAppPackage().equals(key)) {
                                        if (t > table.getAppLimit()) {
                                            sendNotification(table.getAppName());

                                            table.setAppLimit(t + 2 * 60);
                                        }
                                    }
                                }
                            }
                        });
                        sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }
}
