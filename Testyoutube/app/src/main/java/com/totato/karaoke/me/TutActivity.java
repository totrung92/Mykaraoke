package com.totato.karaoke.me;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.Locale;

public class TutActivity extends AppCompatActivity {

    ImageView im1;
    ImageView im2;
    ImageView im3;
    ImageView im4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tut);
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_app_id));
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        if(Locale.getDefault().getLanguage().equals("vi")) {
            im1 = (ImageView) findViewById(R.id.iv1);
            im2 = (ImageView) findViewById(R.id.iv2);
            im3 = (ImageView) findViewById(R.id.iv3);
            im4 = (ImageView) findViewById(R.id.iv4);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                im1.setImageDrawable(getResources().getDrawable(R.drawable.imageid, getApplicationContext().getTheme()));
                im2.setImageDrawable(getResources().getDrawable(R.drawable.imageconect, getApplicationContext().getTheme()));
                im3.setImageDrawable(getResources().getDrawable(R.drawable.inputid, getApplicationContext().getTheme()));
                im4.setImageDrawable(getResources().getDrawable(R.drawable.accept, getApplicationContext().getTheme()));
            } else {
                im1.setImageDrawable(getResources().getDrawable(R.drawable.imageid));
                im2.setImageDrawable(getResources().getDrawable(R.drawable.imageconect));
                im3.setImageDrawable(getResources().getDrawable(R.drawable.inputid));
                im4.setImageDrawable(getResources().getDrawable(R.drawable.accept));
            }
        }
    }
}
