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

package com.blissroms.blissify.fragments.notifications;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.provider.Settings;
import com.android.settings.R;
import android.net.ConnectivityManager;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import com.bliss.support.preferences.CustomSeekBarPreference;
import com.bliss.support.preferences.SystemSettingSwitchPreference;
import com.bliss.support.preferences.SystemSettingSeekBarPreference;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.Utils;
import com.android.internal.util.bliss.BlissUtils;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.blissroms.blissify.fragments.notifications.AmbientLightSettingsPreview;
import com.bliss.support.colorpicker.ColorPickerPreference;
import com.bliss.support.preferences.CustomSeekBarPreference;
import com.bliss.support.preferences.SystemSettingListPreference;
import com.bliss.support.preferences.GlobalSettingMasterSwitchPreference;

public class Notifications extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String HEADS_UP_NOTIFICATIONS_ENABLED = "heads_up_notifications_enabled";
    private static final String SMS_BREATH = "sms_breath";
    private static final String MISSED_CALL_BREATH = "missed_call_breath";
    private static final String VOICEMAIL_BREATH = "voicemail_breath";
    private static final String PULSE_AMBIENT_LIGHT_COLOR = "pulse_ambient_light_color";
    private static final String PULSE_AMBIENT_LIGHT_DURATION = "pulse_ambient_light_duration";
    private static final String NOTIFICATION_PULSE_ACCENT = "ambient_notification_light_accent";
    private static final String NOTIFICATION_PULSE = "pulse_ambient_light";
    private static final String AOD_NOTIFICATION_PULSE_TIMEOUT = "ambient_notification_light_timeout";
    private static final String FLASHLIGHT_ON_CALL = "flashlight_on_call";
    private static final String VIBRATE_ON_CONNECT = "vibrate_on_connect";
    private static final String VIBRATE_ON_CALLWAITING = "vibrate_on_callwaiting";
    private static final String VIBRATE_ON_DISCONNECT = "vibrate_on_disconnect";

    private SwitchPreference mSmsBreath;
    private SwitchPreference mMissedCallBreath;
    private SwitchPreference mVoicemailBreath;
    private SwitchPreference mVibOnConnect;
    private SwitchPreference mVibOnWait;
    private SwitchPreference mVibOnDisconnect;
    private SwitchPreference mEdgeLightPreference;
    private SwitchPreference mEdgeLightAccentColorPreference;
    private GlobalSettingMasterSwitchPreference mHeadsUpEnabled;
    private ColorPickerPreference mEdgeLightColorPreference;
    private SystemSettingSeekBarPreference mEdgeLightDurationPreference;
    private SystemSettingListPreference mPulseTimeout;
    private ListPreference mFlashlightOnCall;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.blissify_notifications);
        PreferenceScreen prefSet = getPreferenceScreen();

        final ContentResolver resolver = getActivity().getContentResolver();

        mHeadsUpEnabled = (GlobalSettingMasterSwitchPreference) findPreference(HEADS_UP_NOTIFICATIONS_ENABLED);
        mHeadsUpEnabled.setOnPreferenceChangeListener(this);
        int headsUpEnabled = Settings.Global.getInt(getContentResolver(),
                HEADS_UP_NOTIFICATIONS_ENABLED, 1);
        mHeadsUpEnabled.setChecked(headsUpEnabled != 0);

        // Breathing Notifications
        mSmsBreath = (SwitchPreference) findPreference(SMS_BREATH);
        mMissedCallBreath = (SwitchPreference) findPreference(MISSED_CALL_BREATH);
        mVoicemailBreath = (SwitchPreference) findPreference(VOICEMAIL_BREATH);

        // In-Call Vibration options
        mVibOnConnect = (SwitchPreference) findPreference(VIBRATE_ON_CONNECT);
        mVibOnWait = (SwitchPreference) findPreference(VIBRATE_ON_CALLWAITING);
        mVibOnDisconnect = (SwitchPreference) findPreference(VIBRATE_ON_DISCONNECT);

        ConnectivityManager cm = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)) {
            mSmsBreath.setChecked(Settings.Global.getInt(resolver,
                    Settings.Global.KEY_SMS_BREATH, 0) == 1);
            mSmsBreath.setOnPreferenceChangeListener(this);

            mMissedCallBreath.setChecked(Settings.Global.getInt(resolver,
                    Settings.Global.KEY_MISSED_CALL_BREATH, 0) == 1);
            mMissedCallBreath.setOnPreferenceChangeListener(this);

            mVoicemailBreath.setChecked(Settings.System.getInt(resolver,
                    Settings.System.KEY_VOICEMAIL_BREATH, 0) == 1);
            mVoicemailBreath.setOnPreferenceChangeListener(this);
        } else {
            prefSet.removePreference(mSmsBreath);
            prefSet.removePreference(mMissedCallBreath);
            prefSet.removePreference(mVoicemailBreath);
            prefSet.removePreference(mVibOnConnect);
            prefSet.removePreference(mVibOnWait);
            prefSet.removePreference(mVoicemailBreath);
        }

        mFlashlightOnCall = (ListPreference) findPreference(FLASHLIGHT_ON_CALL);
        Preference FlashOnCall = findPreference("flashlight_on_call");
        int flashlightValue = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.FLASHLIGHT_ON_CALL, 0, UserHandle.USER_CURRENT);
        mFlashlightOnCall.setValue(String.valueOf(flashlightValue));
        mFlashlightOnCall.setOnPreferenceChangeListener(this);

        if (!BlissUtils.deviceSupportsFlashLight(getActivity())) {
            prefSet.removePreference(FlashOnCall);
        }

        mEdgeLightPreference = (SwitchPreference) findPreference(NOTIFICATION_PULSE);
        boolean mEdgeLightOn = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE, 0) == 1;
        mEdgeLightPreference.setChecked(mEdgeLightOn);
        mEdgeLightPreference.setOnPreferenceChangeListener(this);

        mEdgeLightColorPreference = (ColorPickerPreference) findPreference(PULSE_AMBIENT_LIGHT_COLOR);
        int edgeLightColor = Settings.System.getInt(getContentResolver(),
                Settings.System.PULSE_AMBIENT_LIGHT_COLOR, 0xFF3980FF);
        mEdgeLightColorPreference.setNewPreviewColor(edgeLightColor);

        mPulseTimeout = (SystemSettingListPreference) findPreference(AOD_NOTIFICATION_PULSE_TIMEOUT);
        int value = Settings.System.getInt(getContentResolver(),
                Settings.System.AOD_NOTIFICATION_PULSE_TIMEOUT, 0);

        mPulseTimeout.setValue(Integer.toString(value));
        mPulseTimeout.setSummary(mPulseTimeout.getEntry());
        mPulseTimeout.setOnPreferenceChangeListener(this);

        mEdgeLightAccentColorPreference = (SwitchPreference) findPreference(NOTIFICATION_PULSE_ACCENT);
        boolean mEdgeLightAccentOn = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_ACCENT, 0) == 1;
        mEdgeLightAccentColorPreference.setChecked(mEdgeLightAccentOn);
        mEdgeLightAccentColorPreference.setOnPreferenceChangeListener(this);

        // Update the edge light preference and preview accordingly
        updateEdgeLightColorPreferences(mEdgeLightAccentOn);

        mEdgeLightColorPreference.setAlphaSliderEnabled(false);
        String edgeLightColorHex = ColorPickerPreference.convertToRGB(edgeLightColor);
        if (edgeLightColorHex.equals("#3980ff")) {
            mEdgeLightColorPreference.setSummary(R.string.default_string);
        } else {
            mEdgeLightColorPreference.setSummary(edgeLightColorHex);
        }
        mEdgeLightColorPreference.setOnPreferenceChangeListener(this);

        mEdgeLightDurationPreference = (SystemSettingSeekBarPreference) findPreference(PULSE_AMBIENT_LIGHT_DURATION);
        mEdgeLightDurationPreference.setOnPreferenceChangeListener(this);
        int duration = Settings.System.getInt(getContentResolver(),
                Settings.System.PULSE_AMBIENT_LIGHT_DURATION, 2);
        mEdgeLightDurationPreference.setValue(duration);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mHeadsUpEnabled) {
            boolean value = (Boolean) newValue;
            Settings.Global.putInt(resolver,
		            HEADS_UP_NOTIFICATIONS_ENABLED, value ? 1 : 0);
            return true;
        } else if (preference == mSmsBreath) {
                boolean value = (Boolean) newValue;
                Settings.Global.putInt(resolver, SMS_BREATH, value ? 1 : 0);
                return true;
        } else if (preference == mMissedCallBreath) {
                boolean value = (Boolean) newValue;
                Settings.Global.putInt(resolver, MISSED_CALL_BREATH, value ? 1 : 0);
                return true;
        } else if (preference == mVoicemailBreath) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver, VOICEMAIL_BREATH, value ? 1 : 0);
                return true;
        } else if (preference == mEdgeLightPreference) {
            boolean isOn = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.NOTIFICATION_PULSE, isOn ? 1 : 0);
                return true;
        } else if (preference == mEdgeLightAccentColorPreference) {
            boolean isOn = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.NOTIFICATION_PULSE_ACCENT, isOn ? 1 : 0);
            mEdgeLightAccentColorPreference.setChecked(isOn);
            updateEdgeLightColorPreferences(isOn);
            return true;
        } else if (preference == mEdgeLightColorPreference) {
            String hex = ColorPickerPreference.convertToRGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#3980ff")) {
                preference.setSummary(R.string.default_string);
            } else {
                preference.setSummary(hex);
            }
            AmbientLightSettingsPreview.setAmbientLightPreviewColor(Integer.valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.PULSE_AMBIENT_LIGHT_COLOR, intHex);
            return true;
        } else if (preference == mEdgeLightDurationPreference) {
            int value = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PULSE_AMBIENT_LIGHT_DURATION, value);
            return true;
        } else if (preference == mPulseTimeout) {
            int value = Integer.valueOf((String) newValue);
            int index = mPulseTimeout.findIndexOfValue((String) newValue);
            mPulseTimeout.setSummary(mPulseTimeout.getEntries()[index]);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.AOD_NOTIFICATION_PULSE_TIMEOUT, value);
            return true;
        } else if (preference == mFlashlightOnCall) {
            int flashlightValue = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(resolver,
                  Settings.System.FLASHLIGHT_ON_CALL, flashlightValue, UserHandle.USER_CURRENT);
            mFlashlightOnCall.setValue(String.valueOf(flashlightValue));
            return true;
        }
        return false;
    }

    private void updateEdgeLightColorPreferences(boolean useAccentColor) {
        mEdgeLightColorPreference.setEnabled(!useAccentColor);
        if (useAccentColor) {
            AmbientLightSettingsPreview.setAmbientLightPreviewColor(Utils.getColorAccentDefaultColor(getContext()));
        } else {
            int edgeLightColor = Settings.System.getInt(getContentResolver(),
                    Settings.System.PULSE_AMBIENT_LIGHT_COLOR, 0xFF3980FF);
            AmbientLightSettingsPreview.setAmbientLightPreviewColor(edgeLightColor);
        }
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();

        Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_SHOW_TICKER, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_TICKER_ANIMATION_MODE, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_TICKER_TICK_DURATION, 3000, UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
    }

}
