/*
 * Copyright (C) 2014-2026 The BlissRoms Project
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
import android.content.Intent;
import android.os.Bundle;
import androidx.preference.Preference;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import java.util.List;
import org.blissroms.blissify.fragments.BlissifyDashboardFragment;
import org.blissroms.blissify.utils.DeviceUtils;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class NowPlaying extends BlissifyDashboardFragment {

  public static final String TAG = "NowPlaying";

  private static final String KEY_TRIGGER_SEARCH = "now_playing_trigger_search";

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    Preference triggerSearchPref = findPreference(KEY_TRIGGER_SEARCH);
    if (triggerSearchPref != null) {
      triggerSearchPref.setOnPreferenceClickListener(
          preference -> {
            getContext()
                .sendBroadcast(
                    new Intent("org.blissroms.ambientmusic.action.ON_DEMAND_SEARCH"));
            return true;
          });
    }
  }

  @Override
  public int getMetricsCategory() {
    return MetricsEvent.BLISSIFY;
  }

  @Override
  protected String getLogTag() {
    return TAG;
  }

  @Override
  protected int getPreferenceScreenResId() {
    return R.xml.blissify_now_playing;
  }

  public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
      new BaseSearchIndexProvider(R.xml.blissify_now_playing) {

        @Override
        public List<String> getNonIndexableKeys(Context context) {
          List<String> keys = super.getNonIndexableKeys(context);
          if (DeviceUtils.isPixelDevice()) {
            keys.add("now_playing_enabled");
            keys.add("now_playing_show_lockscreen");
            keys.add("now_playing_show_aod");
            keys.add("now_playing_show_album_art");
            keys.add("now_playing_on_demand");
            keys.add(KEY_TRIGGER_SEARCH);
          }
          return keys;
        }
      };
}
