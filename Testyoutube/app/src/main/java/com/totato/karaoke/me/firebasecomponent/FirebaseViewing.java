package com.totato.karaoke.me.firebasecomponent;

import android.content.Context;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

/**
 * Created by Trung on 01-Apr-17.
 */

public class FirebaseViewing extends FireBaseBase {

    ChildEventListener ClientRequestListening = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String device = dataSnapshot.getKey().toString();
            String value = dataSnapshot.getValue().toString();
            if(value.equals("false"))
            {
                listen.OnClientRequestFromServer(device);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            String device = dataSnapshot.getKey().toString();
            String value = dataSnapshot.getValue().toString();
            if(value.equals("false"))
            {
                listen.OnClientRequestFromServer(device);
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    public FirebaseViewing(Context context, String ID) {
        super(context, ID);
        FireBaseInit();
    }
    @Override
    public void FireBaseInit() {
        super.FireBaseInit();
        databaseReference = database.getReference(RootIsExist);
        databaseReference.setValue("Yes");
        databaseReference = database.getReference(RootServerOnofOff);
        databaseReference.setValue("On");
        databaseReference = database.getReference(RootName);
        databaseReference.setValue(getDeviceName());
        setRootCommandNextListening();
        setClientRequestListening();
    }

    public void AcceptConnect(String device) {
        databaseReference = database.getReference(RootClient+"/"+device);
        databaseReference.setValue("true");
    }

    public void setClientRequestListening() {
        databaseReference = database.getReference(RootClient);
        databaseReference.addChildEventListener(ClientRequestListening);
    }

    public void removeServer() {
        databaseReference = database.getReference(RootClient);
        databaseReference.removeValue();
        databaseReference = database.getReference(RootList);
        databaseReference.removeValue();
        databaseReference = database.getReference(RootServerOnofOff);
        databaseReference.setValue("Off");
        clearCommandNext();
    }

    @Override
    public void FireBaseUninit() {
        super.FireBaseUninit();
        databaseReference = database.getReference(RootClient);
        databaseReference.removeEventListener(ClientRequestListening);
        removeServer();
    }
}
