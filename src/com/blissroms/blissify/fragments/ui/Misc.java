package com.blissroms.blissify.fragments.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
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
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.EditText;
import android.text.TextUtils;
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
import com.android.internal.logging.nano.MetricsProto;
import com.bliss.support.preferences.PackageListAdapter;
import com.bliss.support.preferences.PackageListAdapter.PackageItem;
import com.bliss.support.preferences.SystemSettingSwitchPreference;
import com.bliss.support.preferences.SystemSettingSeekBarPreference;

public class Misc extends SettingsPreferenceFragment
        implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener{


    private static final String TAG = "Misc";
    private static final String SYSUI_ROUNDED_CONTENT_PADDING = "sysui_rounded_content_padding";
    private static final String SYSUI_ROUNDED_SIZE = "sysui_rounded_size";
    private static final String DEVICE_CATEGORY = "device_extras_category";

    private SystemSettingSeekBarPreference mContentPadding;
    private SystemSettingSeekBarPreference mCornerRadius;

    private static final int DIALOG_LEGACYLIST_APPS = 0;

    private PackageListAdapter mPackageAdapter;
    private PackageManager mPackageManager;
    private PreferenceGroup mLegacylistPrefList;
    private Preference mAddLegacylistPref;

    private String mLegacylistPackageList;
    private Map<String, Package> mLegacylistPackages;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.interface_misc);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        Resources systemUiResources;
        try {
            systemUiResources = getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (Exception e) {
            return;
        }

        mContentPadding = (SystemSettingSeekBarPreference) findPreference(SYSUI_ROUNDED_CONTENT_PADDING);
        int contentPadding = Settings.Secure.getInt(resolver,
                Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING, 1);
                mContentPadding.setValue(contentPadding / 1);
                mContentPadding.setOnPreferenceChangeListener(this);

        mCornerRadius = (SystemSettingSeekBarPreference) findPreference(SYSUI_ROUNDED_SIZE);
        int cornerRadius = Settings.Secure.getInt(resolver,
                Settings.Secure.SYSUI_ROUNDED_SIZE, 1);
                mCornerRadius.setValue(cornerRadius / 1);
                mCornerRadius.setOnPreferenceChangeListener(this);

        Preference DeviceExtras = findPreference(DEVICE_CATEGORY);
        if (!getResources().getBoolean(R.bool.has_device_extras)) {
            getPreferenceScreen().removePreference(DeviceExtras);
        }

         // Legacylist
        mPackageManager = getPackageManager();
        mPackageAdapter = new PackageListAdapter(getActivity());

        mLegacylistPrefList = (PreferenceGroup) findPreference("legacylist_applications");
        mLegacylistPackages = new HashMap<String, Package>();
        mAddLegacylistPref = findPreference("add_legacylist_packages");
        mAddLegacylistPref.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mContentPadding) {
            int value = (Integer) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING, value * 1);
            return true;
        } else if (preference == mCornerRadius) {
            int value = (Integer) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.SYSUI_ROUNDED_SIZE, value * 1);
            return true;
        }
        return false;
     }

    @Override
    public void onResume() {
        super.onResume();
        refreshCustomApplicationPrefs();
    }

   @Override
     public int getMetricsCategory() {
    return MetricsProto.MetricsEvent.BLISSIFY;
   }

    @Override
    public int getDialogMetricsCategory(int dialogId) {
        if (dialogId == DIALOG_LEGACYLIST_APPS ) {
            return MetricsProto.MetricsEvent.BLISSIFY;
        }
        return 0;
    }

    /**
     * Utility classes and supporting methods
     */
    @Override
    public Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Dialog dialog;
        final ListView list = new ListView(getActivity());
        list.setAdapter(mPackageAdapter);

        builder.setTitle(R.string.profile_choose_app);
        builder.setView(list);
        dialog = builder.create();

        switch (id) {
            case DIALOG_LEGACYLIST_APPS:
                list.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Add empty application definition, the user will be able to edit it later
                        PackageItem info = (PackageItem) parent.getItemAtPosition(position);
                        addCustomApplicationPref(info.packageName, mLegacylistPackages);
                        dialog.cancel();
                    }
                });
                break;
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
        if (mLegacylistPrefList != null) {
            mLegacylistPrefList.removeAll();

            for (Package pkg : mLegacylistPackages.values()) {
                try {
                    Preference pref = createPreferenceFromInfo(pkg);
                    mLegacylistPrefList.addPreference(pref);
                } catch (PackageManager.NameNotFoundException e) {
                    // Do nothing
                }
            }
        }

        // Keep these at the top
        mAddLegacylistPref.setOrder(0);
        // Add 'add' options
        mLegacylistPrefList.addPreference(mAddLegacylistPref);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mAddLegacylistPref) {
            showDialog(DIALOG_LEGACYLIST_APPS);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_delete_title)
                    .setMessage(R.string.dialog_delete_message)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (preference == mLegacylistPrefList.findPreference(preference.getKey())) {
                                removeApplicationPref(preference.getKey(), mLegacylistPackages);
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
        boolean parsed = false;

        final String legacylistString = Settings.System.getString(getContentResolver(),
                Settings.System.OMNI_ASPECT_RATIO_APPS_LIST);

        if (!TextUtils.equals(mLegacylistPackageList, legacylistString)) {
            mLegacylistPackageList = legacylistString;
            mLegacylistPackages.clear();
            parseAndAddToMap(legacylistString, mLegacylistPackages);
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
            for (Package app : mLegacylistPackages.values()) {
            settings.add(app.toString());
        }
        final String value = TextUtils.join("|", settings);
        if (preferencesUpdated) {
                mLegacylistPackageList = value;
        }
            Settings.System.putString(resolver,
                    Settings.System.OMNI_ASPECT_RATIO_APPS_LIST, value);
    }
}
