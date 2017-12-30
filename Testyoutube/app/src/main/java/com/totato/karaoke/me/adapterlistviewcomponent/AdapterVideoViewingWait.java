package com.totato.karaoke.me.adapterlistviewcomponent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.totato.karaoke.me.R;

import java.util.ArrayList;

/**
 * Created by Trung on 16/12/2016.
 */

public class AdapterVideoViewingWait extends BaseAdapter {
    ArrayList<VideoItem> data;
    LayoutInflater inflater;
    ListenfromAdapterViewingInterface listen;
    public AdapterVideoViewingWait(ArrayList<VideoItem> data, LayoutInflater inflater, ListenfromAdapterViewingInterface listen ) {
        this.data = data;
        this.inflater = inflater;
        this.listen = listen;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public VideoItem getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.video_wait_item, null);
            holder = new Holder();
            holder.title = (TextView) convertView.findViewById(R.id.player_title);
            holder.bt_prio = (Button) convertView.findViewById(R.id.bt_player_tofirst);
            holder.bt_del = (Button) convertView.findViewById(R.id.bt_player_delete);
            convertView.setTag(holder);
        }
        else {
            holder = (Holder)convertView.getTag();
        }
        holder.title.setText(getItem(position).getName());
        holder.bt_prio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listen.priofromAdapter(position);
            }
        });
        holder.bt_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listen.delfromAdapter(position);
            }
        });
        return convertView;
    }
    static class Holder {
        Button bt_prio;
        Button bt_del;
        TextView title;
    }
}
