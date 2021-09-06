/*
 * Copyright (C) 2014-2022 BlissRoms Project
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

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.bliss.support.colorpicker.ColorPickerPreference;
import com.bliss.support.preferences.SystemSettingSwitchPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.Utils;

import java.io.FileDescriptor;

public class StatusBarLogo extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String CUSTOM_SB_LOGO_ENABLED = "custom_sb_logo_enabled";
    private static final String CUSTOM_SB_LOGO_IMAGE = "custom_sb_logo_image";
    private static final int REQUEST_PICK_SB_IMAGE = 0;

    private ListPreference mShowLogo;
    private ListPreference mLogoStyle;
    private ColorPickerPreference mStatusBarLogoColor;
    private Preference mCustomSbLogoImage;
    private SystemSettingSwitchPreference mCustomSbLogoEnabled;
    static final int DEFAULT_LOGO_COLOR = 0xffff8800;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.blissify_statusbar_logo);

        mShowLogo = (ListPreference) findPreference("status_bar_logo");
        mShowLogo.setOnPreferenceChangeListener(this);
        int showLogo = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_LOGO,
                0, UserHandle.USER_CURRENT);
        mShowLogo.setValue(String.valueOf(showLogo));
        mShowLogo.setSummary(mShowLogo.getEntry());

        mLogoStyle = (ListPreference) findPreference("status_bar_logo_style");
        mLogoStyle.setOnPreferenceChangeListener(this);
        int logoStyle = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_LOGO_STYLE,
                0, UserHandle.USER_CURRENT);
        mLogoStyle.setValue(String.valueOf(logoStyle));
        mLogoStyle.setSummary(mLogoStyle.getEntry());

        mStatusBarLogoColor = (ColorPickerPreference) findPreference("status_bar_logo_color");
        mStatusBarLogoColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_LOGO_COLOR, DEFAULT_LOGO_COLOR);
        String hexColor = String.format("#%08x", (DEFAULT_LOGO_COLOR & intColor));
        mStatusBarLogoColor.setSummary(hexColor);
        mStatusBarLogoColor.setNewPreviewColor(intColor);

        mCustomSbLogoImage = findPreference(CUSTOM_SB_LOGO_IMAGE);

        mCustomSbLogoEnabled = (SystemSettingSwitchPreference) findPreference(CUSTOM_SB_LOGO_ENABLED);
        boolean valSbLogo = Settings.System.getIntForUser(getActivity().getContentResolver(),
                Settings.System.CUSTOM_SB_LOGO_ENABLED, 0, UserHandle.USER_CURRENT) == 1;
        mCustomSbLogoEnabled.setOnPreferenceChangeListener(this);
        if (valSbLogo) {
            mLogoStyle.setEnabled(false);
            mStatusBarLogoColor.setEnabled(false);
        } else {
            mLogoStyle.setEnabled(true);
            mStatusBarLogoColor.setEnabled(true);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mCustomSbLogoImage) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_SB_IMAGE);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference.equals(mShowLogo)) {
            int showLogo = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_LOGO, showLogo, UserHandle.USER_CURRENT);
            int index = mShowLogo.findIndexOfValue((String) newValue);
            mShowLogo.setSummary(
                    mShowLogo.getEntries()[index]);
            return true;
        } else if (preference.equals(mLogoStyle)) {
            int logoStyle = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_LOGO_STYLE, logoStyle, UserHandle.USER_CURRENT);
            int index = mLogoStyle.findIndexOfValue((String) newValue);
            mLogoStyle.setSummary(
                    mLogoStyle.getEntries()[index]);
            return true;
        } else if (preference.equals(mStatusBarLogoColor)) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_LOGO_COLOR, intHex);
            return true;
        } else if (preference.equals(mCustomSbLogoEnabled)) {
            boolean valSbLogo = (Boolean) newValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.CUSTOM_SB_LOGO_ENABLED, valSbLogo ? 1 : 0,
                    UserHandle.USER_CURRENT);
            if (valSbLogo) {
                mLogoStyle.setEnabled(false);
                mStatusBarLogoColor.setEnabled(false);
            } else {
                mLogoStyle.setEnabled(true);
                mStatusBarLogoColor.setEnabled(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == REQUEST_PICK_SB_IMAGE) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }
            final Uri uriSb = result.getData();
            setSbPickerIcon(uriSb.toString());
            Settings.System.putString(getContentResolver(), Settings.System.CUSTOM_SB_LOGO_IMAGE, uriSb.toString());
        }
    }

    private void setSbPickerIcon(String uri) {
        try {
                ParcelFileDescriptor parcelFileDescriptor =
                    getContext().getContentResolver().openFileDescriptor(Uri.parse(uri), "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap imageSbLogo = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
                Drawable d = new BitmapDrawable(getResources(), imageSbLogo);
                mCustomSbLogoImage.setIcon(d);
            }
            catch (Exception e) {}
    }
}
