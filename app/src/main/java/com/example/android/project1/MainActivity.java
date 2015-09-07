package com.example.android.project1;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.ShareActionProvider;
import android.widget.Toast;

import com.example.android.project1.models.LocalArtist;
import com.example.android.project1.models.LocalTrack;
import com.example.android.project1.service.TrackPlayerService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity
        implements MainActivityFragment.OnArtistSelectedListener, TrackActivityFragment.OnTrackSelectedListener
        , TrackPlayerActivityFragment.TrackPlayerActivityListener {

    private boolean mDualPane;
    private LocalArtist mSelectedArtist;
    private String mArtistName;
    private String mArtistQueryString = "";
    ServiceStatusReceiver mServiceStatusReceiver;

    private TrackPlayerService mTrackPlayerService;
    private Boolean mBound = false;
    private Boolean mNoNetworkConnectivity = false;
    private MenuItem mIsPlayingMenuItem;
    private Boolean mShowNowPlayingButton = false;

    private ShareActionProvider mShareActionProvider;
    private MenuItem mShareMenuItem;

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

        // Display alert dialog about needing network connectivity
        if ( mNoNetworkConnectivity ) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getString(R.string.network_missing_title));
            alertDialogBuilder.setMessage(getString(R.string.network_missing_message));

            alertDialogBuilder.setPositiveButton(getString(R.string.button_message_positive), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog dialog = alertDialogBuilder.create();
            dialog.show();

            // TODO Shutdown app because network connectivity is missing

        }

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

        final ActionBar actionBar = this.getSupportActionBar();
        if ( actionBar != null && mSelectedArtist != null ) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    actionBar.setSubtitle(mSelectedArtist.name);
                }
            });
        }

    }

    @Override
    public void showActionBarPlayingButton(boolean showButton) {

        final boolean isButtonVisible = showButton;

        final ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null && mIsPlayingMenuItem != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mIsPlayingMenuItem != null && actionBar != null) {
                        mIsPlayingMenuItem.setVisible(isButtonVisible);
                        mShareMenuItem.setVisible(isButtonVisible);
                        actionBar.invalidateOptionsMenu();
                    }
                }
            });
        }

    }

    @Override
    public void onArtistSearchChanged() {

        if (mDualPane) {
            ActionBar actionBar = this.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle("");
            }

            TrackActivityFragment tracksActivityFragment;

            FragmentManager fm = getSupportFragmentManager();
            tracksActivityFragment = (TrackActivityFragment) fm.findFragmentByTag("track");

            FragmentTransaction fragmentTransaction = fm.beginTransaction();

            if (tracksActivityFragment != null) {
                fragmentTransaction.remove(tracksActivityFragment);
                fragmentTransaction.commit();
            }

            TrackFrameLayout trackFrameLayout = (TrackFrameLayout) findViewById(R.id.track_list_container);

            // TODO Need to use correct color
//            trackFrameLayout.setBackgroundColor(Color.LTGRAY);

            Picasso.with(this).load(R.drawable.no_album)
                    .into(trackFrameLayout);
        }

    }

    @Override
    public void OnTrackSelected(ArrayList<LocalTrack> tracks, Integer position) {

        mTrackPlayerService.loadTracks(mArtistQueryString, mSelectedArtist, tracks);
        mTrackPlayerService.setCurrentTrackPosition(position);
        mTrackPlayerService.unloadTrack();
        mTrackPlayerService.playPauseTrack();

        FragmentManager fragmentManager = getSupportFragmentManager();
        TrackPlayerActivityFragment trackPlayerActivityFragment = (TrackPlayerActivityFragment) fragmentManager.findFragmentByTag("dialog");

        if ( trackPlayerActivityFragment == null ) {
            trackPlayerActivityFragment = new TrackPlayerActivityFragment();
        }

        trackPlayerActivityFragment.setValues(mSelectedArtist.name, tracks, position);

        if( mDualPane ) {
            trackPlayerActivityFragment.show(fragmentManager, "dialog");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check for network connectivity
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if ( !isConnected ) {

            mNoNetworkConnectivity = true;

        } else {

            mNoNetworkConnectivity = false;

            if (findViewById(R.id.track_list_container) != null) {
                mDualPane = true;
            } else {
                mDualPane = false;
            }

            if (savedInstanceState != null) {

                if (mDualPane) {

                    mArtistQueryString = savedInstanceState.getString("artistQueryString");
                    mSelectedArtist = (LocalArtist) savedInstanceState.getParcelable("mSelectedArtist");

                }

                mShowNowPlayingButton = savedInstanceState.getBoolean("showNowPlayingActionBarButton", false);

            }

            // Start up service
            // bind - so we can call its methods
            // startService - so it stays around indefinitely
            Intent intent = new Intent(this, TrackPlayerService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            startService(intent);

            intent = getIntent();

            if (mDualPane) {

                if (intent != null & intent.hasExtra("artist") & savedInstanceState == null) {

                    String artistQueryString = intent.getStringExtra("artistQueryString");
                    LocalArtist artist = (LocalArtist) intent.getParcelableExtra("artist");
                    LocalTrack track = (LocalTrack) intent.getParcelableExtra("track");

                    Toast.makeText(this, "Started by notification (artist = " + artist.name + ", query = " + artistQueryString + ")", Toast.LENGTH_SHORT)
                            .show();

                    MainActivityFragment mainActivityFragment;
                    mainActivityFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                    mainActivityFragment.setValues(artistQueryString, artist.id);

                    onArtistSelected(artistQueryString, artist);

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    TrackPlayerActivityFragment trackPlayerActivityFragment = new TrackPlayerActivityFragment();

                    // TODO fix this - unneeded parameters(?)
                    ArrayList<LocalTrack> tracks = new ArrayList<>();
                    tracks.add(track);
                    trackPlayerActivityFragment.setValues(mArtistName, tracks, 0);

                    trackPlayerActivityFragment.show(fragmentManager, "dialog");

//                trackPlayerActivityFragment.requestUiUpdate();

                }

            }
        }
    }

    public void onArtistSelected(String queryString, LocalArtist localArtist) {

        mArtistQueryString = queryString;
        mSelectedArtist = localArtist;

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

//            String id = localArtist.id;
//            mArtistName = localArtist.name;

            if ( mSelectedArtist.id != null && mSelectedArtist.name != null ) {
                tracksActivityFragment.setValues(mSelectedArtist.id, mSelectedArtist.name);
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
            intent.putExtra("id", mSelectedArtist.id);
            intent.putExtra("artist", mSelectedArtist);
            // TODO Need to pull this extra (artistQueryString) in phone activity
            intent.putExtra("artistQueryString", mArtistQueryString);
            intent.putExtra("showButton", mShowNowPlayingButton);
            intent.putExtra("image", mSelectedArtist.getLargestImageUrl());

            startActivityForResult(intent, 1);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if ( requestCode == 1 ) {
            if ( resultCode == RESULT_OK ) {
                mShowNowPlayingButton = data.getBooleanExtra("showButton", false);
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("artistQueryString", mArtistQueryString);
        outState.putParcelable("mSelectedArtist", mSelectedArtist);
        outState.putBoolean("showNowPlayingActionBarButton", mIsPlayingMenuItem.isVisible());

   }

    private void setShareIntent(Intent shareIntent) {
        if ( mShareActionProvider != null ) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mIsPlayingMenuItem = menu.findItem(R.id.action_play);
        mIsPlayingMenuItem.setVisible(mShowNowPlayingButton);

        // Optional Component - Sharing Functionality
        mShareMenuItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareMenuItem);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message_temp));
        intent.setType("text/plain");
        setShareIntent(intent);

        mShareMenuItem.setVisible(mShowNowPlayingButton);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch ( id ) {
            case R.id.action_settings:
                return true;

            case R.id.action_play:

                FragmentManager fragmentManager = getSupportFragmentManager();
                TrackPlayerActivityFragment trackPlayerActivityFragment = new TrackPlayerActivityFragment();

                LocalTrack track = mTrackPlayerService.getCurrentTrack();

                ArrayList<LocalTrack> tracks = new ArrayList<>();
                tracks.add(track);

                trackPlayerActivityFragment.setValues("", tracks, 0);

                trackPlayerActivityFragment.show(fragmentManager, "dialog");

                mTrackPlayerService.requestUiUpdate();

                mIsPlayingMenuItem.setVisible(false);
                mShareMenuItem.setVisible(false);

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

    @Override
    public void onRequestUiUpdate() {
        mTrackPlayerService.requestUiUpdate();
    }

    @Override
    public void setCurrentTrackPosition(int currentTrackPosition) {
        mTrackPlayerService.seekTo(currentTrackPosition);
    }

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

            mTrackPlayerService.requestUiUpdate();

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
            FragmentManager fragmentManager = getSupportFragmentManager();
            TrackPlayerActivityFragment trackPlayerActivityFragment = (TrackPlayerActivityFragment) fragmentManager.findFragmentByTag("dialog");

            String action = intent.getAction();
            int trackDuration;

            switch ( action ) {
                case Constants.BROADCAST_ACTION:

                    String artistName = intent.getStringExtra(Constants.EXTENDED_DATA_STATUS_ARTIST_NAME);
                    LocalTrack localTrack = (LocalTrack) intent.getParcelableExtra(Constants.EXTENDED_DATA_STATUS);

                    // Setup shareintent
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));

                    String shareMessage = String.format(getString(R.string.share_message), localTrack.trackName, artistName, localTrack.preview_url);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

                    shareIntent.setType("text/plain");
                    setShareIntent(shareIntent);

                    // If there is no trackPlayerActivityFragment -> nothing to update
                    if ( trackPlayerActivityFragment == null ) {
                        return;
                    }

                    trackPlayerActivityFragment.updateViews(artistName, localTrack);

                    TrackActivityFragment tracksActivityFragment;

                    tracksActivityFragment = (TrackActivityFragment) fragmentManager.findFragmentByTag("track");

                    if ( tracksActivityFragment != null ) {
                        int currentTrack = intent.getIntExtra(Constants.EXTENDED_DATA_TRACK_CURRENT,0);
                        tracksActivityFragment.setTrackPosition(currentTrack);
                    }

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

                    // If there is no trackPlayerActivityFragment -> nothing to update
                    if ( trackPlayerActivityFragment == null ) {
                        return;
                    }

                    trackPlayerActivityFragment.updateSeekbar(isPlaying, trackDuration, currentTrackPosition);

                    break;
            }
        }
    }
}
