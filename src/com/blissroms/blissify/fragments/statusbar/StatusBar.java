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
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
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

import lineageos.preference.LineageSystemSettingListPreference;
import lineageos.preference.LineageSecureSettingSwitchPreference;
import lineageos.providers.LineageSettings;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import android.util.Log;

import android.widget.Toast;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Set;

import com.android.internal.util.bliss.BlissUtils;
import com.blissroms.blissify.utils.DeviceUtils;
import com.blissroms.blissify.utils.TelephonyUtils;
import com.bliss.support.colorpicker.ColorPickerPreference;

@SearchIndexable
public class StatusBar extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, Indexable {

    private static final String CATEGORY_BATTERY = "status_bar_battery_key";
    private static final String CATEGORY_CLOCK = "status_bar_clock_key";

    private static final String ICON_BLACKLIST = "icon_blacklist";

    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";

    private static final String TEXT_CHARGING_SYMBOL = "text_charging_symbol";
    private static final String CATEGORY_BATTERY_BAR = "batterybar_key";
    private static final String KEY_SHOW_VOLTE = "volte_icon_style";
    private static final String KEY_SHOW_DATA_DISABLED = "data_disabled_icon";
    private static final String KEY_SHOW_ROAMING = "roaming_indicator_icon";
    private static final String KEY_SHOW_FOURG = "show_fourg_icon";
    private static final String KEY_OLD_MOBILETYPE = "use_old_mobiletype";
    private static final String BLISS_LOGO_COLOR = "status_bar_logo_color";
    private static final String KEY_VOWIFI_ICON_STYLE = "vowifi_icon_style";
    private static final String KEY_VOLTE_VOWIFI_OVERRIDE = "volte_vowifi_override";
    private static final String KEY_VOLTE_CATEGORY = "volte_icon_category";
    private static final String KEY_STATUSBAR_FOOTER = "statusbar_footer_pref";

    private LineageSystemSettingListPreference mStatusBarClock;
    private LineageSystemSettingListPreference mStatusBarAmPm;
    private LineageSystemSettingListPreference mStatusBarBattery;
    private LineageSystemSettingListPreference mStatusBarBatteryShowPercent;
    private ListPreference mTextChargingSymbol;

