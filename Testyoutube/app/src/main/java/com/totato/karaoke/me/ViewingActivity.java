package com.totato.karaoke.me;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.totato.karaoke.me.adapterlistviewcomponent.AdapterVideoViewingOnline;
import com.totato.karaoke.me.adapterlistviewcomponent.AdapterVideoViewingWait;
import com.totato.karaoke.me.adapterlistviewcomponent.ListenfromAdapterViewingInterface;
import com.totato.karaoke.me.adapterlistviewcomponent.VideoItem;
import com.totato.karaoke.me.autocompletecomponent.AutocompleteFinder;
import com.totato.karaoke.me.autocompletecomponent.IAutocompleteFinder;
import com.totato.karaoke.me.databasecomponent.DBSource;
import com.totato.karaoke.me.firebasecomponent.FirebaseViewing;
import com.totato.karaoke.me.firebasecomponent.ListenfromFireBaseInterface;
import com.totato.karaoke.me.youtubecomponent.YoutubeConnector;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Trung on 13/11/2016.
 */
public class ViewingActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener, ListenfromFireBaseInterface, ListenfromAdapterViewingInterface {
    private ListView lv_player_wait;
    private ListView lv_player_search;
    private Handler handler;
    private ArrayList<VideoItem> VideoSearchList;
    private ArrayList<VideoItem> VideoWaitsList;
    private YouTubePlayerView playerView;
    private YouTubePlayer YTPlayer;
    private Button bt_ID;
    private TextView tv_info;

    private RadioGroup rg_type_input;

    private Constants.STATE _state;
    private Boolean _isFullScreen;
    private String type;


    private AdapterVideoViewingWait adapterVideoWait;
    private AdapterVideoViewingOnline adapterVideoViewingOnline;

    private AutocompleteFinder autocompleteFinder;
    private AutoCompleteTextView text;
    public List<String> suggest;
    public ArrayAdapter<String> AdapterAutoComplete;

