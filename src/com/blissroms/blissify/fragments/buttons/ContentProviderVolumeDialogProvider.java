/*
 * Copyright (C) 2019-2020 The BlissRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blissroms.blissify.fragments.buttons;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.android.customization.model.CustomizationManager.OptionsFetchedListener;
import com.blissroms.blissify.fragments.buttons.VolumeDialogInfo.Builder;
import com.android.wallpaper.R;
import com.android.wallpaper.asset.ContentUriAsset;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class ContentProviderVolumeDialogProvider implements VolumeDialogProvider {

    private final Context mContext;
    private final ProviderInfo mProviderInfo;
    private List<VolumeDialogInfo> mVolumeDialogs;

    public ContentProviderVolumeDialogProvider(Context context) {
        mContext = context;
        String providerAuthority = mContext.getString(R.string.volumedialog_provider_authority);
        // TODO: check permissions if needed
        mProviderInfo = TextUtils.isEmpty(providerAuthority) ? null
                : mContext.getPackageManager().resolveContentProvider(providerAuthority,
                        PackageManager.MATCH_SYSTEM_ONLY);
    }

    @Override
    public boolean isAvailable() {
        return mProviderInfo != null && (mVolumeDialogs == null || !mVolumeDialogs.isEmpty());
    }

    @Override
    public void fetch(OptionsFetchedListener<VolumeDialogInfo> callback, boolean reload) {
        if (!isAvailable()) {
            if (callback != null) {
                callback.onError(null);
            }
            return;
        }
        if (mVolumeDialogs != null && !reload) {
            if (callback != null) {
                if (!mVolumeDialogs.isEmpty()) {
                    callback.onOptionsLoaded(mVolumeDialogs);
                } else {
                    callback.onError(null);
                }
            }
            return;
        }
        new VolumeDialogFetchTask(mContext, mProviderInfo, options -> {
            mVolumeDialogs = options;
            if (callback != null) {
                if (!mVolumeDialogs.isEmpty()) {
                    callback.onOptionsLoaded(mVolumeDialogs);
                } else {
                    callback.onError(null);
                }
            }
        }).execute();
    }

    private static class VolumeDialogFetchTask extends AsyncTask<Void, Void, List<VolumeDialogInfo>> {

        private static final String LIST_OPTIONS = "list_options";

        private static final String COL_NAME = "name";
        private static final String COL_TITLE = "title";
        private static final String COL_ID = "id";
        private static final String COL_THUMBNAIL = "thumbnail";
        private static final String COL_PREVIEW = "preview";

        private final OptionsFetchedListener<VolumeDialogInfo> mCallback;
        private Context mContext;
        private final ProviderInfo mProviderInfo;

        public VolumeDialogFetchTask(Context context, ProviderInfo providerInfo,
                OptionsFetchedListener<VolumeDialogInfo> callback) {
            super();
            mContext = context;
            mProviderInfo = providerInfo;
            mCallback = callback;
        }

        @Override
        protected List<VolumeDialogInfo> doInBackground(Void... voids) {
            Uri optionsUri = new Uri.Builder()
                    .scheme(ContentResolver.SCHEME_CONTENT)
                    .authority(mProviderInfo.authority)
                    .appendPath(LIST_OPTIONS)
                    .build();

            ContentResolver resolver = mContext.getContentResolver();

            List<VolumeDialogInfo> volumedialogs = new ArrayList<>();
            try (Cursor c = resolver.query(optionsUri, null, null, null, null)) {
                while(c.moveToNext()) {
                    String id = c.getString(c.getColumnIndex(COL_ID));
                    String title = c.getString(c.getColumnIndex(COL_TITLE));
                    String thumbnailUri = c.getString(c.getColumnIndex(COL_THUMBNAIL));
                    String previewUri = c.getString(c.getColumnIndex(COL_PREVIEW));
                    Uri thumbnail = Uri.parse(thumbnailUri);
                    Uri preview = Uri.parse(previewUri);

                    VolumeDialogInfo.Builder builder = new Builder();
                    builder.setId(id).setTitle(title)
                            .setThumbnail(new ContentUriAsset(mContext, thumbnail,
                                    RequestOptions.fitCenterTransform()))
                            .setPreview(new ContentUriAsset(mContext, preview,
                                    RequestOptions.fitCenterTransform()));
                    volumedialogs.add(builder.build());
                }
                Glide.get(mContext).clearDiskCache();
            } catch (Exception e) {
                volumedialogs = null;
            } finally {
                mContext = null;
            }
            return volumedialogs;
        }

        @Override
        protected void onPostExecute(List<VolumeDialogInfo> volumedialogs) {
            super.onPostExecute(volumedialogs);
            mCallback.onOptionsLoaded(volumedialogs);
        }
    }
}
