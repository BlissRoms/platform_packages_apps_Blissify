/*
 * Copyright (C) 2019 The BlissRoms Project
 * Copyright (C) 2019-2020 The Evolution X Project
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
package com.blissroms.blissify.fragments.themes;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;

import java.util.ArrayList;
import java.util.List;

public class SystemThemePreferenceController extends AbstractPreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_SYSTEM_THEME = "system_theme";

    private ListPreference mSystemTheme;

    public SystemThemePreferenceControllerContext context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SYSTEM_THEME;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mSystemTheme = screen.findPreference(KEY_SYSTEM_THEME);
        int systemTheme = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.SYSTEM_THEME, 0, UserHandle.USER_CURRENT);
        int valueIndex = mSystemTheme.findIndexOfValue(String.valueOf(systemTheme));
        mSystemTheme.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mSystemTheme.setSummary(mSystemTheme.getEntry());
        mSystemTheme.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mSystemTheme) {
            int systemThemeValue = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(mContext.getContentResolver(),
                    Settings.System.SYSTEM_THEME, systemThemeValue, UserHandle.USER_CURRENT);
            mSystemTheme.setSummary(mSystemTheme.getEntries()[systemThemeValue]);
            return true;
        }
        return false;
    }
}
