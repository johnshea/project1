package com.example.android.project1;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.project1.models.LocalTrack;
import com.squareup.picasso.Picasso;


/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayerActivityFragment extends DialogFragment implements View.OnClickListener {

    private TrackPlayerActivityListener mCallback;

    private String artistName;
    private LocalTrack localTrack;

    public interface TrackPlayerActivityListener {
        public LocalTrack onGetNextTrack();
        public LocalTrack onGetPreviousTrack();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (TrackPlayerActivityListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement TrackPlayerActivityListener");
        }

    }

        @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ImageButton btnPrevTrack = (ImageButton) getView().findViewById(R.id.btnPrevTrack);
        ImageButton btnPlayPauseTrack = (ImageButton) getView().findViewById(R.id.btnPlayPauseTrack);
        ImageButton btnNextTrack = (ImageButton) getView().findViewById(R.id.btnNextTrack);

        btnPrevTrack.setOnClickListener(this);
        btnPlayPauseTrack.setOnClickListener(this);
        btnNextTrack.setOnClickListener(this);

            populateViews();

    }

    private void populateViews() {

        TextView trackPlayerArtistName = (TextView) getView().findViewById(R.id.trackPlayerArtistName);
        TextView trackPlayerAlbumName = (TextView) getView().findViewById(R.id.trackPlayerAlbumName);
        ImageView trackPlayerAlbumArtwork= (ImageView) getView().findViewById(R.id.trackPlayerAlbumArtwork);
        TextView trackPlayerTrackName = (TextView) getView().findViewById(R.id.trackPlayerTrackName);

        trackPlayerArtistName.setText(artistName);
        trackPlayerAlbumName.setText(localTrack.albumName);
        Picasso.with(getActivity()).load(localTrack.getLargestImageUrl()).into(trackPlayerAlbumArtwork);
        trackPlayerTrackName.setText(localTrack.trackName);

    }

    @Override
    public void onClick(View v) {

        StringBuilder buttonMessage = new StringBuilder();

        switch (v.getId()) {
            case R.id.btnPrevTrack:
                buttonMessage.append("Previous");
                localTrack = mCallback.onGetPreviousTrack();
                populateViews();
                break;
            case R.id.btnPlayPauseTrack:
                buttonMessage.append("Play/Pause");
                break;
            case R.id.btnNextTrack:
                buttonMessage.append("Next");
                localTrack = mCallback.onGetNextTrack();
                populateViews();
                break;
            default:
                buttonMessage.append("Unknown");
                break;
        }

//        Toast.makeText(getActivity(), "You pressed button " + buttonMessage.toString(), Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        return super.onCreateDialog(savedInstanceState);

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;

    }

    public TrackPlayerActivityFragment() {
    }

    public void setValues(String artistName, LocalTrack localTrack) {
        this.artistName = artistName;
        this.localTrack = localTrack;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_track_player, container, false);
    }


}
