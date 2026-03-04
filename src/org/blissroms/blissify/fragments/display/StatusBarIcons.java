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

package org.blissroms.blissify.fragments.display;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.PreferenceScreen;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import java.util.List;
import org.blissroms.blissify.fragments.BlissifyFragment;
import org.blissroms.blissify.preferences.SystemSettingSwitchPreference;
import org.blissroms.blissify.utils.DeviceUtils;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class StatusBarIcons extends BlissifyFragment {

  private static final String KEY_BLUETOOTH_BATTERY_STATUS = "bluetooth_show_battery";

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    addPreferencesFromResource(R.xml.blissify_statusbar_icons);

    final Context context = getContext();
    final PreferenceScreen prefScreen = getPreferenceScreen();

    if (!DeviceUtils.deviceSupportsBluetooth(context)) {
      SystemSettingSwitchPreference btBattery =
          (SystemSettingSwitchPreference) findPreference(KEY_BLUETOOTH_BATTERY_STATUS);
      prefScreen.removePreference(btBattery);
    }
  }

  @Override
  public int getMetricsCategory() {
    return MetricsEvent.BLISSIFY;
  }

  public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
      new BaseSearchIndexProvider(R.xml.blissify_statusbar_icons) {

        @Override
        public List<String> getNonIndexableKeys(Context context) {
          List<String> keys = super.getNonIndexableKeys(context);
          if (!DeviceUtils.deviceSupportsBluetooth(context)) {
            keys.add(KEY_BLUETOOTH_BATTERY_STATUS);
          }
          return keys;
        }
      };
}
