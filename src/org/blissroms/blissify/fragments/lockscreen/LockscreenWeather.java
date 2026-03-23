/*
 * Copyright (C) 2014-2025 The BlissRoms Project
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

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.widget.Toast;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.bliss.OmniJawsClient;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import org.blissroms.blissify.fragments.BlissifyFragment;
import org.blissroms.blissify.utils.SystemUtils;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class LockscreenWeather extends BlissifyFragment
    implements Preference.OnPreferenceChangeListener {

  private static final String KEY_SMARTSPACE = "lockscreen_smartspace_enabled";
  private static final String KEY_OMNIJAWS = "omnijaws_weather";
  private static final String GOOGLE_APP_PACKAGE = "com.google.android.googlequicksearchbox";

  private SwitchPreferenceCompat mSmartspace;
  private Preference mOmniJaws;
  private boolean mHasGoogleApp;
  private boolean mHasOmniJawsService;

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    addPreferencesFromResource(R.xml.blissify_lockscreen_weather);

    mHasGoogleApp = isPackageInstalled(GOOGLE_APP_PACKAGE);
    mHasOmniJawsService = OmniJawsClient.get().isOmniJawsServiceInstalled(getContext());

    mSmartspace = (SwitchPreferenceCompat) findPreference(KEY_SMARTSPACE);
    if (!mHasGoogleApp) {
      mSmartspace.setVisible(false);
      Settings.Secure.putIntForUser(
          getContext().getContentResolver(),
          KEY_SMARTSPACE, 0, UserHandle.USER_CURRENT);
    } else {
      mSmartspace.setOnPreferenceChangeListener(this);
    }

    mOmniJaws = findPreference(KEY_OMNIJAWS);
    if (!mHasOmniJawsService) {
      mOmniJaws.setEnabled(false);
      mOmniJaws.setSummary(R.string.lockscreen_weather_service_missing);
    } else {
      mOmniJaws.setOnPreferenceClickListener(
          pref -> {
            if (mHasGoogleApp && mSmartspace.isChecked()) {
              Toast.makeText(
                      getContext(),
                      R.string.blissify_omnijaws_disable_smartspace_toast,
                      Toast.LENGTH_SHORT)
                  .show();
              return true;
            }
            return false;
          });
    }

    updateState();
  }

  private boolean isPackageInstalled(String packageName) {
    try {
      getContext().getPackageManager().getApplicationInfo(packageName, 0);
      return true;
    } catch (PackageManager.NameNotFoundException e) {
      return false;
    }
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    if (preference == mSmartspace) {
      mSmartspace.setChecked((Boolean) newValue);
      updateState();
      SystemUtils.showSystemUiRestartDialog(getContext());
      return true;
    }
    return false;
  }

  private void updateState() {
    if (mOmniJaws == null || mSmartspace == null) return;

    if (!mHasOmniJawsService) return;

    boolean smartspaceOn = mHasGoogleApp && mSmartspace.isChecked();
    boolean omniJawsConfigured = OmniJawsClient.get().isOmniJawsEnabled(getContext());

    if (smartspaceOn) {
      mOmniJaws.setEnabled(false);
      mOmniJaws.setSummary(R.string.lockscreen_weather_smartspace_active);
    } else if (!omniJawsConfigured) {
      mOmniJaws.setEnabled(true);
      mOmniJaws.setSummary(R.string.lockscreen_weather_not_configured);
    } else {
      mOmniJaws.setEnabled(true);
      mOmniJaws.setSummary(R.string.lockscreen_weather_summary);
    }

    if (mHasGoogleApp) {
      boolean omniJawsWeatherOn =
          Settings.System.getInt(getContext().getContentResolver(), "lockscreen_weather_enabled", 0)
              != 0;
      mSmartspace.setEnabled(!omniJawsWeatherOn);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    updateState();
  }

  @Override
  public int getMetricsCategory() {
    return MetricsEvent.BLISSIFY;
  }

  public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
      new BaseSearchIndexProvider(R.xml.blissify_lockscreen_weather);
}
