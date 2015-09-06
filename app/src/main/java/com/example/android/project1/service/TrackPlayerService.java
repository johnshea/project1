package com.example.android.project1.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.android.project1.Constants;
import com.example.android.project1.models.LocalArtist;
import com.example.android.project1.models.LocalTrack;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by John on 8/2/2015.
 */
public class TrackPlayerService extends Service implements MediaPlayer.OnPreparedListener
, MediaPlayer.OnCompletionListener {

    private final String LOG_TAG = TrackPlayerService.class.getSimpleName();

    private final IBinder mBinder = new LocalBinder();
    private MediaPlayer mMediaPlayer = null;
    private boolean mIsPlaying = false;

    private String mArtistQueryString;
    private LocalArtist mSelectedArtist;

    private ArrayList<LocalTrack> tracks;
    private Integer mCurrentTrackPosition;

    boolean mIsTrackLoaded;
    boolean mIsTrackPlaying;
    int mDuration;
    int mCurrentPosition;
    boolean mIsMediaPlayerPrepared;
    boolean mIsPaused;
    private boolean mJustLoadTrack = false;

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

        if ( mJustLoadTrack ) {

            mIsPlaying = false;
            mIsTrackPlaying = false;
            mIsPaused = true;
            mIsMediaPlayerPrepared = true;
            mDuration = mediaPlayer.getDuration();

            mJustLoadTrack = false;

        } else {

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

            buildNotification("PLAY");

            Intent localIntent = new Intent(Constants.BROADCAST_ACTION_TRACK_UPDATE)
                    .putExtra(Constants.EXTENDED_DATA_TRACK_IS_PLAYING, true)
                    .putExtra(Constants.EXTENDED_DATA_TRACK_DURATION, mediaPlayer.getDuration())
                    .putExtra(Constants.EXTENDED_DATA_TRACK_CURRENT_POSITION, mediaPlayer.getCurrentPosition());

            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        }


            //        TODO This code is used for optional components - commenting out now while working on required functionality
//        // Trying to get to stay in foreground
//        Notification notification = new Notification.Builder(this)
//                .setSmallIcon(R.drawable.ic_action_play)
//                .build();
//        Intent notificationIntent = new Intent(this, MainActivity.class)
//                .putExtra("artistQueryString", mArtistQueryString)
//                .putExtra("artist", mSelectedArtist)
//                .putExtra("track", tracks.get(mCurrentTrackPosition));
//        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 101, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        notification.setLatestEventInfo(getApplicationContext(), "Click To Launch Player", mSelectedArtist.name + " - " + tracks.get(mCurrentTrackPosition).trackName, pendingIntent);
//        startForeground(808, notification);
//        //



//        Log.d(LOG_TAG, "Current position: " + mediaPlayer.getCurrentPosition());
        }

    public void playSong(String url) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
//            mMediaPlayer.setDataSource(url);
            mMediaPlayer.setDataSource(tracks.get(mCurrentTrackPosition).preview_url);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.prepareAsync();
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Unable to playSong");
        }

    }

