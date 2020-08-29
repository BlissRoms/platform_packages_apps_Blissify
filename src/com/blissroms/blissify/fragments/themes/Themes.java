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

package com.blissroms.blissify.fragments.themes;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import com.android.settings.R;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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

import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.darkmode.DarkModeObserver;
import com.android.settings.display.OverlayCategoryPreferenceController;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import android.util.Log;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.bliss.support.colorpicker.ColorPickerPreference;
import com.blissroms.blissify.utils.DeviceUtils;
import com.blissroms.blissify.fragments.themes.QsTileStylePreferenceController;
import com.blissroms.blissify.fragments.themes.SystemThemePreferenceController;

@SearchIndexable
public class Themes extends DashboardFragment  implements
        OnPreferenceChangeListener, Indexable {

    private static final String TAG = "Themes";
    private static final String ACCENT_COLOR = "accent_color";
    private static final String ACCENT_PRESET = "accent_preset";
    private static final String GRADIENT_COLOR = "gradient_color";
    private static final String ACCENT_COLOR_PROP = "persist.sys.theme.accentcolor";
    private static final String QS_HEADER_STYLE = "qs_header_style";
    private static final String SYSTEM_THEMES = "system_theme";
    private static final String FORCE_DARK_PREF = "hwui_force_dark";
    static final int DEFAULT_ACCENT_COLOR = 0xff1a73e8;

    private ColorPickerPreference mAccentColor;
    private ColorPickerPreference mGradientColor;
    private ListPreference mAccentPreset;
    private int mAccentIndex;
    private ListPreference mQsHeaderStyle;
    private boolean mEnabled;
    private DarkModeObserver mDarkModeObserver;
    private Runnable mCallback;
    private ListPreference mThemeSwitch;
    private SwitchPreference mForceDarkPref;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.blissify_themes;
    }

    private static final String CUSTOM_THEME_BROWSE = "theme_select_activity";

    private Preference mThemeBrowse;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final ContentResolver resolver = getActivity().getContentResolver();

        Context mContext = getContext();

        mThemeBrowse = findPreference(CUSTOM_THEME_BROWSE);
        mThemeBrowse.setEnabled(isBrowseThemesAvailable());

        mAccentColor = (ColorPickerPreference) findPreference(ACCENT_COLOR);
        mAccentColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getIntForUser(resolver,
                Settings.System.ACCENT_COLOR, DEFAULT_ACCENT_COLOR, UserHandle.USER_CURRENT);
        String hexColor = String.format("#%08x", (0xff1a73e8 & intColor));
        if (hexColor.equals("#ff1a73e8")) {
            mAccentColor.setSummary(R.string.default_string);
        } else {
            mAccentColor.setSummary(hexColor);
        }
        mAccentColor.setNewPreviewColor(intColor);

        mGradientColor = (ColorPickerPreference) findPreference(GRADIENT_COLOR);
        mGradientColor.setOnPreferenceChangeListener(this);
        int color = Settings.System.getIntForUser(resolver,
                Settings.System.GRADIENT_COLOR, DEFAULT_ACCENT_COLOR, UserHandle.USER_CURRENT);
        String gradientHex = String.format("#%08x", (0xff1a73e8 & color));
        if (gradientHex.equals("#ff1a73e8")) {
            mGradientColor.setSummary(R.string.default_string);
        } else {
            mGradientColor.setSummary(gradientHex);
        }
        mGradientColor.setNewPreviewColor(color);

        mAccentPreset = (ListPreference) findPreference(ACCENT_PRESET);
        mAccentPreset.setOnPreferenceChangeListener(this);
        String colorVal = SystemProperties.get(ACCENT_COLOR_PROP, "-1");
        checkColorPreset(colorVal);

        mQsHeaderStyle = (ListPreference) findPreference(QS_HEADER_STYLE);
        int qsHeaderStyle = Settings.System.getInt(resolver,
                Settings.System.QS_HEADER_STYLE, 0);
        int valueIndex = mQsHeaderStyle.findIndexOfValue(String.valueOf(qsHeaderStyle));
        mQsHeaderStyle.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mQsHeaderStyle.setSummary(mQsHeaderStyle.getEntry());
        mQsHeaderStyle.setOnPreferenceChangeListener(this);

        mThemeSwitch = (ListPreference) findPreference(SYSTEM_THEMES);
        mForceDarkPref = (SwitchPreference) findPreference(FORCE_DARK_PREF);
        mDarkModeObserver = new DarkModeObserver(mContext);
        mCallback = () -> {
            final boolean active = (getContext().getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_YES) != 0;
            if (active) {
                mForceDarkPref.setEnabled(true);
                mThemeSwitch.setEnabled(true);
            } else {
                mForceDarkPref.setEnabled(false);
                mThemeSwitch.setEnabled(false);
                mThemeSwitch.setSummary(R.string.dark_ui_warning);
            }
        };
        mDarkModeObserver.subscribe(mCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDarkModeObserver.subscribe(mCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDarkModeObserver.unsubscribe();
    }

    private boolean isBrowseThemesAvailable() {
        PackageManager pm = getPackageManager();
        Intent browse = new Intent();
        browse.setClassName("com.android.customization", "com.android.customization.picker.CustomizationPickerActivity");
        return pm.resolveActivity(browse, 0) != null;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, Fragment fragment) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.accent_color"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.primary_color"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.font"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.adaptive_icon_shape"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.icon_pack.android"));
        controllers.add(new QsTileStylePreferenceController(context));
        controllers.add(new SystemThemePreferenceController(context));
	controllers.add(new OverlayCategoryPreferenceController(context,
		"android.theme.customization.statusbar_height"));
	controllers.add(new OverlayCategoryPreferenceController(context,
		"android.theme.customization.ui_radius"));
        return controllers;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAccentColor) {
        final ContentResolver resolver = getActivity().getContentResolver();
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ff1a73e8")) {
                mAccentColor.setSummary(R.string.default_string);
            } else {
                mAccentColor.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putIntForUser(resolver,
                    Settings.System.ACCENT_COLOR, intHex, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mGradientColor) {
        final ContentResolver resolver = getActivity().getContentResolver();
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ff1a73e8")) {
                mGradientColor.setSummary(R.string.default_string);
            } else {
                mGradientColor.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putIntForUser(resolver,
                    Settings.System.GRADIENT_COLOR, intHex, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mAccentPreset) {
        final ContentResolver resolver = getActivity().getContentResolver();
            String value = (String) newValue;
            List<String> colorPresets = Arrays.asList(
                    getResources().getStringArray(R.array.accent_presets_values));
            mAccentIndex = mAccentPreset.findIndexOfValue(value);
            int color = DeviceUtils.convertToColorInt(colorPresets.get(mAccentIndex));
            Settings.System.putIntForUser(resolver,
                    Settings.System.ACCENT_COLOR, color, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQsHeaderStyle) {
            String value = (String) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
			    Settings.System.QS_HEADER_STYLE, Integer.valueOf(value));
            int newIndex = mQsHeaderStyle.findIndexOfValue(value);
            mQsHeaderStyle.setSummary(mQsHeaderStyle.getEntries()[newIndex]);
            return true;
        }
        return false;
    }

    private void checkColorPreset(String colorValue) {
        List<String> colorPresets = Arrays.asList(
                getResources().getStringArray(R.array.accent_presets_values));
        if (colorPresets.contains(colorValue)) {
            mAccentPreset.setValue(colorValue);
            int index = mAccentPreset.findIndexOfValue(colorValue);
            mAccentPreset.setSummary(mAccentPreset.getEntries()[index]);
        }
        else {
            mAccentPreset.setSummary(R.string.default_string);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.blissify_themes;
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
