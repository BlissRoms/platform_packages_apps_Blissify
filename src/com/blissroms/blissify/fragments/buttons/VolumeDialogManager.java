/*
 * Copyright (C) 2019 The Android Open Source Project
 * Copyright (C) 2019-2020 The BlissRoms Project
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
package com.blissroms.blissify.fragments.buttons;

import android.content.ContentResolver;
import android.provider.Settings.Secure;

import com.android.customization.module.ThemesUserEventLogger;

/**
 * {@link CustomizationManager} for Volume Dialog that implements apply by writing to secure settings.
 */
public class VolumeDialogManager extends BaseVolumeDialogManager {

    static final String VOLUME_DIALOG_SETTING = Secure.CUSTOM_VOLUME_DIALOG;
    private final ContentResolver mContentResolver;
    private final ThemesUserEventLogger mEventLogger;

    public VolumeDialogManager(ContentResolver resolver, VolumeDialogProvider provider,
            ThemesUserEventLogger logger) {
        super(provider);
        mContentResolver = resolver;
        mEventLogger = logger;
    }

    @Override
    protected void handleApply(VolumeDialogInfo option, Callback callback) {
        if (Secure.putString(mContentResolver, VOLUME_DIALOG_SETTING, option.getId())) {
            mEventLogger.logVolumeDialogApplied(option);
            callback.onSuccess();
        } else {
            callback.onError(null);
        }
    }

    @Override
    protected String lookUpCurrentVolumeDialog() {
        return Secure.getString(mContentResolver, VOLUME_DIALOG_SETTING);
    }
}
