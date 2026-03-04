/*
 * Copyright (C) 2022-2024 crDroid Android Project
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
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.bumptech.glide.Glide;
import java.util.Arrays;
import org.blissroms.blissify.fragments.BlissifyFragment;

public class UdfpsIcons extends BlissifyFragment {

  private RecyclerView mRecyclerView;

  private Resources udfpsRes;

  private String mPkg = "org.blissroms.udfps.icons";

  private String[] mIcons;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getActivity().setTitle(R.string.themes_udfps_icon_title);

    loadResources();
  }

  private void loadResources() {
    try {
      PackageManager pm = getActivity().getPackageManager();
      udfpsRes = pm.getResourcesForApplication(mPkg);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }

    mIcons = udfpsRes.getStringArray(udfpsRes.getIdentifier("udfps_icons", "array", mPkg));
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.item_view, container, false);

    mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
    GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
    mRecyclerView.setLayoutManager(gridLayoutManager);
    UdfpsIconAdapter mUdfpsIconAdapter = new UdfpsIconAdapter(getActivity());
    mRecyclerView.setAdapter(mUdfpsIconAdapter);

    return view;
  }

  @Override
  public int getMetricsCategory() {
    return MetricsEvent.BLISSIFY;
  }

  public class UdfpsIconAdapter extends RecyclerView.Adapter<UdfpsIconAdapter.UdfpsIconViewHolder> {
    Context context;
    String mSelectedIcon;
    String mAppliedIcon;

    public UdfpsIconAdapter(Context context) {
      this.context = context;
    }

    @Override
    public UdfpsIconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View v =
          LayoutInflater.from(parent.getContext()).inflate(R.layout.item_option, parent, false);
      UdfpsIconViewHolder vh = new UdfpsIconViewHolder(v);
      return vh;
    }

    @Override
    public void onBindViewHolder(UdfpsIconViewHolder holder, final int position) {
      String iconRes = mIcons[position];

      Glide.with(holder.image.getContext())
          .load("")
          .placeholder(getDrawable(holder.image.getContext(), mIcons[position]))
          .into(holder.image);

      holder.image.setPadding(20, 20, 20, 20);

      holder.name.setVisibility(View.GONE);

      if (position
          == Settings.System.getInt(context.getContentResolver(), Settings.System.UDFPS_ICON, 0)) {
        mAppliedIcon = iconRes;
        if (mSelectedIcon == null) {
          mSelectedIcon = iconRes;
        }
      }
      holder.itemView.setActivated(iconRes == mSelectedIcon);
      holder.itemView.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              updateActivatedStatus(mSelectedIcon, false);
              updateActivatedStatus(iconRes, true);
              mSelectedIcon = iconRes;
              Settings.System.putInt(
                  getActivity().getContentResolver(), Settings.System.UDFPS_ICON, position);
            }
          });
    }

    @Override
    public int getItemCount() {
      return mIcons.length;
    }

    public class UdfpsIconViewHolder extends RecyclerView.ViewHolder {
      TextView name;
      ImageView image;

      public UdfpsIconViewHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.option_label);
        image = (ImageView) itemView.findViewById(R.id.option_thumbnail);
      }
    }

    private void updateActivatedStatus(String icon, boolean isActivated) {
      int index = Arrays.asList(mIcons).indexOf(icon);
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
      Context ctx = context.createPackageContext(mPkg, Context.CONTEXT_IGNORE_SECURITY);
      return ctx.getDrawable(res.getIdentifier(drawableName, "drawable", mPkg));
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }
}
