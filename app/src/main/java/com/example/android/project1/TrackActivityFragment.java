package com.example.android.project1;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.project1.models.LocalImage;
import com.example.android.project1.models.LocalTrack;
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

    private OnTrackSelectedListener mCallback;

    private final String LOG_TAG = TrackActivityFragment.class.getSimpleName();

    private ListView tracksListView;
    private ArrayList<LocalTrack> tracksArrayList;
    private ArrayAdapter<Tracks> tracksAdapter;

    private String id;
    private String artist;

    private int intCurrentTrack;

    public interface OnTrackSelectedListener {
        public void OnTrackSelectedListener(LocalTrack localTrack);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnTrackSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTrackSelectedListener");
        }
    }

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

        Boolean artistIdChanged = false;

        if ( this.id != id ) {
            artistIdChanged = true;
        } else {
            artistIdChanged = false;
        }

        this.id = id;
        this.artist = artist;

        if ( artistIdChanged ) {
            new DownloadTracks().execute(id);
        }

    }

    public ArrayList<LocalTrack> getData() {
        return this.tracksArrayList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_track, container, false);

        tracksListView = (ListView) rootView.findViewById(R.id.listview_tracks);

        tracksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(getActivity(), TrackPlayerActivity.class);
//                startActivity(intent);

                intCurrentTrack = position;

                mCallback.OnTrackSelectedListener(tracksArrayList.get(intCurrentTrack));
            }
        });

        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        if ( actionBar != null ) {
            actionBar.setSubtitle(artist);
        }

        return rootView;
    }

    public LocalTrack getNextTrack() {
        if ( intCurrentTrack < (tracksArrayList.size() - 1) ) {
            intCurrentTrack++;
        }
        return tracksArrayList.get(intCurrentTrack);
    }

    public LocalTrack getPreviousTrack() {
        if ( intCurrentTrack > 0 ) {
            intCurrentTrack--;
        }
        return tracksArrayList.get(intCurrentTrack);
    }

    static class ViewHolder {
        TextView textViewAlbumName;
        TextView textViewTrackName;
        ImageView imageViewAlbumImage;
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

                ViewHolder viewHolder = new ViewHolder();
                viewHolder.textViewAlbumName = (TextView) convertView.findViewById(R.id.textView_albumName);
                viewHolder.textViewTrackName = (TextView) convertView.findViewById(R.id.textView_trackName);
                viewHolder.imageViewAlbumImage = (ImageView) convertView.findViewById(R.id.imageView_albumImage);

                convertView.setTag(viewHolder);
            }

            ViewHolder viewHolder = (ViewHolder) convertView.getTag();

            TextView tvAlbumName = viewHolder.textViewAlbumName;
            TextView tvTrackName = viewHolder.textViewTrackName;
            ImageView ivAlbumImage = viewHolder.imageViewAlbumImage;

            tvAlbumName.setText(localTrack.albumName);
            tvTrackName.setText(localTrack.trackName);

            if ( localTrack.albumImages.size() > 0 ) {
                Picasso.with(getContext()).load(localTrack.getThumbnailUrl())
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

    public class DownloadTracks extends AsyncTask<String, Void, Tracks> {
        @Override
        protected Tracks doInBackground(String... params) {
            String artistId = params[0];

            HashMap<String, Object> optionsMap = new HashMap<>();
            optionsMap.put("country", "US");

            Tracks results = null;

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            try {
                results = spotify.getArtistTopTrack(artistId, optionsMap);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Spotify exception - " + e.getMessage().toString());
            }

            return results;
        }

        @Override
        protected void onPostExecute(Tracks results) {

            tracksArrayList = new ArrayList<LocalTrack>();

            for(Track track : results.tracks) {

                ArrayList<LocalImage> images = new ArrayList<LocalImage>();

                for(Image i : track.album.images) {
                    images.add(new LocalImage(i.url, i.width, i.height));
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