//    public void pauseSong() {
//        mIsPlaying = false;
//        mMediaPlayer.pause();
//    }

    public void requestUiUpdate() {

        if ( tracks != null ) {

            Intent localIntent = new Intent(Constants.BROADCAST_ACTION)
                    .putExtra(Constants.EXTENDED_DATA_STATUS, tracks.get(mCurrentTrackPosition))
                    .putExtra(Constants.EXTENDED_DATA_TRACK_CURRENT, mCurrentTrackPosition)
                    .putExtra(Constants.EXTENDED_DATA_STATUS_ARTIST_NAME, mSelectedArtist.name);

            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

            localIntent = new Intent(Constants.BROADCAST_ACTION_TRACK_UPDATE)
                    .putExtra(Constants.EXTENDED_DATA_TRACK_IS_PLAYING, mMediaPlayer.isPlaying())
                    .putExtra(Constants.EXTENDED_DATA_TRACK_DURATION, mMediaPlayer.getDuration())
                    .putExtra(Constants.EXTENDED_DATA_TRACK_CURRENT_POSITION, mMediaPlayer.getCurrentPosition())
                    .putExtra(Constants.EXTENDED_DATA_STATUS, tracks.get(mCurrentTrackPosition))
                    .putExtra(Constants.EXTENDED_DATA_STATUS_ARTIST_NAME, mSelectedArtist.name);;

            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        }

    }

    public void previousTrack() {

        unloadTrack();

        if ( mCurrentTrackPosition > 0 ) {
            mCurrentTrackPosition--;
        }

        if ( !isTrackLoaded() ) {
            loadTrack(tracks.get(mCurrentTrackPosition).preview_url);

            mJustLoadTrack = true;
            mMediaPlayer.prepareAsync();


        }

//        TODO This code is used for optional components - commenting out now while working on required functionality
//        Notification notification = new Notification.Builder(this)
//                .setSmallIcon(R.drawable.ic_action_play)
//                .build();
//        Intent notificationIntent = new Intent(this, MainActivity.class)
//                .putExtra("artistQueryString", mArtistQueryString)
//                .putExtra("artist", mSelectedArtist)
//                .putExtra("track", tracks.get(mCurrentTrackPosition));
//        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 101, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        notification.setLatestEventInfo(getApplicationContext(), "Click To Launch Player", mSelectedArtist.name + " - " + tracks.get(mCurrentTrackPosition).trackName, pendingIntent);
//        startForeground(808, notification);

        buildNotification("PREV");

//        Intent localIntent = new Intent(Constants.BROADCAST_ACTION)
//                .putExtra(Constants.EXTENDED_DATA_STATUS, tracks.get(mCurrentTrackPosition).getLargestImageUrl());

        Intent localIntent = new Intent(Constants.BROADCAST_ACTION)
                .putExtra(Constants.EXTENDED_DATA_STATUS, tracks.get(mCurrentTrackPosition))
                .putExtra(Constants.EXTENDED_DATA_TRACK_CURRENT, mCurrentTrackPosition)
                .putExtra(Constants.EXTENDED_DATA_STATUS_ARTIST_NAME, mSelectedArtist.name);

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    public void nextTrack() {

        unloadTrack();

        if ( mCurrentTrackPosition < (tracks.size() - 1) ) {
            mCurrentTrackPosition++;
        }

        if ( !isTrackLoaded() ) {
            loadTrack(tracks.get(mCurrentTrackPosition).preview_url);

            mJustLoadTrack = true;
            mMediaPlayer.prepareAsync();

        }

        //        TODO This code is used for optional components - commenting out now while working on required functionality
//
//        Notification notification = new Notification.Builder(this)
//                .setSmallIcon(R.drawable.ic_action_play)
//                .build();
//        Intent notificationIntent = new Intent(this, MainActivity.class)
//                .putExtra("artistQueryString", mArtistQueryString)
//                .putExtra("artist", mSelectedArtist)
//                .putExtra("track", tracks.get(mCurrentTrackPosition));
//        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 101, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        notification.setLatestEventInfo(getApplicationContext(), "Click To Launch Player", mSelectedArtist.name + " - " + tracks.get(mCurrentTrackPosition).trackName, pendingIntent);
//        startForeground(808, notification);

        buildNotification("NEXT");

//        Intent localIntent = new Intent(Constants.BROADCAST_ACTION)
//                .putExtra(Constants.EXTENDED_DATA_STATUS, tracks.get(mCurrentTrackPosition).getLargestImageUrl());

        Intent localIntent = new Intent(Constants.BROADCAST_ACTION)
                .putExtra(Constants.EXTENDED_DATA_STATUS, tracks.get(mCurrentTrackPosition))
                .putExtra(Constants.EXTENDED_DATA_TRACK_CURRENT, mCurrentTrackPosition)
                .putExtra(Constants.EXTENDED_DATA_STATUS_ARTIST_NAME, mSelectedArtist.name);

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

//    public void restartPlayingSong() {
//        mIsPlaying = true;
//        mMediaPlayer.start();
//    }

//    public void stopSong() {
//        mIsPlaying = false;
//        mMediaPlayer.stop();
//        mMediaPlayer.release();
//        mMediaPlayer = null;
//    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mIsPlaying = false;
        mIsPaused = true;
        mIsTrackPlaying = false;

        Intent localIntent = new Intent(Constants.BROADCAST_ACTION_TRACK_UPDATE)
                .putExtra(Constants.EXTENDED_DATA_TRACK_IS_PLAYING, mp.isPlaying())
                .putExtra(Constants.EXTENDED_DATA_TRACK_DURATION, mp.getDuration())
                .putExtra(Constants.EXTENDED_DATA_TRACK_CURRENT_POSITION, mp.getDuration());

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }

//    public Boolean isPlaying() {
//        return mIsPlaying;
//    }

//    public int getCurrentPosition() {
//        return mMediaPlayer.getCurrentPosition();
//    }

//    public int getDuration() {
//
//        int result = 0;
//
//        if ( mIsMediaPlayerPrepared ) {
//            result = mMediaPlayer.getDuration();
//        } else {
//            result = 30;
//        }
//
//        return result;
//
//    }

    public void loadTracks(String artistQueryString, LocalArtist selectedArtist, ArrayList<LocalTrack> tracks) {
        try {
            this.mArtistQueryString = artistQueryString;
            this.mSelectedArtist = selectedArtist;
            this.tracks = tracks;
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "Unable to loadTracks in Service");
        }
    }

    public void setCurrentTrackPosition(Integer mCurrentTrackPosition) {
        this.mCurrentTrackPosition = mCurrentTrackPosition;
    }

