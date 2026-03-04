/*
 * Copyright (C) 2020 crDroid Android Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.blissroms.blissify.preferences.colorpicker;

import android.content.Context;
import android.util.AttributeSet;
import org.blissroms.blissify.preferences.SystemSettingsStore;

public class SystemSettingColorPickerPreference extends ColorPickerPreference {

  public SystemSettingColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setPreferenceDataStore(new SystemSettingsStore(context.getContentResolver()));
  }

  public SystemSettingColorPickerPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    setPreferenceDataStore(new SystemSettingsStore(context.getContentResolver()));
  }

  public SystemSettingColorPickerPreference(Context context) {
    super(context, null);
    setPreferenceDataStore(new SystemSettingsStore(context.getContentResolver()));
  }
}
