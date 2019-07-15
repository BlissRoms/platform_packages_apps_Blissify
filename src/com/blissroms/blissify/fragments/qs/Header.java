package com.blissroms.blissify.fragments.qs;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.bliss.support.preferences.SystemSettingSwitchPreference;
import com.bliss.support.preferences.CustomSeekBarPreference;
import com.android.internal.logging.nano.MetricsProto;

public class Header extends SettingsPreferenceFragment 
             implements Preference.OnPreferenceChangeListener{

    private static final String CUSTOM_HEADER_BROWSE = "custom_header_browse";
    private static final String DAYLIGHT_HEADER_PACK = "daylight_header_pack";
    private static final String CUSTOM_HEADER_IMAGE_SHADOW = "status_bar_custom_header_shadow";
    private static final String CUSTOM_HEADER_PROVIDER = "custom_header_provider";
    private static final String STATUS_BAR_CUSTOM_HEADER = "status_bar_custom_header";
    private static final String FILE_HEADER_SELECT = "file_header_select";

    private static final int REQUEST_PICK_IMAGE = 0;

    private Preference mHeaderBrowse;
    private ListPreference mDaylightHeaderPack;
    private CustomSeekBarPreference mHeaderShadow;
    private ListPreference mHeaderProvider;
    private String mDaylightHeaderProvider;
    private SystemSettingSwitchPreference mHeaderEnabled;
    private Preference mFileHeader;
    private String mFileHeaderProvider;

    @Override
    public void onResume() {
        super.onResume();
        updateEnablement();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.qs_header);
        ContentResolver resolver = getActivity().getContentResolver();

        mDaylightHeaderProvider = getResources().getString(R.string.daylight_header_provider);
        mFileHeaderProvider = getResources().getString(R.string.file_header_provider);
        mHeaderBrowse = findPreference(CUSTOM_HEADER_BROWSE);

        mHeaderEnabled = (SystemSettingSwitchPreference) findPreference(STATUS_BAR_CUSTOM_HEADER);
        mHeaderEnabled.setOnPreferenceChangeListener(this);

        mDaylightHeaderPack = (ListPreference) findPreference(DAYLIGHT_HEADER_PACK);

        List<String> entries = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        getAvailableHeaderPacks(entries, values);
        mDaylightHeaderPack.setEntries(entries.toArray(new String[entries.size()]));
        mDaylightHeaderPack.setEntryValues(values.toArray(new String[values.size()]));

        boolean headerEnabled = Settings.System.getInt(resolver,
                Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER, 0) != 0;
        updateHeaderProviderSummary(headerEnabled);
        mDaylightHeaderPack.setOnPreferenceChangeListener(this);

        mHeaderShadow = (CustomSeekBarPreference) findPreference(CUSTOM_HEADER_IMAGE_SHADOW);
        final int headerShadow = Settings.System.getInt(resolver,
                Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_SHADOW, 0);
        mHeaderShadow.setValue((int)(((double) headerShadow / 255) * 100));
        mHeaderShadow.setOnPreferenceChangeListener(this);

        mHeaderProvider = (ListPreference) findPreference(CUSTOM_HEADER_PROVIDER);
        mHeaderProvider.setOnPreferenceChangeListener(this);

        mFileHeader = findPreference(FILE_HEADER_SELECT);

        }

    private void updateHeaderProviderSummary(boolean headerEnabled) {
        mDaylightHeaderPack.setSummary(getResources().getString(R.string.header_provider_disabled));
        if (headerEnabled) {
            String settingHeaderPackage = Settings.System.getString(getContentResolver(),
                    Settings.System.OMNI_STATUS_BAR_DAYLIGHT_HEADER_PACK);
            if (settingHeaderPackage != null) {
                int valueIndex = mDaylightHeaderPack.findIndexOfValue(settingHeaderPackage);
                mDaylightHeaderPack.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
                mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntry());
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mFileHeader) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDaylightHeaderPack) {
            String value = (String) newValue;
            Settings.System.putString(getContentResolver(),
                    Settings.System.OMNI_STATUS_BAR_DAYLIGHT_HEADER_PACK, value);
            int valueIndex = mDaylightHeaderPack.findIndexOfValue(value);
            mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntries()[valueIndex]);
        } else if (preference == mHeaderShadow) {
            Integer headerShadow = (Integer) newValue;
            int realHeaderValue = (int) (((double) headerShadow / 100) * 255);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_SHADOW, realHeaderValue);
        } else if (preference == mHeaderProvider) {
            String value = (String) newValue;
            Settings.System.putString(getContentResolver(),
                    Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_PROVIDER, value);
            int valueIndex = mHeaderProvider.findIndexOfValue(value);
            mHeaderProvider.setSummary(mHeaderProvider.getEntries()[valueIndex]);
            updateEnablement();
        } else if (preference == mHeaderEnabled) {
            Boolean headerEnabled = (Boolean) newValue;
            updateHeaderProviderSummary(headerEnabled);
        }
        return true;
    }

    private boolean isBrowseHeaderAvailable() {
        PackageManager pm = getPackageManager();
        Intent browse = new Intent();
        browse.setClassName("org.omnirom.omnistyle", "org.omnirom.omnistyle.PickHeaderActivity");
        return pm.resolveActivity(browse, 0) != null;
    }

    private void getAvailableHeaderPacks(List<String> entries, List<String> values) {
        Map<String, String> headerMap = new HashMap<String, String>();
        Intent i = new Intent();
        PackageManager packageManager = getPackageManager();
        i.setAction("org.omnirom.DaylightHeaderPack");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            headerMap.put(label, packageName);
        }
        i.setAction("org.omnirom.DaylightHeaderPack1");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (r.activityInfo.name.endsWith(".theme")) {
                continue;
            }
            if (label == null) {
                label = packageName;
            }
            headerMap.put(label, packageName  + "/" + r.activityInfo.name);
        }
        List<String> labelList = new ArrayList<String>();
        labelList.addAll(headerMap.keySet());
        Collections.sort(labelList);
        for (String label : labelList) {
            entries.add(label);
            values.add(headerMap.get(label));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == REQUEST_PICK_IMAGE) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }
            final Uri imageUri = result.getData();
            Settings.System.putString(getContentResolver(), Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_PROVIDER, "file");
            Settings.System.putString(getContentResolver(), Settings.System.OMNI_STATUS_BAR_FILE_HEADER_IMAGE, imageUri.toString());
        }
    }

    private void updateEnablement() {
        String providerName = Settings.System.getString(getContentResolver(),
                Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_PROVIDER);
        if (providerName == null) {
            providerName = mDaylightHeaderProvider;
        }
        if (!providerName.equals(mDaylightHeaderProvider)) {
            providerName = mFileHeaderProvider;
        }
        int valueIndex = mHeaderProvider.findIndexOfValue(providerName);
        mHeaderProvider.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mHeaderProvider.setSummary(mHeaderProvider.getEntry());
        mDaylightHeaderPack.setEnabled(providerName.equals(mDaylightHeaderProvider));
        mFileHeader.setEnabled(providerName.equals(mFileHeaderProvider));
        mHeaderBrowse.setEnabled(isBrowseHeaderAvailable() && providerName.equals(mFileHeaderProvider));
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
    }
}
