/*
 * Copyright (C) 2014-2025 The BlissRoms Project
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

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

import org.blissroms.blissify.preferences.SystemSettingListPreference;
import org.blissroms.blissify.preferences.SystemSettingSwitchPreference;
import org.blissroms.blissify.utils.DeviceUtils;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class Statusbar extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_BATTERY_STYLE = "status_bar_battery_style";
    private static final String KEY_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String KEY_BATTERY_TEXT_CHARGING = "status_bar_battery_text_charging";
    private static final String KEY_ICONS_CATEGORY = "status_bar_icons_category";
    private static final String KEY_DATA_DISABLED_ICON = "data_disabled_icon";
    private static final String KEY_BLUETOOTH_BATTERY_STATUS = "bluetooth_show_battery";
    private static final String KEY_FOUR_G_ICON = "show_fourg_icon";

    private static final int BATTERY_STYLE_PORTRAIT = 0;
    private static final int BATTERY_STYLE_TEXT = 4;
    private static final int BATTERY_STYLE_HIDDEN = 5;

    private SystemSettingListPreference mBatteryPercent;
    private SystemSettingListPreference mBatteryStyle;
    private SystemSettingSwitchPreference mBatteryTextCharging;
    private PreferenceCategory mIconsCategory;
    private SystemSettingSwitchPreference mDataDisabledIcon;
    private SystemSettingSwitchPreference mFourgIcon;
    private SystemSettingSwitchPreference mBluetoothBatteryStatus;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.blissify_statusbar);

        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources resources = context.getResources();

        mIconsCategory = (PreferenceCategory) findPreference(KEY_ICONS_CATEGORY);
        mDataDisabledIcon = (SystemSettingSwitchPreference) findPreference(KEY_DATA_DISABLED_ICON);
        mFourgIcon = (SystemSettingSwitchPreference) findPreference(KEY_FOUR_G_ICON);
        mBluetoothBatteryStatus = (SystemSettingSwitchPreference) findPreference(KEY_BLUETOOTH_BATTERY_STATUS);

        if (!DeviceUtils.deviceSupportsMobileData(context)) {
            mIconsCategory.removePreference(mDataDisabledIcon);
            mIconsCategory.removePreference(mFourgIcon);
        }

        if (!DeviceUtils.deviceSupportsBluetooth(context)) {
            mIconsCategory.removePreference(mBluetoothBatteryStatus);
        }

        mBatteryStyle = (SystemSettingListPreference) findPreference(KEY_BATTERY_STYLE);
        mBatteryPercent = (SystemSettingListPreference) findPreference(KEY_BATTERY_PERCENT);
        mBatteryTextCharging = (SystemSettingSwitchPreference) findPreference(KEY_BATTERY_TEXT_CHARGING);
        mBluetoothBatteryStatus = (SystemSettingSwitchPreference) findPreference(KEY_BLUETOOTH_BATTERY_STATUS);

        int batterystyle = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_PORTRAIT, UserHandle.USER_CURRENT);
        int batterypercent = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0, UserHandle.USER_CURRENT);

        mBatteryStyle.setOnPreferenceChangeListener(this);

        mBatteryPercent.setEnabled(
                batterystyle != BATTERY_STYLE_TEXT && batterystyle != BATTERY_STYLE_HIDDEN);
        mBatteryPercent.setOnPreferenceChangeListener(this);

        mBatteryTextCharging.setEnabled(batterystyle == BATTERY_STYLE_HIDDEN ||
                (batterystyle != BATTERY_STYLE_TEXT && batterypercent != 2));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        if (preference == mBatteryStyle) {
            int value = Integer.parseInt((String) newValue);
            int batterypercent = Settings.System.getIntForUser(resolver,
                    Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0, UserHandle.USER_CURRENT);
            mBatteryPercent.setEnabled(
                    value != BATTERY_STYLE_TEXT && value != BATTERY_STYLE_HIDDEN);
            mBatteryTextCharging.setEnabled(value == BATTERY_STYLE_HIDDEN ||
                    (value != BATTERY_STYLE_TEXT && batterypercent != 2));
            return true;
        } else if (preference == mBatteryPercent) {
            int value = Integer.parseInt((String) newValue);
            int batterystyle = Settings.System.getIntForUser(resolver,
                    Settings.System.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_PORTRAIT, UserHandle.USER_CURRENT);
            mBatteryTextCharging.setEnabled(batterystyle == BATTERY_STYLE_HIDDEN ||
                    (batterystyle != BATTERY_STYLE_TEXT && value != 2));
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.BLISSIFY;
    }

    /**
     * For Search.
     */

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider(R.xml.blissify_statusbar) {

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                List<String> keys = super.getNonIndexableKeys(context);
                final Resources resources = context.getResources();

                if (!DeviceUtils.deviceSupportsMobileData(context)) {
                    keys.add(KEY_DATA_DISABLED_ICON);
                    keys.add(KEY_FOUR_G_ICON);
                }
                if (!DeviceUtils.deviceSupportsBluetooth(context)) {
                    keys.add(KEY_BLUETOOTH_BATTERY_STATUS);
                }
                return keys;
            }
        };
}
