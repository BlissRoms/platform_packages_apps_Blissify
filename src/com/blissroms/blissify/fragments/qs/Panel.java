package com.blissroms.blissify.fragments.qs;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.bliss.support.preferences.SystemSettingSwitchPreference;
import com.bliss.support.preferences.SystemSettingSeekBarPreference;
import android.provider.Settings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.bliss.support.colorpicker.ColorPickerPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.blissroms.blissify.preference.SystemSettingSwitchPreference;
import com.android.internal.logging.nano.MetricsProto;

public class Panel extends SettingsPreferenceFragment 
                                         implements Preference.OnPreferenceChangeListener{

        private static final String QS_PANEL_ALPHA = "qs_panel_alpha";
        private static final String QUICK_PULLDOWN = "quick_pulldown";
        private static final String PREF_SMART_PULLDOWN = "smart_pulldown";
        private static final String QS_TILE_STYLE = "qs_tile_style";

        private ListPreference mQsTileStyle;
        private SystemSettingSeekBarPreference mQsPanelAlpha;
        private ListPreference mQuickPulldown;
        private ListPreference mSmartPulldown;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.qs_panel);
            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mQsPanelAlpha = (SystemSettingSeekBarPreference) findPreference(QS_PANEL_ALPHA);
            int qsPanelAlpha = Settings.System.getInt(resolver,
                    Settings.System.OMNI_QS_PANEL_BG_ALPHA, 221);
            mQsPanelAlpha.setValue((int)(((double) qsPanelAlpha / 255) * 100));
            mQsPanelAlpha.setOnPreferenceChangeListener(this);

            // quick pulldown
            mQuickPulldown = (ListPreference) findPreference(QUICK_PULLDOWN);
            mQuickPulldown.setOnPreferenceChangeListener(this);
            int quickPulldownValue = Settings.System.getIntForUser(resolver,
                    Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 0, UserHandle.USER_CURRENT);
            mQuickPulldown.setValue(String.valueOf(quickPulldownValue));
            updatePulldownSummary(quickPulldownValue);

            mSmartPulldown = (ListPreference) findPreference(PREF_SMART_PULLDOWN);
            mSmartPulldown.setOnPreferenceChangeListener(this);
            int smartPulldown = Settings.System.getInt(resolver,
                   Settings.System.QS_SMART_PULLDOWN, 0);
            mSmartPulldown.setValue(String.valueOf(smartPulldown));
            updateSmartPulldownSummary(smartPulldown);

            mQsTileStyle = (ListPreference) findPreference(QS_TILE_STYLE);
            int qsTileStyle = Settings.System.getInt(resolver,
                    Settings.System.QS_TILE_STYLE, 0);
            int valueIndex = mQsTileStyle.findIndexOfValue(String.valueOf(qsTileStyle));
            mQsTileStyle.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
            mQsTileStyle.setSummary(mQsTileStyle.getEntry());
            mQsTileStyle.setOnPreferenceChangeListener(this);
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mQsPanelAlpha) {
                int bgAlpha = (Integer) newValue;
                int trueValue = (int) (((double) bgAlpha / 100) * 255);
                Settings.System.putInt(resolver,
                        Settings.System.OMNI_QS_PANEL_BG_ALPHA, trueValue);
                return true;
            } else if (preference == mQuickPulldown) {
                int quickPulldownValue = Integer.valueOf((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN,
                        quickPulldownValue, UserHandle.USER_CURRENT);
                updatePulldownSummary(quickPulldownValue);
                return true;
            } else if (preference == mSmartPulldown) {
                int smartPulldown = Integer.valueOf((String) newValue);
                Settings.System.putInt(resolver, Settings.System.QS_SMART_PULLDOWN, smartPulldown);
                updateSmartPulldownSummary(smartPulldown);
                return true;
            } else if (preference == mQsTileStyle) {
                String value = (String) newValue;
                Settings.System.putInt(resolver, Settings.System.QS_TILE_STYLE, Integer.valueOf(value));
                int valueIndex = mQsTileStyle.findIndexOfValue(value);
                mQsTileStyle.setSummary(mQsTileStyle.getEntries()[valueIndex]);
                return true;
            }
        return false;
        }

        private void updatePulldownSummary(int value) {
            Resources res = getResources();
             if (value == 0) {
                // quick pulldown deactivated
                mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_off));
            } else if (value == 3) {
                // quick pulldown always
                mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_summary_always));
            } else {
                String direction = res.getString(value == 2
                        ? R.string.quick_pulldown_left
                        : R.string.quick_pulldown_right);
                mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_summary, direction));
            }
        }

        private void updateSmartPulldownSummary(int value) {
            Resources res = getResources();
             if (value == 0) {
                // Smart pulldown deactivated
                mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_off));
            } else if (value == 3) {
                mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_none_summary));
            } else {
                String type = res.getString(value == 1
                        ? R.string.smart_pulldown_dismissable
                        : R.string.smart_pulldown_ongoing);
                mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_summary, type));
            }
        }

        @Override
        public int getMetricsCategory() {
            return MetricsProto.MetricsEvent.BLISSIFY;
        }
}
