package com.example.chao.downloadsapp.downloads;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * Created by chao on 8/31/16.
 */
public class HttpDownloads {
    private URL url;
    private String path;

    private String range;
    private String modifyDate;
    private File downloadingFile;
    private int responseCode;
    private long length=0;
    private long progress=0;
    private boolean pause=false;
    private DownloadSQLHelper sqlHelper;
    private int isFinish=0;
    private Context context;
    public HttpURLConnection httpConnection;
    public InputStream in;
    public BufferedOutputStream out;

    @Deprecated
    public HttpDownloads(Context context,URL url) {
        this.context=context;
        this.url=url;
        path=getDefaultPath();
        Log.d("MainActivity","path:"+path);
        downloadingFile=new File(path);

        modifyDate="";
    }

    public HttpDownloads(Context context,URL url, String path,String modifyDate) {
        this.context=context;
        this.url = url;
        if(path==null){
            this.path=getDefaultPath();
        }else{
            this.path=path;
        }
        if(modifyDate==null){
            this.modifyDate="";
        }else{
            this.modifyDate=modifyDate;
        }
        downloadingFile=new File(this.path);
        sqlHelper=new DownloadSQLHelper(context);
    }


    public long request() throws IOException {
        httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestMethod("GET");
        httpConnection.addRequestProperty("Range","bytes="+downloadingFile.length()+"-");
        Log.d("MainActivity","fileLength:"+downloadingFile.length());
        httpConnection.addRequestProperty("if-range",modifyDate);
        httpConnection.connect();
        responseCode= httpConnection.getResponseCode();
        Log.d("MainActivity","reponsecode:"+responseCode);
        String head;
        String key;
        for(int i=0;(head=httpConnection.getHeaderField(i))!=null;i++){
            key=httpConnection.getHeaderFieldKey(i);
            Log.d(key,head);
        }
        progress=downloadingFile.length();
        String fileLength=httpConnection.getHeaderField("content-length");
        if(TextUtils.isEmpty(fileLength)){
            fileLength="-1";
        }
        length= downloadingFile.length()+Long.parseLong(fileLength);
        modifyDate= httpConnection.getHeaderField("Last-Modified");
        return length;
    }

    public void writeData() throws IOException {
        in = httpConnection.getInputStream();

        boolean append=false;
        if(downloadingFile.length()>0){
            append=true;
        }

        try{
            if(downloadingFile.length()<length){
                out = new BufferedOutputStream(new FileOutputStream(path,append));
                int i=0;
                byte[] bytes=new byte[1024];
                while((i=in.read(bytes))!=-1){
                    out.write(bytes,0,i);
                    progress=progress+i;
                    if((progress%1000)==0){
                        Log.d("MainActivity","progress:"+progress);
                    }

                    if(pause){
                        break;
                    }
                }
            }

        }finally {

            in.close();
            if(out!=null){
                out.close();
            }

            httpConnection.disconnect();
            if(progress==length){
                isFinish=1;
            }
            saveData();
        }

    }

    public void saveData(){
        SQLiteDatabase db=sqlHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(DownloadSQLHelper.URL,url.toExternalForm());
        values.put(DownloadSQLHelper.MODIFYDATE,modifyDate);
        values.put(DownloadSQLHelper.PATH,path);
        values.put(DownloadSQLHelper.LENGTH,length);
        Cursor cursor;
        try{
             cursor=db.query(DownloadSQLHelper.TABLENAME,null,DownloadSQLHelper.PATH+"="+path,null,null,null,null);
;        }catch (Exception e){
            cursor=null;
        }
        String s=null;
        if(cursor!=null){
            if(cursor.moveToFirst()){
                s=cursor.getString(cursor.getColumnIndex(DownloadSQLHelper.PATH));
            }
            cursor.close();
        }

        if(TextUtils.isEmpty(s)){
            long id=db.insert(DownloadSQLHelper.TABLENAME,null,values);
            Log.d("MainActivity","insert id "+id);
        }else{
            db.update(DownloadSQLHelper.TABLENAME,values,DownloadSQLHelper.PATH+"="+path,null);
        }



    }

    public long getProgress() {
        return progress;
    }

    public itemInfo getInfo() {

        return new itemInfo(length,progress,pause,isFinish,path,downloadingFile.getName(),modifyDate);
    }

    public static class itemInfo implements Serializable{
        private long length;
        private long progress;
        private boolean pause;
        private int finish;
        private String path;
        private String fileName;
        private String url;
        private boolean waitState;
        private String modifydate;

        public itemInfo(){
            this.fileName="等待下载";
            this.length=-1;
            this.path=null;
            this.modifydate=null;
        }

        public itemInfo(long length, long progress,boolean pause,int finish,
                        String path,String name,String modifydate) {
            this.length = length;
            this.progress = progress;
            this.pause=pause;
            this.finish=finish;
            this.path=path;
            this.fileName=name;
            this.modifydate=modifydate;

        }

        public String getModifydate() {
            return modifydate;
        }

        public void setAllInfo(HttpDownloads.itemInfo info){
            this.length = info.getLength();
            this.progress =info.getProgress();
            this.pause=info.isPause();
            this.finish=info.getFinish();
            this.path=info.getPath();
            this.fileName=info.getFileName();
            this.modifydate=info.getModifydate();
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public boolean isWaitState() {
            return waitState;
        }

        public void setWaitState(boolean waitState) {
            this.waitState = waitState;
        }

        public String getFileName() {
            return fileName;
        }

        public String getPath() {
            return path;
        }

        public boolean isPause() {
            return pause;
        }

        public int getFinish() {
            return finish;
        }

        public long getLength() {
            return length;
        }

        public long getProgress() {
            return progress;
        }

        public void setUrl(String url) {
            this.url = url;
        }
        public String getUrl(){
            return url;
        }
        public void setPause(boolean b){
            this.pause=b;
        }
    }

    public void pause(){
        pause=true;
    }

    private String getDefaultPath(){
        //文件的父目录
        File file=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if(!file.isDirectory()){
            file.mkdirs();
        }
        //文件名
        String s=url.getPath();
        String[] list=s.split("/");
        String filename;
        if(list.length>1){
            if(!TextUtils.isEmpty(list[list.length-1])){
                filename=list[list.length-1];
            }else filename="index";

        }else{
            filename="index";
        }
        path=file.getAbsolutePath()+"/"+filename;
        file=new File(path);
        if(file.isFile()){
           nameIterater(file);
        }
        return path;
    }

    private String nameIterater(File file){
        String name=file.getName();
        String parent=file.getParent();
        String[] nameClip=name.split("\\.");
        if(nameClip.length>1){
            StringBuilder sb=new StringBuilder();
            for(int i=0;i<(nameClip.length-1);i++){
                sb.append(nameClip[i]);
            }
            sb.append("1");
            sb.append(nameClip[nameClip.length-1]);
            name=sb.toString();
        }else{
            name=name+"1";
        }

        return parent+"/"+name;
    }
}
