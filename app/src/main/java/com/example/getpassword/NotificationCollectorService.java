package com.example.getpassword;

import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * 在这里可以监听数据
 *
 * @author egdw
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationCollectorService extends NotificationListenerService {

    //当接受到新的通知信息时就会调用该函数
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String text = sbn.getNotification().extras.get("android.text").toString();
        if (text != null && text.contains("上网密码是")) {
            Log.i("接收到信息","接收到短信.开始解析");
            //106593005
            //说明是闪讯的内容
            String password = GetUtils.getPassword(text);
            String time = GetUtils.getTime(text);
            Log.i("密码",password);
            Log.i("时间",time);

        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }
}