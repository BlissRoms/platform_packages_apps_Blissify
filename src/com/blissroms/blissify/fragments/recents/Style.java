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
import com.blissroms.blissify.preference.SystemSettingSwitchPreference;
import android.provider.Settings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.blissroms.blissify.R;
import com.android.internal.util.omni.OmniSwitchConstants;
import com.android.internal.util.omni.PackageUtils;
import com.android.internal.util.omni.DeviceUtils;

public class Style extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.default_view,container,false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.default_view, new Style.SystemPreference())
                .commit();
        return view;
    }

    public static class SystemPreference extends PreferenceFragmentCompat
                                         implements Preference.OnPreferenceChangeListener{

        public SystemPreference() {
        }

        private static final String NAVIGATION_BAR_RECENTS_STYLE = "navbar_recents_style";

        private ListPreference mNavbarRecentsStyle;


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.recents_style);
            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mNavbarRecentsStyle = (ListPreference) findPreference(NAVIGATION_BAR_RECENTS_STYLE);
            int recentsStyle = Settings.System.getInt(resolver,
                    Settings.System.OMNI_NAVIGATION_BAR_RECENTS, 0);

            mNavbarRecentsStyle.setValue(Integer.toString(recentsStyle));
            mNavbarRecentsStyle.setSummary(mNavbarRecentsStyle.getEntry());
            mNavbarRecentsStyle.setOnPreferenceChangeListener(this);
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
     }
}
