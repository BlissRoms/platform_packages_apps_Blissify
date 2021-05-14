/*
 * Copyright (C) 2014-2021 BlissRoms Project
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

import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class FODIconPickerFragment extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.blissify_fod_icon_picker);

        getActivity().getActionBar().setTitle(R.string.fod_icon_picker_title);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.BLISSIFY;
    }
}
