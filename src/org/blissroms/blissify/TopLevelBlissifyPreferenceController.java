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

package org.blissroms.blissify;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.BasePreferenceController;
import java.util.Random;

public class TopLevelBlissifyPreferenceController extends BasePreferenceController {

  public TopLevelBlissifyPreferenceController(Context context, String preferenceKey) {
    super(context, preferenceKey);
  }

  @Override
  public int getAvailabilityStatus() {
    return AVAILABLE;
  }

  @Override
  public void displayPreference(PreferenceScreen screen) {
    super.displayPreference(screen);
    Preference preference = screen.findPreference(getPreferenceKey());
    if (preference != null) {
      Drawable icon = mContext.getDrawable(R.drawable.ic_settings_blissify);
      icon.setTint(mContext.getColor(R.color.homepage_connected_device_foreground));
      preference.setIcon(
          Utils.getAdaptiveIcon(
              mContext, icon, mContext.getColor(R.color.homepage_connected_device_background)));
      String[] summaries =
          mContext.getResources().getStringArray(R.array.blissify_settings_summaries);
      preference.setSummary(summaries[new Random().nextInt(summaries.length)]);
    }
  }
}
