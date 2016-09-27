package com.example.chao.downloadsapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.chao.downloadsapp.downloads.DownloadSQLHelper;
import com.example.chao.downloadsapp.downloads.HttpDownloads;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private DownloadService downloadService;
    public static final String FILTERACTION="com.chao.example.download_fresh";
    private String path=null;
    private String url;
    private String modifydate;
    private boolean pause=false;
    private ArrayList<HttpDownloads.itemInfo> itemList= new ArrayList<>();
    private boolean isThreadFree=true;
    private int currentTask=0;
    private int clickPosition=0;



    ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadService=((DownloadService.MyBinder)service).getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    private MainActivityReceiver receiver;
    //private ProgressBar progressBar;
    private DownloadSQLHelper sqlHelper;
    private DownloadLIstAdapter adapter;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent=new Intent(this, DownloadService.class);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);
        findViewById(R.id.add_task).setOnClickListener(this);
        editText = (EditText) findViewById(R.id.website);
        receiver = new MainActivityReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(FILTERACTION);
        registerReceiver(receiver,filter);
        sqlHelper=new DownloadSQLHelper(this);
        ListView listView= (ListView) findViewById(R.id.download_list);

        adapter = new DownloadLIstAdapter(this, R.layout.list_item,itemList);
        listView.setAdapter(adapter);
        //listView.setItemsCanFocus(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("ClickPosition","position:"+position);
               // ImageView button= (ImageView) view.findViewById(R.id.pause_button_download);
                //button.setOnClickListener(MainActivity.this);
                clickPosition=position;
                pause();
                Log.d("ClickPosition","position:"+position);
            }
        });

    }



    public class MainActivityReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            if(TextUtils.equals(intent.getAction(),FILTERACTION)){
                HttpDownloads.itemInfo itemInfo= (HttpDownloads.itemInfo) intent.getSerializableExtra("downloadinfo");
                //int i= (int) (itemInfo.getProgress()*100/itemInfo.getLength());
                Log.d("MainActivity",itemInfo.getProgress()+" "+itemInfo.getLength());

                itemList.get(currentTask).setAllInfo(itemInfo);
                adapter.notifyDataSetChanged();
                Log.d("MainActivity","currentTask:"+currentTask);
                if(itemInfo.getFinish()==1){
                    isThreadFree=true;
                }


            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add_task:
                Log.d("MainActivity","开始下载");
                String urlstring= String.valueOf(editText.getText());
                if(urlstring.matches("http://.*")){
                    HttpDownloads.itemInfo item=new HttpDownloads.itemInfo();
                    item.setUrl(urlstring);
                    item.setFileName(urlstring);
                    item.setWaitState(true);
                    item.setPause(true);
                    itemList.add(item);
                    if(isThreadFree){
                        downloadService.download(item.getUrl(),null,null);
                        item.setPause(false);
                        isThreadFree=false;
                        item.setWaitState(false);
                    }
                    adapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(this, "下载地址不正确", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }



    public void pause(){
        HttpDownloads.itemInfo info=itemList.get(clickPosition);

        if(currentTask==clickPosition){
            if(!itemList.get(currentTask).isPause()){
                downloadService.setPause();
                itemList.get(currentTask).setPause(true);
            }else{
                downloadService.download(info.getUrl(), info.getPath(), info.getModifydate());
                itemList.get(currentTask).setPause(false);
                isThreadFree = false;
                Log.d("MainActivity", "url:" + url + "\n" + path + "\n" + modifydate);
            }
        }else{
            if(!itemList.get(currentTask).isPause()){
                downloadService.setPause();
                itemList.get(currentTask).setPause(true);

            }
            downloadService.download(info.getUrl(), info.getPath(), info.getModifydate());
            currentTask=clickPosition;
            itemList.get(currentTask).setPause(false);
            isThreadFree = false;
            Log.d("MainActivity", "url:" + url + "\n" + path + "\n" + modifydate);
        }
        adapter.notifyDataSetChanged();


    }


    public void querySQLData(String path) throws Exception {
        SQLiteDatabase db=sqlHelper.getWritableDatabase();
        String[] columns={DownloadSQLHelper.URL,DownloadSQLHelper.PATH,DownloadSQLHelper.MODIFYDATE};
        String[] whereArgs={path};
        Cursor cursor=db.query(DownloadSQLHelper.TABLENAME,columns,DownloadSQLHelper.PATH+"="+"?",whereArgs,null,null,null);
        if(cursor.moveToFirst()){
            try{
                url=cursor.getString(cursor.getColumnIndex(DownloadSQLHelper.URL));
                modifydate=cursor.getString(cursor.getColumnIndex(DownloadSQLHelper.MODIFYDATE));
            }catch (Exception e){
                throw new Exception();
            }

        }
        cursor.close();
    }
//    //新建Handler对象
//    public MyHandler mHandler=new MyHandler();
//    //创建自己的Handler类改写handleMessage()方法
//    static class MyHandler extends Handler{
//        @Override
//        public void handleMessage(Message msg) {
//            //判断Message的来源并取出传过来的对象
//           if(msg.what==1){
//               Item item= (Item) msg.obj;
//           }
//
//        }
//    }
//    Item item=new Item();
//    public void run(){
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Message msg=mHandler.obtainMessage();
//                msg.what=1;
//                msg.obj=item;
//                msg.sendToTarget();
//            }
//        }).start();
//    }
//
//    class Item{
//        Runnable runnable=new Runnable() {
//            @Override
//            public void run() {
//                Message msg=mHandler.obtainMessage();
//                msg.what=1;
//                msg.obj=item;
//                msg.sendToTarget();
//            }
//
//        public void pool(){
//            //创建可缓存的线程池，
//            ExecutorService cachedThreadPool= Executors.newCachedThreadPool();
//            cachedThreadPool.execute(runnable);
//
//            //创建固定大小的线程池
//            ExecutorService fixedThreadpool= Executors.newFixedThreadPool(10);
//            fixedThreadpool.execute(runnable);
//
//            //创建定时的线程池，可以替代Timer
//            ScheduledExecutorService scheduledThreadPool=  Executors.newScheduledThreadPool(10);
//            scheduledThreadPool.execute(runnable);
//
//            //创建单一线程的线程池，线程池中只有一个工作线程，所有任务安照指定的优先级执行
//            ExecutorService singleThreadpoll=Executors.newSingleThreadExecutor();
//            singleThreadpoll.execute(runnable);
//        }
//
//    }



}
