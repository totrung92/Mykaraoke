package com.totato.karaoke.me.firebasecomponent;

import com.totato.karaoke.me.adapterlistviewcomponent.VideoItem;

/**
 * Created by Trung on 03-Apr-17.
 */

public interface ListenfromFireBaseInterface {
    public void ReceiverCommandNextFromServer();
    public void OnAddVideoFromServer(VideoItem video);
    public void OnRemoveVideoFromServer(VideoItem video);
    public void ReceiverCommandPrioFromServer(String videoID);
    public void OnClientRequestFromServer(String clientName);
}
