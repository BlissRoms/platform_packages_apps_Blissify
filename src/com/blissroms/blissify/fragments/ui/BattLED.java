package com.blissroms.blissify.fragments.ui;

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
import android.support.v7.preference.PreferenceCategory;
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
import android.provider.Settings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.bliss.support.colorpicker.ColorPickerPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

public class BattLED extends SettingsPreferenceFragment
                                         implements Preference.OnPreferenceChangeListener{

    private ColorPickerPreference mLowColor;
    private ColorPickerPreference mMediumColor;
    private ColorPickerPreference mFullColor;
    private ColorPickerPreference mReallyFullColor;
    private ColorPickerPreference mFastLightColor;
    private SystemSettingSwitchPreference mLowBatteryBlinking;
    private SystemSettingSwitchPreference mFastColorLight;

    private PreferenceCategory mColorCategory;
    private PreferenceCategory mFastColorCategory;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.interface_batteryled);
            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mColorCategory = (PreferenceCategory) findPreference("battery_light_cat");
            mFastColorCategory = (PreferenceCategory) findPreference("fast_color_cat");

            mLowBatteryBlinking = (SystemSettingSwitchPreference)prefSet.findPreference("battery_light_pulse");
            if (getResources().getBoolean(
                            com.android.internal.R.bool.config_intrusiveBatteryLed)) {
                mLowBatteryBlinking.setChecked(Settings.System.getIntForUser(resolver,
                                Settings.System.OMNI_BATTERY_LIGHT_LOW_BLINKING, 0, UserHandle.USER_CURRENT) == 1);
                mLowBatteryBlinking.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mLowBatteryBlinking);
          }

            if (getResources().getBoolean(com.android.internal.R.bool.config_multiColorBatteryLed)) {
                int color = Settings.System.getIntForUser(resolver,
                        Settings.System.OMNI_BATTERY_LIGHT_LOW_COLOR, 0xFFFF0000,
                                UserHandle.USER_CURRENT);
                mLowColor = (ColorPickerPreference) findPreference("low_color");
                mLowColor.setAlphaSliderEnabled(false);
                mLowColor.setNewPreviewColor(color);
                mLowColor.setOnPreferenceChangeListener(this);

                color = Settings.System.getIntForUser(resolver,
                        Settings.System.OMNI_BATTERY_LIGHT_MEDIUM_COLOR, 0xFFFFFF00,
                                UserHandle.USER_CURRENT);
                mMediumColor = (ColorPickerPreference) findPreference("medium_color");
                mMediumColor.setAlphaSliderEnabled(false);
                mMediumColor.setNewPreviewColor(color);
                mMediumColor.setOnPreferenceChangeListener(this);

                color = Settings.System.getIntForUser(resolver,
                        Settings.System.OMNI_BATTERY_LIGHT_FULL_COLOR, 0xFFFFFF00,
                                UserHandle.USER_CURRENT);
                mFullColor = (ColorPickerPreference) findPreference("full_color");
                mFullColor.setAlphaSliderEnabled(false);
                mFullColor.setNewPreviewColor(color);
                mFullColor.setOnPreferenceChangeListener(this);

                color = Settings.System.getIntForUser(resolver,
                        Settings.System.OMNI_BATTERY_LIGHT_REALLY_FULL_COLOR, 0xFF00FF00,
                                UserHandle.USER_CURRENT);
                mReallyFullColor = (ColorPickerPreference) findPreference("really_full_color");
                mReallyFullColor.setAlphaSliderEnabled(false);
                mReallyFullColor.setNewPreviewColor(color);
                mReallyFullColor.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mColorCategory);
            }
            mFastColorLight = (SystemSettingSwitchPreference)prefSet.findPreference("fast_charging_led_enabled");
            if (getResources().getBoolean(com.android.internal.R.bool.config_FastChargingLedSupported)) {

                mFastColorLight.setChecked(Settings.System.getIntForUser(resolver,
                                Settings.System.OMNI_FAST_CHARGING_LED_ENABLED, 0, UserHandle.USER_CURRENT) == 1);
                mFastColorLight.setOnPreferenceChangeListener(this);

                int color = Settings.System.getIntForUser(resolver,
                        Settings.System.OMNI_FAST_BATTERY_LIGHT_COLOR, 0xFFFF0000,
                                UserHandle.USER_CURRENT);
                mFastLightColor = (ColorPickerPreference) findPreference("fast_color");
                mFastLightColor.setAlphaSliderEnabled(false);
                mFastLightColor.setNewPreviewColor(color);
                mFastLightColor.setOnPreferenceChangeListener(this);

            } else {
                prefSet.removePreference(mFastColorCategory);
            }
        }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
         ContentResolver resolver = getActivity().getContentResolver();
        if (preference.equals(mLowColor)) {
            int color = ((Integer) newValue).intValue();
            Settings.System.putIntForUser(resolver,
                    Settings.System.OMNI_BATTERY_LIGHT_LOW_COLOR, color,
                    UserHandle.USER_CURRENT);
            return true;
        } else if (preference.equals(mMediumColor)) {
            int color = ((Integer) newValue).intValue();
            Settings.System.putIntForUser(resolver,
                    Settings.System.OMNI_BATTERY_LIGHT_MEDIUM_COLOR, color,
                    UserHandle.USER_CURRENT);
            return true;
        } else if (preference.equals(mFullColor)) {
            int color = ((Integer) newValue).intValue();
            Settings.System.putIntForUser(resolver,
                    Settings.System.OMNI_BATTERY_LIGHT_FULL_COLOR, color,
                    UserHandle.USER_CURRENT);
            return true;
        } else if (preference.equals(mReallyFullColor)) {
            int color = ((Integer) newValue).intValue();
            Settings.System.putIntForUser(resolver,
                    Settings.System.OMNI_BATTERY_LIGHT_REALLY_FULL_COLOR, color,
                    UserHandle.USER_CURRENT);
            return true;
        } else if (preference.equals(mFastLightColor)) {
            int color = ((Integer) newValue).intValue();
            Settings.System.putIntForUser(resolver,
                    Settings.System.OMNI_FAST_BATTERY_LIGHT_COLOR, color,
                    UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mLowBatteryBlinking) {
            boolean value = (Boolean) newValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.OMNI_BATTERY_LIGHT_LOW_BLINKING, value ? 1 : 0,
                    UserHandle.USER_CURRENT);
            mLowBatteryBlinking.setChecked(value);
            return true;
        } else if (preference == mFastColorLight) {
            boolean value = (Boolean) newValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.OMNI_FAST_CHARGING_LED_ENABLED, value ? 1 : 0,
                    UserHandle.USER_CURRENT);
            mFastColorLight.setChecked(value);
            return true;
        } else if (preference == mLowBatteryBlinking) {
            boolean value = (Boolean) newValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.OMNI_BATTERY_LIGHT_LOW_BLINKING, value ? 1 : 0,
                    UserHandle.USER_CURRENT);
            mLowBatteryBlinking.setChecked(value);
            return true;
        }
        return false;
        }

       @Override
         public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
       }
}
