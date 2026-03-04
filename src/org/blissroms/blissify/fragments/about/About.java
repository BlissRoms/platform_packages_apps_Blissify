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

package org.blissroms.blissify.fragments.about;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.blissroms.blissify.fragments.BlissifyFragment;
import org.json.JSONArray;
import org.json.JSONObject;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class About extends BlissifyFragment {

  private static final String TAG = "BlissifyAbout";

  private static final String API_BLISSROMS =
      "https://api.blisslabs.org/api/public/organizations/blisslabs/projects/blissroms";
  private static final String API_BLISSOS =
      "https://api.blisslabs.org/api/public/organizations/blisslabs/projects/blissos";

  private static final String PREFS_NAME = "blissify_about_cache";
  private static final String KEY_CACHE_BLISSROMS = "cache_blissroms";
  private static final String KEY_CACHE_BLISSOS = "cache_blissos";
  private static final String KEY_CACHE_TIMESTAMP = "cache_timestamp";
  private static final long CACHE_TTL_MS = 24 * 60 * 60 * 1000L; // 24 hours

  private final ExecutorService mExecutor = Executors.newFixedThreadPool(2);
  private final Handler mHandler = new Handler(Looper.getMainLooper());

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    addPreferencesFromResource(R.xml.blissify_about);

    final PreferenceScreen screen = getPreferenceScreen();
    addStaticBlissRomsSection(screen);
    addStaticBlissOsSection(screen);
    fetchAndRefresh();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mExecutor.shutdownNow();
  }

  private void fetchAndRefresh() {
    final Context ctx = getContext();
    if (ctx == null) return;

    SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    long lastFetch = prefs.getLong(KEY_CACHE_TIMESTAMP, 0);
    boolean cacheValid = (System.currentTimeMillis() - lastFetch) < CACHE_TTL_MS;

    if (cacheValid) {
      String cachedRoms = prefs.getString(KEY_CACHE_BLISSROMS, null);
      String cachedOs = prefs.getString(KEY_CACHE_BLISSOS, null);
      if (cachedRoms != null || cachedOs != null) {
        try {
          JSONObject romsJson = cachedRoms != null ? new JSONObject(cachedRoms) : null;
          JSONObject osJson = cachedOs != null ? new JSONObject(cachedOs) : null;
          applyData(romsJson, osJson);
          return;
        } catch (Exception e) {
          Log.w(TAG, "Failed to parse cached JSON, re-fetching", e);
        }
      }
    }

    mExecutor.execute(
        () -> {
          JSONObject blissRoms = fetchJson(API_BLISSROMS);
          JSONObject blissOs = fetchJson(API_BLISSOS);

          SharedPreferences.Editor editor = prefs.edit();
          boolean gotAny = false;
          if (blissRoms != null) {
            editor.putString(KEY_CACHE_BLISSROMS, blissRoms.toString());
            gotAny = true;
          }
          if (blissOs != null) {
            editor.putString(KEY_CACHE_BLISSOS, blissOs.toString());
            gotAny = true;
          }
          if (gotAny) {
            editor.putLong(KEY_CACHE_TIMESTAMP, System.currentTimeMillis());
            editor.apply();
          }

          final JSONObject finalRoms = blissRoms;
          final JSONObject finalOs = blissOs;
          mHandler.post(
              () -> {
                if (!isAdded()) return;
                applyData(finalRoms, finalOs);
              });
        });
  }

  private void applyData(JSONObject blissRoms, JSONObject blissOs) {
    JSONObject roms = projectData(blissRoms);
    JSONObject os = projectData(blissOs);
    if (roms == null && os == null) return;
    PreferenceScreen screen = getPreferenceScreen();
    screen.removeAll();
    if (roms != null) addProjectSection(screen, roms, "blissroms");
    else addStaticBlissRomsSection(screen);
    if (os != null) addProjectSection(screen, os, "blissos");
    else addStaticBlissOsSection(screen);
  }

  private JSONObject projectData(JSONObject response) {
    try {
      if (response != null && response.optBoolean("success", false)) {
        return response.getJSONObject("data").getJSONObject("project");
      }
    } catch (Exception e) {
      Log.w(TAG, "Failed to parse project data", e);
    }
    return null;
  }

  private void addProjectSection(PreferenceScreen screen, JSONObject project, String keyPrefix) {
    final Context ctx = getContext();
    if (ctx == null) return;

    try {
      String title = project.optString("title", keyPrefix);
      String description = project.optString("description", "");

      PreferenceCategory category = new PreferenceCategory(ctx);
      category.setKey("about_" + keyPrefix + "_category");
      category.setTitle(title);
      category.setLayoutResource(R.layout.blissify_category_header);
      screen.addPreference(category);

      if (!description.isEmpty()) {
        Preference descPref = new Preference(ctx);
        descPref.setKey("about_" + keyPrefix + "_desc");
        descPref.setSummary(description);
        descPref.setSelectable(false);
        category.addPreference(descPref);
      }

      JSONArray members = project.optJSONArray("team_members");
      if (members != null) {
        for (int i = 0; i < members.length(); i++) {
          JSONObject member = members.getJSONObject(i);
          String name = member.optString("name", "");
          String designation = getPrimaryDesignation(member);

          Preference memberPref = new Preference(ctx);
          memberPref.setKey("about_" + keyPrefix + "_member_" + i);
          memberPref.setTitle(name);
          if (!designation.isEmpty()) {
            memberPref.setSummary(designation);
          }
          memberPref.setSelectable(false);
          category.addPreference(memberPref);
        }
      }

      JSONArray links = project.optJSONArray("links");
      if (links != null && links.length() > 0) {
        PreferenceCategory linksCategory = new PreferenceCategory(ctx);
        linksCategory.setKey("about_" + keyPrefix + "_links_category");
        linksCategory.setTitle(title + " — " + getString(R.string.about_links_category_title));
        linksCategory.setLayoutResource(R.layout.blissify_category_header);
        screen.addPreference(linksCategory);

        for (int i = 0; i < links.length(); i++) {
          JSONObject link = links.getJSONObject(i);
          String linkName = link.optString("name", "");
          String linkUrl = link.optString("url", "");
          if (linkName.isEmpty() || linkUrl.isEmpty()) continue;

          Preference linkPref = new Preference(ctx);
          linkPref.setKey("about_" + keyPrefix + "_link_" + i);
          linkPref.setTitle(linkName);
          linkPref.setSummary(linkUrl);
          final String finalUrl = linkUrl;
          linkPref.setOnPreferenceClickListener(
              p -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)));
                return true;
              });
          linksCategory.addPreference(linkPref);
        }
      }

    } catch (Exception e) {
      Log.e(TAG, "Error building section for " + keyPrefix, e);
    }
  }

  private String getPrimaryDesignation(JSONObject member) {
    try {
      JSONArray designations = member.optJSONArray("designations");
      if (designations != null && designations.length() > 0) {
        String primary = null;
        for (int i = 0; i < designations.length(); i++) {
          JSONObject d = designations.getJSONObject(i);
          String color = d.optString("color", "");
          String name = d.optString("name", "");
          if ("#DC2626".equalsIgnoreCase(color)) {
            return name;
          }
          if (primary == null) primary = name;
        }
        return primary != null ? primary : "";
      }
    } catch (Exception e) {
      Log.w(TAG, "Failed to parse designation", e);
    }
    return "";
  }

  private void addStaticBlissRomsSection(PreferenceScreen screen) {
    final Context ctx = getContext();
    if (ctx == null) return;

    PreferenceCategory cat = new PreferenceCategory(ctx);
    cat.setKey("about_blissroms_category");
    cat.setTitle(getString(R.string.about_blissroms_category_title));
    cat.setLayoutResource(R.layout.blissify_category_header);
    screen.addPreference(cat);

    addStaticMember(cat, "about_jackeagle", "Jackeagle", "BlissRoms Project Founder");
    addStaticMember(cat, "about_studiokeys", "StudioKeys", "Lead Developer");
    addStaticMember(cat, "about_aryan", "Aryan Arora", "Social Media Manager");
    addStaticMember(cat, "about_hmtheboy154", "HMTheBoy154", "Android Platform Developer");
    addStaticMember(cat, "about_nimueh", "Nimueh Lady of the lake", "Community Moderator");
    addStaticMember(cat, "about_shripal", "Shripal Jain", "Android App Developer");
    addStaticMember(cat, "about_jonwest", "Jon West", "Android Platform Developer");

    PreferenceCategory links = new PreferenceCategory(ctx);
    links.setKey("about_blissroms_links_category");
    links.setTitle(
        getString(R.string.about_blissroms_category_title)
            + " — "
            + getString(R.string.about_links_category_title));
    links.setLayoutResource(R.layout.blissify_category_header);
    screen.addPreference(links);

    addStaticLink(
        links,
        "about_blissroms_website",
        getString(R.string.about_blissroms_website_title),
        "https://blissroms.org");
    addStaticLink(links, "about_blissroms_github", "GitHub", "https://github.com/BlissRoms");
    addStaticLink(links, "about_blissroms_twitter", "Twitter", "https://twitter.com/bliss_roms");
    addStaticLink(
        links, "about_blissroms_instagram", "Instagram", "https://www.instagram.com/blissroms");
    addStaticLink(
        links, "about_blissroms_facebook", "Facebook", "https://www.facebook.com/BlissROMs");
    addStaticLink(links, "about_blissroms_blog", "Blog", "https://blog.blissroms.org");
    addStaticLink(
        links,
        "about_blissroms_telegram",
        "Telegram Community",
        "https://t.me/Team_Bliss_Community");
    addStaticLink(
        links,
        "about_blissroms_telegram_channel",
        "Telegram Channel",
        "https://t.me/BlissROM_Updates");
    addStaticLink(
        links, "about_blissroms_mastodon", "Mastodon", "https://mastodon.social/@blissroms");
    addStaticLink(
        links,
        "about_blissroms_opencollective",
        "OpenCollective",
        "https://opencollective.com/blissroms");
    addStaticLink(
        links, "about_blissroms_bsky", "Bsky", "https://bsky.app/profile/blissroms.bsky.social");
  }

  private void addStaticBlissOsSection(PreferenceScreen screen) {
    final Context ctx = getContext();
    if (ctx == null) return;

    PreferenceCategory cat = new PreferenceCategory(ctx);
    cat.setKey("about_blissos_category");
    cat.setTitle(getString(R.string.about_blissos_category_title));
    cat.setLayoutResource(R.layout.blissify_category_header);
    screen.addPreference(cat);

    addStaticMember(cat, "about_jonwest", "Jon West", "BlissOS Project Co-Founder");
    addStaticMember(cat, "about_huyminh", "HMTheBoy154", "BlissOS Project Manager");
    addStaticMember(cat, "about_blissos_jackeagle", "Jackeagle", "Android Platform Developer");

    PreferenceCategory links = new PreferenceCategory(ctx);
    links.setKey("about_blissos_links_category");
    links.setTitle(
        getString(R.string.about_blissos_category_title)
            + " — "
            + getString(R.string.about_links_category_title));
    links.setLayoutResource(R.layout.blissify_category_header);
    screen.addPreference(links);

    addStaticLink(
        links,
        "about_blissos_website",
        getString(R.string.about_blissos_website_title),
        "https://blissos.org");
    addStaticLink(
        links, "about_blissos_instagram", "Instagram", "https://instagram.com/blissos_org");
    addStaticLink(links, "about_blissos_twitter", "Twitter", "https://twitter.com/blissos_org");
    addStaticLink(links, "about_blissos_mastodon", "Mastodon", "https://mastodon.social/@blissos");
    addStaticLink(
        links, "about_blissos_bsky", "Bsky", "https://bsky.app/profile/blissos-org.bsky.social");
    addStaticLink(links, "about_blissos_telegram", "Telegram", "https://t.me/blissx86");
    addStaticLink(
        links, "about_blissos_discord", "Discord", "https://discord.com/invite/F9n5gbdNy2");
    addStaticLink(links, "about_blissos_github", "GitHub", "https://github.com/BlissOS");
    addStaticLink(
        links, "about_blissos_matrix", "Matrix", "https://matrix.to/#/#blissos:matrix.org");
  }

  private void addStaticMember(PreferenceCategory cat, String key, String name, String role) {
    final Context ctx = getContext();
    if (ctx == null) return;
    Preference p = new Preference(ctx);
    p.setKey(key);
    p.setTitle(name);
    p.setSummary(role);
    p.setSelectable(false);
    cat.addPreference(p);
  }

  private void addStaticLink(PreferenceCategory cat, String key, String name, String url) {
    final Context ctx = getContext();
    if (ctx == null) return;
    Preference p = new Preference(ctx);
    p.setKey(key);
    p.setTitle(name);
    p.setSummary(url);
    p.setOnPreferenceClickListener(
        pref -> {
          startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
          return true;
        });
    cat.addPreference(p);
  }

  private JSONObject fetchJson(String urlString) {
    HttpURLConnection conn = null;
    try {
      conn = (HttpURLConnection) new URL(urlString).openConnection();
      conn.setConnectTimeout(3000);
      conn.setReadTimeout(3000);
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Accept", "application/json");

      if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
        return null;
      }

      BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
      reader.close();
      return new JSONObject(sb.toString());
    } catch (Exception e) {
      Log.w(TAG, "Failed to fetch " + urlString + ": " + e.getMessage());
      return null;
    } finally {
      if (conn != null) conn.disconnect();
    }
  }

  @Override
  public int getMetricsCategory() {
    return MetricsEvent.BLISSIFY;
  }

  /** For Search. */
  public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
      new BaseSearchIndexProvider(R.xml.blissify_about);
}
