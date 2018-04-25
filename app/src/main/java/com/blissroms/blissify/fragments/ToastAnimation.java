package com.blissroms.blissify.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blissroms.blissify.R;

/**
 * Created by jackeagle on 31/12/17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class ToastAnimation extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.default_view,container,false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.default_view, new ToastAnimation.ToastPreference())
                .commit();
        return view;
    }

    public static class ToastPreference extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener{

        public ToastPreference() {
        }

        private static final String TAG = "TrafficSettingsFragment";


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.toast_animation);
            PreferenceScreen prefSet = getPreferenceScreen();
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            return false;
        }


    }
}
