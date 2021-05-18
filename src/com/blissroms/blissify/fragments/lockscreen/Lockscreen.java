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

package com.blissroms.blissify.fragments.lockscreen;

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

import android.os.SystemProperties;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import com.bliss.support.preferences.CustomSeekBarPreference;
import com.bliss.support.preferences.SystemSettingListPreference;
import com.bliss.support.preferences.SystemSettingSeekBarPreference;
import com.bliss.support.preferences.SystemSettingSwitchPreference;
import com.bliss.support.preferences.SecureSettingSwitchPreference;
import com.bliss.support.colorpicker.ColorPickerPreference;


import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import android.util.Log;
import android.hardware.fingerprint.FingerprintManager;
import android.app.WallpaperManager;
import android.os.ParcelFileDescriptor;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.bliss.support.preferences.SystemSettingSeekBarPreference;
import com.blissroms.blissify.utils.Utils;

@SearchIndexable
public class Lockscreen extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, Indexable {

    private static final String LOCKSCREEN_CATEGORY = "lockscreen_category";
    private static final String LOCKSCREEN_FOD_CATEGORY = "lockscreen_fod_category";
    private static final String KEY_LOCKSCREEN_BLUR = "lockscreen_blur";
    private static final String AMBIENT_LIGHT_COLOR = "ambient_notification_color_mode";
    private static final String AMBIENT_LIGHT_CUSTOM_COLOR = "ambient_notification_light_color";
    private static final String AMBIENT_LIGHT_DURATION = "ambient_notification_light_duration";
    private static final String AMBIENT_LIGHT_REPEAT_COUNT = "ambient_notification_light_repeats";
    private ContentResolver mResolver;
    private Preference FODSettings;
    private Context mContext;
    private SystemSettingSeekBarPreference mLockscreenBlur;
    private SystemSettingListPreference mEdgeLightColorMode;
    private ColorPickerPreference mEdgeLightColor;
    private CustomSeekBarPreference mEdgeLightDuration;
    private CustomSeekBarPreference mEdgeLightRepeatCount;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.blissify_lockscreen);
        PreferenceScreen prefScreen = getPreferenceScreen();
        Context mContext = getContext();
        WallpaperManager manager = WallpaperManager.getInstance(mContext);

        FODSettings = (Preference) findPreference(LOCKSCREEN_FOD_CATEGORY);
        if (!getResources().getBoolean(com.android.internal.R.bool.config_needCustomFODView)) {
            prefScreen.removePreference(FODSettings);
        }

        ParcelFileDescriptor pfd = manager.getWallpaperFile(WallpaperManager.FLAG_LOCK);
        mLockscreenBlur = (SystemSettingSeekBarPreference) findPreference(KEY_LOCKSCREEN_BLUR);
        if (!Utils.isBlurSupported() || pfd != null) {
            mLockscreenBlur.setEnabled(false);
            mLockscreenBlur.setSummary(getString(R.string.lockscreen_blur_disabled));
        }
        mEdgeLightColorMode = (SystemSettingListPreference) findPreference(AMBIENT_LIGHT_COLOR);
        int edgeLightColorMode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_COLOR_MODE, 0, UserHandle.USER_CURRENT);
        mEdgeLightColorMode.setValue(String.valueOf(edgeLightColorMode));
        mEdgeLightColorMode.setSummary(mEdgeLightColorMode.getEntry());
        mEdgeLightColorMode.setOnPreferenceChangeListener(this);

        mEdgeLightColor = (ColorPickerPreference) findPreference(AMBIENT_LIGHT_CUSTOM_COLOR);
        int edgeLightColor = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_COLOR, 0xFFFFFFFF);
        mEdgeLightColor.setNewPreviewColor(edgeLightColor);
        String edgeLightColorHex = String.format("#%08x", (0xFFFFFFFF & edgeLightColor));
        if (edgeLightColorHex.equals("#ffffffff")) {
            mEdgeLightColor.setSummary(R.string.default_string);
        } else {
            mEdgeLightColor.setSummary(edgeLightColorHex);
        }
        mEdgeLightColor.setOnPreferenceChangeListener(this);

        mEdgeLightDuration = (CustomSeekBarPreference) findPreference(AMBIENT_LIGHT_DURATION);
        int lightDuration = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_DURATION, 2, UserHandle.USER_CURRENT);
        mEdgeLightDuration.setValue(lightDuration);
        mEdgeLightDuration.setOnPreferenceChangeListener(this);

        mEdgeLightRepeatCount = (CustomSeekBarPreference) findPreference(AMBIENT_LIGHT_REPEAT_COUNT);
        int edgeLightRepeatCount = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_REPEATS, 0, UserHandle.USER_CURRENT);
        mEdgeLightRepeatCount.setValue(edgeLightRepeatCount);
        mEdgeLightRepeatCount.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
         Context context = getContext();
         ContentResolver resolver = getActivity().getContentResolver();

         if (preference == mEdgeLightColorMode) {
            int edgeLightColorMode = Integer.valueOf((String) objValue);
            int index = mEdgeLightColorMode.findIndexOfValue((String) objValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NOTIFICATION_PULSE_COLOR_MODE, edgeLightColorMode, UserHandle.USER_CURRENT);
            mEdgeLightColorMode.setSummary(mEdgeLightColorMode.getEntries()[index]);
            if (edgeLightColorMode == 3) {
                mEdgeLightColor.setEnabled(true);
            } else {
                mEdgeLightColor.setEnabled(false);
            }
            return true;
        } else if (preference == mEdgeLightColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(objValue)));
            if (hex.equals("#ffffffff")) {
                preference.setSummary(R.string.default_string);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NOTIFICATION_PULSE_COLOR, intHex);
            return true;
        } else if (preference == mEdgeLightDuration) {
            int value = (Integer) objValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NOTIFICATION_PULSE_DURATION, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mEdgeLightRepeatCount) {
            int value = (Integer) objValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NOTIFICATION_PULSE_REPEATS, value, UserHandle.USER_CURRENT);
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
                    sir.xmlResId = R.xml.blissify_lockscreen;
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
