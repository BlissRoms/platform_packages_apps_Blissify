/*
 * Copyright (C) 2014-2022 The BlissRoms Project
 * Copyright (C) 2024 ApolloOS
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

package org.blissroms.blissify.fragments.lockscreen;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.UserHandle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.ListPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import com.bliss.support.preferences.SystemSettingSwitchPreference;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.bliss.BlissUtils;
import org.blissroms.blissify.fragments.lockscreen.UdfpsAnimation;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.io.FileDescriptor;
import java.util.Arrays;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class Udfps extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String SCREEN_OFF_UDFPS_ENABLED = "screen_off_udfps_enabled";

    private Preference mScreenOffUdfps;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.blissify_udfps);

        final PreferenceScreen prefSet = getPreferenceScreen();
        Resources resources = getResources();

        mScreenOffUdfps = (Preference) prefSet.findPreference(SCREEN_OFF_UDFPS_ENABLED);
        boolean screenOffUdfpsAvailable = resources.getBoolean(
                com.android.internal.R.bool.config_supportScreenOffUdfps) ||
                !TextUtils.isEmpty(resources.getString(
                    com.android.internal.R.string.config_dozeUdfpsLongPressSensorType));
        if (!screenOffUdfpsAvailable)
            prefSet.removePreference(mScreenOffUdfps);
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.SCREEN_OFF_UDFPS_ENABLED, 0, UserHandle.USER_CURRENT);
        UdfpsAnimation.reset(mContext);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.blissify_udfps);
}
