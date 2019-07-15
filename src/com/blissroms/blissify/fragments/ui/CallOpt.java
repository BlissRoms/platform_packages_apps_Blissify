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
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.bliss.support.preferences.SystemSettingSwitchPreference;
import android.provider.Settings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

public class CallOpt extends SettingsPreferenceFragment
                                         implements Preference.OnPreferenceChangeListener{

        private static final String PREF_FLASHLIGHT_ON_CALL = "flashlight_on_call";

        private ListPreference mFlashOnCall;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

       @Override
         public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
       }
}
