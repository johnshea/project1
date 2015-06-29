package com.example.android.project1;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A placeholder fragment containing a simple view.
 */
public class TrackActivityFragment extends Fragment {

    private ListView tracksListView;
    private ArrayList<LocalTrack> tracksArrayList;
    private ArrayAdapter<Tracks> tracksAdapter;

    private String id;
    private String artist;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if ( tracksArrayList == null ){
            new DownloadTracks().execute(id);
        } else {
            TracksAdapter tracksAdapter = new TracksAdapter(getActivity(), tracksArrayList);

            tracksListView.setAdapter(tracksAdapter);
        }

    }

    public TrackActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    public void setData(ArrayList<LocalTrack> data) {
        this.tracksArrayList = data;
    }

    public void setValues(String id, String artist) {
        this.id = id;
        this.artist = artist;
    }

    public ArrayList<LocalTrack> getData() {
        return this.tracksArrayList;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_track, container, false);

        tracksListView = (ListView) rootView.findViewById(R.id.listview_tracks);

//        Intent intent = getActivity().getIntent();
//
//        // TODO Add error checking in case it is not populated
//        id = intent.getStringExtra("id");
//        String artist = intent.getStringExtra("artist");


        //TODO Need to add second line to action bar with artist's name
        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        if ( actionBar != null ) {
            actionBar.setSubtitle(artist);
        }

        return rootView;
    }

    public class TracksAdapter extends ArrayAdapter<LocalTrack> {

        public TracksAdapter(Context context, ArrayList<LocalTrack> tracks) {
            super(context, 0, tracks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LocalTrack localTrack = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_track, parent, false);
            }

            TextView tvAlbumName = (TextView) convertView.findViewById(R.id.textView_albumName);
            TextView tvTrackName = (TextView) convertView.findViewById(R.id.textView_trackName);
            ImageView ivAlbumImage = (ImageView) convertView.findViewById(R.id.imageView_albumImage);

            tvAlbumName.setText(localTrack.albumName);
            tvTrackName.setText(localTrack.trackName);

            if ( localTrack.albumImages.size() > 0 ) {
                Picasso.with(getContext()).load(localTrack.albumImages.get(0).url)
                        .resize(200, 200)
                        .centerInside()
                        .placeholder(R.drawable.icon_square)
                        .into(ivAlbumImage);

            } else {
                Picasso.with(getContext()).load(R.drawable.no_album)
                        .resize(200, 200)
                        .centerInside()
                        .into(ivAlbumImage);
            }

            return convertView;
        }

    }

    public class LocalTrack {
        public String albumName;
        public String trackName;
        public ArrayList<LocalTrackImage> albumImages;
        public String preview_url;

        public LocalTrack(String albumName, String trackName, ArrayList<LocalTrackImage> albumImages, String preview_url) {
            this.albumName = albumName;
            this.trackName = trackName;
            this.albumImages = albumImages;
            this.preview_url = preview_url;
        }
    }

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

    public class DownloadTracks extends AsyncTask<String, Void, Tracks> {
        @Override
        protected Tracks doInBackground(String... params) {
            String artistId = params[0];

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            HashMap<String, Object> optionsMap = new HashMap<>();
            optionsMap.put("country", "US");

            Tracks results = spotify.getArtistTopTrack(artistId, optionsMap);

            return results;
        }

        @Override
        protected void onPostExecute(Tracks results) {

            tracksArrayList = new ArrayList<LocalTrack>();

            for(Track track : results.tracks) {

                ArrayList<LocalTrackImage> images = new ArrayList<LocalTrackImage>();

                for(Image i : track.album.images) {
                    images.add(new LocalTrackImage(i.url, i.width, i.height));
                }

                LocalTrack localTrack = new LocalTrack(track.album.name, track.name, images, track.preview_url);
                tracksArrayList.add(localTrack);
            }

            TracksAdapter tracksAdapter = new TracksAdapter(getActivity(), tracksArrayList);

            tracksListView.setAdapter(tracksAdapter);

            if ( tracksArrayList.size() == 0 ) {
                Toast.makeText(getActivity(), getString(R.string.message_no_top_10_tracks), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}
