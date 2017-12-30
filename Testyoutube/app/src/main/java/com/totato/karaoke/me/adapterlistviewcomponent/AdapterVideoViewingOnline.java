package com.totato.karaoke.me.adapterlistviewcomponent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.totato.karaoke.me.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Trung on 16/12/2016.
 */

public class AdapterVideoViewingOnline extends BaseAdapter {
    ArrayList<VideoItem> data;
    LayoutInflater inflater;
    Context context;

    public AdapterVideoViewingOnline(ArrayList<VideoItem> data, LayoutInflater inflater, Context context) {
        this.data = data;
        this.inflater = inflater;
        this.context = context;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.video_found_viewing_online_item, null);
            holder = new Holder();
            holder.title = (TextView) convertView.findViewById(R.id.video_title);
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.video_thumbnail);
            convertView.setTag(holder);
        }
        else {
            holder = (Holder)convertView.getTag();
        }
        String text = getItem(position).getName();
        if(text.length()>35)
            text = text.substring(0,30);
        holder.title.setText(text);
        Picasso.with(context).load(getItem(position).getLinkimage()).into(holder.thumbnail);
        return convertView;
    }

    static class Holder {
        ImageView thumbnail;
        TextView title;
    }
}
