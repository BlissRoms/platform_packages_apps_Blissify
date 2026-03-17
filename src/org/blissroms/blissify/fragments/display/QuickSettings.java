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

import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import org.blissroms.blissify.fragments.BlissifyDashboardFragment;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class QuickSettings extends BlissifyDashboardFragment
        implements Preference.OnPreferenceChangeListener {

  public static final String TAG = "QuickSettings";

  private static final String KEY_QS_PANEL_STYLE = "qs_panel_style";
  private static final String KEY_TILE_LABEL_HIDE = "qs_tile_label_hide";

  private Preference mTileLabelHide;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Preference stylePref = findPreference(KEY_QS_PANEL_STYLE);
    mTileLabelHide = findPreference(KEY_TILE_LABEL_HIDE);

    if (stylePref != null) {
      stylePref.setOnPreferenceChangeListener(this);
    }

    int style = Settings.Secure.getIntForUser(getContext().getContentResolver(),
            KEY_QS_PANEL_STYLE, 0, UserHandle.USER_CURRENT);
    updateCircularPrefs(style == 1);
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    if (KEY_QS_PANEL_STYLE.equals(preference.getKey())) {
      int style = Integer.parseInt((String) newValue);
      updateCircularPrefs(style == 1);
    }
    return true;
  }

  private void updateCircularPrefs(boolean circular) {
    if (mTileLabelHide != null) mTileLabelHide.setVisible(circular);
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
    return R.xml.blissify_quicksettings;
  }

  public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
      new BaseSearchIndexProvider(R.xml.blissify_quicksettings);
}
