package com.example.android.project1.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

/**
 * Created by John on 8/2/2015.
 */
public class TrackPlayerService extends Service implements MediaPlayer.OnPreparedListener
, MediaPlayer.OnCompletionListener {

    private final String LOG_TAG = TrackPlayerService.class.getSimpleName();

    private final IBinder mBinder = new LocalBinder();
    private MediaPlayer mMediaPlayer = null;
    private boolean mIsPlaying = false;

    public class LocalBinder extends Binder {
        public TrackPlayerService getService() {
            return TrackPlayerService.this;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onPrepared(final MediaPlayer mediaPlayer) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.start();
            }
        }, "Thread_MediaPlayer").start();

        mIsPlaying = true;
        mIsTrackPlaying = true;
        mIsPaused = false;
        mIsMediaPlayerPrepared = true;
        mDuration = mediaPlayer.getDuration();

//        Log.d(LOG_TAG, "Current position: " + mediaPlayer.getCurrentPosition());
    }

    public void playSong(String url) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.prepareAsync();
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Unable to playSong");
        }

    }

    public void pauseSong() {
        mIsPlaying = false;
        mMediaPlayer.pause();
    }

    public void restartPlayingSong() {
        mIsPlaying = true;
        mMediaPlayer.start();
    }

    public void stopSong() {
        mIsPlaying = false;
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mIsPlaying = false;
    }

    public Boolean isPlaying() {
        return mIsPlaying;
    }

    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    boolean mIsTrackLoaded;
    boolean mIsTrackPlaying;
    int mDuration;
    int mCurrentPosition;
    boolean mIsMediaPlayerPrepared;
    boolean mIsPaused;

    public void loadTrack(String url) {

        try {
            if ( mIsTrackLoaded ) {
                mMediaPlayer.stop();
                mIsTrackPlaying = false;
                mDuration = 0;
                mCurrentPosition = 0;
            }
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(url);
            mIsTrackLoaded = true;
            mIsMediaPlayerPrepared = false;
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Unable to playSong");
        }
    }

    public void playPauseTrack() {
        if ( mIsTrackLoaded && !mIsMediaPlayerPrepared ) {
            mMediaPlayer.prepareAsync();
        }

        if ( mIsMediaPlayerPrepared && mIsTrackPlaying && !mIsPaused ) {
            mMediaPlayer.pause();
            mIsTrackPlaying = false;
            mIsPaused = true;
        } else if (mIsMediaPlayerPrepared && !mIsTrackPlaying && mIsPaused) {
            mMediaPlayer.start();
            mIsTrackPlaying = true;
            mIsPaused = false;
        }
    }

//    public void pausePlayTrack() {
//
//    }

    public boolean isTrackLoaded() {
        return mIsTrackLoaded;
    }

    public boolean isTrackPlaying() {
        return mIsTrackPlaying;
    }

    public boolean isTrackPaused() { return mIsPaused; }

    public int getTrackLength() {

//        int result = -1;
//
//        if ( mIsTrackLoaded ) {
//            result = mMediaPlayer.getDuration();
//        } else {
//            result = -1;
//        }

        return mDuration;
    }

    public int getTrackCurrentPosition() {

        int result = 0;

        if ( mIsMediaPlayerPrepared ) {
            result = mMediaPlayer.getCurrentPosition();
        } else {
            result = 0;
        }

        return result;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);

        mIsTrackLoaded = false;
        mIsTrackPlaying = false;
        mIsPaused = false;
        mIsMediaPlayerPrepared = false;

        mDuration = 0;
        mCurrentPosition = 0;

    }
}
