package com.example.txf.eye;


import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MyAccessibility extends AccessibilityService {
    public MyAccessibility() {
    }

    @Override
    protected void onServiceConnected() {
        Log.d("服务","连接成功");
        super.onServiceConnected();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("服务","开始执行");
        return super.onStartCommand(intent, flags, startId);
    }

    private static final String TAG = "服务";


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        String eventText = "";
        Log.d("服务", "==============Start====================");
        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                eventText = "TYPE_VIEW_LONG_CLICKED";
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                eventText = "TYPE_WINDOW_STATE_CHANGED";
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                eventText = "TYPE_NOTIFICATION_STATE_CHANGED";
                break;
            case AccessibilityEvent.CONTENT_CHANGE_TYPE_SUBTREE:
                eventText = "CONTENT_CHANGE_TYPE_SUBTREE";
                break;
        }

        Log.d(TAG, eventText);
        if(eventText == "TYPE_NOTIFICATION_STATE_CHANGED"){
            List<CharSequence> texts = event.getText();
            Log.d("服务","接到通知");
            for (CharSequence text : texts){
                Log.d("服务 通知信息",text.toString());
                JSONObject jsonObject = new JSONObject();
                String app = event.getPackageName().toString();
                try {
                    jsonObject.put("软件",app);
                    jsonObject.put("内容",text.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                save(jsonObject.toString());
            }
        }
        Log.d(TAG, "=============END=====================");

    }

    @Override
    public void onInterrupt() {

    }

    private void save(String text){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
        String date = dateFormat.format(new Date());

        FileOutputStream out = null;
        BufferedWriter writer = null;

        //Log.d("服务 文件","准备写文件0");
        //Log.d("服务 文件",text);
        try {
            //Log.d("服务 文件","准备写文件");
            //out = new FileOutputStream("date.txt",true);
            out = openFileOutput(date,Context.MODE_APPEND);
            writer = new BufferedWriter(new OutputStreamWriter(out));

            //Log.d("服务 文件","打开文件");
            //out.write(text.getBytes());
            //Log.d("服务 文件","写入数据成功");
            //out.write("\r\n".getBytes());
            //out.close();
            writer.write(text);
            writer.write("\r\n");
            writer.close();

            Log.d("服务 写入 文件",text);
        }catch (IOException e ){
            e.printStackTrace();
        }
        Log.d("服务 写入 文件名",date);


    }
}

