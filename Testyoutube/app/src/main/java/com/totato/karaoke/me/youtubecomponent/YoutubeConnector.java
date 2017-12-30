package com.totato.karaoke.me.youtubecomponent;

import android.content.Context;
import android.util.Log;

import com.totato.karaoke.me.adapterlistviewcomponent.VideoItem;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Trung on 01/10/2016.
 */
public class YoutubeConnector {
    private YouTube youtube;
    private YouTube.Search.List query;

    // Your developer key goes here
    public static final String KEY
            = "AIzaSyC401rXp0VAn_dRFHpeFdngSGFavJUqrok";

    public YoutubeConnector(Context context) {
        youtube = new YouTube.Builder(new NetHttpTransport(),
                new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {}
        }).setApplicationName("Trung test Youtube").build();

        try{
            query = youtube.search().list("id,snippet");
            query.setKey(KEY);
            query.setType("video");
            query.setFields("items(id/videoId,snippet/title,snippet/thumbnails/default/url)");
            query.setMaxResults((long) 15);
        }catch(IOException e){
            Log.d("YC", "Could not initialize: "+e);
        }
    }
    public ArrayList<VideoItem> search(String keywords){
        query.setQ(keywords);
        try{
            SearchListResponse response = query.execute();
            List<SearchResult> results = response.getItems();
            ArrayList<VideoItem> items = new ArrayList<VideoItem>();
            for(SearchResult result:results){
                VideoItem item = new VideoItem();
                item.setName(result.getSnippet().getTitle());
                item.setLinkimage(result.getSnippet().getThumbnails().getDefault().getUrl());
                item.setLinkvideo(result.getId().getVideoId());
                items.add(item);
            }
            return items;
        }catch(IOException e){
            Log.e("karaoke", "Could not search: "+e);
            return null;
        }
    }
}
