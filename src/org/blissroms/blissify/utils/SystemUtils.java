/*
 * Copyright (C) 2025 crDroid Android Project
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
package org.blissroms.blissify.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.IActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.Gravity;
import com.android.settings.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class SystemUtils {

  public static void showSystemUiRestartDialog(final Context context) {
    new AlertDialog.Builder(context)
        .setTitle(R.string.systemui_restart_title)
        .setMessage(R.string.systemui_restart_message)
        .setPositiveButton(
            R.string.systemui_restart_yes,
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                new AsyncTask<Void, Void, Void>() {
                  @Override
                  protected Void doInBackground(Void... params) {
                    try {
                      ActivityManager am =
                          (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                      IActivityManager ams = ActivityManager.getService();
                      for (ActivityManager.RunningAppProcessInfo app :
                          am.getRunningAppProcesses()) {
                        if ("com.android.systemui".equals(app.processName)) {
                          ams.killApplicationProcess(app.processName, app.uid);
                          break;
                        }
                      }
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                    return null;
                  }
                }.execute();
              }
            })
        .setNegativeButton(R.string.systemui_restart_not_now, null)
        .show();
  }

  public static void centerToolbarTitle(Activity activity) {
    if (activity == null) return;
    CollapsingToolbarLayout collapsingToolbar =
        activity.findViewById(com.android.settingslib.collapsingtoolbar.R.id.collapsing_toolbar);
    if (collapsingToolbar != null) {
      collapsingToolbar.setExpandedTitleGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
      collapsingToolbar.setCollapsedTitleGravity(Gravity.CENTER_HORIZONTAL);
    }
  }
}
