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
package com.blissroms.blissify.fragments.statusbar;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.provider.Settings;
import com.android.settings.R;

import android.text.format.DateFormat;
import android.util.ArraySet;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import androidx.preference.DropDownPreference;

import lineageos.preference.LineageSecureSettingSwitchPreference;
import lineageos.providers.LineageSettings;

import lineageos.preference.LineageSystemSettingListPreference;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import android.util.Log;

import android.widget.Toast;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Set;

public class StatusBar extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String CATEGORY_BATTERY = "status_bar_battery_key";
    private static final String CATEGORY_CLOCK = "status_bar_clock_key";

    private static final String ICON_BLACKLIST = "icon_blacklist";

    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";

    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String STATUS_BAR_QUICK_QS_PULLDOWN = "qs_quick_pulldown";

    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 2;

    private static final int PULLDOWN_DIR_NONE = 0;
    private static final int PULLDOWN_DIR_RIGHT = 1;
    private static final int PULLDOWN_DIR_LEFT = 2;

    private static final String NETWORK_TRAFFIC_SETTINGS = "network_traffic_settings";

    private LineageSystemSettingListPreference mQuickPulldown;
    private LineageSystemSettingListPreference mStatusBarClock;
    private LineageSystemSettingListPreference mStatusBarAmPm;
    private LineageSystemSettingListPreference mStatusBarBattery;
    private LineageSystemSettingListPreference mStatusBarBatteryShowPercent;

    private PreferenceCategory mStatusBarBatteryCategory;
    private PreferenceCategory mStatusBarClockCategory;
    private PreferenceScreen mNetworkTrafficPref;

    private static boolean sHasNotch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.blissify_statusbar);

        mNetworkTrafficPref = (PreferenceScreen) findPreference(NETWORK_TRAFFIC_SETTINGS);

        sHasNotch = getResources().getBoolean(
                org.lineageos.platform.internal.R.bool.config_haveNotch);

        if (sHasNotch) {
            getPreferenceScreen().removePreference(mNetworkTrafficPref);
        }

        mStatusBarClock =
                (LineageSystemSettingListPreference) findPreference(STATUS_BAR_CLOCK_STYLE);
        mStatusBarClock.setOnPreferenceChangeListener(this);

        mStatusBarClockCategory =
                (PreferenceCategory) getPreferenceScreen().findPreference(CATEGORY_CLOCK);

        mStatusBarBatteryShowPercent =
                (LineageSystemSettingListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);
        mStatusBarBattery =
                (LineageSystemSettingListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        mStatusBarBattery.setOnPreferenceChangeListener(this);
        enableStatusBarBatteryDependents(mStatusBarBattery.getIntValue(2));

        mStatusBarBatteryCategory =
                (PreferenceCategory) getPreferenceScreen().findPreference(CATEGORY_BATTERY);

        mQuickPulldown =
                (LineageSystemSettingListPreference) findPreference(STATUS_BAR_QUICK_QS_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        updateQuickPulldownSummary(mQuickPulldown.getIntValue(0));

    }

    @Override
    public void onResume() {
        super.onResume();

        final String curIconBlacklist = Settings.Secure.getString(getContext().getContentResolver(),
                ICON_BLACKLIST);

        if (TextUtils.delimitedStringContains(curIconBlacklist, ',', "clock")) {
            getPreferenceScreen().removePreference(mStatusBarClockCategory);
        } else {
            getPreferenceScreen().addPreference(mStatusBarClockCategory);
        }

        if (TextUtils.delimitedStringContains(curIconBlacklist, ',', "battery")) {
            getPreferenceScreen().removePreference(mStatusBarBatteryCategory);
        } else {
            getPreferenceScreen().addPreference(mStatusBarBatteryCategory);
        }

        final boolean disallowCenteredClock = sHasNotch || getNetworkTrafficStatus() != 0;

        // Adjust status bar preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            if (sHasNotch) {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch_rtl);
            } else {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_rtl);
            }
            mQuickPulldown.setEntries(R.array.status_bar_quick_qs_pulldown_entries_rtl);
            mQuickPulldown.setEntryValues(R.array.status_bar_quick_qs_pulldown_values_rtl);
        } else if (sHasNotch) {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch);
            mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch);
        } else {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries);
            mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values);
        }

        // Disable network traffic preferences if clock is centered in the status bar
        updateNetworkTrafficStatus(getClockPosition());

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = Integer.parseInt((String) newValue);
        String key = preference.getKey();
        switch (key) {
            case STATUS_BAR_QUICK_QS_PULLDOWN:
                updateQuickPulldownSummary(value);
                break;
            case STATUS_BAR_BATTERY_STYLE:
                enableStatusBarBatteryDependents(value);
                break;
        }
        return true;
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        mStatusBarBatteryShowPercent.setEnabled(batteryIconStyle != STATUS_BAR_BATTERY_STYLE_TEXT);
    }

    private void updateQuickPulldownSummary(int value) {
        String summary="";
        switch (value) {
            case PULLDOWN_DIR_NONE:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_off);
                break;

            case PULLDOWN_DIR_LEFT:
            case PULLDOWN_DIR_RIGHT:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_summary,
                    getResources().getString(value == PULLDOWN_DIR_LEFT
                        ? R.string.status_bar_quick_qs_pulldown_summary_left
                        : R.string.status_bar_quick_qs_pulldown_summary_right));
                break;
        }
        mQuickPulldown.setSummary(summary);
    }

    private void updateNetworkTrafficStatus(int clockPosition) {
        if (sHasNotch) {
            // Unconditional no network traffic for you
            return;
        }

        boolean isClockCentered = clockPosition == 1;
        mNetworkTrafficPref.setEnabled(!isClockCentered);
        mNetworkTrafficPref.setSummary(getResources().getString(isClockCentered ?
                R.string.network_traffic_disabled_clock :
                R.string.network_traffic_settings_summary
        ));
    }

    private int getNetworkTrafficStatus() {
        return LineageSettings.Secure.getInt(getActivity().getContentResolver(),
                LineageSettings.Secure.NETWORK_TRAFFIC_MODE, 0);
    }

    private int getClockPosition() {
        return LineageSettings.System.getInt(getActivity().getContentResolver(),
                STATUS_BAR_CLOCK_STYLE, 2);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
    }

}
