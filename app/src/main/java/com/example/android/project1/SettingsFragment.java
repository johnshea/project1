package com.example.android.project1;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by John on 9/7/2015.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }


}
