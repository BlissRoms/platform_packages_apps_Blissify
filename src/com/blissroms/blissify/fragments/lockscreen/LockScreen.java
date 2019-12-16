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

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import android.util.Log;
import android.hardware.fingerprint.FingerprintManager;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.bliss.support.preferences.CustomSeekBarPreference;
import com.bliss.support.preferences.SecureSettingListPreference;

import lineageos.app.LineageContextConstants;

public class LockScreen extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String KEY_LOCKSCREEN_MEDIA_BLUR = "lockscreen_media_blur";
    private static final String KEY_LOCKSCREEN_ALBUMART_FILTER = "lockscreen_albumart_filter";
    private static final String FOD_ICON_PICKER_CATEGORY = "fod_icon_picker";

    private CustomSeekBarPreference mLockscreenMediaBlur;
    private SecureSettingListPreference mLockscreenAlbumArt;
    private PreferenceCategory mFODIconPickerCategory;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.blissify_lockscreen);
        PreferenceScreen prefSet = getPreferenceScreen();
        Context mContext = getContext();

        int defaultBlur = 25;
        mLockscreenMediaBlur = (CustomSeekBarPreference) findPreference(KEY_LOCKSCREEN_MEDIA_BLUR);
        int value = Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_MEDIA_BLUR, defaultBlur);
        mLockscreenMediaBlur.setValue(value);
        mLockscreenMediaBlur.setOnPreferenceChangeListener(this);

        mLockscreenAlbumArt = (SecureSettingListPreference) findPreference(KEY_LOCKSCREEN_ALBUMART_FILTER);
        mLockscreenAlbumArt.setOnPreferenceChangeListener(this);

        PackageManager packageManager = mContext.getPackageManager();
        boolean hasFod = packageManager.hasSystemFeature(LineageContextConstants.Features.FOD);

        mFODIconPickerCategory = (PreferenceCategory) findPreference(FOD_ICON_PICKER_CATEGORY);
        if (mFODIconPickerCategory != null && !hasFod) {
            prefSet.removePreference(mFODIconPickerCategory);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {

        if (preference == mLockscreenMediaBlur) {
            int value = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_MEDIA_BLUR, value);
            return true;
        } else if (preference == mLockscreenAlbumArt) {
          int val = Integer.parseInt((String) objValue);
          if (val == 3) {
              mLockscreenMediaBlur.setEnabled(true);
          } else {
              mLockscreenMediaBlur.setEnabled(false);
          }
          return true;
        }

        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.Global.putInt(resolver,
                Settings.Global.LOCKSCREEN_ENABLE_POWER_MENU, 1);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_CHARGING_ANIMATION, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_LOCK_ICON, 1, UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
    }

}
