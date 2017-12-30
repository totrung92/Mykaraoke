package com.totato.karaoke.me.databasecomponent;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Trung on 06/12/2016.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Songlist";
    public static final String TABLE_NAME = "songinfor";

    public static final String VIDEO_ID = "id";
    public static final String VIDEO_LINK_VIDEO = "linkvideo";
    public static final String VIDEO_LINK_IMAGE = "linkimage";
    public static final String VIDEO_NAME = "name";
    public static final String VIDEO_PERMIT = "permit";
    public static final String VIDEO_ACRONYMS = "acronyms";


    Context context;
    String LinkDatabase;


    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
        LinkDatabase = context.getFilesDir().getParent() + "/databases/" + DATABASE_NAME;
        Log.e("Trung: ", LinkDatabase);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public SQLiteDatabase openDatabase() {
        return SQLiteDatabase.openDatabase(LinkDatabase, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public void createDatabase() {
        if (checkdatabasefile())
            Log.d("karaoke", " DB is exist");
        else {
            Log.d("karaoke", " DB is not exist");
            this.getWritableDatabase();
            copyDB();
        }
    }

    public void copyDB() {
        try {
            InputStream is = context.getAssets().open(DATABASE_NAME + ".sqlite");
            OutputStream os = new FileOutputStream(LinkDatabase);
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
            os.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();

            Log.d("karaoke", " error copy DB");
        }
    }

    public boolean checkdatabasefile() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(LinkDatabase, null, SQLiteDatabase.OPEN_READONLY);
        } catch (Exception e) {
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB == null ? false : true;
    }
}
