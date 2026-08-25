/*
 * Copyright (C) 2014-2026 The BlissRoms Project
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

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.blissroms.blissify.fragments.BlissifyDashboardFragment;
import org.json.JSONArray;
import org.json.JSONObject;

public class NowPlayingHistory extends BlissifyDashboardFragment {

  public static final String TAG = "NowPlayingHistory";
  private static final String KEY_HISTORY_CATEGORY = "now_playing_history_category";
  private static final int MENU_CLEAR_ALL = Menu.FIRST;

  private PreferenceCategory mHistoryCategory;
  private final ExecutorService mImageExecutor = Executors.newFixedThreadPool(2);
  private final LruCache<String, Drawable> mIconCache = new LruCache<>(50);

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setHasOptionsMenu(true);
    mHistoryCategory = findPreference(KEY_HISTORY_CATEGORY);
    loadHistory();
  }

  @Override
  public void onResume() {
    super.onResume();
    loadHistory();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mImageExecutor.shutdown();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    menu.add(0, MENU_CLEAR_ALL, 0, R.string.now_playing_clear_history)
        .setIcon(R.drawable.ic_pref_history)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == MENU_CLEAR_ALL) {
      showClearConfirmationDialog();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void showClearConfirmationDialog() {
    new AlertDialog.Builder(getContext())
        .setTitle(R.string.now_playing_clear_history_title)
        .setMessage(R.string.now_playing_clear_history_message)
        .setPositiveButton(
            R.string.now_playing_clear_history,
            (dialog, which) -> {
              clearHistory();
            })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  private void clearHistory() {
    Settings.System.putStringForUser(
        getContext().getContentResolver(),
        "now_playing_history",
        "",
        UserHandle.USER_CURRENT);
    mIconCache.evictAll();
    loadHistory();
  }

  private void loadHistory() {
    if (mHistoryCategory == null) {
      return;
    }
    mHistoryCategory.removeAll();

    String historyJson =
        Settings.System.getStringForUser(
            getContext().getContentResolver(),
            "now_playing_history",
            UserHandle.USER_CURRENT);

    if (TextUtils.isEmpty(historyJson)) {
      Preference emptyPref = new Preference(getPrefContext());
      emptyPref.setTitle(R.string.now_playing_history_empty);
      emptyPref.setSummary(R.string.now_playing_history_empty_summary);
      emptyPref.setSelectable(false);
      mHistoryCategory.addPreference(emptyPref);
      return;
    }

    try {
      JSONArray array = new JSONArray(historyJson);
      if (array.length() == 0) {
        Preference emptyPref = new Preference(getPrefContext());
        emptyPref.setTitle(R.string.now_playing_history_empty);
        emptyPref.setSummary(R.string.now_playing_history_empty_summary);
        emptyPref.setSelectable(false);
        mHistoryCategory.addPreference(emptyPref);
        return;
      }

      long now = System.currentTimeMillis();
      for (int i = 0; i < array.length(); i++) {
        JSONObject item = array.getJSONObject(i);
        String title = item.optString("title");
        String artist = item.optString("artist");
        String albumArtUri = item.optString("albumArtUri");
        long timestamp = item.optLong("timestamp", now);

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(artist)) {
          continue;
        }

        CharSequence timeAgo =
            DateUtils.getRelativeTimeSpanString(
                timestamp, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);

        Preference trackPref = new Preference(getPrefContext());
        trackPref.setTitle(title);
        trackPref.setSummary(artist + " • " + timeAgo);
        trackPref.setIcon(R.drawable.ic_pref_now_playing_aod);

        if (!TextUtils.isEmpty(albumArtUri)) {
          loadArtworkAsync(trackPref, albumArtUri);
        }

        trackPref.setOnPreferenceClickListener(
            preference -> {
              try {
                String query = Uri.encode(title + " " + artist);
                Intent searchIntent =
                    new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/results?search_query=" + query));
                searchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(searchIntent);
              } catch (Exception e) {
                // Ignore
              }
              return true;
            });

        mHistoryCategory.addPreference(trackPref);
      }
    } catch (Exception e) {
      Preference emptyPref = new Preference(getPrefContext());
      emptyPref.setTitle(R.string.now_playing_history_empty);
      emptyPref.setSummary(R.string.now_playing_history_empty_summary);
      emptyPref.setSelectable(false);
      mHistoryCategory.addPreference(emptyPref);
    }
  }

  private void loadArtworkAsync(Preference pref, String uriString) {
    Drawable cached = mIconCache.get(uriString);
    if (cached != null) {
      pref.setIcon(cached);
      return;
    }

    mImageExecutor.execute(
        () -> {
          try {
            Bitmap bitmap = null;
            if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
              URL url = new URL(uriString);
              HttpURLConnection conn = (HttpURLConnection) url.openConnection();
              conn.setConnectTimeout(4000);
              conn.setReadTimeout(4000);
              conn.setDoInput(true);
              try (InputStream in = conn.getInputStream()) {
                bitmap = BitmapFactory.decodeStream(in);
              }
            } else {
              Uri uri = Uri.parse(uriString);
              try (InputStream in = getContext().getContentResolver().openInputStream(uri)) {
                bitmap = BitmapFactory.decodeStream(in);
              }
            }

            if (bitmap != null && getActivity() != null) {
              int sizePx = (int) (48 * getResources().getDisplayMetrics().density);
              Bitmap scaled = Bitmap.createScaledBitmap(bitmap, sizePx, sizePx, true);
              RoundedBitmapDrawable rounded =
                  RoundedBitmapDrawableFactory.create(getResources(), scaled);
              rounded.setCornerRadius(16f);

              mIconCache.put(uriString, rounded);
              getActivity().runOnUiThread(() -> pref.setIcon(rounded));
            }
          } catch (Exception e) {
            // Leave fallback icon intact
          }
        });
  }

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
    return R.xml.blissify_now_playing_history;
  }
}
