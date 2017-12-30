package com.totato.karaoke.me.firebasecomponent;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Trung on 02-Apr-17.
 */

public class FireBaseSelecting extends FireBaseBase {
    private boolean isConnected   = false;
    private boolean isServerOn    = false;
    private boolean isServerExist = false;
    private String nameDevice     = getDeviceName();

    public FireBaseSelecting(Context context, String ID) {
        super(context, ID);
        setCheckServerExistListening();
    }

    @Override
    public void FireBaseInit() {
        super.FireBaseInit();
        setServerOnOffListening();
        setClientwasAllowedListening();
        databaseReference = database.getReference(RootClient + "/" + nameDevice);
        databaseReference.setValue("false");
    }
    ValueEventListener CheckServerExistListening = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                FireBaseInit();
                isServerExist = true;
            }
            else{
                isServerExist = false;
                databaseReference = database.getReference(ServerID).child("Exist");
                databaseReference.removeEventListener(CheckServerExistListening);
            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    ValueEventListener ServerOnOffListening      = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if (dataSnapshot.getValue() == null) {
                isConnected = false;
                return;
            }
            if (dataSnapshot.getValue().toString().equalsIgnoreCase("on")) {
                isServerOn = true;
            } else {
                isServerOn = false;
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    ValueEventListener ClientwasAllowedListening = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() == null) {
                isConnected = false;
                return;
            }
            if (dataSnapshot.getValue().toString().equals("true")) {
                isConnected = true;
            } else {
                isConnected = false;
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    public void setCheckServerExistListening() {
        databaseReference = database.getReference(ServerID).child("Exist");
        databaseReference.addValueEventListener(CheckServerExistListening);
    }
    public void setClientwasAllowedListening() {
        databaseReference = database.getReference(RootClient + "/" + nameDevice);
        databaseReference.addValueEventListener(ClientwasAllowedListening);
    }
    public void setServerOnOffListening() {
        databaseReference = database.getReference(RootServerOnofOff);
        databaseReference.addValueEventListener(ServerOnOffListening);
    }
    public boolean isConnected() {
        return isConnected;
    }
    public boolean isServerOn() {
        return isServerOn;
    }
    public boolean isServerExist() {
        return isServerExist;
    }
    @Override
    public void FireBaseUninit(){
        super.FireBaseUninit();
        databaseReference = database.getReference(ServerID).child("Exist");
        databaseReference.removeEventListener(CheckServerExistListening);
        databaseReference = database.getReference(RootClient + "/" + nameDevice);
        databaseReference.removeEventListener(ClientwasAllowedListening);
        databaseReference = database.getReference(RootServerOnofOff);
        databaseReference.removeEventListener(ServerOnOffListening);
    }
}
