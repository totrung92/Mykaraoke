package com.totato.karaoke.trung;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.totato.karaoke.trung.adapterlistviewcomponent.AdapterVideoViewingOnline;
import com.totato.karaoke.trung.adapterlistviewcomponent.AdapterVideoViewingWait;
import com.totato.karaoke.trung.autocomplete.AutocompleteFinder;
import com.totato.karaoke.trung.autocomplete.IAutocompleteFinder;
import com.totato.karaoke.trung.bluetoothcomponent.BluetoothServiceServer;
import com.totato.karaoke.trung.databasecomponent.DBSource;
import com.totato.karaoke.trung.youtubecomponent.YoutubeConnector;
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
public class ViewingActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {
    private ListView lv_player_wait;
    private ListView lv_player_search;
    private Handler handler;
    private ArrayList<VideoItem> VideoSearchList;
    private ArrayList<VideoItem> VideoWaitsList;
    private YouTubePlayerView playerView;
    private YouTubePlayer YTPlayer;

    private Button bt_stop_play;
    private TextView tv_info;
    private Button bt_connect_BT;
    private Button bt_turn_BT;

    private RadioGroup rg_type_input;

    private Constants.STATE _state;
    private Boolean _isFullScreen;

    private static final int REQUEST_ENABLE_BT = 3;
    private String mConnectedDeviceName = null;
    private String mdata = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothServiceServer mBTSS = null;
    private String type;


    private AdapterVideoViewingWait adapterVideoWait;
    private AdapterVideoViewingOnline adapterVideoViewingOnline;

    private AutocompleteFinder autocompleteFinder;
    private AutoCompleteTextView text;
    public List<String> suggest;
    public ArrayAdapter<String> AdapterAutoComplete;

