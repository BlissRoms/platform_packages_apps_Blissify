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

package com.blissroms.blissify.quicksettings.tabs;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceCategory; 

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.bliss.CustomSeekBarPreference;

public class QSAdvanced extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "QSAdvanced";
    private static final String PREF_ROWS_PORTRAIT = "qs_rows_portrait";
    private static final String PREF_ROWS_LANDSCAPE = "qs_rows_landscape";
    private static final String PREF_COLUMNS = "qs_layout_columns";
    private static final String PREF_SYSUI_QQS_COUNT = "sysui_qqs_count_key";

    private ListPreference mRowsPortrait;
    private ListPreference mRowsLandscape;
    private CustomSeekBarPreference mQsColumns;
    private ListPreference mSysuiQqsCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.qs_advanced);

        final ContentResolver resolver = getActivity().getContentResolver();

        int defaultValue;

        mRowsPortrait = (ListPreference) findPreference(PREF_ROWS_PORTRAIT);
        int rowsPortrait = Settings.Secure.getInt(resolver,
                Settings.Secure.QS_ROWS_PORTRAIT, 3);
        mRowsPortrait.setValue(String.valueOf(rowsPortrait));
        mRowsPortrait.setSummary(mRowsPortrait.getEntry());
        mRowsPortrait.setOnPreferenceChangeListener(this);

        defaultValue = getResources().getInteger(com.android.internal.R.integer.config_qs_num_rows_landscape_default);
        mRowsLandscape = (ListPreference) findPreference(PREF_ROWS_LANDSCAPE);
        int rowsLandscape = Settings.Secure.getInt(resolver,
                Settings.Secure.QS_ROWS_LANDSCAPE, defaultValue);
        mRowsLandscape.setValue(String.valueOf(rowsLandscape));
        mRowsLandscape.setSummary(mRowsLandscape.getEntry());
        mRowsLandscape.setOnPreferenceChangeListener(this);

        mQsColumns = (CustomSeekBarPreference) findPreference(PREF_COLUMNS);
        int columnsQs = Settings.System.getInt(resolver,
                Settings.System.QS_LAYOUT_COLUMNS, 3);
        mQsColumns.setValue(columnsQs / 1);
        mQsColumns.setOnPreferenceChangeListener(this);

        mSysuiQqsCount = (ListPreference) findPreference(PREF_SYSUI_QQS_COUNT);
        int SysuiQqsCount = Settings.Secure.getInt(resolver,
               Settings.Secure.QQS_COUNT, 5);
        mSysuiQqsCount.setValue(Integer.toString(SysuiQqsCount));
        mSysuiQqsCount.setSummary(mSysuiQqsCount.getEntry());
        mSysuiQqsCount.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        int intValue;
        int index;

            if (preference == mRowsPortrait) {
                intValue = Integer.valueOf((String) newValue);
                index = mRowsPortrait.findIndexOfValue((String) newValue);
                Settings.Secure.putInt(resolver,
                        Settings.Secure.QS_ROWS_PORTRAIT, intValue);
                preference.setSummary(mRowsPortrait.getEntries()[index]);
                return true;
            } else if (preference == mRowsLandscape) {
                intValue = Integer.valueOf((String) newValue);
                index = mRowsLandscape.findIndexOfValue((String) newValue);
                Settings.Secure.putInt(resolver,
                        Settings.Secure.QS_ROWS_LANDSCAPE, intValue);
                preference.setSummary(mRowsLandscape.getEntries()[index]);
                return true;
            } else if (preference == mQsColumns) {
                int qsColumns = (Integer) newValue;
                Settings.System.putInt(resolver, Settings.System.QS_LAYOUT_COLUMNS, qsColumns * 1);
                return true;
            } else if (preference == mSysuiQqsCount) {
                String SysuiQqsCount = (String) newValue;
                int SysuiQqsCountValue = Integer.parseInt(SysuiQqsCount);
                Settings.Secure.putInt(resolver,
                        Settings.Secure.QQS_COUNT, SysuiQqsCountValue);
                int SysuiQqsCountIndex = mSysuiQqsCount.findIndexOfValue(SysuiQqsCount);
                mSysuiQqsCount.setSummary(mSysuiQqsCount.getEntries()[SysuiQqsCountIndex]);
            return true;
            }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.BLISSIFY;
    }
}
