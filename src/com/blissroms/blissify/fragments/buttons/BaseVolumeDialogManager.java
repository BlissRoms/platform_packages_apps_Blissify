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

import com.android.customization.model.CustomizationManager;

/**
 * {@link CustomizationManager} for VolumeDialogs.
 */
public abstract class BaseVolumeDialogManager implements CustomizationManager<VolumeDialogInfo> {

    private final VolumeDialogProvider mVolumeDialogProvider;

    public BaseVolumeDialogManager(VolumeDialogProvider provider) {
        mVolumeDialogProvider = provider;
    }

    @Override
    public boolean isAvailable() {
        return mVolumeDialogProvider.isAvailable();
    }

    @Override
    public void apply(VolumeDialogInfo option, Callback callback) {
        handleApply(option, callback);
    }

    @Override
    public void fetchOptions(OptionsFetchedListener<VolumeDialogInfo> callback, boolean reload) {
        mVolumeDialogProvider.fetch(callback, false);
    }

    /** Returns the ID of the current Volume Dialog, which may be null for the default Volume Dialog. */
    String getCurrentVolumeDialog() {
        return lookUpCurrentVolumeDialog();
    }

    /**
     * Implement to apply the VolumeDialog picked by the user for {@link BaseVolumeDialogManager#apply}.
     *
     * @param option VolumeDialog option, containing ID of the VolumeDialog, that the user picked.
     * @param callback Report success and failure.
     */
    protected abstract void handleApply(VolumeDialogInfo option, Callback callback);

    /**
     * Implement to look up the current Volume Dialog for {@link BaseVolumeDialogManager#getCurrentVolumeDialog()}.
     *
     * @return ID of current VolumeDialog. Can be null for the default Volume Dialog.
     */
    protected abstract String lookUpCurrentVolumeDialog();
}
