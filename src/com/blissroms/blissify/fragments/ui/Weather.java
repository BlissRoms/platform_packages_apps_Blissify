package com.blissroms.blissify.fragments.ui;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.database.ContentObserver;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.bliss.support.preferences.SystemSettingSwitchPreference;
import android.provider.Settings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.omni.PackageUtils;

public class Weather extends SettingsPreferenceFragment
                                         implements Preference.OnPreferenceChangeListener{

        private static final String TAG = "OmniJawsSettings";
        private static final String CATEGORY_WEATHER = "weather_category";
        private static final String WEATHER_ICON_PACK = "weather_icon_pack";
        private static final String DEFAULT_WEATHER_ICON_PACKAGE = "org.omnirom.omnijaws";
        private static final String DEFAULT_WEATHER_ICON_PREFIX = "outline";
        private static final String WEATHER_SERVICE_PACKAGE = "org.omnirom.omnijaws";
        private static final String CHRONUS_ICON_PACK_INTENT = "com.dvtonder.chronus.ICON_PACK";

        private PreferenceCategory mWeatherCategory;
        private ListPreference mWeatherIconPack;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.interface_weather);
            PreferenceScreen prefScreen = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mWeatherCategory = (PreferenceCategory) prefScreen.findPreference(CATEGORY_WEATHER);
            if (mWeatherCategory != null && !isOmniJawsServiceInstalled()) {
                    prefScreen.removePreference(mWeatherCategory);
            } else {
                String settingHeaderPackage = Settings.System.getString(resolver,
                        Settings.System.OMNIJAWS_WEATHER_ICON_PACK);
                if (settingHeaderPackage == null) {
                    settingHeaderPackage = DEFAULT_WEATHER_ICON_PACKAGE + "." + DEFAULT_WEATHER_ICON_PREFIX;
                }
                mWeatherIconPack = (ListPreference) findPreference(WEATHER_ICON_PACK);

                List<String> entries = new ArrayList<String>();
                List<String> values = new ArrayList<String>();
                getAvailableWeatherIconPacks(entries, values);
                mWeatherIconPack.setEntries(entries.toArray(new String[entries.size()]));
                mWeatherIconPack.setEntryValues(values.toArray(new String[values.size()]));

                int valueIndex = mWeatherIconPack.findIndexOfValue(settingHeaderPackage);
                if (valueIndex == -1) {
                    // no longer found
                    settingHeaderPackage = DEFAULT_WEATHER_ICON_PACKAGE + "." + DEFAULT_WEATHER_ICON_PREFIX;
                    Settings.System.putString(resolver,
                            Settings.System.OMNIJAWS_WEATHER_ICON_PACK, settingHeaderPackage);
                    valueIndex = mWeatherIconPack.findIndexOfValue(settingHeaderPackage);
                }
                mWeatherIconPack.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
                mWeatherIconPack.setSummary(mWeatherIconPack.getEntry());
                mWeatherIconPack.setOnPreferenceChangeListener(this);
            }
        }

        public boolean onPreferenceChange(Preference preference, Object objValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mWeatherIconPack) {
                String value = (String) objValue;
                Settings.System.putString(resolver,
                        Settings.System.OMNIJAWS_WEATHER_ICON_PACK, value);
                int valueIndex = mWeatherIconPack.findIndexOfValue(value);
                mWeatherIconPack.setSummary(mWeatherIconPack.getEntries()[valueIndex]);
            }
            return true;
        }

        private boolean isOmniJawsServiceInstalled() {
            return PackageUtils.isAvailableApp(WEATHER_SERVICE_PACKAGE, getActivity());
        }

        private void getAvailableWeatherIconPacks(List<String> entries, List<String> values) {
            Intent i = new Intent();
            PackageManager packageManager = getActivity().getPackageManager();
            i.setAction("org.omnirom.WeatherIconPack");
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                String packageName = r.activityInfo.packageName;
                if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                    values.add(0, r.activityInfo.name);
                } else {
                    values.add(r.activityInfo.name);
                }
                String label = r.activityInfo.loadLabel(packageManager).toString();
                if (label == null) {
                    label = r.activityInfo.packageName;
                }
                if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                    entries.add(0, label);
                } else {
                    entries.add(label);
                }
            }
            i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(CHRONUS_ICON_PACK_INTENT);
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                String packageName = r.activityInfo.packageName;
                values.add(packageName + ".weather");
                String label = r.activityInfo.loadLabel(packageManager).toString();
                if (label == null) {
                    label = r.activityInfo.packageName;
                }
                entries.add(label);
            }
        }

        private boolean isOmniJawsEnabled() {
            ContentResolver resolver = getActivity().getContentResolver();
            final Uri SETTINGS_URI
                = Uri.parse("content://org.omnirom.omnijaws.provider/settings");

            final String[] SETTINGS_PROJECTION = new String[] {
                "enabled"
            };

            final Cursor c = resolver.query(SETTINGS_URI, SETTINGS_PROJECTION,
                    null, null, null);
            if (c != null) {
                int count = c.getCount();
                if (count == 1) {
                    c.moveToPosition(0);
                    boolean enabled = c.getInt(0) == 1;
                    return enabled;
                }
            }
            return true;
         }

       @Override
         public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
       }
}
