package com.totato.karaoke.trung;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.totato.karaoke.trung.adapterlistviewcomponent.AdapterVideoSelectingOffline;
import com.totato.karaoke.trung.adapterlistviewcomponent.AdapterVideoSelectingOnline;
import com.totato.karaoke.trung.adapterlistviewcomponent.AdapterVideoSelectingWait;
import com.totato.karaoke.trung.autocomplete.AutocompleteFinder;
import com.totato.karaoke.trung.autocomplete.IAutocompleteFinder;
import com.totato.karaoke.trung.bluetoothcomponent.BluetoothServiceClient;
import com.totato.karaoke.trung.bluetoothcomponent.DeviceListActivity;
import com.totato.karaoke.trung.databasecomponent.DBSource;
import com.totato.karaoke.trung.youtubecomponent.YoutubeConnector;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SelectingActivity extends Activity implements ListenfromAdapterInterface {
    private TextView tv_info;
    private Button bt_connect_BT;
    private Button bt_turn_BT;
    private Button bt_stop_play;
    private LinearLayout bt_page;
    private ListView lv_songs;
    private DBSource db;

    private Constants.PAGE m_Page;

    AdapterVideoSelectingOffline adapterVideoSelectingOffline;
    AdapterVideoSelectingOnline adapterVideoSelectingOnline;
    AdapterVideoSelectingWait adapterVideoSelectingWait;
    private ArrayList<VideoItem> ListSongSearch;
    private ArrayList<VideoItem> ListSongWait;

    private Handler handlerYoutubeSearch;

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private String mConnectedDeviceName = null;
    private String mdata = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothServiceClient mBTsc = null;

    private AutocompleteFinder autocompleteFinder;
    private AutoCompleteTextView text;
    public List<String> suggest;
    public ArrayAdapter<String> AdapterAutoComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecting);
        init();
    }

    private void init() {
        tv_info = (TextView) findViewById(R.id.tv_infor);
        bt_turn_BT = (Button) findViewById(R.id.bt_selecting_turn_BT);
        bt_connect_BT = (Button) findViewById(R.id.bt_selecting_connect_BT);
        bt_stop_play = (Button) findViewById(R.id.bt_selecting_stop);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBTsc = new BluetoothServiceClient(getApplication(), mHandler);

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
                if(m_Page==Constants.PAGE.SEARCH_ONLINE)
                    queryGoogleAutocomplete(text.getText().toString());
                else if (m_Page == Constants.PAGE.SEARCH_OFFLINE && !s.toString().equals("")) {
                    ListSongSearch = db.SearchbyAcronyms(s.toString());
                    adapterVideoSelectingOffline = new AdapterVideoSelectingOffline(ListSongSearch, getLayoutInflater(), getApplicationContext());
                    lv_songs.setAdapter(adapterVideoSelectingOffline);
                }
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

        ListSongSearch = new ArrayList<>();
        ListSongSearch = new ArrayList<>();
        ListSongWait = new ArrayList<>();
        adapterVideoSelectingOnline = new AdapterVideoSelectingOnline(ListSongSearch, getLayoutInflater(), this);
        adapterVideoSelectingOffline = new AdapterVideoSelectingOffline(ListSongSearch, getLayoutInflater(), this);
        adapterVideoSelectingWait = new AdapterVideoSelectingWait(ListSongWait, getLayoutInflater());
        updateDisplayStateBT();
        updateDisplayConnectBT();
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
            case R.id.bt_selecting_connect_BT:
                ConnectBT();
                break;
            case R.id.bt_selecting_turn_BT:
                TurnOnBT();
                break;
            case R.id.bt_selecting_stop:
                StopPlayVideo();
                break;
            case R.id.bt_selecting_delete_text:
                text.setText("");
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
                ListSongSearch = yc.search(keywords);
                handlerYoutubeSearch.post(new Runnable() {
                    public void run() {
                        adapterVideoSelectingOnline = new AdapterVideoSelectingOnline(ListSongSearch, getLayoutInflater(), getApplicationContext());
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
            Toast.makeText(getApplication(), "search on offline", Toast.LENGTH_SHORT).show();
            ListSongSearch = db.getListSong();
            adapterVideoSelectingOffline = new AdapterVideoSelectingOffline(ListSongSearch, getLayoutInflater(), this);
            lv_songs.setAdapter(adapterVideoSelectingOffline);
        } else {
            Toast.makeText(getApplication(), "search on list selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void Add(String link, String name) {
        TranferBT(Constants.ADD, link, name);
    }

    private void DeleteVideo(int position) {
        TranferBT(Constants.DEL, ListSongWait.get(position).getLinkvideo());
    }

    private void MoveVideoToFirst(int position) {
        TranferBT(Constants.PRIO, ListSongWait.get(position).getLinkvideo());
    }

    private void StopPlayVideo() {
        TranferBT(Constants.STOP);
    }

    private void StopPlayVideo(boolean play) {
        if (play)
            bt_stop_play.setBackgroundResource(R.drawable.buton_play);
        else
            bt_stop_play.setBackgroundResource(R.drawable.buton_stop);
    }

    private void NextVideo() {
        TranferBT(Constants.NEXT);
    }

    private void TurnOnBT() {
        if (!mBluetoothAdapter.isEnabled())
            startIntent(Constants.INTENTBT.TURN_ON_BLUETOOTH);
        else
            Toast.makeText(getApplication(), getResources().getString(R.string.toast_bton), Toast.LENGTH_SHORT).show();
        updateDisplayStateBT();
    }

    private void ConnectBT() {
        if (!mBluetoothAdapter.isEnabled())
            startIntent(Constants.INTENTBT.TURN_ON_BLUETOOTH);
        else if (mBTsc.getState() != BluetoothServiceClient.STATE_CONNECTED)
            startIntent(Constants.INTENTBT.SCAN_CONNECT_DEVICE);
        else
            Toast.makeText(getApplication(), getResources().getString(R.string.toast_btconnect), Toast.LENGTH_SHORT).show();
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
        if (mBTsc == null || mBTsc.getState() != BluetoothServiceClient.STATE_CONNECTED) {
            bt_connect_BT.setBackground(this.getResources().getDrawable(R.drawable.buton_bluetooth_disconnect));
        } else {
            bt_connect_BT.setBackground(this.getResources().getDrawable(R.drawable.buton_bluetooth_connected));
        }
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
            ListSongSearch.clear();
            ListSongSearch.clear();
        } else if (m_Page == Constants.PAGE.SEARCH_ONLINE) {
            ListSongSearch.clear();
            lv_songs.setAdapter(adapterVideoSelectingOnline);
        } else if (m_Page == Constants.PAGE.SEARCH_OFFLINE) {
            ListSongSearch.clear();
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
        super.onDestroy();
        if (mBTsc != null) {
            mBTsc.stop();
            mBTsc.destroy();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBTsc == null) {
            mBTsc = new BluetoothServiceClient(this, mHandler);
        }
    }

    private void startIntent(Constants.INTENTBT intentbt) {
        Intent intent;
        switch (intentbt) {
            case TURN_ON_BLUETOOTH: {
                intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BT);
                break;
            }
            case SCAN_CONNECT_DEVICE: {
                intent = new Intent(getApplication(), DeviceListActivity.class);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
                break;
            }
        }
    }

    private void sendMessage(String message) {
        if (mBTsc == null || mBTsc.getState() != BluetoothServiceClient.STATE_CONNECTED) {
            Toast.makeText(getApplication(), getResources().getString(R.string.toast_dontconnect), Toast.LENGTH_SHORT).show();
            return;
        }
        if (message.length() > 0) {

            byte[] send = message.getBytes();
            mBTsc.write(send);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                } else {
                    Toast.makeText(getApplication(), getResources().getString(R.string.toast_dontpermit),
                            Toast.LENGTH_SHORT).show();
                }
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

    private void connectDevice(Intent data) {
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);// Get the device MAC address
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);// Get the BluetoothDevice object
        mBTsc.connect(device);// Attempt to buton_bluetooth_connect to the device
    }

    private void TranferBT(String type, String... data) {
        switch (type) {
            case Constants.ADD:
                mdata = Constants.ADD + data[0] + Constants.DIV + data[1];   //data[0] link, data[1] name
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
                Toast.makeText(getApplication(), "Đã nhận add ", Toast.LENGTH_LONG);
                break;
            case Constants.DEL:
                break;
            case Constants.PRIO:
                break;
            case Constants.PLAY:
                StopPlayVideo(false);
                break;
            case Constants.STOP:
                StopPlayVideo(true);
                break;
            case Constants.NEXT:
                NextVideo();
                break;
            case Constants.QUIT:
                break;
        }
    }

    @Override
    public void choose(int position) {
        TranferBT(Constants.ADD, ListSongSearch.get(position).getLinkvideo(), ListSongSearch.get(position).getName());
        Toast.makeText(this, "click choose", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void edit(final int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_layout, null);
        dialogBuilder.setView(dialogView);
        final EditText name = (EditText) dialogView.findViewById(R.id.tv_dialog_name);
        final EditText singer = (EditText) dialogView.findViewById(R.id.tv_dialog_singer);
        final VideoItem video = ListSongSearch.get(position);
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
    public void save(final int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_layout, null);
        dialogBuilder.setView(dialogView);
        final TextView title = (TextView) dialogView.findViewById(R.id.tv_dialog_title);
        final EditText name = (EditText) dialogView.findViewById(R.id.tv_dialog_name);
        final EditText singer = (EditText) dialogView.findViewById(R.id.tv_dialog_singer);
        title.setText(ListSongSearch.get(position).getName());
        dialogBuilder.setTitle(R.string.dialog_title_save);
        dialogBuilder.setMessage(R.string.dialog_input_infor);
        dialogBuilder.setPositiveButton(R.string.dialog_bt_save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                VideoItem video = ListSongSearch.get(position);
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
    public void prio(int position) {

    }

    @Override
    public void del(int position) {
        TranferBT(Constants.DEL, ListSongWait.get(position).getLinkvideo());
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
