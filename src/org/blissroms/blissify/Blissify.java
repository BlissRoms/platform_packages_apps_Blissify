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

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import com.google.android.material.appbar.AppBarLayout;
import org.blissroms.blissify.fragments.about.About;
import org.blissroms.blissify.fragments.appearance.Appearance;
import org.blissroms.blissify.fragments.display.Display;
import org.blissroms.blissify.fragments.input.Input;
import org.blissroms.blissify.fragments.lockscreen.Lockscreen;
import org.blissroms.blissify.fragments.system.SystemSettings;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class Blissify extends SettingsPreferenceFragment {

  private static class DashboardItem {
    final int titleRes;
    final int subtitleRes;
    final int iconRes;
    final Class<?> fragmentClass;
    final Intent intent;

    DashboardItem(int titleRes, int subtitleRes, int iconRes, Class<?> fragmentClass) {
      this.titleRes = titleRes;
      this.subtitleRes = subtitleRes;
      this.iconRes = iconRes;
      this.fragmentClass = fragmentClass;
      this.intent = null;
    }

    DashboardItem(int titleRes, int subtitleRes, int iconRes, Intent intent) {
      this.titleRes = titleRes;
      this.subtitleRes = subtitleRes;
      this.iconRes = iconRes;
      this.fragmentClass = null;
      this.intent = intent;
    }
  }

  private AppBarLayout mActivityAppBar;
  private CoordinatorLayout.Behavior mSavedBehavior;
  private android.view.View mContentFrame;

  private static DashboardItem[] buildItems() {
    return new DashboardItem[] {
      new DashboardItem(
          R.string.blissify_appearance_title,
          R.string.blissify_appearance_subtitle,
          R.drawable.ic_blissify_themes,
          Appearance.class),
      new DashboardItem(
          R.string.blissify_display_title,
          R.string.blissify_display_subtitle,
          R.drawable.ic_blissify_display,
          Display.class),
      new DashboardItem(
          R.string.blissify_lockscreen_title,
          R.string.blissify_lockscreen_subtitle,
          R.drawable.ic_blissify_lockscreen,
          Lockscreen.class),
      new DashboardItem(
          R.string.blissify_input_title,
          R.string.blissify_input_subtitle,
          R.drawable.ic_blissify_input,
          Input.class),
      new DashboardItem(
          R.string.blissify_system_title,
          R.string.blissify_system_subtitle,
          R.drawable.ic_blissify_system,
          SystemSettings.class),
      new DashboardItem(
          R.string.blissify_about_title,
          R.string.blissify_about_subtitle,
          R.drawable.ic_blissify_about,
          About.class),
    };
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.blissify_dashboard_grid, container, false);
    view.findViewById(R.id.back_button).setOnClickListener(v -> requireActivity().onBackPressed());
    RecyclerView rv = view.findViewById(R.id.dashboard_recycler_view);
    rv.setLayoutManager(new GridLayoutManager(getActivity(), 2));
    rv.setAdapter(new DashboardAdapter(buildItems()));
    view.post(
        () -> {
          mActivityAppBar =
              requireActivity()
                  .findViewById(com.android.settingslib.collapsingtoolbar.R.id.app_bar);
          mContentFrame =
              requireActivity()
                  .findViewById(com.android.settingslib.collapsingtoolbar.R.id.content_frame);
          if (mActivityAppBar != null) {
            mActivityAppBar.setVisibility(View.GONE);
          }
          if (mContentFrame != null) {
            CoordinatorLayout.LayoutParams lp =
                (CoordinatorLayout.LayoutParams) mContentFrame.getLayoutParams();
            mSavedBehavior = lp.getBehavior();
            lp.setBehavior(null);
            mContentFrame.setLayoutParams(lp);
          }
        });
    return view;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (mContentFrame != null) {
      CoordinatorLayout.LayoutParams lp =
          (CoordinatorLayout.LayoutParams) mContentFrame.getLayoutParams();
      lp.setBehavior(mSavedBehavior);
      mContentFrame.setLayoutParams(lp);
      mContentFrame = null;
      mSavedBehavior = null;
    }
    if (mActivityAppBar != null) {
      mActivityAppBar.setVisibility(View.VISIBLE);
      mActivityAppBar = null;
    }
  }

  @Override
  public int getMetricsCategory() {
    return MetricsEvent.BLISSIFY;
  }

  private class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.CardViewHolder> {

    private final DashboardItem[] mItems;

    DashboardAdapter(DashboardItem[] items) {
      mItems = items;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View v =
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.blissify_dashboard_card, parent, false);
      return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
      DashboardItem item = mItems[position];
      holder.title.setText(item.titleRes);
      holder.subtitle.setText(item.subtitleRes);
      holder.icon.setImageResource(item.iconRes);
      holder.itemView.setOnClickListener(
          v -> {
            if (item.fragmentClass != null) {
              new SubSettingLauncher(getActivity())
                  .setDestination(item.fragmentClass.getName())
                  .setTitleRes(item.titleRes)
                  .setSourceMetricsCategory(getMetricsCategory())
                  .launch();
            } else if (item.intent != null) {
              Blissify.this.startActivity(item.intent);
            }
          });
    }

    @Override
    public int getItemCount() {
      return mItems.length;
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
      final TextView title;
      final TextView subtitle;
      final ImageView icon;

      CardViewHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.card_title);
        subtitle = itemView.findViewById(R.id.card_subtitle);
        icon = itemView.findViewById(R.id.card_icon);
      }
    }
  }

  public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
      new BaseSearchIndexProvider(R.xml.blissify);
}
