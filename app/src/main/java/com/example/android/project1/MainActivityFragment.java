package com.example.android.project1;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ArrayAdapter<String> artistAdapter;
    private ListView listView;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] citiesArray = {
                "New York",
                "Istanbul",
                "London",
                "Edinburgh"
        };

        ArrayList<String> citiesArrayList = new ArrayList<>();

        for(int i=0; i < 50; i++) {
            citiesArrayList.add(i + " Some City");
        }

//        List<String> cities = new ArrayList<String>(Arrays.asList(citiesArray));
//        List<String> cities = new ArrayList<String>(Arrays.asList(citiesArrayList));

//        ArrayAdapter<String> citiesAdapter = new ArrayAdapter<String>(
//                getActivity(),
//                R.layout.list_item_artist,
//                R.id.textView_artistName,
//                citiesArrayList);

        artistAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_artist,
                R.id.textView_artistName,
                citiesArrayList);

        listView = (ListView) rootView.findViewById(R.id.listview_artist);

        listView.setAdapter(artistAdapter);

//        ImageView iconImage = (ImageView) rootView.findViewById(R.id.imageview_icon);
//
//        Picasso.with(getActivity())
//                .load("http://i.imgur.com/DvpvklR.png")
//                .into(iconImage);

//        new DownloadArtists().execute("Vedder");
//        iconImage.setImageResource(R.drawable.arrow_pointing_right_orange_transparent);

//        return inflater.inflate(R.layout.fragment_main, container, false);
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
                }
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
//            super.onPostExecute(aVoid);

            ArrayList<String> artistsArrayList = new ArrayList<>();

            for(Artist a:results.artists.items) {
                artistsArrayList.add(a.name);
            }

            List<String> artists = new ArrayList<String>(artistsArrayList);

            artistAdapter.clear();
            artistAdapter.addAll(artists);
            artistAdapter.notifyDataSetChanged();

//            ArtistAdapter artistWithImageAdapter = new ArtistAdapter(getActivity(),R.layout.list_item_artist);
//
//            listView.setAdapter(artistWithImageAdapter);

//            artistAdapter = new ArrayAdapter<String>(
//                    getActivity(),
//                    R.layout.list_item_artist,
//                    R.id.textView_artistName,
//                    artists);

//            listView = (ListView) getView().findViewById(R.id.listview_artist);

//            listView.setAdapter(artistAdapter);

        }
    }

    public class LocalArtist {
        public String name;
        public String url;
        public String image;
    }

    public class ArtistAdapter extends ArrayAdapter<LocalArtist> {

        public ArtistAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return super.getView(position, convertView, parent);
        }
    }
}


