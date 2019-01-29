package com.blissroms.blissify.fragments.statusbar;

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
import android.support.v14.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.blissroms.blissify.preference.SystemSettingSwitchPreference;
import android.provider.Settings;
import android.view.WindowManagerGlobal;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.android.settings.bliss.preference.ColorPickerPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

public class Battery extends SettingsPreferenceFragment
                                         implements Preference.OnPreferenceChangeListener{

    private static final String PREF_BATT_BAR = "battery_bar_list";
    private static final String PREF_BATT_BAR_STYLE = "battery_bar_style";
    private static final String PREF_BATT_BAR_COLOR = "battery_bar_color";
    private static final String PREF_BATT_BAR_CHARGING_COLOR = "battery_bar_charging_color";
    private static final String PREF_BATT_BAR_LOW_COLOR_WARNING = "battery_bar_battery_low_color_warning";
    private static final String PREF_BATT_BAR_USE_GRADIENT_COLOR = "battery_bar_use_gradient_color";
    private static final String PREF_BATT_BAR_LOW_COLOR = "battery_bar_low_color";
    private static final String PREF_BATT_BAR_HIGH_COLOR = "battery_bar_high_color";
    private static final String PREF_BATT_BAR_WIDTH = "battery_bar_thickness";
    private static final String PREF_BATT_ANIMATE = "battery_bar_animate";

    private ListPreference mBatteryIconStyle;
    private ListPreference mBatteryPercentage;
    private ListPreference mBatteryBar;
    private ListPreference mBatteryBarStyle;
    private ListPreference mBatteryBarThickness;
    private SwitchPreference mBatteryBarChargingAnimation;
    private SwitchPreference mBatteryBarUseGradient;
    private ColorPickerPreference mBatteryBarColor;
    private ColorPickerPreference mBatteryBarChargingColor;
    private ColorPickerPreference mBatteryBarBatteryLowColor;
    private ColorPickerPreference mBatteryBarBatteryLowColorWarn;
    private ColorPickerPreference mBatteryBarBatteryHighColor;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.statusbar_battery);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
	
        // Battery styles
        int batteryStyle = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.STATUS_BAR_BATTERY_STYLE, 0/*portrait*/,
                UserHandle.USER_CURRENT);
        mBatteryIconStyle = (ListPreference) findPreference("status_bar_battery_style");
        mBatteryIconStyle.setValue(Integer.toString(batteryStyle));
        mBatteryIconStyle.setOnPreferenceChangeListener(this);

        int percentage = Settings.System.getIntForUser(resolver,
                Settings.System.SHOW_BATTERY_PERCENT, 1,
                UserHandle.USER_CURRENT);
        mBatteryPercentage = (ListPreference) findPreference("status_bar_show_battery_percent");
        mBatteryPercentage.setValue(Integer.toString(percentage));
        mBatteryPercentage.setOnPreferenceChangeListener(this);
        boolean hideForcePercentage =
                batteryStyle == 5 || batteryStyle == 6; /*text or hidden style*/
        mBatteryPercentage.setEnabled(!hideForcePercentage);

        // battery bar
        mBatteryBar = (ListPreference) findPreference(PREF_BATT_BAR);
        int batteryBarLocation = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_LOCATION, 0);
        mBatteryBar.setValue(String.valueOf(batteryBarLocation));
        mBatteryBar.setSummary(mBatteryBar.getEntry());
        mBatteryBar.setOnPreferenceChangeListener(this);

        mBatteryBarStyle = (ListPreference) findPreference(PREF_BATT_BAR_STYLE);
        int batteryBarStyle = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_STYLE, 0);
        mBatteryBarStyle.setValue(String.valueOf(batteryBarStyle));
        mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntry());
        mBatteryBarStyle.setOnPreferenceChangeListener(this);

        mBatteryBarThickness = (ListPreference) findPreference(PREF_BATT_BAR_WIDTH);
        int batteryBarThickness = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_THICKNESS, 1);
        mBatteryBarThickness.setValue(String.valueOf(batteryBarThickness));
        mBatteryBarThickness.setSummary(mBatteryBarThickness.getEntry());
        mBatteryBarThickness.setOnPreferenceChangeListener(this);

        mBatteryBarColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_COLOR);
        int barColor = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_COLOR, 0xff00a3ff);
        String barColorHex = String.format("#%08x", (0xff00a3ff & barColor));
        mBatteryBarColor.setSummary(barColorHex);
        mBatteryBarColor.setNewPreviewColor(barColor);
        mBatteryBarColor.setAlphaSliderEnabled(true);
        mBatteryBarColor.setOnPreferenceChangeListener(this);

        mBatteryBarChargingColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_CHARGING_COLOR);
        int chargeColor = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_CHARGING_COLOR, 0xff00ff00);
        String chargeColorHex = String.format("#%08x", (0xff00ff00 & chargeColor));
        mBatteryBarChargingColor.setSummary(chargeColorHex);
        mBatteryBarChargingColor.setNewPreviewColor(chargeColor);
        mBatteryBarChargingColor.setAlphaSliderEnabled(true);
        mBatteryBarChargingColor.setOnPreferenceChangeListener(this);

        mBatteryBarBatteryLowColorWarn = (ColorPickerPreference) findPreference(PREF_BATT_BAR_LOW_COLOR_WARNING);
        int warnColor = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_BATTERY_LOW_COLOR_WARNING, 0xffff6600);
        String warnColorHex = String.format("#%08x", (0xffff6600 & warnColor));
        mBatteryBarBatteryLowColorWarn.setSummary(warnColorHex);
        mBatteryBarBatteryLowColorWarn.setNewPreviewColor(warnColor);
        mBatteryBarBatteryLowColorWarn.setAlphaSliderEnabled(true);
        mBatteryBarBatteryLowColorWarn.setOnPreferenceChangeListener(this);

        mBatteryBarBatteryLowColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_LOW_COLOR);
        int lowColor = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_LOW_COLOR, 0xffff0040);
        String lowColorHex = String.format("#%08x", (0xffff0040 & lowColor));
        mBatteryBarBatteryLowColor.setSummary(lowColorHex);
        mBatteryBarBatteryLowColor.setNewPreviewColor(lowColor);
        mBatteryBarBatteryLowColor.setAlphaSliderEnabled(true);
        mBatteryBarBatteryLowColor.setOnPreferenceChangeListener(this);

        mBatteryBarBatteryHighColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_HIGH_COLOR);
        int highColor = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_HIGH_COLOR, 0xff99CC00);
        String highColorHex = String.format("#%08x", (0xff99CC00 & highColor));
        mBatteryBarBatteryHighColor.setSummary(highColorHex);
        mBatteryBarBatteryHighColor.setNewPreviewColor(highColor);
        mBatteryBarBatteryHighColor.setAlphaSliderEnabled(true);
        mBatteryBarBatteryHighColor.setOnPreferenceChangeListener(this);

        mBatteryBarUseGradient = (SwitchPreference) findPreference(PREF_BATT_BAR_USE_GRADIENT_COLOR);
        mBatteryBarUseGradient.setChecked(Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_USE_GRADIENT_COLOR, 0) == 1);
        mBatteryBarUseGradient.setOnPreferenceChangeListener(this);

        mBatteryBarChargingAnimation = (SwitchPreference) findPreference(PREF_BATT_ANIMATE);
        mBatteryBarChargingAnimation.setChecked(Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_ANIMATE, 0) == 1);
        mBatteryBarChargingAnimation.setOnPreferenceChangeListener(this);

        updateBatteryBarOptions();
        }

       @Override
         public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
       }

    private void updateBatteryBarOptions() {
        if (Settings.System.getInt(getActivity().getContentResolver(),
            Settings.System.BATTERY_BAR_LOCATION, 0) == 0) {
            mBatteryBarStyle.setEnabled(false);
            mBatteryBarThickness.setEnabled(false);
            mBatteryBarChargingAnimation.setEnabled(false);
            mBatteryBarColor.setEnabled(false);
            mBatteryBarChargingColor.setEnabled(false);
            mBatteryBarUseGradient.setEnabled(false);
            mBatteryBarBatteryLowColor.setEnabled(false);
            mBatteryBarBatteryHighColor.setEnabled(false);
            mBatteryBarBatteryLowColorWarn.setEnabled(false);
        } else {
            mBatteryBarStyle.setEnabled(true);
            mBatteryBarThickness.setEnabled(true);
            mBatteryBarChargingAnimation.setEnabled(true);
            mBatteryBarColor.setEnabled(true);
            mBatteryBarChargingColor.setEnabled(true);
            mBatteryBarUseGradient.setEnabled(true);
            mBatteryBarBatteryLowColor.setEnabled(true);
            mBatteryBarBatteryHighColor.setEnabled(true);
            mBatteryBarBatteryLowColorWarn.setEnabled(true);
        }
    }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
            AlertDialog dialog;
        if (preference == mBatteryIconStyle) {
            int value = Integer.valueOf((String) newValue);
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.STATUS_BAR_BATTERY_STYLE, value,
                    UserHandle.USER_CURRENT);
            boolean hideForcePercentage = value == 5 || value == 6;/*text or hidden style*/
            mBatteryPercentage.setEnabled(!hideForcePercentage);
            return true;
        } else  if (preference == mBatteryPercentage) {
            int value = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.SHOW_BATTERY_PERCENT, value,
                    UserHandle.USER_CURRENT);
            boolean hideForcePercentage = value == 5 || value == 6;/*text or hidden style*/
            mBatteryPercentage.setEnabled(!hideForcePercentage);
            return true;
        } else if (preference == mBatteryBarColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mBatteryBarChargingColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_CHARGING_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mBatteryBarBatteryLowColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_LOW_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mBatteryBarBatteryLowColorWarn) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_BATTERY_LOW_COLOR_WARNING, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mBatteryBarBatteryHighColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_HIGH_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mBatteryBar) {
            int val = Integer.parseInt((String) newValue);
            int index = mBatteryBar.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_LOCATION, val);
            mBatteryBar.setSummary(mBatteryBar.getEntries()[index]);
            updateBatteryBarOptions();
            return true;
        } else if (preference == mBatteryBarStyle) {
            int val = Integer.parseInt((String) newValue);
            int index = mBatteryBarStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_STYLE, val);
            mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntries()[index]);
            return true;
        } else if (preference == mBatteryBarThickness) {
            int val = Integer.parseInt((String) newValue);
            int index = mBatteryBarThickness.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_THICKNESS, val);
            mBatteryBarThickness.setSummary(mBatteryBarThickness.getEntries()[index]);
            return true;
        } else if (preference == mBatteryBarChargingAnimation) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_ANIMATE, value ? 1 : 0);
            return true;
        } else if (preference == mBatteryBarUseGradient) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_USE_GRADIENT_COLOR, value ? 1 : 0);
            return true;
       }
       return false;
       }
}