    private PreferenceCategory mStatusBarBatteryCategory;
    private PreferenceCategory mStatusBarClockCategory;
    private PreferenceScreen mNetworkTrafficPref;
    private PreferenceCategory mBatteryBarCategory;
    private ListPreference mShowVolte;
    private SwitchPreference mDataDisabled;
    private SwitchPreference mShowRoaming;
    private SwitchPreference mShowFourg;
    private ColorPickerPreference mBlissLogoColor;
    private SwitchPreference mOldMobileType;
    private ListPreference mVowifiIconStyle;
    private SwitchPreference mOverride;
    private PreferenceCategory mVolteCategory;
    private Preference mStatusbarFooter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.blissify_statusbar);

        PreferenceScreen prefSet = getPreferenceScreen();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();
        Context mContext = getActivity().getApplicationContext();

        mStatusBarClock =
                (LineageSystemSettingListPreference) findPreference(STATUS_BAR_CLOCK_STYLE);

        mStatusBarClockCategory =
                (PreferenceCategory) prefSet.findPreference(CATEGORY_CLOCK);

        mStatusBarBatteryCategory =
                (PreferenceCategory) prefSet.findPreference(CATEGORY_BATTERY);

        mBatteryBarCategory =
                (PreferenceCategory) prefSet.findPreference(CATEGORY_BATTERY_BAR);

        mTextChargingSymbol = (ListPreference) findPreference(TEXT_CHARGING_SYMBOL);

        mShowVolte = (ListPreference) findPreference(KEY_SHOW_VOLTE);
        mDataDisabled = (SwitchPreference) findPreference(KEY_SHOW_DATA_DISABLED);
        mShowRoaming = (SwitchPreference) findPreference(KEY_SHOW_ROAMING);
        mShowFourg = (SwitchPreference) findPreference(KEY_SHOW_FOURG);
        mVowifiIconStyle = (ListPreference) findPreference(KEY_VOWIFI_ICON_STYLE);
        mOverride = (SwitchPreference) findPreference(KEY_VOLTE_VOWIFI_OVERRIDE);

        if (!TelephonyUtils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(mDataDisabled);
            prefScreen.removePreference(mShowVolte);
            prefScreen.removePreference(mShowRoaming);
            prefScreen.removePreference(mShowFourg);
            prefScreen.removePreference(mVowifiIconStyle);
            prefScreen.removePreference(mOverride);
        }

        mBlissLogoColor =
                (ColorPickerPreference) findPreference(BLISS_LOGO_COLOR);
        int intColor = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_LOGO_COLOR, 0xFFFFFFFF,
                UserHandle.USER_CURRENT);
        String hexColor = ColorPickerPreference.convertToARGB(intColor);
        mBlissLogoColor.setNewPreviewColor(intColor);
        if (intColor != 0xFFFFFFFF) {
            mBlissLogoColor.setSummary(hexColor);
        } else {
            mBlissLogoColor.setSummary(R.string.default_string);
        }
        mBlissLogoColor.setOnPreferenceChangeListener(this);

        mOldMobileType = (SwitchPreference) findPreference(KEY_OLD_MOBILETYPE);
        boolean mConfigUseOldMobileType = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_useOldMobileIcons);

        boolean showing = Settings.System.getIntForUser(resolver,
                Settings.System.USE_OLD_MOBILETYPE,
                mConfigUseOldMobileType ? 1 : 0, UserHandle.USER_CURRENT) != 0;
        mOldMobileType.setChecked(showing);

        mVolteCategory = (PreferenceCategory) findPreference(KEY_VOLTE_CATEGORY);

        if (!DeviceUtils.isVowifiAvailable(mContext)) {
            mVolteCategory.removePreference(mVowifiIconStyle);
            mVolteCategory.removePreference(mOverride);
        } else {
            mVolteCategory.addPreference(mVowifiIconStyle);
            mVolteCategory.addPreference(mOverride);
        }

        mStatusbarFooter = (Preference) findPreference(KEY_STATUSBAR_FOOTER);

        if (!DeviceUtils.hasNotch(mContext)) {
            prefScreen.removePreference(mStatusbarFooter);
        }
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

        if (BlissUtils.hasNotch(getContext())) {
            getPreferenceScreen().removePreference(mBatteryBarCategory);
        } else {
            getPreferenceScreen().addPreference(mBatteryBarCategory);
        }

        // Adjust status bar preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            if (BlissUtils.hasNotch(getContext())) {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch_rtl);
            } else {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_rtl);
            }
        } else if (BlissUtils.hasNotch(getContext())) {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch);
            mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch);
        } else {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries);
            mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values);
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBlissLogoColor) {
            String hex = ColorPickerPreference.convertToARGB(
                Integer.parseInt(String.valueOf(newValue)));
            int value = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_LOGO_COLOR, value,
                UserHandle.USER_CURRENT);
            if (value != 0xFFFFFFFF) {
                mBlissLogoColor.setSummary(hex);
            } else {
                mBlissLogoColor.setSummary(R.string.default_string);
            }
            return true;
        }
        return false;
    }

    private int getClockPosition() {
        return LineageSettings.System.getInt(getActivity().getContentResolver(),
                STATUS_BAR_CLOCK_STYLE, 2);
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        boolean mConfigUseOldMobileType = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_useOldMobileIcons);

        Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_BATTERY_STYLE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_BATTERY_TEXT_CHARGING, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.TEXT_CHARGING_SYMBOL, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.VOLTE_ICON_STYLE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.VOWIFI_ICON_STYLE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.VOLTE_VOWIFI_OVERRIDE, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.DATA_DISABLED_ICON, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ROAMING_INDICATOR_ICON, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.BLUETOOTH_SHOW_BATTERY, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.SHOW_FOURG_ICON, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_LOGO, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_LOGO_COLOR, 0xFFFFFFFF, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_LOGO_POSITION, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_LOGO_STYLE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.USE_OLD_MOBILETYPE, mConfigUseOldMobileType ? 1 : 0, UserHandle.USER_CURRENT);
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
                    sir.xmlResId = R.xml.blissify_statusbar;
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
