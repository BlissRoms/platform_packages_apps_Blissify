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

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import org.blissroms.blissify.fragments.BlissifyDashboardFragment;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class SystemMisc extends BlissifyDashboardFragment {

  public static final String TAG = "SystemMisc";

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
    return R.xml.blissify_system_misc;
  }

  public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
      new BaseSearchIndexProvider(R.xml.blissify_system_misc);
}
