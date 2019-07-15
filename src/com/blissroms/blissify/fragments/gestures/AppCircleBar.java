/*
 * Copyright (C) 2018 CarbonROM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blissroms.blissify.fragments.gestures;

import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Camera;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManagerGlobal;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.bliss.support.preferences.AppMultiSelectListPreference;
import com.bliss.support.preferences.SystemSettingSeekBarPreference;
import com.bliss.support.preferences.SystemSettingSwitchPreference;

import java.io.File;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class AppCircleBar extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, OnPreferenceClickListener {

    private static final String TAG = "AppCircleBar";

    private static final String PREF_INCLUDE_APP_CIRCLE_BAR_KEY = "app_circle_bar_included_apps";
    private static final String KEY_TRIGGER_WIDTH = "trigger_width";
    private static final String KEY_TRIGGER_TOP = "trigger_top";
    private static final String KEY_TRIGGER_BOTTOM = "trigger_bottom";
    private static final String ENABLE_APP_CIRCLE_BAR = "enable_app_circle_bar";

    private AppMultiSelectListPreference mIncludedAppCircleBar;

    private SystemSettingSwitchPreference mUseAppCircle;
    private SystemSettingSeekBarPreference mTriggerWidthPref;
    private SystemSettingSeekBarPreference mTriggerTopPref;
    private SystemSettingSeekBarPreference mTriggerBottomPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_circlebar);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        Resources res = getResources();

        // use app circle
        mUseAppCircle = (SystemSettingSwitchPreference) findPreference(ENABLE_APP_CIRCLE_BAR);
        mIncludedAppCircleBar = (AppMultiSelectListPreference) prefSet.findPreference(PREF_INCLUDE_APP_CIRCLE_BAR_KEY);
        mTriggerWidthPref = (SystemSettingSeekBarPreference) findPreference(KEY_TRIGGER_WIDTH);
        mTriggerTopPref = (SystemSettingSeekBarPreference) findPreference(KEY_TRIGGER_TOP);
        mTriggerBottomPref = (SystemSettingSeekBarPreference) findPreference(KEY_TRIGGER_BOTTOM);

        int useAppCircle = Settings.System.getIntForUser(resolver,
                Settings.System.ENABLE_APP_CIRCLE_BAR, 0, UserHandle.USER_CURRENT);
        mUseAppCircle.setOnPreferenceChangeListener(this);
        mUseAppCircle.setChecked(useAppCircle != 0);

        Set<String> includedApps = getIncludedApps();
        if (includedApps != null) mIncludedAppCircleBar.setValues(includedApps);
        mIncludedAppCircleBar.setOnPreferenceChangeListener(this);

        mTriggerWidthPref.setValue(Settings.System.getInt(resolver,
                Settings.System.APP_CIRCLE_BAR_TRIGGER_WIDTH, 25));
        mTriggerWidthPref.setOnPreferenceChangeListener(this);

        mTriggerTopPref.setValue(Settings.System.getInt(resolver,
                Settings.System.APP_CIRCLE_BAR_TRIGGER_TOP, 0));
        mTriggerTopPref.setOnPreferenceChangeListener(this);

        mTriggerBottomPref.setValue(Settings.System.getInt(resolver,
                Settings.System.APP_CIRCLE_BAR_TRIGGER_HEIGHT, 100));
        mTriggerBottomPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.BLISSIFY;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        final String key = preference.getKey();
        if (preference == mIncludedAppCircleBar) {
            storeIncludedApps((Set<String>) objValue);
        } else if (preference == mTriggerWidthPref) {
            int width = ((Integer)objValue).intValue();
            Settings.System.putInt(resolver,
                    Settings.System.APP_CIRCLE_BAR_TRIGGER_WIDTH, width);
            return true;
        } else if (preference == mTriggerTopPref) {
            int top = ((Integer)objValue).intValue();
            Settings.System.putInt(resolver,
                    Settings.System.APP_CIRCLE_BAR_TRIGGER_TOP, top);
            return true;
        } else if (preference == mTriggerBottomPref) {
            int bottom = ((Integer)objValue).intValue();
            Settings.System.putInt(resolver,
                    Settings.System.APP_CIRCLE_BAR_TRIGGER_HEIGHT, bottom);
            return true;
        } else if (preference == mUseAppCircle) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver,
                    Settings.System.ENABLE_APP_CIRCLE_BAR, value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    private Set<String> getIncludedApps() {
        String included = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.WHITELIST_APP_CIRCLE_BAR);
        if (TextUtils.isEmpty(included)) {
            return null;
        }
        return new HashSet<String>(Arrays.asList(included.split("\\|")));
    }

    private void storeIncludedApps(Set<String> values) {
        StringBuilder builder = new StringBuilder();
        String delimiter = "";
        for (String value : values) {
            builder.append(delimiter);
            builder.append(value);
            delimiter = "|";
        }
        Settings.System.putString(getActivity().getContentResolver(),
                Settings.System.WHITELIST_APP_CIRCLE_BAR, builder.toString());
    }

    @Override
    public void onPause() {
        super.onPause();
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.APP_CIRCLE_BAR_SHOW_TRIGGER, 0);
    }
     @Override
    public void onResume() {
        super.onResume();
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.APP_CIRCLE_BAR_SHOW_TRIGGER, 1);
    }
}
