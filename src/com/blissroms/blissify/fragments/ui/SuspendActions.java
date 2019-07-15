package com.blissroms.blissify.fragments.ui;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
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

import com.bliss.support.preferences.SystemSettingSeekBarPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

public class SuspendActions extends SettingsPreferenceFragment
                                         implements Preference.OnPreferenceChangeListener{

    private static final String TAG = "SuspendActions";

    private static final String SCREEN_STATE_TOOGLES_ENABLE = "screen_state_toggles_enable_key";
    private static final String SCREEN_STATE_TOOGLES_TWOG = "screen_state_toggles_twog";
    private static final String SCREEN_STATE_TOOGLES_GPS = "screen_state_toggles_gps";
    private static final String SCREEN_STATE_TOOGLES_MOBILE_DATA = "screen_state_toggles_mobile_data";
    private static final String SCREEN_STATE_ON_DELAY = "screen_state_on_delay";
    private static final String SCREEN_STATE_OFF_DELAY = "screen_state_off_delay";
    private static final String SCREEN_STATE_CATGEGORY_LOCATION = "screen_state_toggles_location_key";
    private static final String SCREEN_STATE_CATGEGORY_MOBILE_DATA = "screen_state_toggles_mobile_key";

    private Context mContext;

    private SwitchPreference mEnableScreenStateToggles;
    private SwitchPreference mEnableScreenStateTogglesTwoG;
    private SwitchPreference mEnableScreenStateTogglesGps;
    private SwitchPreference mEnableScreenStateTogglesMobileData;
    private SystemSettingSeekBarPreference mMinutesOffDelay;
    private SystemSettingSeekBarPreference mMinutesOnDelay;
    private PreferenceCategory mMobileDateCategory;
    private PreferenceCategory mLocationCategory;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.suspend_actions);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mContext = (Context) getActivity();

        mEnableScreenStateToggles = (SwitchPreference) findPreference(
                SCREEN_STATE_TOOGLES_ENABLE);

        int enabled = Settings.System.getInt(resolver, Settings.System.START_SCREEN_STATE_SERVICE, 0);

        mEnableScreenStateToggles.setChecked(enabled != 0);
        mEnableScreenStateToggles.setOnPreferenceChangeListener(this);

        mMinutesOffDelay = (SystemSettingSeekBarPreference) findPreference(SCREEN_STATE_OFF_DELAY);
        int offd = Settings.System.getInt(resolver,
                Settings.System.SCREEN_STATE_OFF_DELAY, 0);
        mMinutesOffDelay.setValue(offd / 60);
        mMinutesOffDelay.setOnPreferenceChangeListener(this);

        mMinutesOnDelay = (SystemSettingSeekBarPreference) findPreference(SCREEN_STATE_ON_DELAY);
        int ond = Settings.System.getInt(resolver,
                Settings.System.SCREEN_STATE_ON_DELAY, 0);
        mMinutesOnDelay.setValue(ond / 60);
        mMinutesOnDelay.setOnPreferenceChangeListener(this);

        mMobileDateCategory = (PreferenceCategory) findPreference(
                SCREEN_STATE_CATGEGORY_MOBILE_DATA);
        mLocationCategory = (PreferenceCategory) findPreference(
                SCREEN_STATE_CATGEGORY_LOCATION);

        mEnableScreenStateTogglesTwoG = (SwitchPreference) findPreference(
                SCREEN_STATE_TOOGLES_TWOG);

        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (!cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)){
            getPreferenceScreen().removePreference(mEnableScreenStateTogglesTwoG);
        } else {
            mEnableScreenStateTogglesTwoG.setChecked((
                    Settings.System.getInt(resolver, Settings.System.SCREEN_STATE_TWOG, 0) == 1));
            mEnableScreenStateTogglesTwoG.setOnPreferenceChangeListener(this);
        }

        mEnableScreenStateTogglesMobileData = (SwitchPreference) findPreference(
                SCREEN_STATE_TOOGLES_MOBILE_DATA);

        if (!cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)){
            getPreferenceScreen().removePreference(mEnableScreenStateTogglesMobileData);
        } else {
            mEnableScreenStateTogglesMobileData.setChecked((
                    Settings.System.getInt(resolver, Settings.System.SCREEN_STATE_MOBILE_DATA, 0) == 1));
            mEnableScreenStateTogglesMobileData.setOnPreferenceChangeListener(this);
        }

        // Only enable these controls if this user is allowed to change location
        // sharing settings.
        final UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        boolean isLocationChangeAllowed = !um.hasUserRestriction(UserManager.DISALLOW_SHARE_LOCATION);

        // TODO: check if gps is available on this device?
        mEnableScreenStateTogglesGps = (SwitchPreference) findPreference(
                SCREEN_STATE_TOOGLES_GPS);

        if (!isLocationChangeAllowed){
            getPreferenceScreen().removePreference(mEnableScreenStateTogglesGps);
            mEnableScreenStateTogglesGps = null;
        } else {
            mEnableScreenStateTogglesGps.setChecked((
                    Settings.System.getInt(getActivity().getContentResolver(), Settings.System.SCREEN_STATE_GPS, 0) == 1));
            mEnableScreenStateTogglesGps.setOnPreferenceChangeListener(this);
        }

        mMobileDateCategory.setEnabled(enabled != 0);
        mLocationCategory.setEnabled(enabled != 0);
        }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
         ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mEnableScreenStateToggles) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.START_SCREEN_STATE_SERVICE, value ? 1 : 0);
            Intent service = (new Intent())
                    .setClassName("com.android.systemui", "com.android.systemui.screenstate.ScreenStateService");
            if (value) {
                getActivity().stopService(service);
                getActivity().startService(service);
            } else {
                getActivity().stopService(service);
            }
            mMobileDateCategory.setEnabled(value);
            mLocationCategory.setEnabled(value);
            return true;
        } else if (preference == mEnableScreenStateTogglesTwoG) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.SCREEN_STATE_TWOG, value ? 1 : 0);

            Intent intent = new Intent("android.intent.action.SCREEN_STATE_SERVICE_UPDATE");
            mContext.sendBroadcast(intent);
            return true;
        } else if (preference == mEnableScreenStateTogglesGps) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.SCREEN_STATE_GPS, value ? 1 : 0);

            Intent intent = new Intent("android.intent.action.SCREEN_STATE_SERVICE_UPDATE");
            mContext.sendBroadcast(intent);
            return true;
        } else if (preference == mEnableScreenStateTogglesMobileData) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.SCREEN_STATE_MOBILE_DATA, value ? 1 : 0);

            Intent intent = new Intent("android.intent.action.SCREEN_STATE_SERVICE_UPDATE");
            mContext.sendBroadcast(intent);
            return true;
        } else if (preference == mMinutesOffDelay) {
            int delay = ((Integer) newValue) * 60;
            Settings.System.putInt(resolver,
                    Settings.System.SCREEN_STATE_OFF_DELAY, delay);
            return true;
        } else if (preference == mMinutesOnDelay) {
            int delay = ((Integer) newValue) * 60;
            Settings.System.putInt(resolver,
                    Settings.System.SCREEN_STATE_ON_DELAY, delay);
            return true;
        }
        return false;
        }

    private void restartService(){
        Intent service = (new Intent())
                .setClassName("com.android.systemui", "com.android.systemui.screenstate.ScreenStateService");
        getActivity().stopService(service);
        getActivity().startService(service);
    }

       @Override
         public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
       }
}
