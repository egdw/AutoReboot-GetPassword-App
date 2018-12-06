package com.example.getpassword;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetPasswordService extends IntentService {
    private static final String ACTION_FOO = "com.example.getpassword.action.FOO";
    private static final String ACTION_BAZ = "com.example.getpassword.action.BAZ";

    private static final String EXTRA_PARAM1 = "com.example.getpassword.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.getpassword.extra.PARAM2";

    public GetPasswordService() {
        super("GetPasswordService");
    }

    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, GetPasswordService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, GetPasswordService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }


    //乱七八糟一堆，有用的就下面这个
    String body = "", time = "";
    String TAG = "GetPassWordService";

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "weimeng 得到密码了");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
        body = intent.getStringExtra("body");
        time = intent.getStringExtra("time");
        Log.d(TAG, "weimeng Get body :" + body);
        final String password = GetUtils.getPassword(body);
        final String mytime = GetUtils.getTime(body);
        Log.d(TAG, "weimeng Get time" + time);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String regEx = "[0-9]{6}";//10086
                Pattern pattern = Pattern.compile(regEx);
                Matcher matcher = pattern.matcher(body);
                final String send;
                if (matcher.find()) {
                    send = matcher.group();
                } else {
                    send = "没有找到匹配数字";
                }

                Intent sendIntent = new Intent();
                sendIntent.setAction(MainActivity.letPasswordBroadcast);//这里广播的 action 一定要记住，不要搞混了
                sendIntent.putExtra("password", send);
                sendIntent.putExtra("time", time);

                if (send != null && time != null) {
                    //说明解析出来的数据是正确的.
                    //提交到mjson.com进行修改请求操作
                    //创建okHttpClient对象
                    OkHttpClient mOkHttpClient = new OkHttpClient();
                    //创建一个Request
                    //这里需要进行修改.
                    final SharedPreferences data = getSharedPreferences("data", Context.MODE_MULTI_PROCESS);
                    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(new Password(password, mytime)));
                    final Request request = new Request.Builder()
                            .url("https://api.myjson.com/bins/15u89e")
                            .put(body)
                            .build();
                    //new call
                    Call call = mOkHttpClient.newCall(request);
                    //请求加入调度
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            //如果请求失败了...
                            Log.i("请求失败", e + "" + request);
                        }

                        @Override
                        public void onResponse(final Response response) throws IOException {
                            Log.i("请求成功", response + "");
                            SharedPreferences.Editor edit = data.edit();
                            edit.putString("next_update_time", mytime);
                            edit.putString("new_password", send);
                            edit.commit();
                        }
                    });
                }


                //1 包名 2 接收器类名
//            sendIntent.setComponent(new ComponentName("com.example.getpassword",
//                        "com.example.getpassword.MainActivity.myReceiver"));//android8.0要设置的
                sendBroadcast(sendIntent);//轮了一圈再发回 MainActivity
            }
        }).start();


    }

    private void handleActionFoo(String param1, String param2) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleActionBaz(String param1, String param2) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