    private DBSource db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewing);
        init();
        addClickListener();
    }

    private void init() {
        rg_type_input = (RadioGroup) findViewById(R.id.rg_viewing_type_search);
        type = "online";
        bt_stop_play = (Button) findViewById(R.id.bt_viewing_stop);
        tv_info = (TextView) findViewById(R.id.tv_infor);

        bt_turn_BT = (Button) findViewById(R.id.bt_viewing_turn_BT);
        bt_connect_BT = (Button) findViewById(R.id.bt_viewing_connect_BT);

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
                if(type.equals("online"))
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
        VideoWaitsList = new ArrayList<VideoItem>();
        VideoSearchList = new ArrayList<VideoItem>();
        handler = new Handler();

        _state = Constants.STATE.ON_INIT;
        _isFullScreen = false;

        db = new DBSource(this);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        adapterVideoWait = new AdapterVideoViewingWait(VideoWaitsList, getLayoutInflater());
        lv_player_wait.setAdapter(adapterVideoWait);

        updateDisplayStateBT();
        updateDisplayConnectBT();
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
            case R.id.bt_viewing_connect_BT:
                ConnectBT();
                break;
            case R.id.bt_viewing_turn_BT:
                TurnOnBT();
                break;
            case R.id.bt_viewing_stop:
                StopPlayVideo();
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
        bt_stop_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopPlayVideo();
            }
        });
        bt_turn_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TurnOnBT();
            }
        });
        bt_connect_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectBT();
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
            }
        });
    }

    public void AddVideo(VideoItem video) {
        VideoWaitsList.add(video);
        if (_state == Constants.STATE.ON_WAIT_ADD_VIDEO) {
            PlayVideo(0);
        } else
            adapterVideoWait.notifyDataSetChanged();
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
        VideoWaitsList.remove(position);
        adapterVideoWait.notifyDataSetChanged();
    }

    public void StopPlayVideo() {
        if (YTPlayer.isPlaying()) {
            YTPlayer.pause();
            bt_stop_play.setBackgroundResource(R.drawable.buton_play);
            TranferBT(Constants.STOP);
        } else {
            YTPlayer.play();
            bt_stop_play.setBackgroundResource(R.drawable.buton_stop);
            TranferBT(Constants.PLAY);
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

    public void TurnOnBT() {
        if (!mBluetoothAdapter.isEnabled())
            startIntent(Constants.INTENTBT.TURN_ON_BLUETOOTH);
        else
            Toast.makeText(getApplication(), getResources().getString(R.string.toast_bton), Toast.LENGTH_SHORT).show();
        updateDisplayStateBT();
    }

    public void ConnectBT() {
        if (mBTSS == null) {
            mBTSS = new BluetoothServiceServer(getApplication(), mHandler);
            startIntent(Constants.INTENTBT.TURN_ON_DISCOVERABLE);
        } else if (mBTSS.getState() == BluetoothServiceServer.STATE_LISTEN)
            mBTSS.stop();
        else if (mBTSS.getState() == BluetoothServiceServer.STATE_NONE) {
            startIntent(Constants.INTENTBT.TURN_ON_DISCOVERABLE);
            mBTSS.start();
        } else if (mBTSS.getState() == BluetoothServiceServer.STATE_CONNECTED)
            Toast.makeText(getApplication(), getResources().getString(R.string.toast_btconnect), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplication(), getResources().getString(R.string.toast_btconnecting), Toast.LENGTH_SHORT).show();
        updateDisplayConnectBT();
    }

    private void updateDisplayStateBT() {
        if (mBluetoothAdapter.isEnabled()) {
            bt_turn_BT.setBackground(this.getResources().getDrawable(R.drawable.buton_bluetooth_onl));
        } else {
            bt_turn_BT.setBackground(this.getResources().getDrawable(R.drawable.buton_bluetooth_off));
        }
    }

    private void updateDisplayConnectBT() {
        if (mBTSS == null || mBTSS.getState() == BluetoothServiceServer.STATE_NONE || mBTSS.getState() == BluetoothServiceServer.STATE_OFF)
            bt_connect_BT.setBackground(this.getResources().getDrawable(R.drawable.buton_bluetooth_disconnect));
        else if (mBTSS.getState() == BluetoothServiceServer.STATE_LISTEN || mBTSS.getState() == BluetoothServiceServer.STATE_CONNECTING)
            bt_connect_BT.setBackground(this.getResources().getDrawable(R.drawable.buton_bluetooth_connect));
        else {
            bt_connect_BT.setBackground(this.getResources().getDrawable(R.drawable.buton_bluetooth_connected));
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

    private void startIntent(Constants.INTENTBT intentbt) {
        Intent intent;
        switch (intentbt) {
            case TURN_ON_BLUETOOTH: {
                intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BT);
                break;
            }
            case TURN_ON_DISCOVERABLE: {
                if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(intent);
                }
                break;
            }
        }
    }

    private void sendMessage(String message) {
        // Check that we're actually buton_bluetooth_connected before trying anything
        if (mBTSS == null || mBTSS.getState() != BluetoothServiceServer.STATE_CONNECTED) {
            Toast.makeText(getApplication(), getResources().getString(R.string.toast_dontconnect), Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {

            byte[] send = message.getBytes(); //
            mBTSS.write(send);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    mBTSS = new BluetoothServiceServer(getApplication(), mHandler);
                    updateDisplayStateBT();
                } else {
                    Toast.makeText(getApplication(), getResources().getString(R.string.toast_dontpermit),
                            Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBTSS != null) {
            mBTSS.stop();
            mBTSS.destroy();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mBluetoothAdapter.isEnabled() && mBTSS == null) {
            mBTSS = new BluetoothServiceServer(getApplication(), mHandler);
        }

    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    updateDisplayConnectBT();
                    updateDisplayStateBT();
                    break;
                case Constants.MESSAGE_WRITE:
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    ReceiverBT(readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != getApplication()) {
                        Toast.makeText(getApplication(), getResources().getString(R.string.toast_connect)
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != getApplication()) {
                        Toast.makeText(getApplication(), msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };


    private void TranferBT(String type, String... data) {
        switch (type) {
            case Constants.ADD:
                mdata = Constants.ADD + data[0] + Constants.DIV + data[1];   //data[0] id, data[1] title
                sendMessage(mdata);
                Toast.makeText(getApplication(), "Đã gửi add ", Toast.LENGTH_LONG);
                break;
            case Constants.DEL:
                mdata = Constants.DEL + data;                             //data[0] pos
                sendMessage(mdata);
                break;
            case Constants.PRIO:
                mdata = Constants.PRIO + data;                            //data[0] pos
                sendMessage(mdata);
                break;
            case Constants.PLAY:
                mdata = Constants.PLAY;
                sendMessage(mdata);
                break;
            case Constants.STOP:
                mdata = Constants.STOP;
                sendMessage(mdata);
                break;
            case Constants.NEXT:
                mdata = Constants.NEXT;
                sendMessage(mdata);
                break;
            case Constants.QUIT:
                mdata = Constants.QUIT;
                sendMessage(mdata);
                break;
        }
    }

    private void ReceiverBT(String data) {
        String type = data.substring(0, 5);
        data = data.substring(5);
        switch (type) {
            case Constants.ADD:
                VideoItem item = new VideoItem();
                int div = data.indexOf(Constants.DIV);
                String id = data.substring(0, div);
                data = data.substring(div + 5);
                item.setLinkvideo(id);
                item.setName(data);
                AddVideo(item);
                Toast.makeText(getApplication(), "Đã nhận add ", Toast.LENGTH_LONG);
                break;
            case Constants.DEL:
                for (int i = 0; i < VideoWaitsList.size(); i++) {
                    if (VideoWaitsList.get(i).getLinkvideo().compareTo(data) == 0) {
                        DeleteVideo(i);
                        break;
                    }
                }
                break;
            case Constants.PRIO:
                for (int i = 0; i < VideoWaitsList.size(); i++) {
                    if (VideoWaitsList.get(i).getLinkvideo().compareTo(data) == 0) {
                        MoveVideoToFirst(i);
                        break;
                    }
                }
                break;
            case Constants.PLAY:
            case Constants.STOP:
                StopPlayVideo();
                break;
            case Constants.NEXT:
                NextVideo();
                break;
            case Constants.QUIT:

                break;
        }
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
}
