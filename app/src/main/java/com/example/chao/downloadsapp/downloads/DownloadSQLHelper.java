package com.example.chao.downloadsapp.downloads;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chao on 9/1/16.
 */
public class DownloadSQLHelper extends SQLiteOpenHelper {
    public static final String DATABASE="downloads";
    public static final String TABLENAME="items";
    public static final String ID = "id";
    public static final String URL = "url";
    public static final String PATH = "path";
    public static final String MODIFYDATE = "modifydate";
    public static final String LENGTH = "length";
    public static final String ISFINISH = "isfinish";

    public DownloadSQLHelper(Context context) {
        super(context,DATABASE, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLENAME+"("
                + ID + " INTEGER PRIMARY KEY,"
                + URL + " TEXT,"
                + PATH + " TEXT,"
                + LENGTH + " REAL,"
                + ISFINISH + "INTEGER,"
                + MODIFYDATE + " TEXT"+")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
