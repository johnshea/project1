package com.example.android.project1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.project1.models.LocalArtist;
import com.example.android.project1.models.LocalTrack;
import com.squareup.picasso.Picasso;


public class MainActivity extends ActionBarActivity
        implements MainActivityFragment.OnArtistSelectedListener, TrackActivityFragment.OnTrackSelectedListener
        , TrackPlayerActivityFragment.TrackPlayerActivityListener {

    private String mArtistName;

    @Override
    public void OnTrackSelectedListener(LocalTrack localTrack) {
//        Toast.makeText(this, "(MainActivity) Track selected: " + localTrack.trackName.toString(), Toast.LENGTH_SHORT).show();

        FragmentManager fragmentManager = getSupportFragmentManager();
        TrackPlayerActivityFragment trackPlayerActivityFragment = (TrackPlayerActivityFragment) fragmentManager.findFragmentByTag("dialog");

        if ( trackPlayerActivityFragment == null ) {
            trackPlayerActivityFragment = new TrackPlayerActivityFragment();
        }

        trackPlayerActivityFragment.setValues(mArtistName, localTrack);

        if( mDualPane ) {
            trackPlayerActivityFragment.show(fragmentManager, "dialog");
        }
    }

    private boolean mDualPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.track_list_container) != null) {
            mDualPane = true;
        } else {
            mDualPane = false;
        }
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
    public LocalTrack onGetNextTrack() {

        TrackActivityFragment tracksActivityFragment;

        FragmentManager fm = getSupportFragmentManager();
        tracksActivityFragment = (TrackActivityFragment) fm.findFragmentByTag("track");

        LocalTrack nextTrack = tracksActivityFragment.getNextTrack();

        return nextTrack;
    }

    @Override
    public LocalTrack onGetPreviousTrack() {

        TrackActivityFragment tracksActivityFragment;

        FragmentManager fm = getSupportFragmentManager();
        tracksActivityFragment = (TrackActivityFragment) fm.findFragmentByTag("track");

        LocalTrack previousTrack = tracksActivityFragment.getPreviousTrack();

        return previousTrack;
    }
}
