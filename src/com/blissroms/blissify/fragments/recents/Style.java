package com.blissroms.blissify.fragments.recents;

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
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.blissroms.blissify.preference.SystemSettingSwitchPreference;
import android.provider.Settings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.omni.OmniSwitchConstants;
import com.android.internal.util.omni.PackageUtils;
import com.android.internal.util.omni.DeviceUtils;

public class Style extends SettingsPreferenceFragment
                                         implements Preference.OnPreferenceChangeListener{

    private static final String NAVIGATION_BAR_RECENTS_STYLE = "navbar_recents_style";
    private static final String RECENTS_COMPONENT_TYPE = "recents_component";

    private ListPreference mNavbarRecentsStyle;
    private ListPreference mRecentsComponentType;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.recents_style);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mNavbarRecentsStyle = (ListPreference) findPreference(NAVIGATION_BAR_RECENTS_STYLE);
        int recentsStyle = Settings.System.getInt(resolver,
                Settings.System.OMNI_NAVIGATION_BAR_RECENTS, 0);

        mNavbarRecentsStyle.setValue(Integer.toString(recentsStyle));
        mNavbarRecentsStyle.setSummary(mNavbarRecentsStyle.getEntry());
        mNavbarRecentsStyle.setOnPreferenceChangeListener(this);

        // recents component type
        mRecentsComponentType = (ListPreference) findPreference(RECENTS_COMPONENT_TYPE);
        int type = Settings.System.getInt(resolver,
                Settings.System.RECENTS_COMPONENT, 0);
        mRecentsComponentType.setValue(String.valueOf(type));
        mRecentsComponentType.setSummary(mRecentsComponentType.getEntry());
        mRecentsComponentType.setOnPreferenceChangeListener(this);
        }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mNavbarRecentsStyle) {
            int value = Integer.valueOf((String) newValue);
            if (value == 1) {
                if (!isOmniSwitchInstalled()){
                    doOmniSwitchUnavail();
                } else if (!OmniSwitchConstants.isOmniSwitchRunning(getActivity())) {
                    doOmniSwitchConfig();
                }
            }
            int index = mNavbarRecentsStyle.findIndexOfValue((String) newValue);
            mNavbarRecentsStyle.setSummary(mNavbarRecentsStyle.getEntries()[index]);
            Settings.System.putInt(resolver, Settings.System.OMNI_NAVIGATION_BAR_RECENTS, value);
            return true;
        } else if (preference == mRecentsComponentType) {
            int type = Integer.valueOf((String) newValue);
            int index = mRecentsComponentType.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.RECENTS_COMPONENT, type);
            mRecentsComponentType.setSummary(mRecentsComponentType.getEntries()[index]);
            if (type == 1) { // Disable swipe up gesture, if oreo type selected
               Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.SWIPE_UP_TO_SWITCH_APPS_ENABLED, 0);
            }
            DeviceUtils.showSystemUiRestartDialog(getContext());
            return true;
            }
            return false;
        }

        private void checkForOmniSwitchRecents() {
            if (!isOmniSwitchInstalled()){
                doOmniSwitchUnavail();
            } else if (!OmniSwitchConstants.isOmniSwitchRunning(getActivity())) {
                doOmniSwitchConfig();
            }
        }

        private void doOmniSwitchConfig() {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(R.string.omniswitch_title);
            alertDialogBuilder.setMessage(R.string.omniswitch_dialog_running_new)
                .setPositiveButton(R.string.omniswitch_settings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        startActivity(OmniSwitchConstants.INTENT_LAUNCH_APP);
                    }
                });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        private void doOmniSwitchUnavail() {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(R.string.omniswitch_title);
            alertDialogBuilder.setMessage(R.string.omniswitch_dialog_unavail);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        private boolean isOmniSwitchInstalled() {
            return PackageUtils.isAvailableApp(OmniSwitchConstants.APP_PACKAGE_NAME, getActivity());
        }

        @Override
        public int getMetricsCategory() {
            return MetricsProto.MetricsEvent.BLISSIFY;
        }
}
