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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.blissroms.blissify.preference.SystemSettingSwitchPreference;
import com.blissroms.blissify.preference.SystemSettingSeekBarPreference;
import android.provider.Settings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.blissroms.blissify.R;

public class Panel extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.default_view,container,false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.default_view, new Panel.SystemPreference())
                .commit();
        return view;
    }

    public static class SystemPreference extends PreferenceFragmentCompat
                                         implements Preference.OnPreferenceChangeListener{

        public SystemPreference() {
        }

        private static final String QS_PANEL_ALPHA = "qs_panel_alpha";

        private SystemSettingSeekBarPreference mQsPanelAlpha;


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.qs_panel);
            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mQsPanelAlpha = (SystemSettingSeekBarPreference) findPreference(QS_PANEL_ALPHA);
            int qsPanelAlpha = Settings.System.getInt(resolver,
                    Settings.System.OMNI_QS_PANEL_BG_ALPHA, 221);
            mQsPanelAlpha.setValue((int)(((double) qsPanelAlpha / 255) * 100));
            mQsPanelAlpha.setOnPreferenceChangeListener(this);
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mQsPanelAlpha) {
                int bgAlpha = (Integer) newValue;
                int trueValue = (int) (((double) bgAlpha / 100) * 255);
                Settings.System.putInt(resolver,
                        Settings.System.OMNI_QS_PANEL_BG_ALPHA, trueValue);
                return true;
             }
        return false;
        }
     }
}
