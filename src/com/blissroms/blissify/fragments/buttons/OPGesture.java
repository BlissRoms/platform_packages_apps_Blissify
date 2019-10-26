package com.blissroms.blissify.fragments.buttons;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import android.provider.Settings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.bliss.support.preferences.SystemSettingSwitchPreference;
import com.bliss.support.preferences.SeekBarPreferenceCham;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.omni.DeviceUtils;

public class OPGesture extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener{

    private static final String TAG = "NavBar";
    private static final String KEYS_SHOW_NAVBAR_KEY = "navbar_visibility";
    private static final String KEY_SWIPE_LENGTH = "gesture_swipe_length";
    private static final String KEY_SWIPE_START = "gesture_swipe_start";
    private static final String KEY_SWIPE_TIMEOUT = "gesture_swipe_timeout";

    private SeekBarPreferenceCham mSwipeTriggerLength;
    private SeekBarPreferenceCham mSwipeTriggerStart;
    private SeekBarPreferenceCham mSwipeTriggerTimeout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.buttons_op_gesture);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mSwipeTriggerLength = (SeekBarPreferenceCham) findPreference(KEY_SWIPE_LENGTH);
        int value = Settings.System.getInt(getContentResolver(),
                Settings.System.OMNI_BOTTOM_GESTURE_SWIPE_LIMIT,
                getSwipeLengthInPixel(getResources().getInteger(com.android.internal.R.integer.config_navgestureswipeminlength)));

        mSwipeTriggerLength.setMin(getSwipeLengthInPixel(40));
        mSwipeTriggerLength.setMax(getSwipeLengthInPixel(80));
        mSwipeTriggerLength.setValue(value);
        mSwipeTriggerLength.setOnPreferenceChangeListener(this);

        mSwipeTriggerStart = (SeekBarPreferenceCham) findPreference(KEY_SWIPE_START);
        value = Settings.System.getInt(getContentResolver(),
                Settings.System.BOTTOM_GESTURE_SWIPE_START,
                getResources().getInteger(com.android.internal.R.integer.config_navgestureswipestart));
        mSwipeTriggerStart.setValue(value);
        mSwipeTriggerStart.setOnPreferenceChangeListener(this);

        mSwipeTriggerTimeout = (SeekBarPreferenceCham) findPreference(KEY_SWIPE_TIMEOUT);
        value = Settings.System.getInt(getContentResolver(),
                Settings.System.OMNI_BOTTOM_GESTURE_TRIGGER_TIMEOUT,
                getResources().getInteger(com.android.internal.R.integer.config_navgestureswipetimout));
        mSwipeTriggerTimeout.setValue(value);
        mSwipeTriggerTimeout.setOnPreferenceChangeListener(this);

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference.equals(mSwipeTriggerLength)) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.OMNI_BOTTOM_GESTURE_SWIPE_LIMIT,
                    value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference.equals(mSwipeTriggerStart)) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.BOTTOM_GESTURE_SWIPE_START,
                    value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference.equals(mSwipeTriggerTimeout)) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.OMNI_BOTTOM_GESTURE_TRIGGER_TIMEOUT,
                    value, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    private int getSwipeLengthInPixel(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
    }
}
