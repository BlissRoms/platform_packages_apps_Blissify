package com.blissroms.blissify.fragments.ui;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
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
import android.text.TextUtils;
import com.blissroms.blissify.preference.SystemSettingSwitchPreference;
import android.provider.Settings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.blissroms.blissify.R;
import com.blissroms.blissify.preference.AppMultiSelectListPreference;
import com.blissroms.blissify.preference.ScrollAppsViewPreference;
import com.blissroms.blissify.preference.SystemSettingSwitchPreference;

public class Misc extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.default_view,container,false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.default_view, new Misc.SystemPreference())
                .commit();
        return view;
    }

    public static class SystemPreference extends PreferenceFragmentCompat
                                         implements Preference.OnPreferenceChangeListener{

        public SystemPreference() {
        }

        private static final String TAG = "Misc";
        private static final String KEY_ASPECT_RATIO_APPS_ENABLED = "aspect_ratio_apps_enabled";
        private static final String KEY_ASPECT_RATIO_APPS_LIST = "aspect_ratio_apps_list";
        private static final String KEY_ASPECT_RATIO_CATEGORY = "aspect_ratio_category";
        private static final String KEY_ASPECT_RATIO_APPS_LIST_SCROLLER = "aspect_ratio_apps_list_scroller";

        private AppMultiSelectListPreference mAspectRatioAppsSelect;
        private ScrollAppsViewPreference mAspectRatioApps;


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.interface_misc);
            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            final PreferenceCategory aspectRatioCategory =
                    (PreferenceCategory) getPreferenceScreen().findPreference(KEY_ASPECT_RATIO_CATEGORY);
            final boolean supportMaxAspectRatio = getResources().getBoolean(com.android.internal.R.bool.config_haveHigherAspectRatioScreen);
            if (!supportMaxAspectRatio) {
                getPreferenceScreen().removePreference(aspectRatioCategory);
            } else {
                mAspectRatioAppsSelect = (AppMultiSelectListPreference) findPreference(KEY_ASPECT_RATIO_APPS_LIST);
                mAspectRatioApps = (ScrollAppsViewPreference) findPreference(KEY_ASPECT_RATIO_APPS_LIST_SCROLLER);
                final String valuesString = Settings.System.getString(resolver, Settings.System.OMNI_ASPECT_RATIO_APPS_LIST);
                List<String> valuesList = new ArrayList<String>();
                if (!TextUtils.isEmpty(valuesString)) {
                    valuesList.addAll(Arrays.asList(valuesString.split(":")));
                    mAspectRatioApps.setVisible(true);
                    mAspectRatioApps.setValues(valuesList);
                } else {
                    mAspectRatioApps.setVisible(false);
                }
                mAspectRatioAppsSelect.setValues(valuesList);
                mAspectRatioAppsSelect.setOnPreferenceChangeListener(this);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mAspectRatioAppsSelect) {
                Collection<String> valueList = (Collection<String>) newValue;
                mAspectRatioApps.setVisible(false);
                if (valueList != null) {
                    Settings.System.putString(resolver, Settings.System.OMNI_ASPECT_RATIO_APPS_LIST,
                            TextUtils.join(":", valueList));
                    mAspectRatioApps.setVisible(true);
                    mAspectRatioApps.setValues(valueList);
                } else {
                    Settings.System.putString(resolver, Settings.System.OMNI_ASPECT_RATIO_APPS_LIST, "");
                }
                return true;
            }
            return false;
         }
     }
}
