package com.blissroms.blissify.fragments.lockscreen;

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
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import android.provider.Settings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.bliss.support.colorpicker.ColorPickerPreference;
import com.bliss.support.preferences.SystemSettingSwitchPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

public class LockUI extends SettingsPreferenceFragment
                                         implements Preference.OnPreferenceChangeListener{


    private static final String LOCK_SCREEN_VISUALIZER_CUSTOM_COLOR = "lock_screen_visualizer_custom_color";
    private static final String LOCK_CLOCK_FONTS = "lock_clock_fonts";

    private ColorPickerPreference mVisualizerColor;
    ListPreference mLockClockFonts;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.lockscreen_ui);
            PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        // Visualizer custom color
        mVisualizerColor = (ColorPickerPreference) findPreference(LOCK_SCREEN_VISUALIZER_CUSTOM_COLOR);
        int visColor = Settings.System.getInt(resolver,
                Settings.System.LOCK_SCREEN_VISUALIZER_CUSTOM_COLOR, 0xff1976D2);
        String visColorHex = String.format("#%08x", (0xff1976D2 & visColor));
        mVisualizerColor.setSummary(visColorHex);
        mVisualizerColor.setNewPreviewColor(visColor);
        mVisualizerColor.setAlphaSliderEnabled(true);
        mVisualizerColor.setOnPreferenceChangeListener(this);

        // Lockscren Clock Fonts
        mLockClockFonts = (ListPreference) findPreference(LOCK_CLOCK_FONTS);
        mLockClockFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_CLOCK_FONTS, 0)));
        mLockClockFonts.setSummary(mLockClockFonts.getEntry());
        mLockClockFonts.setOnPreferenceChangeListener(this);
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
         ContentResolver resolver = getActivity().getContentResolver();
         if (preference == mVisualizerColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.LOCK_SCREEN_VISUALIZER_CUSTOM_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mLockClockFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_CLOCK_FONTS,
                    Integer.valueOf((String) newValue));
            mLockClockFonts.setValue(String.valueOf(newValue));
            mLockClockFonts.setSummary(mLockClockFonts.getEntry());
            return true;
            }
         return false;
        }

       @Override
         public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
       }
}
