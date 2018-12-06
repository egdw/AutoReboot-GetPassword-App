package com.example.getpassword.broad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.getpassword.service.MonitorService;


/**
 * Created by hdy on 2017/9/10.
 */

public class BootBroadcastReciver extends BroadcastReceiver {
    private MonitorService monitorService;
    private boolean flag;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, MonitorService.class);
        context.startService(serviceIntent);
    }
}
