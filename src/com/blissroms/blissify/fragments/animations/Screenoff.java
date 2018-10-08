package com.blissroms.blissify.fragments.animations;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
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

public class Screenoff extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.default_view,container,false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.default_view, new Screenoff.SystemPreference())
                .commit();
        return view;
        }

    public static class SystemPreference extends PreferenceFragmentCompat
                                         implements Preference.OnPreferenceChangeListener{

        public SystemPreference() {
        }

    private static final String KEY_SCREEN_OFF_ANIMATION = "screen_off_animation";

    private ListPreference mScreenOffAnimation;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.screenoff_animation);
            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mScreenOffAnimation = (ListPreference) findPreference(KEY_SCREEN_OFF_ANIMATION);
            int screenOffAnimation = Settings.Global.getInt(resolver,
                    Settings.Global.SCREEN_OFF_ANIMATION, 0);
            mScreenOffAnimation.setValue(Integer.toString(screenOffAnimation));
            mScreenOffAnimation.setSummary(mScreenOffAnimation.getEntry());
            mScreenOffAnimation.setOnPreferenceChangeListener(this);

        }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
         ContentResolver resolver = getActivity().getContentResolver();
         if (preference == mScreenOffAnimation) {
            int value = Integer.valueOf((String) newValue);
            int index = mScreenOffAnimation.findIndexOfValue((String) newValue);
            mScreenOffAnimation.setSummary(mScreenOffAnimation.getEntries()[index]);
            Settings.Global.putInt(resolver, Settings.Global.SCREEN_OFF_ANIMATION, value);
            return true;
         }
         return false;
       }
     }
}
