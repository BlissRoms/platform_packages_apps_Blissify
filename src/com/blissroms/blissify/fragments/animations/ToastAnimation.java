package com.blissroms.blissify.fragments.animations;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

public class ToastAnimation extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener{

        private static final String TAG = "ToastPreference";
        private static final String PREF_TOAST_ANIMATION = "toast_animation";
        private static final String KEY_LISTVIEW_ANIMATION = "listview_animation";
        private static final String KEY_LISTVIEW_INTERPOLATOR = "listview_interpolator";

        private ListPreference mToastAnimation;
        private ListPreference mListViewAnimation;
        private ListPreference mListViewInterpolator;
        Toast mToast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.toast_animation);
            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            // Toast animation
            mToastAnimation = (ListPreference) findPreference(PREF_TOAST_ANIMATION);
            int toastanimation = Settings.System.getInt(resolver,
                    Settings.System.TOAST_ANIMATION, 1);
            mToastAnimation.setValue(String.valueOf(toastanimation));
            mToastAnimation.setSummary(mToastAnimation.getEntry());
            mToastAnimation.setOnPreferenceChangeListener(this);
            if (mToast != null) {
                mToast.cancel();
                mToast = null;
            }

            // ListView Animations
            mListViewAnimation = (ListPreference) prefSet.findPreference(KEY_LISTVIEW_ANIMATION);
            int listviewanimation =Settings.System.getInt(resolver,
                    Settings.System.LISTVIEW_ANIMATION, 0);
            mListViewAnimation.setValue(String.valueOf(listviewanimation));
            mListViewAnimation.setSummary(mListViewAnimation.getEntry());
            mListViewAnimation.setOnPreferenceChangeListener(this);
            mListViewInterpolator = (ListPreference) prefSet.findPreference(KEY_LISTVIEW_INTERPOLATOR);
            int listviewinterpolator = Settings.System.getInt(resolver,
                    Settings.System.LISTVIEW_INTERPOLATOR, 0);
            mListViewInterpolator.setValue(String.valueOf(listviewinterpolator));
            mListViewInterpolator.setSummary(mListViewInterpolator.getEntry());
            mListViewInterpolator.setOnPreferenceChangeListener(this);
            mListViewInterpolator.setEnabled(listviewanimation > 0);
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            int value;
            int index;

            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mToastAnimation) {
                value = Integer.parseInt((String) newValue);
                index = mToastAnimation.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.TOAST_ANIMATION, value);
                mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(getActivity(), "Toast Test",
                        Toast.LENGTH_SHORT);
                mToast.show();
                return true;
            } else if (preference == mListViewAnimation) {
                value = Integer.parseInt((String) newValue);
                index = mListViewAnimation.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.LISTVIEW_ANIMATION, value);
                mListViewAnimation.setSummary(mListViewAnimation.getEntries()[index]);
                mListViewInterpolator.setEnabled(value > 0);
                return true;
            } else if (preference == mListViewInterpolator) {
                value = Integer.parseInt((String) newValue);
                index = mListViewInterpolator.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.LISTVIEW_INTERPOLATOR, value);
                mListViewInterpolator.setSummary(mListViewInterpolator.getEntries()[index]);
                return true;
            } else if (preference == mListViewAnimation) {
                value = Integer.parseInt((String) newValue);
                index = mListViewAnimation.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.LISTVIEW_ANIMATION, value);
                mListViewAnimation.setSummary(mListViewAnimation.getEntries()[index]);
                mListViewInterpolator.setEnabled(value > 0);
                return true;
            } else if (preference == mListViewInterpolator) {
                value = Integer.parseInt((String) newValue);
                index = mListViewInterpolator.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.LISTVIEW_INTERPOLATOR, value);
                mListViewInterpolator.setSummary(mListViewInterpolator.getEntries()[index]);
                return true;
            }
            return false;
        }


        @Override
         public int getMetricsCategory() {
            return MetricsProto.MetricsEvent.BLISSIFY;
         }
}
