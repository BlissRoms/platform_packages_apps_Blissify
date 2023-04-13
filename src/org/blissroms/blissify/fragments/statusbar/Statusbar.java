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

package org.blissroms.blissify.fragments.statusbar;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import android.text.format.DateFormat;
import android.provider.Settings;
import com.android.settings.R;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;
import com.bliss.support.preferences.SystemSettingSwitchPreference;
import com.bliss.support.preferences.SystemSettingSeekBarPreference;
import com.bliss.support.preferences.SecureSettingSwitchPreference;
import com.bliss.support.preferences.SystemSettingListPreference;
import com.bliss.support.preferences.SecureSettingListPreference;
import com.bliss.support.preferences.CustomSeekBarPreference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import com.android.settings.Utils;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class Statusbar extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_STATUS_BAR_AM_PM = "status_bar_am_pm";
    private static final String BATTERY_STYLE = "status_bar_battery_style";
    private static final String SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String SHOW_BATTERY_PERCENT_CHARGING = "status_bar_show_battery_percent_charging";
    private static final String SHOW_BATTERY_PERCENT_INSIDE = "status_bar_show_battery_percent_inside";
    private static final String STATUSBAR_LEFT_PADDING = "statusbar_left_padding";
    private static final String STATUSBAR_RIGHT_PADDING = "statusbar_right_padding";
    private static final String STATUSBAR_TOP_PADDING = "statusbar_top_padding";

    private SystemSettingListPreference mBatteryStyle;
    private SystemSettingSwitchPreference mBatteryPercent;
    private SystemSettingSwitchPreference mBatteryPercentCharging;
    private SystemSettingSwitchPreference mBatteryPercentInside;
    private SecureSettingListPreference mStatusBarAmPm;
    private SystemSettingSwitchPreference mThreshold;
    private SystemSettingSwitchPreference mNetMonitor;
    private SystemSettingSeekBarPreference mSbLeftPadding;
    private SystemSettingSeekBarPreference mSbRightPadding;
    private SystemSettingSeekBarPreference mSbTopPadding;
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.blissify_statusbar);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        Context mContext = getActivity().getApplicationContext();
        Resources res = null;
        Context ctx = getContext();

	mStatusBarAmPm = findPreference(KEY_STATUS_BAR_AM_PM);

        boolean isNetMonitorEnabled = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_STATE, 1, UserHandle.USER_CURRENT) == 1;
        mNetMonitor = (SystemSettingSwitchPreference) findPreference("network_traffic_state");
        mNetMonitor.setChecked(isNetMonitorEnabled);
        mNetMonitor.setOnPreferenceChangeListener(this);

        boolean isThresholdEnabled = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 0, UserHandle.USER_CURRENT) == 1;
        mThreshold = (SystemSettingSwitchPreference) findPreference("network_traffic_autohide_threshold");
        mThreshold.setChecked(isThresholdEnabled);
        mThreshold.setOnPreferenceChangeListener(this);

        mBatteryPercent = findPreference(SHOW_BATTERY_PERCENT);
        final boolean percentEnabled = Settings.System.getIntForUser(resolver,
                SHOW_BATTERY_PERCENT, 0, UserHandle.USER_CURRENT) == 1;
        mBatteryPercent.setChecked(percentEnabled);
        mBatteryPercent.setOnPreferenceChangeListener(this);

        mBatteryPercentInside = findPreference(SHOW_BATTERY_PERCENT_INSIDE);
        mBatteryPercentInside.setEnabled(percentEnabled);
        final boolean percentInside = Settings.System.getIntForUser(resolver,
                SHOW_BATTERY_PERCENT_INSIDE, 0, UserHandle.USER_CURRENT) == 1;
        mBatteryPercentInside.setChecked(percentInside);
        mBatteryPercentInside.setOnPreferenceChangeListener(this);

        mBatteryStyle = findPreference(BATTERY_STYLE);
        int value = Settings.System.getIntForUser(resolver,
                BATTERY_STYLE, 0, UserHandle.USER_CURRENT);
        mBatteryStyle.setValue(Integer.toString(value));
        mBatteryStyle.setSummary(mBatteryStyle.getEntry());
        mBatteryStyle.setOnPreferenceChangeListener(this);
        updatePercentEnablement(value != 2);

        mBatteryPercentCharging = findPreference(SHOW_BATTERY_PERCENT_CHARGING);
        updatePercentChargingEnablement(value, percentEnabled, percentInside);

        float density = Resources.getSystem().getDisplayMetrics().density;

        try {
            res = ctx.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        mSbLeftPadding = (SystemSettingSeekBarPreference) findPreference(STATUSBAR_LEFT_PADDING);
        int sbLeftPadding = Settings.System.getIntForUser(ctx.getContentResolver(),
                Settings.System.LEFT_PADDING, ((int) (res.getIdentifier("com.android.systemui:dimen/status_bar_padding_start", null, null) / density)), UserHandle.USER_CURRENT);
        mSbLeftPadding.setValue(sbLeftPadding);
        mSbLeftPadding.setOnPreferenceChangeListener(this);

        mSbRightPadding = (SystemSettingSeekBarPreference) findPreference(STATUSBAR_RIGHT_PADDING);
        int sbRightPadding = Settings.System.getIntForUser(ctx.getContentResolver(),
                Settings.System.RIGHT_PADDING, ((int) (res.getIdentifier("com.android.systemui:dimen/status_bar_padding_end", null, null) / density)), UserHandle.USER_CURRENT);
        mSbRightPadding.setValue(sbRightPadding);
        mSbRightPadding.setOnPreferenceChangeListener(this);
        
        mSbTopPadding = (SystemSettingSeekBarPreference) findPreference(STATUSBAR_TOP_PADDING);
        int sbTopPadding = Settings.System.getIntForUser(ctx.getContentResolver(),
               Settings.System.TOP_PADDING, ((int) (res.getIdentifier("com.android.systemui:dimen/status_bar_padding_top", null, null) / density)), UserHandle.USER_CURRENT);
        mSbTopPadding.setValue(sbTopPadding);
        mSbTopPadding.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        Resources res = null;
        Context ctx = getContext();

         if (preference == mNetMonitor) {
            boolean value = (Boolean) newValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_STATE, value ? 1 : 0,
                    UserHandle.USER_CURRENT);
            mNetMonitor.setChecked(value);
            mThreshold.setEnabled(value);
            return true;
        } else if (preference == mThreshold) {
            boolean value = (Boolean) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, value ? 1 : 0,
                    UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mBatteryStyle) {
            int value = Integer.valueOf((String) newValue);
            int index = mBatteryStyle.findIndexOfValue((String) newValue);
            mBatteryStyle.setSummary(mBatteryStyle.getEntries()[index]);
            Settings.System.putIntForUser(resolver,
                    BATTERY_STYLE, value, UserHandle.USER_CURRENT);
            updatePercentEnablement(value != 2);
            updatePercentChargingEnablement(value, null, null);
            return true;
        } else if (preference == mBatteryPercent) {
            boolean enabled = (boolean) newValue;
            Settings.System.putInt(resolver,
                    SHOW_BATTERY_PERCENT, enabled ? 1 : 0);
            mBatteryPercentInside.setEnabled(enabled);
            updatePercentChargingEnablement(null, enabled, null);
            return true;
        } else if (preference == mBatteryPercentInside) {
            boolean enabled = (boolean) newValue;
            Settings.System.putInt(resolver,
                    SHOW_BATTERY_PERCENT_INSIDE, enabled ? 1 : 0);
            // we already know style isn't text and percent is enabled
            mBatteryPercentCharging.setEnabled(enabled);
            return true;
        }
       float density = Resources.getSystem().getDisplayMetrics().density;

        try {
            res = ctx.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        if (preference == mSbLeftPadding) {
            int leftValue = (Integer) newValue;
            int sbLeft = ((int) (leftValue / density));
            Settings.System.putIntForUser(getContext().getContentResolver(),
                    Settings.System.LEFT_PADDING, sbLeft, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mSbRightPadding) {
            int rightValue = (Integer) newValue;
            int sbRight = ((int) (rightValue / density));
            Settings.System.putIntForUser(getContext().getContentResolver(),
                    Settings.System.RIGHT_PADDING, sbRight, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mSbTopPadding) {
            int topValue = (Integer) newValue;
            int sbTop = ((int) (topValue / density));
            Settings.System.putIntForUser(getContext().getContentResolver(),
                    Settings.System.TOP_PADDING, sbTop, UserHandle.USER_CURRENT);
            return true;
        }
       return false;
    }

    private void updatePercentEnablement(boolean enabled) {
        mBatteryPercent.setEnabled(enabled);
        mBatteryPercentInside.setEnabled(enabled && mBatteryPercent.isChecked());
    }

    private void updatePercentChargingEnablement(Integer style, Boolean percent, Boolean inside) {
        if (style == null) style = Integer.valueOf(mBatteryStyle.getValue());
        if (percent == null) percent = mBatteryPercent.isChecked();
        if (inside == null) inside = mBatteryPercentInside.isChecked();
        mBatteryPercentCharging.setEnabled(style != 2 && (!percent || inside));
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.BLISSIFY;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (DateFormat.is24HourFormat(requireContext())) {
            mStatusBarAmPm.setEnabled(false);
            mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_unavailable);
        }
    }

    /**
     * For Search.
     */

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.blissify_statusbar);
}
