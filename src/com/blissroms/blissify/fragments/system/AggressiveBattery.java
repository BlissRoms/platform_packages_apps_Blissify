/*
 * Copyright (C) 2016 CarbonROM
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

package com.blissroms.blissify.fragments.system;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.SearchIndexableResource;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class AggressiveBattery extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "AggressiveBattery";
    private static final String PREF_IDLE_ENABLED = "aggressive_idle_enabled";
    private static final String PREF_STANDBY_ENABLED = "aggressive_standby_enabled";
    private static final String PREF_BATTERY_SAVER = "aggressive_battery_saver";

    private SwitchPreference mAgressiveIdleEnabled;
    private SwitchPreference mAgressiveStandbyEnabled;
    private SwitchPreference mAgressiveBatterySaver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.aggressive_battery);

        mAgressiveIdleEnabled = (SwitchPreference) findPreference(PREF_IDLE_ENABLED);
        mAgressiveIdleEnabled.setChecked((Settings.Global.getInt(
                getActivity().getApplicationContext().getContentResolver(),
                Settings.Global.AGGRESSIVE_IDLE_ENABLED, 0) == 1));
        mAgressiveIdleEnabled.setOnPreferenceChangeListener(this);

        mAgressiveStandbyEnabled = (SwitchPreference) findPreference(PREF_STANDBY_ENABLED);
        mAgressiveStandbyEnabled.setChecked((Settings.Global.getInt(
                getActivity().getApplicationContext().getContentResolver(),
                Settings.Global.AGGRESSIVE_STANDBY_ENABLED, 0) == 1));
        mAgressiveStandbyEnabled.setOnPreferenceChangeListener(this);

        mAgressiveBatterySaver = (SwitchPreference) findPreference(PREF_BATTERY_SAVER);
        mAgressiveBatterySaver.setChecked((Settings.Global.getInt(
                getActivity().getApplicationContext().getContentResolver(),
                Settings.Global.AGGRESSIVE_BATTERY_SAVER, 0) == 1));
        mAgressiveBatterySaver.setOnPreferenceChangeListener(this);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.BLISSIFY;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAgressiveIdleEnabled) {
            Settings.Global.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.Global.AGGRESSIVE_IDLE_ENABLED,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mAgressiveStandbyEnabled) {
            Settings.Global.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.Global.AGGRESSIVE_STANDBY_ENABLED,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mAgressiveBatterySaver) {
            Settings.Global.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.Global.AGGRESSIVE_BATTERY_SAVER,
                    (Boolean) newValue ? 1 : 0);
            return true;
        }
        return false;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                            boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.aggressive_battery;
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
