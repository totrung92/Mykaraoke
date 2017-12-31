package com.totato.karaoke.me.firebasecomponent;

import android.content.Context;
import android.os.Build;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.totato.karaoke.me.adapterlistviewcomponent.VideoItem;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Trung on 02-Apr-17.
 */

public class FireBaseBase {

    protected String ServerID;
    protected String RootIsExist = "/Exist";
    protected String RootName = "/Server/Name";
    protected String RootClient = "/Client";
    protected String RootList = "/List";
    protected String RootCommandNext = "/Command/Next";
    protected String RootCommandPrio = "/Command/Prio";
    protected String RootServerOnofOff = "/Server/OnOff";
    protected DatabaseReference databaseReference = null;
    protected FirebaseDatabase database = null;
    protected Context m_Context;
    protected ListenfromFireBaseInterface listen;
    public void setListen(ListenfromFireBaseInterface listen) {
        this.listen = listen;
    }
    public FireBaseBase(Context context,String ID) {
        ServerID=ID;
        this.m_Context = context;

        database       = FirebaseDatabase.getInstance();
    }
    public void FireBaseInit(){
        RootName    = ServerID + RootName;
        RootClient  = ServerID + RootClient;
        RootList    = ServerID + RootList;
        RootIsExist = ServerID +  RootIsExist;
        RootCommandNext   = ServerID + RootCommandNext;
        RootServerOnofOff = ServerID + RootServerOnofOff;
        RootCommandPrio   = ServerID + RootCommandPrio;
        clearCommandNext();
        setItemsListening();
        setRootCommandPrioListening();
    }
    ChildEventListener ItemsListening = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            VideoItem video = new VideoItem();
            video.setName(dataSnapshot.getValue().toString());
            video.setLinkvideo(dataSnapshot.getKey().toString());
            listen.OnAddVideoFromServer(video);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            VideoItem video = new VideoItem();
            video.setName(dataSnapshot.getValue().toString());
            video.setLinkvideo(dataSnapshot.getKey().toString());
            listen.OnRemoveVideoFromServer(video);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    ValueEventListener RootCommandNextListening = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue().toString().equals("Yes"));
            clearCommandNext();
            listen.OnNextFromServer();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
    public void setRootCommandNextListening() {
        databaseReference = database.getReference(RootCommandNext);
        databaseReference.addValueEventListener(RootCommandNextListening);
    }
    ValueEventListener RootCommandPrioListening = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()) {
                String videoID = dataSnapshot.getValue().toString();
                if (!videoID.equals("No"))
                    clearCommandPrio();
                listen.OnPrioFromServer(videoID);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
    public void setRootCommandPrioListening() {
        databaseReference = database.getReference(RootCommandPrio);
        databaseReference.addValueEventListener(RootCommandPrioListening);
    }
    public void setItemsListening() {
        databaseReference = database.getReference(RootList);
        databaseReference.addChildEventListener(ItemsListening);
    }
    public void clearCommandNext() {
        databaseReference = database.getReference(RootCommandNext);
        databaseReference.setValue("No");
    }
    public void clearCommandPrio() {
        databaseReference = database.getReference(RootCommandPrio);
        databaseReference.setValue("No");
    }
    public void addVideo(VideoItem video) {
        Map<String, Object> child = new HashMap<String, Object>();
        child.put(video.getLinkvideo(), video.getName());
        databaseReference = database.getReference(RootList);
        databaseReference.updateChildren(child);
    }
    public void removeVideo(VideoItem video) {
        databaseReference = database.getReference(RootList+"/"+video.getLinkvideo());
        databaseReference.removeValue();
    }
    public void PrioVideo(String videoID){
        databaseReference = database.getReference(RootCommandPrio);
        databaseReference.setValue(videoID);
    }
    public void NextVideo(){
        databaseReference = database.getReference(RootCommandNext);
        databaseReference.setValue("Yes");
    }
    public void FireBaseUninit(){
        databaseReference = database.getReference(RootCommandNext);
        databaseReference.removeEventListener(RootCommandNextListening);
        databaseReference = database.getReference(RootList);
        databaseReference.removeEventListener(ItemsListening);
        databaseReference = database.getReference(RootCommandPrio);
        databaseReference.removeEventListener(RootCommandPrioListening);
    }
    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }
    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
