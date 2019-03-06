package com.example.getpassword;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SmsReceiver extends BroadcastReceiver {
    private String TAG = "SmsReceiver";
    private static final String queryString = "尊敬的";
    private static final String phoneNumber = "106593005";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "weimeng onReceive: 广播接收器接收到了");
        Bundle bundle = intent.getExtras();
        SmsMessage msg;
        if (null != bundle) {
            Object[] smsObj = (Object[]) bundle.get("pdus");
            for (Object object : smsObj) {
                msg = SmsMessage.createFromPdu((byte[]) object);
                Date date = new Date(msg.getTimestampMillis());//时间
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String receiveTime = format.format(date);
                String messageBody = msg.getMessageBody();
                if (msg.getOriginatingAddress().equals(phoneNumber) &&
                        messageBody.toLowerCase().startsWith(queryString)) {
                    //是这个号码&& 是这个短信内容
                    Intent serviceIntent = new Intent(context, GetPasswordService.class);
                    serviceIntent.putExtra("body", messageBody);
                    serviceIntent.putExtra("time", receiveTime);

                    context.startService(serviceIntent);//去 GetPasswordService.class
                }
            }
        }
    }
}

