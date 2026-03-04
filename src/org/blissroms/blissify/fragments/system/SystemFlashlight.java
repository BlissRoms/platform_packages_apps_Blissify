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

package org.blissroms.blissify.fragments.system;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.bliss.BlissUtils;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import java.util.List;
import org.blissroms.blissify.fragments.BlissifyFragment;
import org.blissroms.blissify.preferences.CustomSeekBarPreference;
import org.blissroms.blissify.preferences.SystemSettingSwitchPreference;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class SystemFlashlight extends BlissifyFragment
    implements Preference.OnPreferenceChangeListener {

  private static final String FLASHLIGHT_CALL_PREF = "flashlight_on_call";
  private static final String FLASHLIGHT_DND_PREF = "flashlight_on_call_ignore_dnd";
  private static final String FLASHLIGHT_RATE_PREF = "flashlight_on_call_rate";

  private ListPreference mFlashOnCall;
  private SystemSettingSwitchPreference mFlashOnCallIgnoreDND;
  private CustomSeekBarPreference mFlashOnCallRate;

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    addPreferencesFromResource(R.xml.blissify_system_flashlight);

    final ContentResolver resolver = getActivity().getContentResolver();

    mFlashOnCall = (ListPreference) findPreference(FLASHLIGHT_CALL_PREF);
    mFlashOnCall.setOnPreferenceChangeListener(this);

    mFlashOnCallIgnoreDND = (SystemSettingSwitchPreference) findPreference(FLASHLIGHT_DND_PREF);
    mFlashOnCallRate = (CustomSeekBarPreference) findPreference(FLASHLIGHT_RATE_PREF);

    int value = Settings.System.getInt(resolver, Settings.System.FLASHLIGHT_ON_CALL, 0);
    mFlashOnCallIgnoreDND.setEnabled(value > 1);
    mFlashOnCallRate.setEnabled(value > 0);
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    if (preference == mFlashOnCall) {
      int value = Integer.parseInt((String) newValue);
      mFlashOnCallIgnoreDND.setEnabled(value > 1);
      mFlashOnCallRate.setEnabled(value > 0);
      return true;
    }
    return false;
  }

  @Override
  public int getMetricsCategory() {
    return MetricsEvent.BLISSIFY;
  }

  public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
      new BaseSearchIndexProvider(R.xml.blissify_system_flashlight) {

        @Override
        public List<String> getNonIndexableKeys(Context context) {
          List<String> keys = super.getNonIndexableKeys(context);
          if (!BlissUtils.deviceHasFlashlight(context)) {
            keys.add(FLASHLIGHT_CALL_PREF);
            keys.add(FLASHLIGHT_DND_PREF);
            keys.add(FLASHLIGHT_RATE_PREF);
          }
          return keys;
        }
      };
}
