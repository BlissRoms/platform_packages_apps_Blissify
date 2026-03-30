/*
 * Copyright (C) 2014-2026 The BlissRoms Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.blissroms.blissify.fragments.notifications;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;

import org.blissroms.blissify.fragments.BlissifyFragment;
import org.blissroms.blissify.preferences.PackageListAdapter;
import org.blissroms.blissify.preferences.PackageListAdapter.PackageItem;

public class NotificationSchedule extends BlissifyFragment
        implements Preference.OnPreferenceClickListener {

    private static final int DIALOG_APP_PICKER = 1;
    private static final String KEY_CONFIG = "bliss_notification_schedule_config";
    private static final String KEY_START_TIME = "schedule_start_time";
    private static final String KEY_END_TIME = "schedule_end_time";
    private static final String KEY_ADD_PACKAGES = "add_schedule_packages";
    private static final String KEY_APPLICATIONS = "schedule_applications";

    private PackageListAdapter mPackageAdapter;
    private PackageManager mPackageManager;
    private PreferenceGroup mAppListGroup;
    private Preference mAddAppPref;
    private Preference mStartTimePref;
    private Preference mEndTimePref;

    private int mStartHour = 22;
    private int mStartMinute = 0;
    private int mEndHour = 8;
    private int mEndMinute = 0;
    private Set<String> mPackages = new LinkedHashSet<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.blissify_notification_schedule);

        mPackageManager = getPackageManager();
        mPackageAdapter = new PackageListAdapter(getActivity());

        mStartTimePref = findPreference(KEY_START_TIME);
        mEndTimePref = findPreference(KEY_END_TIME);
        mAddAppPref = findPreference(KEY_ADD_PACKAGES);
        mAppListGroup = findPreference(KEY_APPLICATIONS);
        mAppListGroup.setOrderingAsAdded(false);

        mStartTimePref.setOnPreferenceClickListener(this);
        mEndTimePref.setOnPreferenceClickListener(this);
        mAddAppPref.setOnPreferenceClickListener(this);

        loadConfig();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAppList();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
    }

    @Override
    public int getDialogMetricsCategory(int dialogId) {
        return MetricsProto.MetricsEvent.BLISSIFY;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mStartTimePref) {
            showTimePicker(mStartHour, mStartMinute, (hour, minute) -> {
                mStartHour = hour;
                mStartMinute = minute;
                saveConfig();
                updateTimeSummaries();
            });
        } else if (preference == mEndTimePref) {
            showTimePicker(mEndHour, mEndMinute, (hour, minute) -> {
                mEndHour = hour;
                mEndMinute = minute;
                saveConfig();
                updateTimeSummaries();
            });
        } else if (preference == mAddAppPref) {
            showDialog(DIALOG_APP_PICKER);
        } else {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.delete)
                    .setMessage(R.string.delete_message)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        mPackages.remove(preference.getKey());
                        saveConfig();
                        refreshAppList();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
        return true;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        if (id != DIALOG_APP_PICKER) return null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        ListView list = new ListView(getActivity());
        list.setAdapter(mPackageAdapter);
        list.setDivider(null);
        builder.setTitle(R.string.choose_app);
        builder.setView(list);
        Dialog dialog = builder.create();
        list.setOnItemClickListener((parent, view, position, itemId) -> {
            PackageItem info = (PackageItem) parent.getItemAtPosition(position);
            if (!mPackages.contains(info.packageName)) {
                mPackages.add(info.packageName);
                saveConfig();
                refreshAppList();
            }
            dialog.cancel();
        });
        return dialog;
    }

    private void showTimePicker(int hour, int minute, TimeSetCallback callback) {
        boolean is24h = DateFormat.is24HourFormat(getActivity());
        new TimePickerDialog(getActivity(), (view, h, m) -> callback.onTimeSet(h, m),
                hour, minute, is24h).show();
    }

    private void loadConfig() {
        try {
            String json = Settings.Secure.getStringForUser(
                    getContentResolver(), KEY_CONFIG, UserHandle.USER_CURRENT);
            if (json != null) {
                JSONObject config = new JSONObject(json);
                mStartHour = config.optInt("startHour", 22);
                mStartMinute = config.optInt("startMinute", 0);
                mEndHour = config.optInt("endHour", 8);
                mEndMinute = config.optInt("endMinute", 0);
                JSONArray pkgArray = config.optJSONArray("packages");
                if (pkgArray != null) {
                    for (int i = 0; i < pkgArray.length(); i++) {
                        mPackages.add(pkgArray.getString(i));
                    }
                }
            }
        } catch (Exception e) {
            // use defaults
        }
        updateTimeSummaries();
    }

    private void saveConfig() {
        try {
            JSONObject config = new JSONObject();
            config.put("startHour", mStartHour);
            config.put("startMinute", mStartMinute);
            config.put("endHour", mEndHour);
            config.put("endMinute", mEndMinute);
            config.put("packages", new JSONArray(mPackages));
            Settings.Secure.putStringForUser(
                    getContentResolver(), KEY_CONFIG, config.toString(),
                    UserHandle.USER_CURRENT);
        } catch (Exception e) {
            // ignore
        }
    }

    private void updateTimeSummaries() {
        mStartTimePref.setSummary(formatTime(mStartHour, mStartMinute));
        mEndTimePref.setSummary(formatTime(mEndHour, mEndMinute));
    }

    private String formatTime(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        return DateFormat.getTimeFormat(getActivity()).format(cal.getTime());
    }

    private void refreshAppList() {
        if (mAppListGroup == null) return;
        mAppListGroup.removeAll();
        mAddAppPref.setOrder(0);
        mAppListGroup.addPreference(mAddAppPref);

        boolean removed = false;
        for (java.util.Iterator<String> it = mPackages.iterator(); it.hasNext(); ) {
            String pkg = it.next();
            try {
                PackageInfo info = mPackageManager.getPackageInfo(pkg, PackageManager.GET_META_DATA);
                Preference pref = new Preference(getActivity());
                pref.setKey(pkg);
                pref.setTitle(info.applicationInfo.loadLabel(mPackageManager));
                pref.setIcon(info.applicationInfo.loadIcon(mPackageManager));
                pref.setPersistent(false);
                pref.setOnPreferenceClickListener(this);
                mAppListGroup.addPreference(pref);
            } catch (PackageManager.NameNotFoundException e) {
                it.remove();
                removed = true;
            }
        }
        if (removed) saveConfig();
    }

    private interface TimeSetCallback {
        void onTimeSet(int hour, int minute);
    }
}
