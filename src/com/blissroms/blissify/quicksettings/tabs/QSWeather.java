/*
* Copyright (C) 2014 BlissRoms Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.blissroms.blissify.quicksettings.tabs;

import android.os.Bundle;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceCategory;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.util.bliss.PackageUtils;

import java.util.ArrayList;
import java.util.List;

public class QSWeather extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "QSWEATHER";


    private static final String CATEGORY_WEATHER = "weather_category";

    private static final String WEATHER_ICON_PACK = "weather_icon_pack";
    private static final String DEFAULT_WEATHER_ICON_PACKAGE = "org.omnirom.omnijaws";
    private static final String WEATHER_SERVICE_PACKAGE = "org.omnirom.omnijaws";
    private static final String CHRONUS_ICON_PACK_INTENT = "com.dvtonder.chronus.ICON_PACK";

    private ListPreference mWeatherIconPack;
    private PreferenceCategory mWeatherCategory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.qs_weather);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mWeatherCategory = (PreferenceCategory) findPreference(CATEGORY_WEATHER);
        if (mWeatherCategory != null && !isOmniJawsServiceInstalled()) {
            prefSet.removePreference(mWeatherCategory);
        } else {
            String settingJawsPackage = Settings.System.getString(getContentResolver(),
                    Settings.System.OMNIJAWS_WEATHER_ICON_PACK);
            if (settingJawsPackage == null) {
                settingJawsPackage = DEFAULT_WEATHER_ICON_PACKAGE;
            }
            mWeatherIconPack = (ListPreference) findPreference(WEATHER_ICON_PACK);

            List<String> jawsentries = new ArrayList<String>();
            List<String> jawsvalues = new ArrayList<String>();
            getAvailableWeatherIconPacks(jawsentries, jawsvalues);
            mWeatherIconPack.setEntries(jawsentries.toArray(new String[jawsentries.size()]));
            mWeatherIconPack.setEntryValues(jawsvalues.toArray(new String[jawsvalues.size()]));

            int jawsvalueIndex = mWeatherIconPack.findIndexOfValue(settingJawsPackage);
            if (jawsvalueIndex == -1) {
                // no longer found
                settingJawsPackage = DEFAULT_WEATHER_ICON_PACKAGE;
                Settings.System.putString(getContentResolver(),
                        Settings.System.OMNIJAWS_WEATHER_ICON_PACK, settingJawsPackage);
                jawsvalueIndex = mWeatherIconPack.findIndexOfValue(settingJawsPackage);
            }
            mWeatherIconPack.setValueIndex(jawsvalueIndex >= 0 ? jawsvalueIndex : 0);
            mWeatherIconPack.setSummary(mWeatherIconPack.getEntry());
            mWeatherIconPack.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mWeatherIconPack) {
            String value = (String) newValue;
            Settings.System.putString(getContentResolver(),
                    Settings.System.OMNIJAWS_WEATHER_ICON_PACK, value);
            int valueIndex = mWeatherIconPack.findIndexOfValue(value);
            mWeatherIconPack.setSummary(mWeatherIconPack.getEntries()[valueIndex]);
            return true;
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.BLISSIFY;
    }

    private boolean isOmniJawsServiceInstalled() {
        return PackageUtils.isAvailableApp(WEATHER_SERVICE_PACKAGE, getActivity());
    }

    private void getAvailableWeatherIconPacks(List<String> entries, List<String> values) {
        Intent i = new Intent();
        PackageManager packageManager = getPackageManager();
        i.setAction("org.omnirom.WeatherIconPack");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                values.add(0, r.activityInfo.name);
            } else {
                values.add(r.activityInfo.name);
            }
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                entries.add(0, label);
            } else {
                entries.add(label);
            }
        }
        i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(CHRONUS_ICON_PACK_INTENT);
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            values.add(packageName + ".weather");
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            entries.add(label);
        }
    }
}
