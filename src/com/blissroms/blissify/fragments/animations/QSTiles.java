package com.blissroms.blissify.fragments.animations;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.Fragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

public class QSTiles extends SettingsPreferenceFragment
                                        implements Preference.OnPreferenceChangeListener{


        private static final String TAG = "QSTilesPreference";
        private static final String PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
        private static final String PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";
        private static final String PREF_TILE_ANIM_INTERPOLATOR = "qs_tile_animation_interpolator";

        private ListPreference mTileAnimationStyle;
        private ListPreference mTileAnimationDuration;
        private ListPreference mTileAnimationInterpolator;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.qstiles_animation);
            PreferenceScreen prefSet = getPreferenceScreen();

            ContentResolver resolver = getActivity().getContentResolver();

            // QS animation
            mTileAnimationStyle = (ListPreference) findPreference(PREF_TILE_ANIM_STYLE);
            int tileAnimationStyle = Settings.System.getIntForUser(resolver,
                    Settings.System.ANIM_TILE_STYLE, 0, UserHandle.USER_CURRENT);
            mTileAnimationStyle.setValue(String.valueOf(tileAnimationStyle));
            updateTileAnimationStyleSummary(tileAnimationStyle);
            mTileAnimationStyle.setOnPreferenceChangeListener(this);

            mTileAnimationDuration = (ListPreference) findPreference(PREF_TILE_ANIM_DURATION);
            mTileAnimationDuration.setEnabled(tileAnimationStyle > 0);
            int tileAnimationDuration = Settings.System.getIntForUser(resolver,
                    Settings.System.ANIM_TILE_DURATION, 2000, UserHandle.USER_CURRENT);
            mTileAnimationDuration.setValue(String.valueOf(tileAnimationDuration));
            updateTileAnimationDurationSummary(tileAnimationDuration);
            mTileAnimationDuration.setOnPreferenceChangeListener(this);

            mTileAnimationInterpolator = (ListPreference) findPreference(PREF_TILE_ANIM_INTERPOLATOR);
            mTileAnimationInterpolator.setEnabled(tileAnimationStyle > 0);
            int tileAnimationInterpolator = Settings.System.getIntForUser(resolver,
                    Settings.System.ANIM_TILE_INTERPOLATOR, 0, UserHandle.USER_CURRENT);
            mTileAnimationInterpolator.setValue(String.valueOf(tileAnimationInterpolator));
            updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
            mTileAnimationInterpolator.setOnPreferenceChangeListener(this);
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mTileAnimationStyle) {
                int tileAnimationStyle = Integer.valueOf((String) newValue);
                mTileAnimationDuration.setEnabled(tileAnimationStyle > 0);
                mTileAnimationInterpolator.setEnabled(tileAnimationStyle > 0);
                Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_STYLE,
                        tileAnimationStyle, UserHandle.USER_CURRENT);
                updateTileAnimationStyleSummary(tileAnimationStyle);
                return true;
            } else if (preference == mTileAnimationDuration) {
                int tileAnimationDuration = Integer.valueOf((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_DURATION,
                        tileAnimationDuration, UserHandle.USER_CURRENT);
                updateTileAnimationDurationSummary(tileAnimationDuration);
                return true;
            } else if (preference == mTileAnimationInterpolator) {
                int tileAnimationInterpolator = Integer.valueOf((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_INTERPOLATOR,
                        tileAnimationInterpolator, UserHandle.USER_CURRENT);
                updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
                return true;
            }
            return false;
        }

        private void updateTileAnimationStyleSummary(int tileAnimationStyle) {
            String prefix = (String) mTileAnimationStyle.getEntries()[mTileAnimationStyle.findIndexOfValue(String
                    .valueOf(tileAnimationStyle))];
            mTileAnimationStyle.setSummary(getResources().getString(R.string.qs_set_animation_style, prefix));
        }

        private void updateTileAnimationDurationSummary(int tileAnimationDuration) {
            String prefix = (String) mTileAnimationDuration.getEntries()[mTileAnimationDuration.findIndexOfValue(String
                    .valueOf(tileAnimationDuration))];
            mTileAnimationDuration.setSummary(getResources().getString(R.string.qs_set_animation_duration, prefix));
        }

        private void updateTileAnimationInterpolatorSummary(int tileAnimationInterpolator) {
            String prefix = (String) mTileAnimationInterpolator.getEntries()[mTileAnimationInterpolator.findIndexOfValue(String
                    .valueOf(tileAnimationInterpolator))];
            mTileAnimationInterpolator.setSummary(getResources().getString(R.string.qs_set_animation_interpolator, prefix));
        }

        @Override
         public int getMetricsCategory() {
            return MetricsProto.MetricsEvent.BLISSIFY;
        }

}
