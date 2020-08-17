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

package com.blissroms.blissify;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.ContentResolver;
import android.os.Bundle;
import android.view.Surface;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.R;

import com.android.settings.SettingsPreferenceFragment;

public class Blissify extends SettingsPreferenceFragment {

    private static final String KEY_BIOMETRICS_CATEGORY = "biometrics_category";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.blissify);
        PreferenceScreen prefSet = getPreferenceScreen();

        mBiometrics = (BlissPreference) findPreference(KEY_BIOMETRICS_CATEGORY);

        PackageManager packageManager = mContext.getPackageManager();
        boolean hasFod = packageManager.hasSystemFeature(LineageContextConstants.Features.FOD);

        if (!hasFod) {
            prefSet.removePreference(mBiometrics);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
    }

    public static void lockCurrentOrientation(Activity activity) {
        int currentRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int orientation = activity.getResources().getConfiguration().orientation;
        int frozenRotation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        switch (currentRotation) {
            case Surface.ROTATION_0:
                frozenRotation = orientation == Configuration.ORIENTATION_LANDSCAPE
                        ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
            case Surface.ROTATION_90:
                frozenRotation = orientation == Configuration.ORIENTATION_PORTRAIT
                        ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                        : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            case Surface.ROTATION_180:
                frozenRotation = orientation == Configuration.ORIENTATION_LANDSCAPE
                        ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        : ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                break;
            case Surface.ROTATION_270:
                frozenRotation = orientation == Configuration.ORIENTATION_PORTRAIT
                        ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        : ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                break;
        }
        activity.setRequestedOrientation(frozenRotation);
    }
}
