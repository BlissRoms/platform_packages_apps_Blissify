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

package com.blissroms.blissify.fragments.biometrics;

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
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import android.util.Log;
import android.hardware.fingerprint.FingerprintManager;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.blissroms.blissify.utils.DeviceUtils;

@SearchIndexable
public class Biometrics extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, Indexable {

    private static final String KEY_FOD_ICON_PICKER_CATEGORY = "fod_icon_picker_category";
    private static final String KEY_FOD_ICON_PICKER = "fod_icon_picker";
    private static final String KEY_FOD_RECOGNIZING_ANIMATION = "fod_recognizing_animation";
    private static final String KEY_FOD_ANIM = "fod_anim";
    private static final String KEY_FOD_PRESSED_STATE = "fod_pressed_state";

    private PreferenceCategory mFodIconPickerCat;
    private Preference mFodIconPicker;
    private SwitchPreference mFodRecogAnim;
    private ListPreference mFodAnim;
    private ListPreference mFodPressedState;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.blissify_biometrics);
        ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefSet = getPreferenceScreen();
        Context mContext = getContext();

        mFodIconPickerCat = (PreferenceCategory) findPreference(KEY_FOD_ICON_PICKER_CATEGORY);
        mFodIconPicker = (Preference) findPreference(KEY_FOD_ICON_PICKER);
        mFodRecogAnim = (SwitchPreference) findPreference(KEY_FOD_RECOGNIZING_ANIMATION);
        mFodAnim = (ListPreference) findPreference(KEY_FOD_ANIM);
        mFodPressedState = (ListPreference) findPreference(KEY_FOD_PRESSED_STATE);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
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
                    sir.xmlResId = R.xml.blissify_biometrics;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    if (DeviceUtils.hasFod(mContext)) {
                        keys.add(KEY_FOD_ICON_PICKER_CATEGORY);
                        keys.add(KEY_FOD_ICON_PICKER);
                        keys.add(KEY_FOD_RECOGNIZING_ANIMATION);
                        keys.add(KEY_FOD_ANIM);
                        keys.add(KEY_FOD_PRESSED_STATE);
                    }
                    return keys;
                }
    };
}
