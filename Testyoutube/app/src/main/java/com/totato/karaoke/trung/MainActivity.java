package com.totato.karaoke.trung;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Trung on 13/11/2016.
 */
public class MainActivity extends Activity {

    private Button bt_Viewing;
    private Button bt_Selecting;
    private Button bt_quit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt_Selecting = (Button) findViewById(R.id.bt_Selecting);
        bt_Viewing = (Button) findViewById(R.id.bt_Viewing);
        bt_quit = (Button) findViewById(R.id.bt_quit);
        bt_Viewing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnline())
                    newActivity(0);
                else
                    Toast.makeText(getApplication(), getResources().getString(R.string.toast_checkconnect), Toast.LENGTH_LONG).show();
            }
        });
        bt_Selecting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnline())
                    newActivity(1);
                else
                    Toast.makeText(getApplication(), getResources().getString(R.string.toast_checkconnect), Toast.LENGTH_LONG).show();
            }
        });
        bt_quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void newActivity(int mode) {    //0:viewing  1:selecting
        Intent intent;
        if (mode == 0)
            intent = new Intent(this, ViewingActivity.class);
        else if (mode == 1)
            intent = new Intent(this, SelectingActivity.class);
        else
            return;
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener dialogClickListenner = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE: {
                        onquit();
                        break;
                    }
                    case DialogInterface.BUTTON_NEGATIVE: {
                        break;
                    }
                }

            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ;
        builder.setTitle(getResources().getString(R.string.dl_quittitle))
                .setMessage(getResources().getString(R.string.dl_quitquestion))
                .setPositiveButton(getResources().getString(R.string.dl_quityes), dialogClickListenner)
                .setNegativeButton(getResources().getString(R.string.dl_quitno), dialogClickListenner)
                .show();
    }

    public void onquit() {
        super.onBackPressed();
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
