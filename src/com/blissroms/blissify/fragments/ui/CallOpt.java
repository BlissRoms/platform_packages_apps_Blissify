package com.blissroms.blissify.fragments.ui;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.blissroms.blissify.preference.SystemSettingSwitchPreference;
import android.provider.Settings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.blissroms.blissify.R;

public class CallOpt extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.default_view,container,false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.default_view, new CallOpt.SystemPreference())
                .commit();
        return view;
    }

    public static class SystemPreference extends PreferenceFragmentCompat
                                         implements Preference.OnPreferenceChangeListener{

        public SystemPreference() {
        }

        private static final String PREF_FLASHLIGHT_ON_CALL = "flashlight_on_call";

        private ListPreference mFlashOnCall;


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.interface_callopt);
            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mFlashOnCall = (ListPreference) findPreference(PREF_FLASHLIGHT_ON_CALL);
            mFlashOnCall.setOnPreferenceChangeListener(this);
            mFlashOnCall.setValue(Integer.toString(Settings.System.getInt(resolver,
            Settings.System.FLASHLIGHT_ON_CALL, 0)));
            mFlashOnCall.setSummary(mFlashOnCall.getEntry());
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mFlashOnCall) {
            int val = Integer.parseInt((String) newValue);
            int index = mFlashOnCall.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.FLASHLIGHT_ON_CALL, val);
            mFlashOnCall.setSummary(mFlashOnCall.getEntries()[index]);
            return true;
        }
        return false;
        }
     }
}
