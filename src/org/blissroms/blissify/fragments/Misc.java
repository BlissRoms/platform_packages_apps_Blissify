/*
 * Copyright (C) 2014-2021 The BlissRoms Project
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

package org.blissroms.blissify.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.bliss.BlissUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.bliss.support.preferences.CustomSeekBarPreference;
import com.bliss.support.preferences.SystemSettingSwitchPreference;
import com.bliss.support.preferences.SystemSettingListPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class Misc extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String PREF_FLASH_ON_CALL = "flashlight_on_call";
    private static final String PREF_FLASH_ON_CALL_DND = "flashlight_on_call_ignore_dnd";
    private static final String PREF_FLASH_ON_CALL_RATE = "flashlight_on_call_rate";
    private static final String FLASHLIGHT_CATEGORY = "flashlight_category";

    private CustomSeekBarPreference mFlashOnCallRate;
    private SystemSettingSwitchPreference mFlashOnCallIgnoreDND;
    private SystemSettingListPreference mFlashOnCall;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.blissify_misc);

        final ContentResolver resolver = getActivity().getContentResolver();
        final Context mContext = getActivity().getApplicationContext();
        final PreferenceScreen prefSet = getPreferenceScreen();
        final Resources res = mContext.getResources();

        if (!BlissUtils.deviceHasFlashlight(mContext)) {
            final PreferenceCategory flashlightCategory =
                    (PreferenceCategory) findPreference(FLASHLIGHT_CATEGORY);
            prefSet.removePreference(flashlightCategory);
        } else {
            mFlashOnCallRate = (CustomSeekBarPreference)
                    findPreference(PREF_FLASH_ON_CALL_RATE);
            int value = Settings.System.getInt(resolver,
                    Settings.System.FLASHLIGHT_ON_CALL_RATE, 1);
            mFlashOnCallRate.setValue(value);
            mFlashOnCallRate.setOnPreferenceChangeListener(this);

            mFlashOnCallIgnoreDND = (SystemSettingSwitchPreference)
                    findPreference(PREF_FLASH_ON_CALL_DND);
            value = Settings.System.getInt(resolver,
                    Settings.System.FLASHLIGHT_ON_CALL, 0);
            mFlashOnCallIgnoreDND.setVisible(value > 1);
            mFlashOnCallRate.setVisible(value != 0);

            mFlashOnCall = (SystemSettingListPreference)
                    findPreference(PREF_FLASH_ON_CALL);
            mFlashOnCall.setSummary(mFlashOnCall.getEntries()[value]);
            mFlashOnCall.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mFlashOnCall) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.FLASHLIGHT_ON_CALL, value);
            mFlashOnCall.setSummary(mFlashOnCall.getEntries()[value]);
            mFlashOnCallIgnoreDND.setVisible(value > 1);
            mFlashOnCallRate.setVisible(value != 0);
            return true;
        } else if (preference == mFlashOnCallRate) {
            int value = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.FLASHLIGHT_ON_CALL_RATE, value);
            return true;
        } else {
        return false;
    }
  }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.BLISSIFY;
    }

    /**
     * For Search.
     */

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.blissify_misc);
}
