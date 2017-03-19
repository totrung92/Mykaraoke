package com.totato.karaoke.trung;

/**
 * Created by Trung on 01/10/2016.
 */
public class VideoItem {
    private int id;
    private String name;
    private String linkvideo;
    private String linkimage;
    private String acronyms;
    private boolean permit;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLinkvideo() {
        return linkvideo;
    }

    public void setLinkvideo(String linkvideo) {
        this.linkvideo = linkvideo;
    }

    public String getLinkimage() {
        return linkimage;
    }

    public void setLinkimage(String linkimage) {
        this.linkimage = linkimage;
    }

    public String getAcronyms() {
        return acronyms;
    }

    public void setAcronyms(String acronyms) {
        this.acronyms = acronyms;
    }

    public boolean isPermit() {
        return permit;
    }

    public void setPermit(boolean permit) {
        this.permit = permit;
    }
}
