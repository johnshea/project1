package com.example.android.project1;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class TrackPlayerActivity extends ActionBarActivity implements TrackPlayerActivityFragment.TrackPlayerActivityListener {

    public TrackPlayerActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_player);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track_player, menu);
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
    }

    @Override
    public void onClickPreviousTrack() {
    }

    @Override
    public void onClickPlayPauseTrack() {

    }

    @Override
    public void onRequestUiUpdate() {

    }

    @Override
    public void setCurrentTrackPosition(int currentTrackPosition) {

    }

}
