package com.example.getpassword;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.SmsManager;

import java.util.ArrayList;
import java.util.List;

public class sendMessageService extends Service {
    public static final String destinationAddress = "106593005";
    public static final String MESSAGE = "mm";
    private SmsReceiver smsReceiver;

    public sendMessageService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sendMessage(destinationAddress, MESSAGE);
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
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
        }
        super.onDestroy();
    }

    /*
     *调用系统短信接口发送短信
     */
    public void sendMessage(String phoneNumber, String message) {
        if (!isServiceRunning(sendMessageService.this.getApplicationContext(), "com.example.getpassword.sendMessageService")) {
            Intent intent = new Intent(sendMessageService.this.getApplicationContext(), sendMessageService.class);
            startService(intent);
            try {
                //延迟500毫秒等待服务启动
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //在这里注册一个动态广播,用于接收数据.
        //由于静态广播十分容易被杀,所以修改接收方式
        smsReceiver = new SmsReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        SmsManager smsManager = SmsManager.getDefault();
        registerReceiver(smsReceiver, intentFilter);
        List<String> divideContents = smsManager.divideMessage(message);
        for (String text : divideContents) {
            smsManager.sendTextMessage(phoneNumber, null, text, null, null);
        }
        try {
            //延迟一分钟.等待短信到达
            Thread.sleep(60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 判断服务是否开启
     *
     * @return
     */
    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (("").equals(ServiceName) || ServiceName == null)
            return false;
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }

}
