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
import com.blissroms.blissify.utils.TelephonyUtils;

import com.bliss.support.preferences.CustomSeekBarPreference;
import com.bliss.support.preferences.SystemSettingListPreference;
import com.bliss.support.preferences.SystemSettingSeekBarPreference;

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
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

@SearchIndexable
public class LockScreenClock extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String LOCKSCREEN_CLOCK_SELECTION = "lockscreen_clock_selection";
    private static final String TEXT_CLOCK_ALIGNMENT = "text_clock_alignment";
    private static final String TEXT_CLOCK_PADDING = "text_clock_padding";
    private static final String KEY_LOCKSCREEN_CLOCK_CATEGORY = "lockscreen_clock_category";
    private static final String KEY_LOCKSCREEN_FONT_STYLE = "lock_clock_font_style";
    private static final String KEY_LOCKSCREEN_FONT_SIZE = "lock_clock_font_size";
    private static final String KEY_TEXT_CLOCK_CATEGORY = "text_clock_customizations";

    private SystemSettingListPreference mLockClockSelection;
    private ListPreference mTextClockAlign;
    private CustomSeekBarPreference mTextClockPadding;
    private PreferenceCategory mLockClockCategory;
    private PreferenceCategory mTextClockCategory;
    private ListPreference mFontStyle;
    private SystemSettingSeekBarPreference mFontSize;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.lockscreen_clock);
        ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefSet = getPreferenceScreen();
        Context mContext = getContext();

        mLockClockCategory = (PreferenceCategory) findPreference(KEY_LOCKSCREEN_CLOCK_CATEGORY);
        mFontStyle = (ListPreference) findPreference(KEY_LOCKSCREEN_FONT_STYLE);
        mFontSize = (SystemSettingSeekBarPreference) findPreference(KEY_LOCKSCREEN_FONT_SIZE);

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
        mTextClockCategory = (PreferenceCategory) findPreference(KEY_TEXT_CLOCK_CATEGORY);
        mTextClockAlign = (ListPreference) findPreference(TEXT_CLOCK_ALIGNMENT);
        mTextClockAlign.setEnabled(mClockSelection);
        mTextClockAlign.setOnPreferenceChangeListener(this);

        // Text Clock Padding
        mTextClockPadding = (CustomSeekBarPreference) findPreference(TEXT_CLOCK_PADDING);
        boolean mTextClockAlignx = Settings.System.getIntForUser(resolver,
                    Settings.System.TEXT_CLOCK_ALIGNMENT, 0, UserHandle.USER_CURRENT) == 1;
        mTextClockPadding.setEnabled(!mTextClockAlignx);

        updatePreferences();
    }

    @Override
    public void onPause() {
        super.onPause();

        updateClock();
    }

    @Override
    public void onResume() {
        super.onResume();

        updateClock();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mLockClockSelection) {
            int value = Integer.parseInt((String) objValue);
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

    private void updatePreferences() {
        ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefSet = getPreferenceScreen();

        final String currentClock = Settings.Secure.getString(
            resolver, Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_FACE);
        final boolean mIsAnalogeClock = currentClock != null &&
                         currentClock.contains("AnalogClockController") ||
                         currentClock.contains("BlissClockController") ||
                         currentClock.contains("CustomNumClockController") ||
                         currentClock.contains("DotClockController") ||
                         currentClock.contains("SneekyClockControllerk") ||
                         currentClock.contains("SpectrumClockController") ||
                         currentClock.contains("SpideyClockController") ||
                         currentClock.contains("OP") ? true : false;

        if (mIsAnalogeClock) {
            mLockClockCategory.removePreference(mLockClockSelection);
            mLockClockCategory.removePreference(mFontStyle);
            mLockClockCategory.removePreference(mFontSize);
            prefSet.removePreference(mTextClockCategory);
        } else {
            mLockClockCategory.addPreference(mLockClockSelection);
            mLockClockCategory.addPreference(mFontStyle);
            mLockClockCategory.addPreference(mFontSize);
        }
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_HIDE_CLOCK, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_CLOCK_SELECTION, 2, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_CLOCK_FONT_STYLE, 36, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_DATE_FONT_STYLE, 36, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_DATE_SELECTION, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_DATE_HIDE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_DATE_FONT_SIZE, 18, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_CLOCK_FONT_SIZE , 50, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_OWNERINFO_FONTS, 36, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKOWNER_FONT_SIZE, 18, UserHandle.USER_CURRENT);
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
                    sir.xmlResId = R.xml.lockscreen_clock;
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
