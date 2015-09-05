package com.example.android.project1.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by John on 7/3/2015.
 */
public class LocalImage implements Parcelable {
    public String url;
    public Integer height;
    public Integer width;

    public LocalImage(String url, Integer width, Integer height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeInt(width);
        dest.writeInt(height);
    }

    private LocalImage(Parcel in) {
        url = in.readString();
        width = in.readInt();
        height = in.readInt();
    }

    public static final Parcelable.Creator<LocalImage> CREATOR =
            new Parcelable.Creator<LocalImage>() {
                @Override
                public LocalImage createFromParcel(Parcel source) {
                    return new LocalImage(source);
                }

                @Override
                public LocalImage[] newArray(int size) {
                    return new LocalImage[size];
                }
            };
}
