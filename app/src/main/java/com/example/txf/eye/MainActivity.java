package com.example.txf.eye;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static Activity sInstance = null;
    Set<String> permissionSet = new HashSet<>();
    String IMEI = "";
    TextView IMEI_Text;

    private MyService.PhoneBinder phoneBinder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("绑定","成功");

            phoneBinder = (MyService.PhoneBinder) service;
            IMEI = phoneBinder.getIMEI();
            IMEI_Text.setText(IMEI);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        FragmentManager fm = getSupportFragmentManager();
        int count = fm.getBackStackEntryCount();

        // 判断按下的按钮是不是 BACK 键，以及当前是不是栈的最后一个
        // 如果是，使用 HOME 键替换 BACK 键，并返回true
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(count <= 0) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sInstance = this;

        //开启运行时权限
        startPermission();

        //注册IMEI的显示框
        IMEI_Text = (TextView) findViewById(R.id.IMEI_text);


        //TODO:注册地图使用的窗口

        //TODO:开启服务
        Intent ServerIntent = new Intent(this,MyService.class);
        Log.d("准备","开启服务");

        String[] per = permissionSet.toArray(new String[permissionSet.size()]);
        ServerIntent.putExtra("permission",per);

        bindService(ServerIntent,connection,BIND_AUTO_CREATE);
        startService(ServerIntent);

        Log.d("设置IMEI","开始");


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        sInstance = null;
        Log.d("结束","结束了");
    }

    /**
    *设置运行时权限
     **/
    private void startPermission(){
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE ) !=
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS ) !=
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS ) !=
                        PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CONTACTS,Manifest.permission.READ_SMS},1);
        }
        else{
            permissionSet.add(new String("手机硬件信息权限"));
            permissionSet.add(new String("联系人权限"));
            permissionSet.add(new String("短信权限"));
        }


        permissionSet.add(new String("网络连接权限"));



    }

    //permissions和grantResults的长度一样，为申请的权限个数，顺序和申请的顺序一样
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){ ;
                    permissionSet.add(new String("手机硬件信息权限"));
                }
                if(grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED){ ;
                    permissionSet.add(new String("联系人权限"));
                }
                if(grantResults.length > 0 && grantResults[2] == PackageManager.PERMISSION_GRANTED){ ;
                    permissionSet.add(new String("短信权限"));
                }

            default:
                break;

        }
    }
}
