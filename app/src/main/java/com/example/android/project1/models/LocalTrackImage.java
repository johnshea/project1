package com.example.android.project1.models;

/**
 * Created by John on 7/3/2015.
 */
public class LocalTrackImage {
    public String url;
    public Integer height;
    public Integer width;

    public LocalTrackImage(String url, Integer width, Integer height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }
}
