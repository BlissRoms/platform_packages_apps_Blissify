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

package com.blissroms.blissify.fragments.statusbar;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.ConnectivityManager;
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
import android.util.Log;
import android.hardware.fingerprint.FingerprintManager;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class Breathing extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String SMS_BREATH = "sms_breath";
    private static final String MISSED_CALL_BREATH = "missed_call_breath";
    private static final String VOICEMAIL_BREATH = "voicemail_breath";

    private SwitchPreference mSmsBreath;
    private SwitchPreference mMissedCallBreath;
    private SwitchPreference mVoicemailBreath;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.statusbar_breathing);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

           // Breathing Notifications
           mSmsBreath = (SwitchPreference) findPreference(SMS_BREATH);
           mMissedCallBreath = (SwitchPreference) findPreference(MISSED_CALL_BREATH);
           mVoicemailBreath = (SwitchPreference) findPreference(VOICEMAIL_BREATH);

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
           }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
	if (preference == mSmsBreath) {
            boolean value = (Boolean) newValue;
            Settings.Global.putInt(getContentResolver(), SMS_BREATH, value ? 1 : 0);
            return true;
        } else if (preference == mMissedCallBreath) {
            boolean value = (Boolean) newValue;
            Settings.Global.putInt(getContentResolver(), MISSED_CALL_BREATH, value ? 1 : 0);
            return true;
        } else if (preference == mVoicemailBreath) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(), VOICEMAIL_BREATH, value ? 1 : 0);
            return true;
	}
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
    }

}
