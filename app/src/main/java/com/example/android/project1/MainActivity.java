package com.example.android.project1;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.project1.models.LocalArtist;
import com.example.android.project1.models.LocalTrack;
import com.example.android.project1.service.TrackPlayerService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity
        implements MainActivityFragment.OnArtistSelectedListener, TrackActivityFragment.OnTrackSelectedListener
        , TrackPlayerActivityFragment.TrackPlayerActivityListener {

    private boolean mDualPane;
    private String mArtistName;
    ServiceStatusReceiver mServiceStatusReceiver;

    @Override
    protected void onPause() {
        super.onPause();

        if ( mServiceStatusReceiver != null ) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mServiceStatusReceiver);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set-up receiver
        IntentFilter mStatusIntentFilter = new IntentFilter(
                Constants.BROADCAST_ACTION);

        mServiceStatusReceiver = new ServiceStatusReceiver();

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mServiceStatusReceiver,
                mStatusIntentFilter);

        mStatusIntentFilter = new IntentFilter(
                Constants.BROADCAST_ACTION_TRACK_UPDATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mServiceStatusReceiver,
                mStatusIntentFilter);

    }

    @Override
    public void OnTrackSelectedListener(ArrayList<LocalTrack> tracks, Integer position) {
//        Toast.makeText(this, "(MainActivity) Track selected: " + localTrack.trackName.toString(), Toast.LENGTH_SHORT).show();

        mTrackPlayerService.loadTracks(mArtistName, tracks);
        mTrackPlayerService.setCurrentTrackPosition(position);
        mTrackPlayerService.playPauseTrack();

        FragmentManager fragmentManager = getSupportFragmentManager();
        TrackPlayerActivityFragment trackPlayerActivityFragment = (TrackPlayerActivityFragment) fragmentManager.findFragmentByTag("dialog");

        if ( trackPlayerActivityFragment == null ) {
            trackPlayerActivityFragment = new TrackPlayerActivityFragment();
        }

        trackPlayerActivityFragment.setValues(mArtistName, tracks, position);

        if( mDualPane ) {
            trackPlayerActivityFragment.show(fragmentManager, "dialog");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.track_list_container) != null) {
            mDualPane = true;
        } else {
            mDualPane = false;
        }