//    public void setupTrack() {
//        this.unloadTrack();
//        this.loadTrack(tracks.get(mCurrentTrackPosition).preview_url);
//    }

    public void loadTrack(String url) {

        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(url);

            mIsTrackLoaded = true;
            mIsMediaPlayerPrepared = false;
            mIsTrackPlaying = false;
            mIsPaused = false;

            mDuration = 0;
            mCurrentPosition = 0;
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Unable to playSong");
        }

    }

    public void unloadTrack() {

        if ( mIsTrackPlaying ) {
            mMediaPlayer.stop();
            mIsTrackPlaying = false;
        }

        if ( mIsTrackLoaded ) {
            mMediaPlayer.reset();
            mIsTrackPlaying = false;
            mIsPaused = false;
            mIsTrackLoaded = false;
            mIsMediaPlayerPrepared = false;
        }

        Intent localIntent = new Intent(Constants.BROADCAST_ACTION_TRACK_UPDATE)
                .putExtra(Constants.EXTENDED_DATA_TRACK_IS_PLAYING, false)
                .putExtra(Constants.EXTENDED_DATA_TRACK_DURATION, 30000)
                .putExtra(Constants.EXTENDED_DATA_TRACK_CURRENT_POSITION, 0);

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }

    public void playPauseTrack() {

        if ( !isTrackLoaded() ) {
            loadTrack(tracks.get(mCurrentTrackPosition).preview_url);
        }

        if ( mIsTrackLoaded && !mIsMediaPlayerPrepared && !mJustLoadTrack) {
            mMediaPlayer.prepareAsync();
        }

        if ( mIsMediaPlayerPrepared && mIsTrackPlaying && !mIsPaused ) {
            mMediaPlayer.pause();
            mIsTrackPlaying = false;
            mIsPaused = true;

            buildNotification("PAUSE");

            Intent localIntent = new Intent(Constants.BROADCAST_ACTION_TRACK_UPDATE)
                    .putExtra(Constants.EXTENDED_DATA_TRACK_IS_PLAYING, mMediaPlayer.isPlaying())
                    .putExtra(Constants.EXTENDED_DATA_TRACK_DURATION, mMediaPlayer.getDuration())
                    .putExtra(Constants.EXTENDED_DATA_TRACK_CURRENT_POSITION, mMediaPlayer.getCurrentPosition());

            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        } else if (mIsMediaPlayerPrepared && !mIsTrackPlaying && mIsPaused) {
            mMediaPlayer.start();
            mIsTrackPlaying = true;
            mIsPaused = false;

            buildNotification("PLAY");

            Intent localIntent = new Intent(Constants.BROADCAST_ACTION_TRACK_UPDATE)
                    .putExtra(Constants.EXTENDED_DATA_TRACK_IS_PLAYING, mMediaPlayer.isPlaying())
                    .putExtra(Constants.EXTENDED_DATA_TRACK_DURATION, mMediaPlayer.getDuration())
                    .putExtra(Constants.EXTENDED_DATA_TRACK_CURRENT_POSITION, mMediaPlayer.getCurrentPosition());

            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        }
    }