    private DBSource db;
    private FirebaseViewing fv;
    public String firebaseID = null;
    ValueEventListener valuevent = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            firebaseID = dataSnapshot.getValue().toString();
            int v = Integer.parseInt(firebaseID)+1;
            firebaseID = String.valueOf(v);
            DatabaseReference databaseReference;
            FirebaseDatabase database;
            database = FirebaseDatabase.getInstance();
            databaseReference = database.getReference("ID");
            databaseReference.setValue(firebaseID);
            databaseReference.removeEventListener(valuevent);
            bt_ID.setText("ID:" + firebaseID);
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("ID", firebaseID);
            editor.commit();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewing);

        init();
        addClickListener();
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        firebaseID = sharedPref.getString("ID","null");
        if(firebaseID.equalsIgnoreCase("null"))
        {
            DatabaseReference databaseReference;
            FirebaseDatabase database;
            database = FirebaseDatabase.getInstance();
            databaseReference = database.getReference("ID");
            databaseReference.addValueEventListener(valuevent);
        }
    }

    private void init() {
        type = "online";
        rg_type_input = (RadioGroup) findViewById(R.id.rg_viewing_type_search);
        bt_ID = (Button) findViewById(R.id.bt_id);
        tv_info = (TextView) findViewById(R.id.tv_infor);
        lv_player_search = (ListView) findViewById(R.id.lv_viewing_search_players);
        playerView = (YouTubePlayerView) findViewById(R.id.yt_viewing_player);
        playerView.initialize(YoutubeConnector.KEY, this);
        lv_player_wait = (ListView) findViewById(R.id.lv_viewing_wait_player);
        text = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (type.equals("online"))
                    queryGoogleAutocomplete(text.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        text.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchVideo();
            }
        });
        VideoWaitsList = new ArrayList<>();
        VideoSearchList = new ArrayList<>();
        handler = new Handler();

        _state = Constants.STATE.ON_INIT;
        _isFullScreen = false;

        db = new DBSource(this);
        adapterVideoWait = new AdapterVideoViewingWait(VideoWaitsList, getLayoutInflater(), this);
        lv_player_wait.setAdapter(adapterVideoWait);
        bt_ID.setText("ID:" + firebaseID);

        fv = new FirebaseViewing(this, firebaseID);
        fv.setListen(this);
    }

    private void searchOnYoutube(final String keywords) {
        new Thread() {
            public void run() {
                YoutubeConnector yc = new YoutubeConnector(ViewingActivity.this);
                VideoSearchList = yc.search(keywords);
                handler.post(new Runnable() {
                    public void run() {
                        adapterVideoViewingOnline = new AdapterVideoViewingOnline(VideoSearchList, getLayoutInflater(), getApplicationContext());
                        lv_player_search.setAdapter(adapterVideoViewingOnline);
                    }
                });
            }
        }.start();
    }

    public void viewingButtonOnClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bt_viewing_next:
                NextVideo();
                break;
            case R.id.bt_viewing_quit:
                onBackPressed();
                break;
            case R.id.bt_viewing_search:
                SearchVideo();
                break;
            case R.id.bt_viewing_delete_text:
                text.setText("");
                break;
        }
    }

    private void addClickListener() {
        lv_player_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AddVideo(VideoSearchList.get(position));
            }
        });
        lv_player_search.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (type.equals("online"))
                    save(position);
                return false;
            }
        });
        rg_type_input.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checked = rg_type_input.getCheckedRadioButtonId();
                switch (checked) {
                    case R.id.rb_viewing_search_online: {
                        type = "online";
                        break;
                    }
                    case R.id.rb_viewing_search_offline: {
                        type = "offline";
                        break;
                    }
                }
            }
        });
    }

    public void setYoutubeStateChange() {
        YTPlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onLoading() {
                _state = Constants.STATE.ON_LOADING_VIDEO;
            }

            @Override
            public void onLoaded(String s) {
                _state = Constants.STATE.ON_LOADED_VIDEO;
                UpdateByState();
            }

            @Override
            public void onAdStarted() {
                _state = Constants.STATE.ON_STARTED_AD;
            }

            @Override
            public void onVideoStarted() {
                _state = Constants.STATE.ON_STARTED_VIDEO;
            }

            @Override
            public void onVideoEnded() {
                _state = Constants.STATE.ON_ENDED_VIDEO;
                UpdateByState();
            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {
                _state = Constants.STATE.ON_ERROR_VIDEO;
                Toast.makeText(getApplication(), getResources().getString(R.string.toast_videoerror), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void AddVideo(VideoItem video) {
        if(!firebaseID.equalsIgnoreCase("null")) {
            fv.addVideo(video);
        }
        else{
            OnAddVideoFromServer(video);
        }
    }

    public void MoveVideoToFirst(int position) {
        if (position != 0) {
            VideoWaitsList.add(0, VideoWaitsList.get(position));
            VideoWaitsList.remove(position + 1);
            adapterVideoWait.notifyDataSetChanged();
        }
    }

    public void SearchVideo() {
        if (isOnline()) {
            if (type.equals("online")) {
                String textInput = text.getText().toString();
                if (!textInput.isEmpty()) {
                    searchOnYoutube(textInput + "karaoke");
                } else
                    Toast.makeText(getApplication(), getResources().getString(R.string.toast_inputkey), Toast.LENGTH_SHORT).show();
            } else if (type.equals("offline")) {
                VideoSearchList = db.getListSong();
                adapterVideoViewingOnline = new AdapterVideoViewingOnline(VideoSearchList, getLayoutInflater(), this);
                lv_player_search.setAdapter(adapterVideoViewingOnline);
            }
        } else
            Toast.makeText(this, R.string.toast_check_connect_when_search, Toast.LENGTH_SHORT).show();
    }

    public void PlayVideo(int position) {
        if (VideoWaitsList.size() != 0) {
            YTPlayer.cueVideo(VideoWaitsList.get(position).getLinkvideo());
            YTPlayer.play();
            tv_info.setText(VideoWaitsList.get(position).getName());
            DeleteVideo(position);
        } else
            Toast.makeText(getApplication(), getResources().getString(R.string.toast_listempty), Toast.LENGTH_SHORT).show();
    }

    public void DeleteVideo(int position) {
        if(!firebaseID.equalsIgnoreCase("null")) {
            fv.removeVideo(VideoWaitsList.get(position));
        }
        else
        {
            OnRemoveVideoFromServer(VideoWaitsList.get(position));
        }
    }

    public void NextVideo() {
        PlayVideo(0);
    }

    public void UpdateByState() {
        switch (_state) {
            case ON_INIT: {
                break;
            }
            case ON_INIT_FAIL: {
                onquit();
                break;
            }
            case ON_WAIT_ADD_VIDEO: {
                break;
            }
            case ON_LOADING_VIDEO: {
                break;
            }
            case ON_LOADED_VIDEO: {
                YTPlayer.play();
                break;
            }
            case ON_STARTED_VIDEO: {
                break;
            }
            case ON_ENDED_VIDEO: {
                if (VideoWaitsList.isEmpty()) {
                    _state = Constants.STATE.ON_WAIT_ADD_VIDEO;
                    Toast.makeText(getApplication(), getResources().getString(R.string.toast_listempty), Toast.LENGTH_LONG).show();
                } else
                    PlayVideo(0);
                break;
            }
            case ON_ERROR_VIDEO: {
                Toast.makeText(getApplication(), getResources().getString(R.string.toast_videoerror), Toast.LENGTH_LONG).show();
                break;
            }
            case ON_STARTED_AD: {
                break;
            }
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        Toast.makeText(this, getResources().getString(R.string.toast_initYTsuccess), Toast.LENGTH_SHORT).show();
        YTPlayer = youTubePlayer;
        setYoutubeStateChange();
        YTPlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
            @Override
            public void onFullscreen(boolean b) {
                _isFullScreen = true;
            }
        });
        _state = Constants.STATE.ON_WAIT_ADD_VIDEO;
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(this, getResources().getString(R.string.toast_initYTfail), Toast.LENGTH_SHORT).show();
        _state = Constants.STATE.ON_INIT_FAIL;
        UpdateByState();
    }

    @Override
    public void ReceiverCommandNextFromServer() {
        NextVideo();
    }

    @Override
    public void OnAddVideoFromServer(VideoItem video) {
        VideoWaitsList.add(video);
        if (_state == Constants.STATE.ON_WAIT_ADD_VIDEO) {
            PlayVideo(0);
        } else
            adapterVideoWait.notifyDataSetChanged();
    }

    @Override
    public void OnRemoveVideoFromServer(VideoItem video) {
        for (int i = 0; i < VideoWaitsList.size(); i++) {
            if (VideoWaitsList.get(i).getLinkvideo().compareTo(video.getLinkvideo()) == 0) {
                VideoWaitsList.remove(i);
                adapterVideoWait.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void ReceiverCommandPrioFromServer(String videoID) {
        for (int i = 0; i < VideoWaitsList.size(); i++) {
            if (VideoWaitsList.get(i).getLinkvideo().compareTo(videoID) == 0) {
                MoveVideoToFirst(i);
                break;
            }
        }
    }
    @Override
    public void OnClientRequestFromServer(final String clientName) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.dialog_title_accept_connect);
        dialogBuilder.setMessage(clientName + " ?");
        dialogBuilder.setPositiveButton(R.string.dialog_bt_connect, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                fv.AcceptConnect(clientName);
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
        if(!firebaseID.equalsIgnoreCase("null")) {
            fv.PrioVideo(VideoWaitsList.get(position).getLinkvideo());
        }
        else
        {
            MoveVideoToFirst(position);
        }

    }

    @Override
    public void delfromAdapter(int position) {
        DeleteVideo(position);
    }

    public void save(final int position) {

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
                video.setName(name.getText().toString().trim() + " -" + singer.getText().toString());
                video.setPermit(true);
                video.setAcronyms(getAcronyms(name.getText().toString()));
                int result = db.addVideo(video);
                if (result != -1) {
                    Toast.makeText(ViewingActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ViewingActivity.this, R.string.save_false, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        if (_isFullScreen) {
            YTPlayer.setFullscreen(false);
            _isFullScreen = false;
        } else {
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
    }

    public void onquit() {
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        if(fv!=null) {
            fv.FireBaseUninit();
            fv = null;
        }
        if (YTPlayer != null) {
            YTPlayer.release();
        }
        if (!VideoWaitsList.isEmpty()) {
            VideoWaitsList.clear();
        }
        if (!VideoSearchList.isEmpty()) {
            VideoSearchList.clear();
        }
        super.onDestroy();
    }

}
