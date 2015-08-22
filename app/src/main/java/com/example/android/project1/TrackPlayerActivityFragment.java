package com.example.android.project1;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.android.project1.models.LocalTrack;
import com.example.android.project1.service.TrackPlayerService;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayerActivityFragment extends DialogFragment implements View.OnClickListener,
SeekBar.OnSeekBarChangeListener {

    private final String LOG_TAG = TrackActivityFragment.class.getSimpleName();
    private TrackPlayerActivityListener mCallback;

    private boolean mIsPlaying = false;
    private boolean mIsSongLoaded = false;

    private String artistName;
    private LocalTrack localTrack;

    private TrackPlayerService mTrackPlayerService;
    private Boolean mBound = false;

    private Thread mMoveSeekBarThread;

    private boolean mPausedBySeekBarMove = false;

    private boolean mNewTrackSelected = true;

    @Override
    public void onStart() {
        super.onStart();

//        Intent intent = new Intent(getActivity(), TrackPlayerService.class);
//        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//
//        getActivity().startService(intent);
    }

    @Override
    public void onStop() {
        super.onStop();

        if ( mMoveSeekBarThread != null ) {
            mMoveSeekBarThread.interrupt();
            mMoveSeekBarThread = null;
        }

        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if ( mTrackPlayerService.isTrackLoaded() ) {
            if ( !mTrackPlayerService.isTrackPaused() ) {
                mPausedBySeekBarMove = true;
                mTrackPlayerService.playPauseTrack();
            }
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if ( mTrackPlayerService.isTrackLoaded() ) {
            mTrackPlayerService.seekTo(seekBar.getProgress());
            if ( mPausedBySeekBarMove ) {
                mPausedBySeekBarMove = false;
                mTrackPlayerService.playPauseTrack();
            }
        }
    }

    public interface TrackPlayerActivityListener {
        public LocalTrack onGetNextTrack();
        public LocalTrack onGetPreviousTrack();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("localTrack", localTrack);
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
        SeekBar seekBarTrackPosition = (SeekBar) getView().findViewById(R.id.seekBar);

        btnPrevTrack.setOnClickListener(this);
        btnPlayPauseTrack.setOnClickListener(this);
        btnNextTrack.setOnClickListener(this);
        seekBarTrackPosition.setOnSeekBarChangeListener(this);

        if ( savedInstanceState != null ) {
            localTrack = savedInstanceState.getParcelable("localTrack");
            mNewTrackSelected = false;
        }

        populateViews();

    }

    private void populateViews() {

        TextView trackPlayerArtistName = (TextView) getView().findViewById(R.id.trackPlayerArtistName);
        TextView trackPlayerAlbumName = (TextView) getView().findViewById(R.id.trackPlayerAlbumName);
        ImageView trackPlayerAlbumArtwork= (ImageView) getView().findViewById(R.id.trackPlayerAlbumArtwork);
        TextView trackPlayerTrackName = (TextView) getView().findViewById(R.id.trackPlayerTrackName);

        trackPlayerArtistName.setText(artistName);
        trackPlayerAlbumName.setText(localTrack.albumName);
        Picasso.with(getActivity())
                .load(localTrack.getLargestImageUrl())
                .resize(600, 600)
                .centerInside()
                .placeholder(R.drawable.no_album)
                .into(trackPlayerAlbumArtwork);

        trackPlayerTrackName.setText(localTrack.trackName);

//        mTrackPlayerService.loadTrack(localTrack.preview_url);

    }

    @Override
    public void onResume() {
        super.onResume();

        if ( mTrackPlayerService != null ) {
            if ( mTrackPlayerService.isTrackLoaded() ) {
                final SeekBar seekBar = (SeekBar) getView().findViewById(R.id.seekBar);
                seekBar.setMax(mTrackPlayerService.getDuration());

                RunnableProgress r = new RunnableProgress(seekBar);

                mMoveSeekBarThread = new Thread(r, "Thread_mMoveSeekBarThread");
                mMoveSeekBarThread.start();
            }
        }

    }

    @Override
    public void onClick(View v) {

        StringBuilder buttonMessage = new StringBuilder();

        ImageButton btnPlayPauseTrack = (ImageButton) getView().findViewById(R.id.btnPlayPauseTrack);
        final SeekBar seekBar = (SeekBar) getView().findViewById(R.id.seekBar);

        if ( mNewTrackSelected ) {
            mNewTrackSelected = false;

            mTrackPlayerService.unloadTrack();
            mTrackPlayerService.loadTrack(localTrack.preview_url);
        }
//        if ( mMoveSeekBarThread == null ) {
//
////            final SeekBar seekBar = (SeekBar) getView().findViewById(R.id.seekBar);
//            seekBar.setMax(mTrackPlayerService.getDuration());
//
//            RunnableProgress r = new RunnableProgress(seekBar);
//
//            mMoveSeekBarThread = new Thread(r, "Thread_mMoveSeekBarThread");
//            mMoveSeekBarThread.start();
//        }

        switch (v.getId()) {

            case R.id.btnPrevTrack:

                buttonMessage.append("Previous");

                if ( mMoveSeekBarThread != null ) {
                    mMoveSeekBarThread.interrupt();
                    mMoveSeekBarThread = null;
                }

                localTrack = mCallback.onGetPreviousTrack();
                populateViews();

                mTrackPlayerService.unloadTrack();
                mTrackPlayerService.loadTrack(localTrack.preview_url);

                btnPlayPauseTrack.setImageResource(android.R.drawable.ic_media_play);

                seekBar.setMax(0);

                break;

            case R.id.btnPlayPauseTrack:

                buttonMessage.append("Play/Pause");

                if ( !mTrackPlayerService.isTrackLoaded() ) {
                    mTrackPlayerService.loadTrack(localTrack.preview_url);
                    mTrackPlayerService.playPauseTrack();
                } else {
                    mTrackPlayerService.playPauseTrack();
                }

                if ( mTrackPlayerService.isTrackPaused() ) {
                    btnPlayPauseTrack.setImageResource(android.R.drawable.ic_media_pause);
                    if ( mMoveSeekBarThread != null ) {
                        mMoveSeekBarThread.interrupt();
                        mMoveSeekBarThread = null;
                    }
                } else {
                    btnPlayPauseTrack.setImageResource(android.R.drawable.ic_media_play);

                    seekBar.setMax(mTrackPlayerService.getDuration());

                    RunnableProgress r = new RunnableProgress(seekBar);

                    mMoveSeekBarThread = new Thread(r, "Thread_mMoveSeekBarThread");
                    mMoveSeekBarThread.start();
                }

                break;

            case R.id.btnNextTrack:

                buttonMessage.append("Next");

                if ( mMoveSeekBarThread != null ) {
                    mMoveSeekBarThread.interrupt();
                    mMoveSeekBarThread = null;
                }

                localTrack = mCallback.onGetNextTrack();
                populateViews();

                mTrackPlayerService.unloadTrack();
                mTrackPlayerService.loadTrack(localTrack.preview_url);

                btnPlayPauseTrack.setImageResource(android.R.drawable.ic_media_play);

                seekBar.setMax(0);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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

        Intent intent = new Intent(getActivity(), TrackPlayerService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        getActivity().startService(intent);

        return inflater.inflate(R.layout.fragment_track_player, container, false);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TrackPlayerService.LocalBinder binder = (TrackPlayerService.LocalBinder) service;
            mTrackPlayerService = binder.getService();
            mBound = true;

            if ( mTrackPlayerService.isTrackLoaded() ) {
                final SeekBar seekBar = (SeekBar) getView().findViewById(R.id.seekBar);
                seekBar.setMax(mTrackPlayerService.getDuration());

                RunnableProgress r = new RunnableProgress(seekBar);

                mMoveSeekBarThread = new Thread(r, "Thread_mMoveSeekBarThread");
                mMoveSeekBarThread.start();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    class RunnableProgress implements Runnable {

        SeekBar mSeekBar;

        public RunnableProgress() {

        }

        public RunnableProgress(SeekBar object) {
            mSeekBar = object;
        }

        public void run() {

            while( !mTrackPlayerService.isTrackPlaying() ) {
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
//                    Log.e(LOG_TAG, "Updating seekbar");
                    return;
                }
            }

            mSeekBar.setMax(mTrackPlayerService.getDuration());

            while( true ) {

                if ( Thread.interrupted() ) {
                    return;
                }
//                    seekBar.setProgress(mTrackPlayerService.getCurrentPosition());
//                    Log.d(LOG_TAG, "getCurrentPosition: " + mTrackPlayerService.getCurrentPosition());
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
//                    Log.e(LOG_TAG, "Updating seekbar");
                    return;
                }

                if ( mTrackPlayerService != null && mTrackPlayerService.isTrackPlaying() ) {
                    if ( getActivity() != null ) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSeekBar.setProgress(mTrackPlayerService.getCurrentPosition());
                            }
                        });
                    }
                }

            }
        }
    }
}