//        if ( savedInstanceState != null ) {
//
//            MainActivityFragment mainActivityFragment;
//            mainActivityFragment = (MainActivityFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mainActivityFragment");
//
//            mainActivityFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
//
//            FragmentManager fm = getSupportFragmentManager();
//            FragmentTransaction fragmentTransaction = fm.beginTransaction();
//            fragmentTransaction.replace(R.id.fragment, mainActivityFragment);
//            fragmentTransaction.commit();
//        }

        // Start up service
        // bind - so we can call its methods
        // startService - so it stays around indefinitely
        Intent intent = new Intent(this, TrackPlayerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        startService(intent);

    }

    public void onArtistSelected(LocalArtist localArtist) {

        if (mDualPane) {

            ActionBar actionBar = this.getSupportActionBar();
            if ( actionBar != null ) {
                actionBar.setSubtitle(localArtist.name);
            }

            TrackActivityFragment tracksActivityFragment;

            FragmentManager fm = getSupportFragmentManager();
            tracksActivityFragment = (TrackActivityFragment) fm.findFragmentByTag("track");

            FragmentTransaction fragmentTransaction = fm.beginTransaction();

            if ( tracksActivityFragment == null ) {
                tracksActivityFragment = new TrackActivityFragment();
                fragmentTransaction.add(R.id.track_list_container, tracksActivityFragment, "track");
                fragmentTransaction.commit();
            }

            String id = localArtist.id;
            mArtistName = localArtist.name;

            if ( id != null && mArtistName != null ) {
                tracksActivityFragment.setValues(id, mArtistName);
                TrackFrameLayout trackFrameLayout = (TrackFrameLayout) findViewById(R.id.track_list_container);

                if (localArtist.artistImages.size() > 0) {
                    String imageUrl = localArtist.getThumbnailUrl();
                    Picasso.with(this).load(imageUrl)
                            .into(trackFrameLayout);

                } else {
                    Picasso.with(this).load(R.drawable.no_album)
                            .into(trackFrameLayout);
                }

            }
        } else {

            Intent intent = new Intent(this, TrackActivity.class);
            intent.putExtra("id", localArtist.id);
            intent.putExtra("artist", localArtist.name);
            intent.putExtra("image", localArtist.getLargestImageUrl());

            startActivity(intent);

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//        MainActivityFragment mainActivityFragment;
//        mainActivityFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
//        getSupportFragmentManager().putFragment(outState, "mainActivityFragment", mainActivityFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickNextTrack() {
        mTrackPlayerService.nextTrack();
    }

    @Override
    public void onClickPreviousTrack() {
        mTrackPlayerService.previousTrack();
    }

    @Override
    public void onClickPlayPauseTrack() {
        mTrackPlayerService.playPauseTrack();
    }

    private TrackPlayerService mTrackPlayerService;
    private Boolean mBound = false;

    @Override
    protected void onStop() {
        super.onStop();

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TrackPlayerService.LocalBinder binder = (TrackPlayerService.LocalBinder) service;
            mTrackPlayerService = binder.getService();
            mBound = true;

//            mTrackPlayerService.loadTracks(tracks);
//            mTrackPlayerService.setCurrentTrackPosition(currentTrackPosition);

//            if ( mTrackPlayerService.isTrackLoaded() ) {
//                final SeekBar seekBar = (SeekBar) getView().findViewById(R.id.seekBar);
//                seekBar.setMax(mTrackPlayerService.getDuration());
//
//                RunnableProgress r = new RunnableProgress(seekBar);
//
//                mMoveSeekBarThread = new Thread(r, "Thread_mMoveSeekBarThread");
//                mMoveSeekBarThread.start();
//            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    private class ServiceStatusReceiver extends BroadcastReceiver {

        private ServiceStatusReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(context, "move next - Track image changed", Toast.LENGTH_SHORT).show();
            FragmentManager fragmentManager = getSupportFragmentManager();
            TrackPlayerActivityFragment trackPlayerActivityFragment = (TrackPlayerActivityFragment) fragmentManager.findFragmentByTag("dialog");

            if ( trackPlayerActivityFragment == null ) {
                trackPlayerActivityFragment = new TrackPlayerActivityFragment();
            }

            String action = intent.getAction();
            int trackDuration;

            switch ( action ) {
                case Constants.BROADCAST_ACTION:

                    String artistName = intent.getStringExtra(Constants.EXTENDED_DATA_STATUS_ARTIST_NAME);
                    LocalTrack localTrack = (LocalTrack) intent.getParcelableExtra(Constants.EXTENDED_DATA_STATUS);
//            trackPlayerActivityFragment.updateImage(intent.getStringExtra(Constants.EXTENDED_DATA_STATUS));
                    trackPlayerActivityFragment.updateViews(artistName, localTrack);

                    break;

//                case Constants.BROADCAST_ACTION_TRACK_STARTED:
//
//                    trackDuration = intent.getIntExtra(Constants.EXTENDED_DATA_TRACK_DURATION, 30);
//                    trackPlayerActivityFragment.updateSeekbar(trackDuration);
//
//                    break;

                case Constants.BROADCAST_ACTION_TRACK_UPDATE:

                    boolean isPlaying = intent.getBooleanExtra(Constants.EXTENDED_DATA_TRACK_IS_PLAYING, false);
                    trackDuration = intent.getIntExtra(Constants.EXTENDED_DATA_TRACK_DURATION, 30);
                    int currentTrackPosition = intent.getIntExtra(Constants.EXTENDED_DATA_TRACK_CURRENT_POSITION, 0);

                    trackPlayerActivityFragment.updateSeekbar(isPlaying, trackDuration, currentTrackPosition);

                    break;
            }
        }
    }
}
