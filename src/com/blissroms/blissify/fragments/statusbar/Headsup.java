package com.blissroms.blissify.fragments.statusbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.ListPreference;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.format.DateFormat;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.EditText;
import com.blissroms.blissify.preference.SystemSettingSwitchPreference;
import com.blissroms.blissify.preference.PackageListAdapter;
import com.blissroms.blissify.preference.PackageListAdapter.PackageItem;
import android.provider.Settings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.blissroms.blissify.preference.SystemSettingSwitchPreference;
import com.android.internal.logging.nano.MetricsProto;

public class Headsup extends SettingsPreferenceFragment
        implements Preference.OnPreferenceClickListener, OnPreferenceChangeListener{

        private static final int DIALOG_BLACKLIST_APPS = 0;

        private PackageListAdapter mPackageAdapter;
        private PackageManager mPackageManager;
        private PreferenceGroup mBlacklistPrefList;
        private Preference mAddBlacklistPref;

        private String mBlacklistPackageList;
        private Map<String, Package> mBlacklistPackages;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.statusbar_headsup);
            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            Resources systemUiResources;
            try {
                systemUiResources = getPackageManager().getResourcesForApplication("com.android.systemui");
            } catch (Exception e) {
                return;
            }

            // Blacklist
            mPackageManager = getPackageManager();
            mPackageAdapter = new PackageListAdapter(getActivity());

            mBlacklistPrefList = (PreferenceGroup) findPreference("blacklist_applications");
            mBlacklistPrefList.setOrderingAsAdded(false);
            mBlacklistPackages = new HashMap<String, Package>();

            mAddBlacklistPref = findPreference("add_blacklist_packages");
            mAddBlacklistPref.setOnPreferenceClickListener(this);

        }

        @Override
        public void onResume() {
            super.onResume();
            refreshCustomApplicationPrefs();
        }

        public int getDialogMetricsCategory(int dialogId) {
            if (dialogId == DIALOG_BLACKLIST_APPS ) {
                return MetricsProto.MetricsEvent.BLISSIFY;
            }
            return 0;
        }

        public Dialog onCreateDialog(int id) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final Dialog dialog;
            final ListView list = new ListView(getActivity());
            list.setAdapter(mPackageAdapter);

            builder.setTitle(R.string.profile_choose_app);
            builder.setView(list);
            dialog = builder.create();

            switch (id) {
                case DIALOG_BLACKLIST_APPS:
                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent,
                                View view, int position, long id) {
                            PackageItem info = (PackageItem) parent.getItemAtPosition(position);
                            addCustomApplicationPref(info.packageName, mBlacklistPackages);
                            dialog.cancel();
                        }
                    });
            }
            return dialog;
        }

        /**
         * Application class
         */
        private static class Package {
            public String name;
            /**
             * Stores all the application values in one call
             * @param name
             */
            public Package(String name) {
                this.name = name;
            }

            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append(name);
                return builder.toString();
            }

            public static Package fromString(String value) {
                if (TextUtils.isEmpty(value)) {
                    return null;
                }

                try {
                    Package item = new Package(value);
                    return item;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        };

        private void refreshCustomApplicationPrefs() {
            if (!parsePackageList()) {
                return;
            }

            // Add the Application Preferences
            if (mBlacklistPrefList != null) {
                mBlacklistPrefList.removeAll();

                for (Package pkg : mBlacklistPackages.values()) {
                    try {
                        Preference pref = createPreferenceFromInfo(pkg);
                        mBlacklistPrefList.addPreference(pref);
                    } catch (PackageManager.NameNotFoundException e) {
                        // Do nothing
                    }
                }
            }

            // Keep these at the top
            mAddBlacklistPref.setOrder(0);
            // Add 'add' options
            mBlacklistPrefList.addPreference(mAddBlacklistPref);
        }

        public boolean onPreferenceClick(Preference preference) {
            if (preference == mAddBlacklistPref) {
                showDialog(DIALOG_BLACKLIST_APPS);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.heads_up_blacklist_dialog_delete_title)
                        .setMessage(R.string.heads_up_blacklist_dialog_delete_message)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (preference == mBlacklistPrefList.findPreference(preference.getKey())) {
                                     removeApplicationPref(preference.getKey(), mBlacklistPackages);
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null);

            builder.show();
            }
            return true;
        }

         private void addCustomApplicationPref(String packageName, Map<String,Package> map) {
            Package pkg = map.get(packageName);
            if (pkg == null) {
                pkg = new Package(packageName);
                map.put(packageName, pkg);
                savePackageList(false);
                refreshCustomApplicationPrefs();
            }
        }

        private Preference createPreferenceFromInfo(Package pkg)
                throws PackageManager.NameNotFoundException {
            PackageInfo info = mPackageManager.getPackageInfo(pkg.name,
                    PackageManager.GET_META_DATA);
            Preference pref =
                    new Preference(getActivity());

            pref.setKey(pkg.name);
            pref.setTitle(info.applicationInfo.loadLabel(mPackageManager));
            pref.setIcon(info.applicationInfo.loadIcon(mPackageManager));
            pref.setPersistent(false);
            pref.setOnPreferenceClickListener(this);
            return pref;
        }

        private void removeApplicationPref(String packageName, Map<String,Package> map) {
            if (map.remove(packageName) != null) {
                savePackageList(false);
                refreshCustomApplicationPrefs();
           }
        }

        private boolean parsePackageList() {
            ContentResolver resolver = getActivity().getContentResolver();
            boolean parsed = false;

            final String blacklistString = Settings.System.getString(resolver,
                    Settings.System.HEADS_UP_BLACKLIST_VALUES);

            if (!TextUtils.equals(mBlacklistPackageList, blacklistString)) {
                mBlacklistPackageList = blacklistString;
                mBlacklistPackages.clear();
                parseAndAddToMap(blacklistString, mBlacklistPackages);
                parsed = true;
            }

            return parsed;
        }

        private void parseAndAddToMap(String baseString, Map<String,Package> map) {
            if (baseString == null) {
                return;
            }

            final String[] array = TextUtils.split(baseString, "\\|");
            for (String item : array) {
                if (TextUtils.isEmpty(item)) {
                    continue;
                }
                Package pkg = Package.fromString(item);
                map.put(pkg.name, pkg);
             }
         }

         private void savePackageList(boolean preferencesUpdated) {
            ContentResolver resolver = getActivity().getContentResolver();
            List<String> settings = new ArrayList<String>();
            for (Package app : mBlacklistPackages.values()) {
                settings.add(app.toString());
            }
            final String value = TextUtils.join("|", settings);
            if (preferencesUpdated) {
               mBlacklistPackageList = value;
            }
            Settings.System.putString(resolver,
                    Settings.System.HEADS_UP_BLACKLIST_VALUES, value);
        }


        public int getMetricsCategory() {
            return MetricsProto.MetricsEvent.BLISSIFY;
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            return false;
        }
}
