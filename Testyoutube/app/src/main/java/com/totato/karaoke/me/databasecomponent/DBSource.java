package com.totato.karaoke.me.databasecomponent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.totato.karaoke.me.adapterlistviewcomponent.VideoItem;

import java.util.ArrayList;

/**
 * Created by Trung on 07/12/2016.
 */
public class DBSource {
    SQLiteDatabase db;
    DataBaseHelper helper;

    public DBSource(Context context) {
        helper = new DataBaseHelper(context);
        helper.createDatabase();
        db = helper.openDatabase();
    }
    public ArrayList<VideoItem> getListSong(){
        ArrayList<VideoItem> list = new ArrayList<VideoItem>();
        String[] column = {DataBaseHelper.VIDEO_ID, DataBaseHelper.VIDEO_NAME,
                DataBaseHelper.VIDEO_LINK_VIDEO,DataBaseHelper.VIDEO_LINK_IMAGE,
                DataBaseHelper.VIDEO_ACRONYMS,DataBaseHelper.VIDEO_PERMIT};
        Cursor c =db.query(DataBaseHelper.TABLE_NAME,column,null,null,null,null,null);
        c.moveToFirst();
        while(!c.isAfterLast()){
            VideoItem item = new VideoItem();
            item.setId(Integer.parseInt(c.getString(0)));
            item.setName(c.getString(1));
            item.setLinkvideo(c.getString(2));
            item.setLinkimage(c.getString(3));
            item.setAcronyms(c.getString(4));
            item.setPermit(Boolean.getBoolean(c.getString(5)));
            list.add(item);
            c.moveToNext();
        }
        return list;
    }
    public int addVideo(VideoItem video){
        ContentValues value = new ContentValues();
        value.put(DataBaseHelper.VIDEO_NAME,video.getName());
        value.put(DataBaseHelper.VIDEO_LINK_VIDEO,video.getLinkvideo());
        value.put(DataBaseHelper.VIDEO_LINK_IMAGE,video.getLinkimage());
        value.put(DataBaseHelper.VIDEO_ACRONYMS,video.getAcronyms().toUpperCase());
        value.put(DataBaseHelper.VIDEO_PERMIT,String.valueOf(video.isPermit()));
        return (int) db.insert(DataBaseHelper.TABLE_NAME,null,value);
    }
    public int delVideo(String Linkvideo){
        db.delete(DataBaseHelper.TABLE_NAME,DataBaseHelper.VIDEO_LINK_VIDEO+" = ?", new String[]{Linkvideo});
        return -1;
    }
    public ArrayList<VideoItem> SearchbyAcronyms(String acronyms){
        ArrayList<VideoItem> list = new ArrayList<VideoItem>();
        String[] column = {DataBaseHelper.VIDEO_ID, DataBaseHelper.VIDEO_NAME,
                DataBaseHelper.VIDEO_LINK_VIDEO,DataBaseHelper.VIDEO_LINK_IMAGE,
                DataBaseHelper.VIDEO_ACRONYMS,DataBaseHelper.VIDEO_PERMIT};
        String search ="Select "+column[0]+" , "+column[1]+" , " +column[2]+" , "+column[3]+" , "+column[4]+" , "+column[5]
                + " From "+ DataBaseHelper.TABLE_NAME+" Where "
                + DataBaseHelper.VIDEO_ACRONYMS + " LIKE '%"+acronyms.toUpperCase()+ "%'";
        Log.e("Trung: ",search);
        Cursor c = db.rawQuery(search,null);
        c.moveToFirst();
        while(!c.isAfterLast()){
            VideoItem item = new VideoItem();
            item.setId(Integer.parseInt(c.getString(0)));
            item.setName(c.getString(1));
            item.setLinkvideo(c.getString(2));
            item.setLinkimage(c.getString(3));
            item.setAcronyms(c.getString(4));
            item.setPermit(Boolean.getBoolean(c.getString(5)));
            list.add(item);
            c.moveToNext();
        }
        return  list;
    }
    public int editVideo(VideoItem video){
        ContentValues value = new ContentValues();
        value.put(DataBaseHelper.VIDEO_NAME,video.getName());
        value.put(DataBaseHelper.VIDEO_LINK_VIDEO,video.getLinkvideo());
        value.put(DataBaseHelper.VIDEO_LINK_IMAGE,video.getLinkimage());
        value.put(DataBaseHelper.VIDEO_ACRONYMS,video.getAcronyms().toUpperCase());
        value.put(DataBaseHelper.VIDEO_PERMIT,String.valueOf(video.isPermit()));
        return (int) db.update(DataBaseHelper.TABLE_NAME,value,DataBaseHelper.VIDEO_ID+" = ?",new String[]{String.valueOf(video.getId())});
    }
}
