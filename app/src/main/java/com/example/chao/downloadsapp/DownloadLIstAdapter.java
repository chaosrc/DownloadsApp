package com.example.chao.downloadsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chao.downloadsapp.downloads.HttpDownloads;

import java.util.List;

/**
 * Created by chao on 9/3/16.
 */
public class DownloadLIstAdapter extends ArrayAdapter<HttpDownloads.itemInfo>{
    private Context mContext;
    private int resource;
    private List<HttpDownloads.itemInfo> itemInfo;
    private int position=0;

    public DownloadLIstAdapter(Context context, int resource, List<HttpDownloads.itemInfo> objects) {
        super(context, resource, objects);
        this.mContext=context;
        this.resource=resource;
        this.itemInfo=objects;
    }

//    public DownloadLIstAdapter(Context context, int resource, HttpDownloads.itemInfo[] objects) {
//        super(context, resource, objects);
//        this.mContext=context;
//        this.resource=resource;
//        this.itemInfo=objects;
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        this.position=position;
        ViewHolder viewHolder;
        View view;
        if(convertView==null){
            viewHolder=new ViewHolder();
            view =  LayoutInflater.from(mContext).inflate(resource,null);
            viewHolder.downloadName= (TextView) view.findViewById(R.id.state_download);
            viewHolder.progressBar= (ProgressBar) view.findViewById(R.id.progressbar_download);
            viewHolder.pause= (ImageView) view.findViewById(R.id.pause_button_download);
            viewHolder.percent= (TextView) view.findViewById(R.id.percent_progress_download);
            viewHolder.downloadSize= (TextView) view.findViewById(R.id.size_download);
            view.setTag(viewHolder);
        }else{
            view=convertView;
            viewHolder= (ViewHolder) view.getTag();
        }
        HttpDownloads.itemInfo item=itemInfo.get(position);
        viewHolder.downloadName.setText(item.getFileName());
        int progress;
        try{
            progress= (int) (item.getProgress()*100/item.getLength());
        }catch (Exception e){
            progress=0;
        }

        viewHolder.progressBar.setProgress(progress);
        if(item.isPause()){
            viewHolder.pause.setBackgroundResource(R.drawable.ic_play_arrow_white_24dp);
        }else{
            viewHolder.pause.setBackgroundResource(R.drawable.ic_pause_white_24dp);
        }
        //viewHolder.pause.setOnClickListener(this);
        viewHolder.percent.setText(progress+"/100");
        viewHolder.downloadSize.setText(String.format("%.2f",item.getProgress()/1024/1024.0)+" /"+
                String.format("%.2f",item.getLength()/1024/1024.0)+"Mb");

        return view;
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.pause_button_download:
//                MainActivity activity= (MainActivity) mContext;
//                activity.pause();
//                break;
//
//        }
//    }

    class ViewHolder{
        public TextView downloadName;
        public ProgressBar progressBar;
        public ImageView pause;
        public TextView percent;
        public TextView downloadSize;
    }


}
