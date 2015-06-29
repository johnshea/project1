package com.example.android.project1;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ArrayAdapter<String> artistAdapter;
    private ArrayList<LocalArtist> artistsArrayList;
    private ListView listView;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        listView = (ListView) rootView.findViewById(R.id.listview_artist);

        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final EditText artistQuery = (EditText) getView().findViewById(R.id.edittext_artist_query);

        artistQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = artistQuery.getText().toString();
                if ( !query.isEmpty() ) {
                    new DownloadArtists().execute(query);
                } else {

                    artistsArrayList.clear();
                    ArtistsAdapter adapter = new ArtistsAdapter(getActivity(), artistsArrayList);

                    listView.setAdapter(adapter);
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                LocalArtist selectedArtist;

                // TODO this should be artistAdapter.getItem..see lesson 3 - 5th session
                selectedArtist = artistsArrayList.get(position);

//                TextView tvArtistName = (TextView) view.findViewById(R.id.textView_artistName);
//                String artistName = tvArtistName.getText().toString();
//
//                Toast.makeText(getActivity(), "You clicked on " + artistName, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(), TrackActivity.class);
                intent.putExtra("id", selectedArtist.id);
                intent.putExtra("artist", selectedArtist.name);
                startActivity(intent);

            }
        });

    }

    public class DownloadArtists extends AsyncTask<String, Void, ArtistsPager> {

        @Override
        protected ArtistsPager doInBackground(String... params) {

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager results = spotify.searchArtists(params[0]);

            return results;
        }

        @Override
        protected void onPostExecute(ArtistsPager results) {

            artistsArrayList = new ArrayList<LocalArtist>();

            for(Artist a : results.artists.items) {

                ArrayList<LocalImage> images = new ArrayList<LocalImage>();

                for(Image i : a.images) {
                    images.add(new LocalImage(i.url, i.width, i.height));
                }

                LocalArtist oneArtist = new LocalArtist(a.id, a.name, images);
                artistsArrayList.add(oneArtist);
            }

            ArtistsAdapter adapter = new ArtistsAdapter(getActivity(), artistsArrayList);

            listView.setAdapter(adapter);

            if ( artistsArrayList.size() == 0 ) {
                Toast.makeText(getActivity(), getString(R.string.message_no_artists_found), Toast.LENGTH_SHORT).show();
            }

        }
    }

    public class LocalImage {
        public String url;
        public Integer height;
        public Integer width;

        public LocalImage(String url, Integer width, Integer height) {
            this.url = url;
            this.width = width;
            this.height = height;
        }
    }

    public class LocalArtist {
        public String id;
        public String name;
        public String url;
        public ArrayList<LocalImage> artistImages;

        public LocalArtist(String id, String name, ArrayList<LocalImage> artistImages) {
            this.id = id;
            this.name = name;
            this.artistImages = artistImages;
        }
    }

    public class ArtistsAdapter extends ArrayAdapter<LocalArtist> {

        public ArtistsAdapter(Context context, ArrayList<LocalArtist> artists) {
            super(context, 0, artists);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LocalArtist artist = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);
            }

            TextView tvArtistName = (TextView) convertView.findViewById(R.id.textView_artistName);
            ImageView ivArtistImage = (ImageView) convertView.findViewById(R.id.imageView_artistImage);

            tvArtistName.setText(artist.name);

            if ( artist.artistImages.size() > 0 ) {
//                Picasso.with(getContext()).setIndicatorsEnabled(true);
                Picasso.with(getContext()).load(artist.artistImages.get(0).url)
                        .resize(200, 200)
                        .centerInside()
                        .into(ivArtistImage);

            } else {
//                Picasso.with(getContext()).setIndicatorsEnabled(true);
                Picasso.with(getContext()).load(R.drawable.no_album)
                        .resize(200, 200)
                        .centerInside()
                        .into(ivArtistImage);
            }

            return convertView;
        }
    }
}


