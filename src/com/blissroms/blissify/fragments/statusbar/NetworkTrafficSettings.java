/*
 * Copyright (C) 2014-2020 BlissRoms Project
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

package com.blissroms.blissify.fragments.statusbar;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.bliss.support.preferences.CustomSeekBarPreference;
import com.bliss.support.preferences.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class NetworkTrafficSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private CustomSeekBarPreference mThreshold;
    private CustomSeekBarPreference mNetTrafficSize;
    private SystemSettingSwitchPreference mShowArrows;
    private ListPreference mNetTrafficLocation;
    private ListPreference mNetTrafficType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.network_traffic_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        // Traffic location/state now combined (0=disabled, 1=statusbar, 2=header)
        int location = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_STATE, 0, UserHandle.USER_CURRENT);
        mNetTrafficLocation = (ListPreference) findPreference("network_traffic_state");
		mNetTrafficLocation.setValue(String.valueOf(location));
        mNetTrafficLocation.setSummary(mNetTrafficLocation.getEntry());
        mNetTrafficLocation.setOnPreferenceChangeListener(this);

        int typeValue = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_TYPE, 0, UserHandle.USER_CURRENT);
        mNetTrafficType = (ListPreference) findPreference("network_traffic_type");
        mNetTrafficType.setValue(String.valueOf(typeValue));
        mNetTrafficType.setSummary(mNetTrafficType.getEntry());
        mNetTrafficType.setOnPreferenceChangeListener(this);

        int thresholdValue = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 1, UserHandle.USER_CURRENT);
        mThreshold = (CustomSeekBarPreference) findPreference("network_traffic_autohide_threshold");
        mThreshold.setValue(thresholdValue);
        mThreshold.setOnPreferenceChangeListener(this);

        int sizeValue = Settings.System.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_FONT_SIZE, 36);
        mNetTrafficSize = (CustomSeekBarPreference) findPreference("network_traffic_font_size");
        mNetTrafficSize.setValue(sizeValue / 1);
        mNetTrafficSize.setOnPreferenceChangeListener(this);

        mShowArrows = (SystemSettingSwitchPreference) findPreference("network_traffic_arrow");
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNetTrafficLocation) {
            int location = Integer.valueOf((String) newValue);
            int index = mNetTrafficLocation.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_STATE,
                    location, UserHandle.USER_CURRENT);
            mNetTrafficLocation.setSummary(mNetTrafficLocation.getEntries()[index]);
            // Preference enablement requirement checks
            mNetTrafficType.setEnabled(netTrafficEnabled());
            mShowArrows.setEnabled(netTrafficEnabled());
            mThreshold.setEnabled(netTrafficEnabled());
            mNetTrafficSize.setEnabled(netTrafficEnabled() && fontResizingAvailable());
            return true;
        } else if (preference == mNetTrafficType) {
            int typeValue = Integer.valueOf((String) newValue);
            int index = mNetTrafficType.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_TYPE,
                    typeValue, UserHandle.USER_CURRENT);
            mNetTrafficType.setSummary(mNetTrafficType.getEntries()[index]);
            // Check font resizing enablement requirements again here for certain use cases
            mNetTrafficSize.setEnabled(fontResizingAvailable() && netTrafficEnabled());
            return true;
        } else if (preference == mThreshold) {
            int thresholdValue = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD,
                    thresholdValue, UserHandle.USER_CURRENT);
            return true;
        }  else if (preference == mNetTrafficSize) {
            int sizeValue = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_FONT_SIZE,
                    sizeValue, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    private boolean netTrafficEnabled() {
        final ContentResolver resolver = getActivity().getContentResolver();
        return Settings.System.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_STATE, 0) != 0;
	}

    private boolean fontResizingAvailable() {
        final ContentResolver resolver = getActivity().getContentResolver();
        return Settings.System.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_TYPE, 0) != 0;
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
                final ArrayList<SearchIndexableResource> result = new ArrayList<>();
                final SearchIndexableResource sir = new SearchIndexableResource(context);
                sir.xmlResId = R.xml.network_traffic_settings;
                result.add(sir);
                return result;
            }

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                final List<String> keys = super.getNonIndexableKeys(context);
                return keys;
            }
    };
}

