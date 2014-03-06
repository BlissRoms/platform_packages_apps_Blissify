package com.blissroms.blissify.fragments.animations;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.provider.Settings;

import com.blissroms.blissify.R;

@SuppressWarnings("DefaultFileTemplate")
public class ToastAnimation extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.default_view,container,false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.default_view, new ToastAnimation.ToastPreference())
                .commit();
        return view;
    }

    public static class ToastPreference extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener{

        public ToastPreference() {
        }

        private static final String TAG = "ToastPreference";
        private static final String PREF_TILE_ANIM_STYLE = "anim_tile_style";
        private static final String PREF_TILE_ANIM_DURATION = "anim_tile_duration";
        private static final String PREF_TILE_ANIM_INTERPOLATOR = "anim_tile_interpolator";
        private static final String PREF_TOAST_ANIMATION = "toast_animation";

        private ListPreference mTileAnimationDuration;
        private ListPreference mTileAnimationInterpolator;
        private ListPreference mTileAnimationStyle;
        private ListPreference mToastAnimation;
        Toast mToast;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.toast_animation);
            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            // Toast Animations
            mToastAnimation = (ListPreference) findPreference(PREF_TOAST_ANIMATION);
            mToastAnimation.setSummary(mToastAnimation.getEntry());
            int CurrentToastAnimation = Settings.System.getInt(resolver,
                    Settings.System.TOAST_ANIMATION, 1);
            mToastAnimation.setValueIndex(CurrentToastAnimation); //set to index of default value
            mToastAnimation.setSummary(mToastAnimation.getEntries()[CurrentToastAnimation]);
            mToastAnimation.setOnPreferenceChangeListener(this);
            if (mToast != null) {
                mToast.cancel();
                mToast = null;
            }
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            int value;
            int index;
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mToastAnimation) {
                index = mToastAnimation.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.TOAST_ANIMATION, index);
                mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(getActivity(), mToastAnimation.getEntries()[index],
                        Toast.LENGTH_SHORT);
                mToast.show();
                return true;
            }
            return false;
        }
    }
}
