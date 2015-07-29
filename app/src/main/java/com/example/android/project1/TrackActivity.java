package com.example.android.project1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.project1.models.LocalTrack;
import com.squareup.picasso.Picasso;


public class TrackActivity extends ActionBarActivity implements TrackActivityFragment.OnTrackSelectedListener {

    private TrackActivityFragment.OnTrackSelectedListener mCallback;

    @Override
    public void OnTrackSelectedListener(LocalTrack localTrack) {
        Toast.makeText(this, "(TrackActivity) Track selected: " + localTrack.trackName.toString(), Toast.LENGTH_SHORT).show();
    }

    private TrackActivityFragment tracksActivityFragment;

    private String id;
    private String artist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        FragmentManager fm = getSupportFragmentManager();
        tracksActivityFragment = (TrackActivityFragment) fm.findFragmentByTag("track");

        FragmentTransaction fragmentTransaction = fm.beginTransaction();

        if ( tracksActivityFragment == null ) {
            tracksActivityFragment = new TrackActivityFragment();
            fragmentTransaction.add(R.id.track_list_container, tracksActivityFragment, "track");
            fragmentTransaction.commit();
            }

        Intent intent = getIntent();

        if ( intent != null ) {
            String id = intent.getStringExtra("id");
            String artist = intent.getStringExtra("artist");
            String imageUrl = intent.getStringExtra("image");
            if ( id != null && artist != null ) {
                tracksActivityFragment.setValues(id, artist);
                TrackFrameLayout trackFrameLayout = (TrackFrameLayout)findViewById(R.id.track_list_container);

                if ( !imageUrl.equals("") ) {
                    Picasso.with(this).load(imageUrl)
                            .into(trackFrameLayout);
                }

            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track, menu);
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
}
