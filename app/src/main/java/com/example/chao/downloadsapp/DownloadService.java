package com.example.chao.downloadsapp;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.chao.downloadsapp.downloads.HttpDownloads;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by chao on 9/1/16.
 */
public class DownloadService extends Service {

    public class MyBinder extends Binder{
        public DownloadService getService(){
            return DownloadService.this;
        }
    }

    public static class ServiceHandler extends Handler{
        private Context context;
        private MyRunnable myRunnable;
        public ServiceHandler(Context context,MyRunnable myRunnable) {
            this.context=context;
            this.myRunnable=myRunnable;
        }

        @Override
        public void handleMessage(Message msg) {
            if(msg.what==0){
                Log.d("MainActivity","message");
                HttpDownloads.itemInfo item= (HttpDownloads.itemInfo) msg.obj;
                Intent intent=new Intent();
                intent.putExtra("downloadinfo",item);
                intent.setAction(MainActivity.FILTERACTION);
                context.sendBroadcast(intent);
                if((!item.isPause())&&(item.getFinish()==0)){
                   postDelayed(new FreshRunnable(myRunnable), 1000);
                }

            }

        }

        class FreshRunnable implements Runnable{
            private MyRunnable myRunnable;
            public FreshRunnable(MyRunnable myRunnable) {
                this.myRunnable=myRunnable;
            }

            @Override
            public void run() {
              //  myRunnable.sendDownloadInfo();
                Log.d("MainActivity","run");
                Message msg=myRunnable.runHandler.obtainMessage();
                msg.what=1;
                msg.sendToTarget();

            }
        }
    }
    public ServiceHandler mHandler;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler=new ServiceHandler(this,myRunnable);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void setPause(){
       // myRunnable.pause();
        Message msg=myRunnable.runHandler.obtainMessage();
        msg.what=2;
        msg.sendToTarget();
    }



    private MyRunnable myRunnable=new MyRunnable(this);
    public void download(String url,String path,String modifydate){
        Log.d("MainActivity","开始下载");
        myRunnable.setData(url,path,modifydate);
        new Thread(myRunnable).start();
    }

    public  class MyRunnable implements Runnable {
        private Context context;
        public RunHandler runHandler;
        private HttpDownloads downloads;
        private DownloadService service;
        private String urlString;
        private String path;
        private String modifydate;

        public MyRunnable(Context context){
            this(context,null,null,null);
        }
        public MyRunnable(Context context,String url,String path,String modifydate) {
           this.context=context;
            this.urlString=url;
            this.path=path;
            this.modifydate=modifydate;
        }

        public void setData(String url,String path,String modifydate){
            this.urlString=url;
            this.path=path;
            this.modifydate=modifydate;
        }

        @Override
        public void run() {
            try {
                Looper.prepare();
                runHandler = new RunHandler(this);
                Log.d("MainActivity","loop1");


                service = (DownloadService) context;
                URL url=new URL(urlString);
                downloads= new HttpDownloads(context,url,path,modifydate);
                downloads.request();
                sendDownloadInfo();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            downloads.writeData();
                            sendDownloadInfo();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                Looper.loop();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d("MainActivity","URL错误");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("MainActivity","请求错误");
            }
        }
        public void sendDownloadInfo(){
            Message msg=service.mHandler.obtainMessage();
            msg.what=0;
            msg.obj=downloads.getInfo();
            Log.d("MainActivity","sendToTarget");
            msg.sendToTarget();
        }
        public void pause(){
            downloads.pause();
            //sendDownloadInfo();
        }

    }
    public static class  RunHandler extends Handler{
        private MyRunnable runnable;
        public RunHandler(MyRunnable runnable) {
            super();
            this.runnable=runnable;
        }

        @Override
        public void handleMessage(Message msg) {
           if (msg.what==1){
               runnable.sendDownloadInfo();
               Log.d("MainActivity","what:"+msg.what);
           }
            if(msg.what==2){
                Log.d("MainActivity","what:"+msg.what);
                runnable.pause();
            }

        }
    }



}
