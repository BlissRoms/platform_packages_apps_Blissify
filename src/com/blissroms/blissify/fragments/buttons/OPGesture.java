package com.blissroms.blissify.fragments.buttons;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.format.DateFormat;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import android.provider.Settings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.bliss.support.preferences.SystemSettingSwitchPreference;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.omni.OmniSwitchConstants;
import com.android.internal.util.omni.PackageUtils;
import com.android.internal.util.omni.DeviceUtils;

public class OPGesture extends SettingsPreferenceFragment
                                         implements Preference.OnPreferenceChangeListener{

        private static final String TAG = "NavBar";
        private static final String KEYS_SHOW_NAVBAR_KEY = "navbar_visibility";

        private SwitchPreference mEnableNavBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.buttons_op_gesture);
            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mEnableNavBar = (SwitchPreference) prefSet.findPreference(KEYS_SHOW_NAVBAR_KEY);
            boolean showNavBarDefault = DeviceUtils.deviceSupportNavigationBar(getActivity());
            boolean showNavBar = Settings.Secure.getInt(resolver,
                    Settings.Secure.NAVIGATION_BAR_VISIBLE, showNavBarDefault ? 1 : 0) == 1;
            mEnableNavBar.setChecked(showNavBar);

        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            return false;
        }

        public boolean onPreferenceTreeClick(Preference preference) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mEnableNavBar) {
                boolean checked = ((SwitchPreference)preference).isChecked();
                Settings.Secure.putInt(resolver,
                        Settings.Secure.NAVIGATION_BAR_VISIBLE, checked ? 1:0);
                return true;
            }
            return false;
        }

        @Override
        public int getMetricsCategory() {
            return MetricsProto.MetricsEvent.BLISSIFY;
        }
}
