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

import android.content.Context;
import android.os.Bundle;
import androidx.preference.PreferenceScreen;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.bliss.BlissUtils;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import java.util.List;
import org.blissroms.blissify.fragments.BlissifyFragment;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class UdfpsSettings extends BlissifyFragment {

  private static final String KEY_UDFPS_ICON = "udfps_icon";
  private static final String KEY_UDFPS_ANIMATION = "udfps_animation";

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    addPreferencesFromResource(R.xml.blissify_lockscreen_udfps_settings);

    final Context context = getContext();
    final PreferenceScreen prefScreen = getPreferenceScreen();

    if (!BlissUtils.isPackageInstalled(context, "org.blissroms.udfps.icons")) {
      prefScreen.removePreference(findPreference(KEY_UDFPS_ICON));
    }
    if (!BlissUtils.isPackageInstalled(context, "org.blissroms.udfps.animations")) {
      prefScreen.removePreference(findPreference(KEY_UDFPS_ANIMATION));
    }
  }

  @Override
  public int getMetricsCategory() {
    return MetricsEvent.BLISSIFY;
  }

  public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
      new BaseSearchIndexProvider(R.xml.blissify_lockscreen_udfps_settings) {

        @Override
        public List<String> getNonIndexableKeys(Context context) {
          List<String> keys = super.getNonIndexableKeys(context);
          if (!BlissUtils.isPackageInstalled(context, "org.blissroms.udfps.icons")) {
            keys.add(KEY_UDFPS_ICON);
          }
          if (!BlissUtils.isPackageInstalled(context, "org.blissroms.udfps.animations")) {
            keys.add(KEY_UDFPS_ANIMATION);
          }
          return keys;
        }
      };
}
