package com.blissroms.blissify.fragments.lockscreen;

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
import com.blissroms.blissify.preference.SystemSettingSwitchPreference;
import android.provider.Settings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.android.settings.bliss.preference.ColorPickerPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

public class LockSecurity extends SettingsPreferenceFragment
             implements Preference.OnPreferenceChangeListener{


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_security);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
         ContentResolver resolver = getActivity().getContentResolver();
         return false;
        }

       @Override
         public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
       }
}
