/*
 * Copyright (C) 2020 crDroid Android Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.blissroms.blissify.preferences.colorpicker;

import android.content.Context;
import android.util.AttributeSet;
import org.blissroms.blissify.preferences.SecureSettingsStore;

public class SecureSettingColorPickerPreference extends ColorPickerPreference {

  public SecureSettingColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setPreferenceDataStore(new SecureSettingsStore(context.getContentResolver()));
  }

  public SecureSettingColorPickerPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    setPreferenceDataStore(new SecureSettingsStore(context.getContentResolver()));
  }

  public SecureSettingColorPickerPreference(Context context) {
    super(context, null);
    setPreferenceDataStore(new SecureSettingsStore(context.getContentResolver()));
  }
}
