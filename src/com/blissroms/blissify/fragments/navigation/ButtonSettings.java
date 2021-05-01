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

package com.blissroms.blissify.fragments.buttons;

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
import android.os.PowerManager;
import android.os.ServiceManager;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import android.util.Log;
import android.hardware.fingerprint.FingerprintManager;
import com.android.internal.util.hwkeys.ActionConstants;
import com.android.internal.util.hwkeys.ActionUtils;
import com.blissroms.blissify.ui.ActionFragment;
import com.bliss.support.preferences.CustomSeekBarPreference;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

@SearchIndexable
public class ButtonSettings extends ActionFragment implements
        OnPreferenceChangeListener, Indexable {

    private static final String BUTTONS_CATEGORY = "buttons_category";
    private static final String HWKEY_DISABLE = "hardware_keys_disable";

    // category keys
    private static final String CATEGORY_HWKEY = "hardware_keys";
    private static final String CATEGORY_BACK = "back_key";
    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_MENU = "menu_key";
    private static final String CATEGORY_ASSIST = "assist_key";
    private static final String CATEGORY_APPSWITCH = "app_switch_key";
    private static final String CATEGORY_VOLUME = "volume_keys";
    private static final String CATEGORY_POWER = "power_key";
   	private static final String KEY_BUTTON_MANUAL_BRIGHTNESS_NEW = "button_manual_brightness_new";
    private static final String KEY_BUTTON_TIMEOUT = "button_timeout";
    private static final String KEY_BUTON_BACKLIGHT_OPTIONS = "button_backlight_options_category";

     // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    public static final int KEY_MASK_HOME = 0x01;
    public static final int KEY_MASK_BACK = 0x02;
    public static final int KEY_MASK_MENU = 0x04;
    public static final int KEY_MASK_ASSIST = 0x08;
    public static final int KEY_MASK_APP_SWITCH = 0x10;
    public static final int KEY_MASK_CAMERA = 0x20;
    public static final int KEY_MASK_VOLUME = 0x40;

    private SwitchPreference mHwKeyDisable;
    private CustomSeekBarPreference mButtonTimoutBar;
    private CustomSeekBarPreference mManualButtonBrightness;
    private PreferenceCategory mButtonBackLightCategory;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final ContentResolver resolver = getActivity().getContentResolver();
        addPreferencesFromResource(R.xml.blissify_buttons);
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final boolean needsNavbar = ActionUtils.hasNavbarByDefault(getActivity());
        final PreferenceCategory hwkeyCat = (PreferenceCategory) prefScreen
                .findPreference(CATEGORY_HWKEY);
        int keysDisabled = 0;
        if (!needsNavbar) {
            mHwKeyDisable = (SwitchPreference) findPreference(HWKEY_DISABLE);
            keysDisabled = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.HARDWARE_KEYS_DISABLE, 0,
                    UserHandle.USER_CURRENT);
            mHwKeyDisable.setChecked(keysDisabled != 0);
            mHwKeyDisable.setOnPreferenceChangeListener(this);
        } else {
            prefScreen.removePreference(hwkeyCat);
        }

         // bits for hardware keys present on device
        final int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
         // read bits for present hardware keys
        final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        final boolean hasBackKey = (deviceKeys & KEY_MASK_BACK) != 0;
        final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
        final boolean hasAssistKey = (deviceKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;
         // load categories and init/remove preferences based on device
        // configuration
        final PreferenceCategory backCategory = (PreferenceCategory) prefScreen
                .findPreference(CATEGORY_BACK);
        final PreferenceCategory homeCategory = (PreferenceCategory) prefScreen
                .findPreference(CATEGORY_HOME);
        final PreferenceCategory menuCategory = (PreferenceCategory) prefScreen
                .findPreference(CATEGORY_MENU);
        final PreferenceCategory assistCategory = (PreferenceCategory) prefScreen
                .findPreference(CATEGORY_ASSIST);
        final PreferenceCategory appSwitchCategory = (PreferenceCategory) prefScreen
                .findPreference(CATEGORY_APPSWITCH);
         // back key
        if (!hasBackKey) {
            prefScreen.removePreference(backCategory);
        }
         // home key
        if (!hasHomeKey) {
            prefScreen.removePreference(homeCategory);
        }
         // App switch key (recents)
        if (!hasAppSwitchKey) {
            prefScreen.removePreference(appSwitchCategory);
        }
         // menu key
        if (!hasMenuKey) {
            prefScreen.removePreference(menuCategory);
        }
         // search/assist key
        if (!hasAssistKey) {
            prefScreen.removePreference(assistCategory);
        }

        // load preferences first
        setActionPreferencesEnabled(keysDisabled == 0);

         // let super know we can load ActionPreferences
        onPreferenceScreenLoaded(ActionConstants.getDefaults(ActionConstants.HWKEYS));

        mManualButtonBrightness = (CustomSeekBarPreference) findPreference(
                KEY_BUTTON_MANUAL_BRIGHTNESS_NEW);
        final int customButtonBrightness = getResources().getInteger(
                com.android.internal.R.integer.config_button_brightness_default);
        final int currentBrightness = Settings.System.getInt(resolver,
                Settings.System.CUSTOM_BUTTON_BRIGHTNESS, customButtonBrightness);
        PowerManager pm = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
        mManualButtonBrightness.setMax(pm.getMaximumScreenBrightnessSetting());
        mManualButtonBrightness.setValue(currentBrightness);
        mManualButtonBrightness.setOnPreferenceChangeListener(this);

        mButtonTimoutBar = (CustomSeekBarPreference) findPreference(KEY_BUTTON_TIMEOUT);
        int currentTimeout = Settings.System.getInt(resolver,
                Settings.System.BUTTON_BACKLIGHT_TIMEOUT, 0);
        mButtonTimoutBar.setValue(currentTimeout);
        mButtonTimoutBar.setOnPreferenceChangeListener(this);

        final boolean enableBacklightOptions = getResources().getBoolean(
                com.android.internal.R.bool.config_button_brightness_support);

        mButtonBackLightCategory = (PreferenceCategory) findPreference(KEY_BUTON_BACKLIGHT_OPTIONS);

        if (!enableBacklightOptions) {
            prefScreen.removePreference(mButtonBackLightCategory);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mButtonTimoutBar) {
            int buttonTimeout = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.BUTTON_BACKLIGHT_TIMEOUT, buttonTimeout);
        } else if (preference == mHwKeyDisable) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(), Settings.System.HARDWARE_KEYS_DISABLE,
                    value ? 1 : 0);
            setActionPreferencesEnabled(!value);
        } else if (preference == mManualButtonBrightness) {
            int buttonBrightness = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.CUSTOM_BUTTON_BRIGHTNESS, buttonBrightness);
        } else {
            return false;
		}
        return true;
    }

    @Override
    protected boolean usesExtendedActionsList() {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
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
                    sir.xmlResId = R.xml.blissify_buttons;
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