//    public void pausePlayTrack() {
//
//    }

    private void buildNotification(String action) {

        return;

//        TODO This code is used for optional components - commenting out now while working on required functionality
//        Intent prevIntent = new Intent(this, TrackPlayerService.class);
//        prevIntent.putExtra("action", "PREVIOUS");
//
//        Intent playPauseIntent = new Intent(this, TrackPlayerService.class);
//        playPauseIntent.putExtra("action", "PLAYPAUSE");
//
//        Intent nextIntent = new Intent(this, TrackPlayerService.class);
//        nextIntent.putExtra("action", "NEXT");
//
//        PendingIntent piPrevIntent = PendingIntent.getService(this, 0, prevIntent, 0);
//        PendingIntent piPlayPauseIntent = PendingIntent.getService(this, 1, playPauseIntent, 0);
//        PendingIntent piNextIntent = PendingIntent.getService(this, 2, nextIntent, 0);
//
//        final NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.drawable.no_album)
//                        .setContentTitle("Now Playing")
//                        .setContentText(tracks.get(mCurrentTrackPosition).trackName)
//                        .setStyle(new NotificationCompat.BigTextStyle().bigText(tracks.get(mCurrentTrackPosition).trackName))
//                        .addAction(android.R.drawable.ic_media_previous, "prev", piPrevIntent)
//                        .addAction(R.drawable.ic_action_play, action, piPlayPauseIntent)
//                        .addAction(android.R.drawable.ic_media_next, "next", piNextIntent);
//
//        NotificationManager mNotificationManager =
//                (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);
//        mNotificationManager.notify(0, mBuilder.build());

    }

    public boolean isTrackLoaded() {
        return mIsTrackLoaded;
    }

//    public boolean isTrackPlaying() {
//        return mIsTrackPlaying;
//    }

//    public boolean isTrackPaused() { return mIsPaused; }
//
//    public int getTrackLength() {
//
//        int result = -1;
//
//        if ( mIsTrackLoaded ) {
//            result = mMediaPlayer.getDuration();
//        } else {
//            result = -1;
//        }
//
//        return mDuration;
//    }

//    public int getTrackCurrentPosition() {
//
//        int result = 0;
//
//        if ( mIsMediaPlayerPrepared ) {
//            result = mMediaPlayer.getCurrentPosition();
//        } else {
//            result = 0;
//        }
//
//        return result;
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if ( intent != null ) {
            Bundle bundle = intent.getExtras();

            if ( bundle != null ) {
                String action = bundle.getString("action");

                switch ( action ) {
                    case "PLAYPAUSE":
                        playPauseTrack();
                        break;
                    case "PREVIOUS":
                        previousTrack();
                        break;
                    case "NEXT":
                        nextTrack();
                        break;
                }
            }

        }

//        return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
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

    public void seekTo(int newPosition) {
        mMediaPlayer.seekTo(newPosition);
    }
}
