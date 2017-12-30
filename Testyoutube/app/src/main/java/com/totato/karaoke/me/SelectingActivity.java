package com.totato.karaoke.me;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.totato.karaoke.me.adapterlistviewcomponent.AdapterVideoSelectingOffline;
import com.totato.karaoke.me.adapterlistviewcomponent.AdapterVideoSelectingOnline;
import com.totato.karaoke.me.adapterlistviewcomponent.AdapterVideoSelectingWait;
import com.totato.karaoke.me.adapterlistviewcomponent.ListenfromAdapterSelectingInterface;
import com.totato.karaoke.me.adapterlistviewcomponent.VideoItem;
import com.totato.karaoke.me.autocompletecomponent.AutocompleteFinder;
import com.totato.karaoke.me.autocompletecomponent.IAutocompleteFinder;
import com.totato.karaoke.me.databasecomponent.DBSource;
import com.totato.karaoke.me.firebasecomponent.ListenfromFireBaseInterface;
import com.totato.karaoke.me.firebasecomponent.FireBaseSelecting;
import com.totato.karaoke.me.youtubecomponent.YoutubeConnector;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SelectingActivity extends Activity implements ListenfromAdapterSelectingInterface, ListenfromFireBaseInterface {
    private TextView tv_info;
    private Button bt_connect;
    private LinearLayout bt_page;
    private ListView lv_songs;
    private DBSource db;
    private FireBaseSelecting fs = null;
    private Constants.PAGE m_Page;

    AdapterVideoSelectingOffline adapterVideoSelectingOffline;
    AdapterVideoSelectingOnline adapterVideoSelectingOnline;
    AdapterVideoSelectingWait adapterVideoSelectingWait;
    private ArrayList<VideoItem> VideoSearchList;
    private ArrayList<VideoItem> VideoWaitsList;

    private Handler handlerYoutubeSearch;

    private AutocompleteFinder autocompleteFinder;
    private AutoCompleteTextView text;
    public List<String> suggest;
    public ArrayAdapter<String> AdapterAutoComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecting);
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_app_id));
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        init();
    }

    private void init() {
        tv_info = (TextView) findViewById(R.id.tv_infor);
        bt_connect = (Button) findViewById(R.id.bt_connect);

        lv_songs = (ListView) findViewById(R.id.lv_searchfound);
        db = new DBSource(this);
        handlerYoutubeSearch = new Handler();

        text = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (m_Page == Constants.PAGE.SEARCH_ONLINE)
                    queryGoogleAutocomplete(text.getText().toString());
                else if (m_Page == Constants.PAGE.SEARCH_OFFLINE && !s.toString().equals("")) {
                    VideoSearchList = db.SearchbyAcronyms(s.toString());
                    adapterVideoSelectingOffline = new AdapterVideoSelectingOffline(VideoSearchList, getLayoutInflater(), getApplicationContext());
                    lv_songs.setAdapter(adapterVideoSelectingOffline);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        bt_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(fs != null && fs.isConnected())) {
                    inputIDtoConnect();
                }
            }
        });
        text.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchVideo();
            }
        });
        VideoSearchList = new ArrayList<>();
        VideoSearchList = new ArrayList<>();
        VideoWaitsList = new ArrayList<>();
        adapterVideoSelectingOnline = new AdapterVideoSelectingOnline(VideoSearchList, getLayoutInflater(), this);
        adapterVideoSelectingOffline = new AdapterVideoSelectingOffline(VideoSearchList, getLayoutInflater(), this);
        adapterVideoSelectingWait = new AdapterVideoSelectingWait(VideoWaitsList, getLayoutInflater());
        AdapterVideoSelectingOnline.setListen(this);
        AdapterVideoSelectingOffline.setListen(this);
        AdapterVideoSelectingWait.setListen(this);
        setDefaulPage();
    }

    public void selectingButtonOnClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bt_selecting_next:
                NextVideo();
                break;
            case R.id.bt_selecting_quit:
                onBackPressed();
                break;
            case R.id.bt_selecting_search:
                SearchVideo();
                break;
            case R.id.bt_selecting_list_video_wait:
                setPage(Constants.PAGE.LIST_WAITING, (LinearLayout) v);
                break;
            case R.id.bt_selecting_search_video_offline:
                setPage(Constants.PAGE.SEARCH_OFFLINE, (LinearLayout) v);
                break;
            case R.id.bt_selecting_search_video_online:
                setPage(Constants.PAGE.SEARCH_ONLINE, (LinearLayout) v);
                break;
        }
    }

    private void searchOnYoutube(final String keywords) {
        new Thread() {
            public void run() {
                YoutubeConnector yc = new YoutubeConnector(SelectingActivity.this);
                VideoSearchList = yc.search(keywords);
                handlerYoutubeSearch.post(new Runnable() {
                    public void run() {
                        adapterVideoSelectingOnline = new AdapterVideoSelectingOnline(VideoSearchList, getLayoutInflater(), getApplicationContext());
                        lv_songs.setAdapter(adapterVideoSelectingOnline);
                    }
                });
            }
        }.start();
    }

    public void SearchVideo() {
        if (m_Page == Constants.PAGE.SEARCH_ONLINE) {
            if (isOnline()) {
                String textInput = text.getText().toString();
                if (!textInput.isEmpty()) {
                    searchOnYoutube(textInput + "karaoke");
                } else
                    Toast.makeText(getApplication(), getResources().getString(R.string.toast_inputkey), Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, R.string.toast_check_connect_when_search, Toast.LENGTH_SHORT).show();
        } else if (m_Page == Constants.PAGE.SEARCH_OFFLINE) {
            VideoSearchList = db.SearchbyAcronyms(text.getText().toString());
            adapterVideoSelectingOffline = new AdapterVideoSelectingOffline(VideoSearchList, getLayoutInflater(), getApplicationContext());
            lv_songs.setAdapter(adapterVideoSelectingOffline);
        } else {
            Toast.makeText(getApplication(), "search on list selected", Toast.LENGTH_SHORT).show();
        }
    }

    public void MoveVideoToFirst(int position) {
        if (position != 0) {
            VideoWaitsList.add(0, VideoWaitsList.get(position));
            VideoWaitsList.remove(position + 1);
            adapterVideoSelectingWait.notifyDataSetChanged();
        }
    }

    private void NextVideo() {
        if (fs != null && fs.isServerOn() && fs.isConnected()) {
            fs.NextVideo();
        }
    }

    public void DeleteVideo(int position) {
        fs.removeVideo(VideoWaitsList.get(position));
    }

    private void setPage(Constants.PAGE page, LinearLayout bt) {
        if (page == null || page != m_Page) {
            bt_page.setSelected(false);
            bt.setSelected(true);
            bt_page = bt;
            m_Page = page;
            updatePage();
        }
    }

    private void setDefaulPage() {
        bt_page = (LinearLayout) findViewById(R.id.bt_selecting_list_video_wait);
        bt_page.setSelected(true);
        m_Page = Constants.PAGE.LIST_WAITING;
        updatePage();
    }

    private void updatePage() {
        text.setText("");
        if (m_Page == Constants.PAGE.LIST_WAITING) {
            lv_songs.setAdapter(adapterVideoSelectingWait);
            VideoSearchList.clear();
            VideoSearchList.clear();
        } else if (m_Page == Constants.PAGE.SEARCH_ONLINE) {
            VideoSearchList.clear();
            lv_songs.setAdapter(adapterVideoSelectingOnline);
        } else if (m_Page == Constants.PAGE.SEARCH_OFFLINE) {
            VideoSearchList.clear();
            lv_songs.setAdapter(adapterVideoSelectingOffline);
        }
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
        builder.setTitle(getResources().getString(R.string.dl_quittomaintitle))
                .setMessage(getResources().getString(R.string.dl_quitquestion))
                .setPositiveButton(getResources().getString(R.string.dl_quityes), dialogClickListenner)
                .setNegativeButton(getResources().getString(R.string.dl_quitno), dialogClickListenner)
                .show();
    }

    public void onquit() {
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        if (fs != null) {
            fs.FireBaseUninit();
            fs = null;
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void ReceiverCommandNextFromServer() {

    }

    @Override
    public void OnAddVideoFromServer(VideoItem video) {
        VideoWaitsList.add(video);
        adapterVideoSelectingWait.notifyDataSetChanged();
    }

    @Override
    public void OnRemoveVideoFromServer(VideoItem video) {
        for (int i = 0; i < VideoWaitsList.size(); i++) {
            if (VideoWaitsList.get(i).getLinkvideo().compareTo(video.getLinkvideo()) == 0) {
                VideoWaitsList.remove(i);
                adapterVideoSelectingWait.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void ReceiverCommandPrioFromServer(String videoID) {
        for (int i = 0; i < VideoWaitsList.size(); i++) {
            if (VideoWaitsList.get(i).getLinkvideo().compareTo(videoID) == 0) {
                MoveVideoToFirst(i);
                fs.clearCommandPrio();
                break;
            }
        }
    }

    @Override
    public void OnClientRequestFromServer(String clientName) {

    }

    @Override
    public void choosefromAdapter(int position) {
        if (fs != null && fs.isServerOn() && fs.isConnected()) {
            fs.addVideo(VideoSearchList.get(position));
        }
    }

    @Override
    public void editfromAdapter(final int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_layout, null);
        dialogBuilder.setView(dialogView);
        final EditText name = (EditText) dialogView.findViewById(R.id.tv_dialog_name);
        final EditText singer = (EditText) dialogView.findViewById(R.id.tv_dialog_singer);
        final VideoItem video = VideoSearchList.get(position);
        int j = video.getName().indexOf(" -");
        if (j != -1) {
            name.setText(video.getName().substring(0, j));
            singer.setText(video.getName().substring(j + 2));
        } else
            name.setText(video.getName());
        dialogBuilder.setTitle(R.string.dialog_title_save);
        dialogBuilder.setMessage(R.string.dialog_input_infor);
        dialogBuilder.setPositiveButton(R.string.dialog_bt_save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                video.setName(name.getText().toString().trim() + " -" + singer.getText().toString().trim());
                video.setPermit(true);
                video.setAcronyms(getAcronyms(name.getText().toString()));
                int result = db.editVideo(video);
                if (result != -1) {
                    Toast.makeText(SelectingActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SelectingActivity.this, R.string.save_false, Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.dialog_bt_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        dialogBuilder.setNeutralButton(R.string.dialog_bt_del, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                db.delVideo(video.getLinkvideo());
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @Override
    public void savefromAdapter(final int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_layout, null);
        dialogBuilder.setView(dialogView);
        final TextView title = (TextView) dialogView.findViewById(R.id.tv_dialog_title);
        final EditText name = (EditText) dialogView.findViewById(R.id.tv_dialog_name);
        final EditText singer = (EditText) dialogView.findViewById(R.id.tv_dialog_singer);
        title.setText(VideoSearchList.get(position).getName());
        dialogBuilder.setTitle(R.string.dialog_title_save);
        dialogBuilder.setMessage(R.string.dialog_input_infor);
        dialogBuilder.setPositiveButton(R.string.dialog_bt_save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                VideoItem video = VideoSearchList.get(position);
                video.setName(name.getText().toString().trim() + " -" + singer.getText().toString().trim());
                video.setPermit(true);
                video.setAcronyms(getAcronyms(name.getText().toString()));
                int result = db.addVideo(video);
                if (result != -1) {
                    Toast.makeText(SelectingActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SelectingActivity.this, R.string.save_false, Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.dialog_bt_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @Override
    public void priofromAdapter(int position) {
        if (fs != null && fs.isServerOn() && fs.isConnected()) {
            fs.PrioVideo(VideoWaitsList.get(position).getLinkvideo());
        }
    }

    @Override
    public void delfromAdapter(int position) {
        DeleteVideo(position);
    }

    public void inputIDtoConnect() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_input_id_device, null);
        dialogBuilder.setView(dialogView);
        final EditText ID = (EditText) dialogView.findViewById(R.id.et_ID);
        dialogBuilder.setTitle(R.string.dialog_title_connect);
        dialogBuilder.setMessage(R.string.dialog_message_connect);
        dialogBuilder.setPositiveButton(R.string.dialog_bt_connect, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String id = ID.getText().toString();
                if (!id.isEmpty()) {
                    Connectfirebase(id);
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.dialog_bt_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void Connectfirebase(String id) {
        if (fs == null || !fs.isServerExist()) {
            fs = new FireBaseSelecting(this, id);
            fs.setListen(this);
            Toast.makeText(this, R.string.toast_requestconnect, Toast.LENGTH_SHORT).show();
        } else if (!fs.isConnected()) {
            Toast.makeText(this, R.string.toast_requestconnect, Toast.LENGTH_SHORT).show();
        } else if (fs.isConnected()) {

        }
    }

    public String getAcronyms(String name) {
        String acronyms = "";
        name = name.trim();
        name = name.replaceAll("\\s+", " ");
        String temp[] = name.split(" ");
        for (int i = 0; i < temp.length; i++) {
            acronyms += temp[i].charAt(0);
        }
        return unAccent(acronyms.toUpperCase());
    }

    public static String unAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replaceAll("Đ", "D");
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private IAutocompleteFinder callBack = new IAutocompleteFinder() {
        @Override
        public void success(String search, List<String> items) {
            suggest = items;
            AdapterAutoComplete = new ArrayAdapter<String>(getApplicationContext(), R.layout.itemautocomplete, suggest);
            text.setAdapter(AdapterAutoComplete);
            AdapterAutoComplete.notifyDataSetChanged();
        }

        @Override
        public void error(String response) {
            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
        }
    };

    private void queryGoogleAutocomplete(String key) {
        autocompleteFinder = AutocompleteFinder.getInstance(callBack, "vi");
        if (autocompleteFinder != null) autocompleteFinder.start(key);
    }
}