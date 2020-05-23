/*
 * Copyright (C) 2019 The BlissRoms Project
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

package com.blissroms.blissify.fragments.lockscreen;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.provider.Settings;
import com.android.settings.R;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import android.util.Log;
import android.hardware.fingerprint.FingerprintManager;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.bliss.support.preferences.CustomSeekBarPreference;
import com.bliss.support.preferences.SystemSettingListPreference;

import lineageos.app.LineageContextConstants;

@SearchIndexable
public class LockScreen extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, Indexable {

    private static final String KEY_LOCKSCREEN_MEDIA_BLUR = "lockscreen_media_blur";
    private static final String FOD_ICON_PICKER_CATEGORY = "fod_icon_picker";
    private static final String LOCKSCREEN_CLOCK_SELECTION = "lockscreen_clock_selection";
    private static final String TEXT_CLOCK_ALIGNMENT = "text_clock_alignment";
    private static final String TEXT_CLOCK_PADDING = "text_clock_padding";

    private CustomSeekBarPreference mLockscreenMediaBlur;
    private PreferenceCategory mFODIconPickerCategory;
    private SystemSettingListPreference mLockClockSelection;
    private ListPreference mTextClockAlign;
    private CustomSeekBarPreference mTextClockPadding;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.blissify_lockscreen);
        ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefSet = getPreferenceScreen();
        Context mContext = getContext();

        int defaultBlur = 25;
        mLockscreenMediaBlur = (CustomSeekBarPreference) findPreference(KEY_LOCKSCREEN_MEDIA_BLUR);
        int value = Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_MEDIA_BLUR, defaultBlur);
        mLockscreenMediaBlur.setValue(value);
        mLockscreenMediaBlur.setOnPreferenceChangeListener(this);

        PackageManager packageManager = mContext.getPackageManager();
        boolean hasFod = packageManager.hasSystemFeature(LineageContextConstants.Features.FOD);

        mFODIconPickerCategory = (PreferenceCategory) findPreference(FOD_ICON_PICKER_CATEGORY);
        if (mFODIconPickerCategory != null && !hasFod) {
            prefSet.removePreference(mFODIconPickerCategory);
        }

        // Lockscreen Clock
        mLockClockSelection = (SystemSettingListPreference) findPreference(LOCKSCREEN_CLOCK_SELECTION);
        boolean mClockSelection = Settings.System.getIntForUser(resolver,
                Settings.System.LOCKSCREEN_CLOCK_SELECTION, 0, UserHandle.USER_CURRENT) == 12
                || Settings.System.getIntForUser(resolver,
                Settings.System.LOCKSCREEN_CLOCK_SELECTION, 0, UserHandle.USER_CURRENT) == 13;
        if (mLockClockSelection == null) {
            Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_CLOCK_SELECTION, 0, UserHandle.USER_CURRENT);
        }
        mLockClockSelection.setOnPreferenceChangeListener(this);

        // Text Clock Alignment
        mTextClockAlign = (ListPreference) findPreference(TEXT_CLOCK_ALIGNMENT);
        mTextClockAlign.setEnabled(mClockSelection);
        mTextClockAlign.setOnPreferenceChangeListener(this);

        // Text Clock Padding
        mTextClockPadding = (CustomSeekBarPreference) findPreference(TEXT_CLOCK_PADDING);
        boolean mTextClockAlignx = Settings.System.getIntForUser(resolver,
                    Settings.System.TEXT_CLOCK_ALIGNMENT, 0, UserHandle.USER_CURRENT) == 1;
        mTextClockPadding.setEnabled(!mTextClockAlignx);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateClock();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mLockClockSelection) {
            updateClock();
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {

        if (preference == mLockscreenMediaBlur) {
            int value = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_MEDIA_BLUR, value);
            return true;
        } else if (preference == mLockClockSelection) {
            int value = (Integer) objValue;
            String[] defaultClock = getResources().getStringArray(R.array.lockscreen_clock_selection_entries);
            String summary = defaultClock[value];
            mLockClockSelection.setSummary(summary);
            boolean val = Integer.valueOf((String) objValue) == 12
                    || Integer.valueOf((String) objValue) == 13;
            mTextClockAlign.setEnabled(val);
            return true;
        } else if (preference == mTextClockAlign) {
            boolean val = Integer.valueOf((String) objValue) == 1;
            mTextClockPadding.setEnabled(!val);
            return true;
        }

        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.Global.putInt(resolver,
                Settings.Global.LOCKSCREEN_ENABLE_POWER_MENU, 1);
        Settings.Global.putInt(resolver,
                Settings.Global.LOCKSCREEN_POWERMENU_SECURE, 0);
        Settings.Global.putInt(resolver,
                Settings.Global.LOCKSCREEN_ENABLE_QS, 1);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_CHARGING_ANIMATION_STYLE, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_LOCK_ICON, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_HIDE_CLOCK, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_CLOCK_SELECTION, 2, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_CLOCK_FONT_STYLE, 4, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_DATE_FONT_STYLE, 14, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_DATE_SELECTION, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_DATE_HIDE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_DATE_FONT_SIZE, 18, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_CLOCK_FONT_SIZE , 50, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_OWNERINFO_FONTS, 4, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKOWNER_FONT_SIZE, 18, UserHandle.USER_CURRENT);
    }

    private void updateClock() {
        ContentResolver resolver = getActivity().getContentResolver();
        String currentClock = Settings.Secure.getString(
            resolver, Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_FACE);
        final boolean mIsDefaultClock = currentClock != null && currentClock.contains("DefaultClock") ? true : false;
        String[] defaultClock = getResources().getStringArray(R.array.lockscreen_clock_selection_entries);
        String[] defaultClockValues = getResources().getStringArray(R.array.lockscreen_clock_selection_values);
        String[] pluginClock = getResources().getStringArray(R.array.lockscreen_clock_plugin_entries);
        String[] pluginClockValues = getResources().getStringArray(R.array.lockscreen_clock_plugin_values);
        if (mIsDefaultClock) {
            mLockClockSelection.setEntries(defaultClock);
            mLockClockSelection.setEntryValues(defaultClockValues);
        } else {
            mLockClockSelection.setEntries(pluginClock);
            mLockClockSelection.setEntryValues(pluginClockValues);
            Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_CLOCK_SELECTION, 0, UserHandle.USER_CURRENT);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.blissify_lockscreen;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };
}
