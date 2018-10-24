/*
 * Copyright (C) 2019 crDroid Android Project
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
package com.blissroms.blissify.fragments.lockscreen;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settings.R;
import com.bliss.support.colorpicker.ColorPickerPreference;

import java.util.List;
import java.util.ArrayList;

public class Weather extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener  {

    private static final String TAG = "OmniJawsSettings";
    private static final String WEATHER_ICON_PACK = "weather_icon_pack";
    private static final String DEFAULT_WEATHER_ICON_PACKAGE = "org.omnirom.omnijaws";
    private static final String DEFAULT_WEATHER_ICON_PREFIX = "outline";
    private static final String CHRONUS_ICON_PACK_INTENT = "com.dvtonder.chronus.ICON_PACK";
    private static final String LOCKSCREEN_WEATHER_TEMP_COLOR = "lockscreen_weather_temp_color";
    private static final String LOCKSCREEN_WEATHER_CITY_COLOR = "lockscreen_weather_city_color";
    private static final String LOCKSCREEN_WEATHER_ICON_COLOR = "lockscreen_weather_icon_color";

    static final int TRANSPARENT = 0x99FFFFFF;

    private ListPreference mWeatherIconPack;
    private ColorPickerPreference mWeatherRightTextColorPicker;
    private ColorPickerPreference mWeatherLeftTextColorPicker;
    private ColorPickerPreference mWeatherIconColorPicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.weather);

        ContentResolver resolver = getActivity().getContentResolver();

        String settingsJaws = Settings.System.getString(resolver,
                Settings.System.OMNIJAWS_WEATHER_ICON_PACK);
        if (settingsJaws == null) {
            settingsJaws = DEFAULT_WEATHER_ICON_PACKAGE + "." + DEFAULT_WEATHER_ICON_PREFIX;
        }
        mWeatherIconPack = (ListPreference) findPreference(WEATHER_ICON_PACK);

        List<String> entries = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        getAvailableWeatherIconPacks(entries, values);
        mWeatherIconPack.setEntries(entries.toArray(new String[entries.size()]));
        mWeatherIconPack.setEntryValues(values.toArray(new String[values.size()]));

        int valueJawsIndex = mWeatherIconPack.findIndexOfValue(settingsJaws);
        if (valueJawsIndex == -1) {
            // no longer found
            settingsJaws = DEFAULT_WEATHER_ICON_PACKAGE + "." + DEFAULT_WEATHER_ICON_PREFIX;
            Settings.System.putString(resolver,
                    Settings.System.OMNIJAWS_WEATHER_ICON_PACK, settingsJaws);
            valueJawsIndex = mWeatherIconPack.findIndexOfValue(settingsJaws);
        }
        mWeatherIconPack.setValueIndex(valueJawsIndex >= 0 ? valueJawsIndex : 0);
        mWeatherIconPack.setSummary(mWeatherIconPack.getEntry());
        mWeatherIconPack.setOnPreferenceChangeListener(this);

        int intColor;
        String hexColor;

        mWeatherRightTextColorPicker = (ColorPickerPreference) findPreference(LOCKSCREEN_WEATHER_TEMP_COLOR);
        mWeatherRightTextColorPicker.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(getContentResolver(),
                     Settings.System.LOCK_SCREEN_WEATHER_TEMP_COLOR, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mWeatherRightTextColorPicker.setSummary(hexColor);
        mWeatherRightTextColorPicker.setNewPreviewColor(intColor);

        mWeatherLeftTextColorPicker = (ColorPickerPreference) findPreference(LOCKSCREEN_WEATHER_CITY_COLOR);
        mWeatherLeftTextColorPicker.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCK_SCREEN_WEATHER_CITY_COLOR, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mWeatherLeftTextColorPicker.setSummary(hexColor);
        mWeatherLeftTextColorPicker.setNewPreviewColor(intColor);

        mWeatherIconColorPicker = (ColorPickerPreference) findPreference(LOCKSCREEN_WEATHER_ICON_COLOR);
        mWeatherIconColorPicker.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(getContentResolver(),
                     Settings.System.LOCK_SCREEN_WEATHER_ICON_COLOR, TRANSPARENT);
        hexColor = String.format("#%08x", (0x99FFFFFF & intColor));
        mWeatherIconColorPicker.setSummary(hexColor);
        mWeatherIconColorPicker.setNewPreviewColor(intColor);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mWeatherIconPack) {
            String value = (String) newValue;
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.OMNIJAWS_WEATHER_ICON_PACK, value);
            int valueIndex = mWeatherIconPack.findIndexOfValue(value);
            mWeatherIconPack.setSummary(mWeatherIconPack.getEntries()[valueIndex]);
        } else if (preference == mWeatherRightTextColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCK_SCREEN_WEATHER_TEMP_COLOR, intHex);
            return true;
        } else if (preference == mWeatherLeftTextColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCK_SCREEN_WEATHER_CITY_COLOR, intHex);
            return true;
        } else if (preference == mWeatherIconColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCK_SCREEN_WEATHER_ICON_COLOR, intHex);
            return true;
        }
        return true;
    }

    private void getAvailableWeatherIconPacks(List<String> entries, List<String> values) {
        Intent i = new Intent();
        PackageManager packageManager = getActivity().getPackageManager();
        i.setAction("org.omnirom.WeatherIconPack");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                values.add(0, r.activityInfo.name);
            } else {
                values.add(r.activityInfo.name);
            }
            String label = r.activityInfo.loadLabel(getActivity().getPackageManager()).toString();
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
            String label = r.activityInfo.loadLabel(getActivity().getPackageManager()).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            entries.add(label);
        }
    }

    private boolean isOmniJawsEnabled() {
        final Uri SETTINGS_URI
            = Uri.parse("content://org.omnirom.omnijaws.provider/settings");

        final String[] SETTINGS_PROJECTION = new String[] {
            "enabled"
        };

        final Cursor c = getActivity().getContentResolver().query(SETTINGS_URI, SETTINGS_PROJECTION,
                null, null, null);
        if (c != null) {
            int count = c.getCount();
            if (count == 1) {
                c.moveToPosition(0);
                boolean enabled = c.getInt(0) == 1;
                return enabled;
            }
        }
        return true;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_SCREEN_WEATHER_TEMP_COLOR, 0xffffffff, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_SCREEN_WEATHER_CITY_COLOR, 0xffffffff, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_SCREEN_WEATHER_ICON_COLOR, 0xffffffff, UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
    }
}
