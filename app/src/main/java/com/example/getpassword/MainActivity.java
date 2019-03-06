package com.example.getpassword;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String letPasswordBroadcast = "action.getPasswordBroadcast";//让密码广播！

    private Button send;
    private Button read;
    private Button get;
    private Switch mSwitch;
    public TextView tv;

    private String TAG = "MainActivity";

    public static final String destinationAddress = "106593005";
    public static final String MESSAGE = "mm";
    private final String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    private final String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";

    private sendReceiver mSendReceiver;//有没有发送成功
    private oppositeReceiver mOppositeReceiver;//对方有没有接收到
    private myReceiver mMyReceiver;

    private static PendingIntent sentPI, deliverPI;//,receiverPI;

    private List<String> permissionList;

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        mDrawerLayout = findViewById(R.id.drawer_layout);
        send = findViewById(R.id.send);
        read = findViewById(R.id.read);
        get = findViewById(R.id.get);
        tv = findViewById(R.id.tv);
        mSwitch = findViewById(R.id.switchToSend);
        send.setOnClickListener(this);
        read.setOnClickListener(this);
        get.setOnClickListener(this);
        //注册发送短信的状态 的广播接收器

        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        sentPI = PendingIntent.getBroadcast(MainActivity.this, 0, sentIntent, 0);
        mSendReceiver = new sendReceiver();
        registerReceiver(mSendReceiver, new IntentFilter(SENT_SMS_ACTION));
        //注册对方接收到短信状态的 广播接收器
        Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
        deliverPI = PendingIntent.getBroadcast(this, 0, deliverIntent, 0);
        mOppositeReceiver = new oppositeReceiver();
        registerReceiver(mOppositeReceiver, new IntentFilter(DELIVERED_SMS_ACTION));
        //注册incomingSMSReceiver
        mMyReceiver = new myReceiver();//用于接受返回的密码等内容
        registerReceiver(mMyReceiver, new IntentFilter(letPasswordBroadcast));

        //权限表
        permissionList = new ArrayList<>();
        askPermissions();//        运行时权限 (动态权限申请)
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);//list-->String
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }

        Intent rebootIntent = getIntent();
        String name = rebootIntent.getStringExtra("name");
        if (name != null && name.equals("rebootIntent")) {//短路机制，前一个条件不满足自动跳过if
            Log.d(TAG, "收到重启广播 的 命令并已重启，将发送短信");
            sendMessage(destinationAddress, MESSAGE);
        } else {
            Log.d(TAG, "没有收到rebootIntent");
        }
        /*处理switch功能*/
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {//switch 改变时记录状态
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_MULTI_PROCESS).edit();
                editor.putBoolean("ContinueSend", isChecked);//存储选中的状态。
                editor.apply();//这句又忘了。。牢记牢记
                Log.d(TAG, "weimeng 保存状态continueSend=" + isChecked);
            }
        });

        SharedPreferences pref = getSharedPreferences("data", MODE_MULTI_PROCESS);
        boolean flag = pref.getBoolean("ContinueSend", true);//默认重复发送
        //为了保存switch的状态
        mSwitch.setChecked(flag);

        //显示之前保存的密码和那个时间
        showText();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                Intent intent = new Intent(MainActivity.this, sendMessageService.class);
                startService(intent);
                showToast("发送中..");
                break;
            case R.id.read:
                openLoginDisplay();
                break;
            case R.id.get:
                showText();
                break;
            default:
                break;
        }

    }


    private void openLoginDisplay() {
        SharedPreferences data = getSharedPreferences("data", MODE_MULTI_PROCESS);
        final SharedPreferences.Editor editor = data.edit();
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.login_layout, null);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        final EditText SCKEY_input =  view.findViewById(R.id.sckey_input);
        SCKEY_input.setText(data.getString("SCKEY", ""));
        final Switch aSwitch = view.findViewById(R.id.server_open);
        aSwitch.setChecked(data.getBoolean("server_open", true));
        Button button = view.findViewById(R.id.login_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String trim = SCKEY_input.getText().toString().trim();
                boolean checked = aSwitch.isChecked();
//                "https://api.myjson.com/bins/15u89e"
                editor.putString("SCKEY", trim);
                editor.putBoolean("server_open", checked);
                editor.commit();
                dialog.dismiss();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_MULTI_PROCESS).edit();


        switch (item.getItemId()) {
            case android.R.id.home:
                //这个键永远叫 home 内置的
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;

            case R.id.explain:
                tv.setText(R.string.explainText);
            default:
                break;
        }
        editor.apply();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        invalidateOptionsMenu();//toolbar要显示选项按钮
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.set_menu, menu);
        return true;
    }


    public void askPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.SEND_SMS);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.RECEIVE_SMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {//多项危险权限动态申请的代码
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            showToast("拒绝权限将无法使用程序");
                            finish();
                            return;
                        }
                    }
                } else {
                    showToast("发生未知错误");
                    finish();
                }
                break;
            default:
                break;
        }
    }

    /*
     *调用系统短信接口发送短信
     */
    public static void sendMessage(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        List<String> divideContents = smsManager.divideMessage(message);
        for (String text : divideContents) {
            smsManager.sendTextMessage(phoneNumber, null, text, MainActivity.sentPI, MainActivity.deliverPI);
        }
    }


    private void showToast(String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    class sendReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    showToast("发送成功");
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    showToast("发送失败");
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    break;
                default:
                    break;
            }
        }
    }

    class oppositeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            showToast("收件人接收成功");
        }
    }

    /*
     *调用系统短信接口发送短信
     */
    class myReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String password = intent.getStringExtra("password");
            String time = intent.getStringExtra("time");
            SharedPreferences.Editor editor = getSharedPreferences("data", MODE_MULTI_PROCESS).edit();
            editor.putString("password", password);
            editor.putString("time", time);
            editor.apply();
            showText();
        }
    }


    private void showText() {
        SharedPreferences pref = getSharedPreferences("data", MODE_MULTI_PROCESS);
        String password = pref.getString("password", "没有获取过");
        String time = pref.getString("time", "没有获取过");
        tv.setText("密码: " + password + "\n上次更新时间：" + time);
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mOppositeReceiver);
        unregisterReceiver(mSendReceiver);
        unregisterReceiver(mMyReceiver);
        //取消注册
        super.onDestroy();
    }
}
