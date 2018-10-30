package com.example.txf.eye;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.LoginFilter;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyService extends Service {
    private PhoneBinder mBinder = new PhoneBinder();

    String IMEI = "";

    public MyService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("输出","元素1");


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("输出","元素2");
        String[] permission = intent.getStringArrayExtra("permission");
        final List<String> perList = Arrays.asList(permission);

        //  调试：输出权限信息
        for (String str : permission){
            Log.d("permisson",str);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                //查询IMEI,设置IMEI的显示
                if(perList.contains("手机硬件信息权限")){
                    Context context = getApplicationContext();
                    TelephonyManager tm = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
                    IMEI = tm.getDeviceId();
                    Log.d("IMEI",IMEI);
                }

                //查询短信敏感字

                //查询联系人敏感字

                //查询所有开启的权限


                //查询不允许使用的应用


                //查询GPS坐标

                //Socket编程，向服务器传输信息

                stopSelf();
            }
        }).start();


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopSelf();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    private void getIMEI(){
        //使用静态变量把活动本身传递到服务
        Activity activity = MainActivity.sInstance;
        Context context = getApplicationContext();
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
        IMEI = tm.getDeviceId();

    }

    class PhoneBinder extends Binder{
        public String getIMEI(){
            return IMEI;
        }

    }

}
