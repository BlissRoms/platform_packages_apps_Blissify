/*
 * Copyright (C) 2014-2022 BlissRoms Project
 * Copyright (C) 2024 ApolloOS
 * SPDX-License-Identifier: Apache-2.0
*/
package org.blissroms.blissify.fragments.lockscreen;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.recyclerview.widget.RecyclerView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;

import com.bumptech.glide.Glide;

import java.util.Arrays;

public class UdfpsAnimation extends SettingsPreferenceFragment {

    private RecyclerView mRecyclerView;
    private String mPkg = "org.bliss.udfps.animations";
    private AnimationDrawable animation;

    private Resources udfpsRes;

    private String[] mAnims;
    private String[] mAnimPreviews;
    private String[] mTitles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.udfps_animation_title);

        loadResources();
    }

    private void loadResources() {
        try {
            PackageManager pm = getActivity().getPackageManager();
            udfpsRes = pm.getResourcesForApplication(mPkg);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mAnims = udfpsRes.getStringArray(udfpsRes.getIdentifier("udfps_animation_styles",
                "array", mPkg));
        mAnimPreviews = udfpsRes.getStringArray(udfpsRes.getIdentifier("udfps_animation_previews",
                "array", mPkg));
        mTitles = udfpsRes.getStringArray(udfpsRes.getIdentifier("udfps_animation_titles",
                "array", mPkg));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.item_view, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        UdfpsAnimAdapter mUdfpsAnimAdapter = new UdfpsAnimAdapter(getActivity());
        mRecyclerView.setAdapter(mUdfpsAnimAdapter);

        return view;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.UDFPS_ANIM_STYLE, 0, UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.BLISSIFY;
    }

    public class UdfpsAnimAdapter extends RecyclerView.Adapter<UdfpsAnimAdapter.UdfpsAnimViewHolder> {
        Context context;
        String mSelectedAnim;
        String mAppliedAnim;

        public UdfpsAnimAdapter(Context context) {
            this.context = context;
        }

        @Override
        public UdfpsAnimViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_option, parent, false);
            UdfpsAnimViewHolder vh = new UdfpsAnimViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(UdfpsAnimViewHolder holder, final int position) {
            String animName = mAnims[position];

            Glide.with(holder.image.getContext())
                    .load("")
                    .placeholder(getDrawable(holder.image.getContext(), mAnimPreviews[position]))
                    .into(holder.image);

            holder.name.setText(mTitles[position]);

            if (position == Settings.System.getInt(context.getContentResolver(),
                Settings.System.UDFPS_ANIM_STYLE, 0)) {
                mAppliedAnim = animName;
                if (mSelectedAnim == null) {
                    mSelectedAnim = animName;
                }
            }

            holder.itemView.setActivated(animName == mSelectedAnim);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateActivatedStatus(mSelectedAnim, false);
                    updateActivatedStatus(animName, true);
                    mSelectedAnim = animName;
                    holder.image.setBackgroundDrawable(getDrawable(v.getContext(), mAnims[position]));
                    animation = (AnimationDrawable) holder.image.getBackground();
                    animation.setOneShot(true);
                    animation.start();
                    Settings.System.putInt(getActivity().getContentResolver(),
                            Settings.System.UDFPS_ANIM_STYLE, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAnims.length;
        }

        public class UdfpsAnimViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            ImageView image;
            public UdfpsAnimViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.option_label);
                image = (ImageView) itemView.findViewById(R.id.option_thumbnail);
            }
        }

        private void updateActivatedStatus(String anim, boolean isActivated) {
            int index = Arrays.asList(mAnims).indexOf(anim);
            if (index < 0) {
                return;
            }
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(index);
            if (holder != null && holder.itemView != null) {
                holder.itemView.setActivated(isActivated);
            }
        }
    }

    public Drawable getDrawable(Context context, String drawableName) {
        try {
            PackageManager pm = context.getPackageManager();
            Resources res = pm.getResourcesForApplication(mPkg);
            return res.getDrawable(res.getIdentifier(drawableName, "drawable", mPkg));
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
