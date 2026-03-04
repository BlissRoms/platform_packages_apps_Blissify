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

import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.Preference;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import lineageos.preference.SystemSettingMainSwitchPreference;
import org.blissroms.blissify.fragments.BlissifyFragment;
import org.blissroms.blissify.utils.SystemUtils;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class OmniJawsWeather extends BlissifyFragment
    implements Preference.OnPreferenceChangeListener {

  private static final String KEY_WEATHER_ENABLED = "lockscreen_weather_enabled";

  private SystemSettingMainSwitchPreference mWeatherEnabled;

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    addPreferencesFromResource(R.xml.blissify_lockscreen_omnijaws);

    mWeatherEnabled = (SystemSettingMainSwitchPreference) findPreference(KEY_WEATHER_ENABLED);
    mWeatherEnabled.setOnPreferenceChangeListener(this);
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    if (preference == mWeatherEnabled) {
      boolean enabled = (Boolean) newValue;
      mWeatherEnabled.setChecked(enabled);
      if (enabled) {
        Settings.Secure.putIntForUser(
            getContext().getContentResolver(),
            "lockscreen_smartspace_enabled",
            0,
            UserHandle.USER_CURRENT);
      }
      SystemUtils.showSystemUiRestartDialog(getContext());
      return true;
    }
    return false;
  }

  @Override
  public int getMetricsCategory() {
    return MetricsEvent.BLISSIFY;
  }

  public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
      new BaseSearchIndexProvider(R.xml.blissify_lockscreen_omnijaws);
}
