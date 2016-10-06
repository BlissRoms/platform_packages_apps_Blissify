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

package com.blissroms.blissify.statusbar.tabs;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class BlissLogo extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "BlissLogo";

    private static final String KEY_BLISS_LOGO_COLOR = "status_bar_bliss_logo_color";

    private ColorPickerPreference mBlissLogoColor;
    private ListPreference mBlissLogoStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.statusbar_logo);

        PreferenceScreen prefSet = getPreferenceScreen();

        // Bliss logo color
        mBlissLogoColor =
        (ColorPickerPreference) prefSet.findPreference(KEY_BLISS_LOGO_COLOR);
        mBlissLogoColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getInt(getContentResolver(),
        Settings.System.STATUS_BAR_BLISS_LOGO_COLOR, 0xffffffff);
       	String hexColor = String.format("#%08x", (0xffffffff & intColor));
        mBlissLogoColor.setSummary(hexColor);
        mBlissLogoColor.setNewPreviewColor(intColor);
    }

	@Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBlissLogoColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_BLISS_LOGO_COLOR, intHex);
            return true;
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.BLISSIFY;
    }
}
