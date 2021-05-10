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
package com.blissroms.blissify.fragments.system;

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

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.android.settings.Utils;
import android.util.Log;
import android.hardware.fingerprint.FingerprintManager;
import com.bliss.support.preferences.SystemSettingSwitchPreference;

import com.android.internal.util.bliss.BlissUtils;

import java.util.List;
import java.util.ArrayList;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class SystemSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String INCALL_VIB_OPTIONS = "incall_vib_options";
	private static final String FINGERPRINT_VIB = "fingerprint_success_vib";

    private FingerprintManager mFingerprintManager;
    private SystemSettingSwitchPreference mFingerprintVib;
    private static final String AGGRESSIVE_BATTERY ="aggressive_battery";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ContentResolver resolver = getActivity().getContentResolver();
        addPreferencesFromResource(R.xml.blissify_system);
        setRetainInstance(true);
        final PreferenceScreen prefSet = getPreferenceScreen();

        PreferenceCategory incallVibCategory = (PreferenceCategory) findPreference(INCALL_VIB_OPTIONS);
        if (!Utils.isVoiceCapable(getActivity())) {
                prefSet.removePreference(incallVibCategory);
        }

        final PackageManager mPm = getActivity().getPackageManager();

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SystemSettingSwitchPreference) findPreference(FINGERPRINT_VIB);
        if (mPm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) &&
                 mFingerprintManager != null) {
            if (!mFingerprintManager.isHardwareDetected()){
                prefSet.removePreference(mFingerprintVib);
            } else {
            mFingerprintVib.setChecked((Settings.System.getInt(getContentResolver(),
                    Settings.System.FP_SUCCESS_VIBRATE, 1) == 1));
            mFingerprintVib.setOnPreferenceChangeListener(this);
            }
        } else {
            prefSet.removePreference(mFingerprintVib);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	    if (preference == mFingerprintVib) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FP_SUCCESS_VIBRATE, value ? 1 : 0);
            return true;
        }
        return false;
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
                    sir.xmlResId = R.xml.blissify_system;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    keys.add(AGGRESSIVE_BATTERY);
                    return keys;
                }
    };
}
