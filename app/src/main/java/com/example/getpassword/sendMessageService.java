package com.example.getpassword;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class sendMessageService extends Service {
    public sendMessageService() {
    }

    private String TAG = "sendMessageService";
    public static Boolean continueSend;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "weimeng 启动了服务发送短信");
        continueSend = getSharedPreferences("data", MODE_PRIVATE).
                getBoolean("ContinueSend", true);
//                sharedPreference在一个app里是共享的
        Log.d(TAG, "weimeng 布尔变量continueSend = " + continueSend);
        //判断是否继续发送
        if (continueSend) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //MainActivity.sendMessage(MainActivity.destinationAddress,MainActivity.MESSAGE);
                    //改用广播，上面那个方法牵扯到MainActivity的好多属性，不能直接来用
                    Intent mybroadcastIntent = new Intent();
                    mybroadcastIntent.setAction(MainActivity.rebootBroadcast);
                    //1 包名 2 接收器类名
                    mybroadcastIntent.setComponent(new ComponentName("com.example.getpassword",
                            "com.example.getpassword.rebootBroadcastReceiver"));//android8.0要设置的
                    sendBroadcast(mybroadcastIntent);//发送一个广播
                }
            }).start();
            final SharedPreferences data = getSharedPreferences("data", Context.MODE_MULTI_PROCESS);
            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            //默认的时间
            int aDayMore = 29 * 60 * 60 * 1000;//一天又多个小时
//            int aMinute = 60*1000;//测试用
            long triggerAtTime = System.currentTimeMillis() + aDayMore;
            //到点自动更新闪讯密码
            String update_time = data.getString("next_update_time", String.valueOf(new Date(System.currentTimeMillis() + triggerAtTime)));
            Log.i("获取到的更新时间", update_time);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            triggerAtTime = 0;
            try {
                triggerAtTime = format.parse(update_time).getTime() + (60 * 1000 * 5);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Intent i = new Intent(this, sendMessageService.class);//这里搞错了，弄了两天
            PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
            manager.set(AlarmManager.RTC_WAKEUP, triggerAtTime, pi);
            Log.d(TAG, "onStartCommand: 发送成功");
        }

        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
