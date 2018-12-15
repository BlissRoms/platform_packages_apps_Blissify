/*
 * Copyright (C) 2019-2021 The BlissRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blissroms.blissify.fragments.themes;

import static android.os.UserHandle.USER_CURRENT;
import static android.os.UserHandle.USER_SYSTEM;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.om.IOverlayManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.om.OverlayInfo;
import android.graphics.Color;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.content.pm.PackageManager.NameNotFoundException;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.*;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.OverlayCategoryPreferenceController;
import com.android.settings.display.FontPickerPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;
import com.blissroms.blissify.fragments.themes.SystemThemePreferenceController;
import com.bliss.support.colorpicker.ColorPickerPreference;
import com.bliss.support.preference.SecureSettingSwitchPreference;

import com.android.internal.util.bliss.BlissUtils;
import com.android.internal.util.bliss.ThemesUtils;
import com.bliss.support.preferences.SystemSettingListPreference;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class Themes extends DashboardFragment  implements
        OnPreferenceChangeListener {

    public static final String TAG = "Themes";

    private static final String PREF_NAVBAR_STYLE = "theme_navbar_style";
    private static final String SLIDER_STYLE  = "slider_style";
    private static final String SYSUI_ROUNDED_SIZE = "sysui_rounded_size";
    private static final String SYSUI_ROUNDED_CONTENT_PADDING = "sysui_rounded_content_padding";
    private static final String SYSUI_ROUNDED_FWVALS = "sysui_rounded_fwvals";

    private Context mContext;
    private Handler mHandler;
    private IOverlayManager mOverlayManager;
    private IOverlayManager mOverlayService;
    private CustomSeekBarPreference mCornerRadius;
    private CustomSeekBarPreference mContentPadding;
    private SecureSettingSwitchPreference mRoundedFwvals;

    private ListPreference mNavbarPicker;

    private static final String PREF_RGB_ACCENT_PICKER = "rgb_accent_picker";

    private ColorPickerPreference rgbAccentPicker;

    private IntentFilter mIntentFilter;
    private static FontPickerPreferenceController mFontPickerPreference;
    private SystemSettingListPreference mSlider;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("com.android.server.ACTION_FONT_CHANGED")) {
                mFontPickerPreference.stopProgress();
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.android.server.ACTION_FONT_CHANGED");

        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        mNavbarPicker = (ListPreference) findPreference(PREF_NAVBAR_STYLE);
        int navbarStyleValues = getOverlayPosition(ThemesUtils.NAVBAR_STYLES);
        if (navbarStyleValues != -1) {
            mNavbarPicker.setValue(String.valueOf(navbarStyleValues + 2));
        } else {
            mNavbarPicker.setValue("1");
        }
        mNavbarPicker.setSummary(mNavbarPicker.getEntry());
        mNavbarPicker.setOnPreferenceChangeListener(this);

        rgbAccentPicker = (ColorPickerPreference) findPreference(PREF_RGB_ACCENT_PICKER);
        String colorVal = Settings.Secure.getStringForUser(mContext.getContentResolver(),
                Settings.Secure.ACCENT_COLOR, UserHandle.USER_CURRENT);
        int color = (colorVal == null)
                ? Color.WHITE
                : Color.parseColor("#" + colorVal);
        rgbAccentPicker.setNewPreviewColor(color);
        rgbAccentPicker.setOnPreferenceChangeListener(this);

        mSlider = (SystemSettingListPreference) findPreference(SLIDER_STYLE);
        mCustomSettingsObserver.observe();
    }

        Resources res = null;
        Context ctx = getContext();
        float density = Resources.getSystem().getDisplayMetrics().density;

        try {
            res = ctx.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        // Rounded Corner Radius
        mCornerRadius = (CustomSeekBarPreference) findPreference(SYSUI_ROUNDED_SIZE);
        int resourceIdRadius = (int) ctx.getResources().getDimension(com.android.internal.R.dimen.rounded_corner_radius);
        int cornerRadius = Settings.Secure.getIntForUser(ctx.getContentResolver(), Settings.Secure.SYSUI_ROUNDED_SIZE,
                ((int) (resourceIdRadius / density)), UserHandle.USER_CURRENT);
        mCornerRadius.setValue(cornerRadius);
        mCornerRadius.setOnPreferenceChangeListener(this);

        // Rounded Content Padding
        //mContentPadding = (CustomSeekBarPreference) findPreference(SYSUI_ROUNDED_CONTENT_PADDING);
        //int resourceIdPadding = res.getIdentifier("com.android.systemui:dimen/rounded_corner_content_padding", null,
        //        null);
        //int contentPadding = Settings.Secure.getIntForUser(ctx.getContentResolver(),
        //        Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING,
        //        (int) (res.getDimension(resourceIdPadding) / density), UserHandle.USER_CURRENT);
        //mContentPadding.setValue(contentPadding);
        //mContentPadding.setOnPreferenceChangeListener(this);

        // Rounded use Framework Values
        mRoundedFwvals = (SecureSettingSwitchPreference) findPreference(SYSUI_ROUNDED_FWVALS);
        mRoundedFwvals.setOnPreferenceChangeListener(this);

    private int getOverlayPosition(String[] overlays) {
        int position = -1;
        for (int i = 0; i < overlays.length; i++) {
            String overlay = overlays[i];
            if (BlissUtils.isThemeEnabled(overlay)) {
                position = i;
            }
        }
        return position;
    }

    private String getOverlayName(String[] overlays) {
        String overlayName = null;
        for (int i = 0; i < overlays.length; i++) {
            String overlay = overlays[i];
            if (BlissUtils.isThemeEnabled(overlay)) {
                overlayName = overlay;
            }
        }
        return overlayName;
    }

    public void handleOverlays(String packagename, Boolean state, IOverlayManager mOverlayManager) {
        try {
            mOverlayService.setEnabled(packagename, state, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private CustomSettingsObserver mCustomSettingsObserver = new CustomSettingsObserver(mHandler);
    private class CustomSettingsObserver extends ContentObserver {

        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            Context mContext = getContext();
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.SLIDER_STYLE),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.SLIDER_STYLE))) {
                updateSlider();
            }
        }
    }

    private void updateSlider() {
        ContentResolver resolver = getActivity().getContentResolver();

        boolean sliderDefault = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.SLIDER_STYLE , 0, UserHandle.USER_CURRENT) == 0;
        boolean sliderOOS = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.SLIDER_STYLE , 0, UserHandle.USER_CURRENT) == 1;
        boolean sliderAosp = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.SLIDER_STYLE , 0, UserHandle.USER_CURRENT) == 2;
        boolean sliderRUI = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.SLIDER_STYLE , 0, UserHandle.USER_CURRENT) == 3;
        boolean sliderA12 = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.SLIDER_STYLE , 0, UserHandle.USER_CURRENT) == 4;

        if (sliderDefault) {
            setDefaultSlider(mOverlayService);
        } else if (sliderOOS) {
            enableSlider(mOverlayService, "com.android.theme.systemui_slider_oos");
        } else if (sliderAosp) {
            enableSlider(mOverlayService, "com.android.theme.systemui_slider.aosp");
        } else if (sliderRUI) {
            enableSlider(mOverlayService, "com.android.theme.systemui_slider.rui");
        } else if (sliderA12) {
            enableSlider(mOverlayService, "com.android.theme.systemui_slider.a12");
        }
    }

    public static void setDefaultSlider(IOverlayManager overlayManager) {
        for (int i = 0; i < SLIDERS.length; i++) {
            String sliders = SLIDERS[i];
            try {
                overlayManager.setEnabled(sliders, false, USER_SYSTEM);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void enableSlider(IOverlayManager overlayManager, String overlayName) {
        try {
            for (int i = 0; i < SLIDERS.length; i++) {
                String sliders = SLIDERS[i];
                try {
                    overlayManager.setEnabled(sliders, false, USER_SYSTEM);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            overlayManager.setEnabled(overlayName, true, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static final String[] SLIDERS = {
        "com.android.theme.systemui_slider_oos",
        "com.android.theme.systemui_slider.aosp",
        "com.android.theme.systemui_slider.rui",
        "com.android.theme.systemui_slider.a12"
    };

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNavbarPicker) {
            String navbarStyle = (String) newValue;
            int navbarStyleValue = Integer.parseInt(navbarStyle);
            mNavbarPicker.setValue(String.valueOf(navbarStyleValue));
            String overlayName = getOverlayName(ThemesUtils.NAVBAR_STYLES);
                if (overlayName != null) {
                    handleOverlays(overlayName, false, mOverlayManager);
                }
                if (navbarStyleValue > 1) {
                    handleOverlays(ThemesUtils.NAVBAR_STYLES[navbarStyleValue - 2],
                            true, mOverlayManager);
            }
            mNavbarPicker.setSummary(mNavbarPicker.getEntry());
	    } else if (preference == rgbAccentPicker) {
            int color = (Integer) newValue;
            String hexColor = String.format("%08X", (0xFFFFFFFF & color));
            Settings.Secure.putStringForUser(mContext.getContentResolver(),
                        Settings.Secure.ACCENT_COLOR,
                        hexColor, UserHandle.USER_CURRENT);
            try {
                 mOverlayManager.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
                 mOverlayManager.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
             } catch (RemoteException ignored) {
             }
            return true;
        } else if (preference == mSlider) {
            mCustomSettingsObserver.observe();
            return true;
        } else if (preference == mCornerRadius) {
            Settings.Secure.putIntForUser(getContext().getContentResolver(), Settings.Secure.SYSUI_ROUNDED_SIZE,
                    (int) newValue, UserHandle.USER_CURRENT);
            return true;
        //} else if (preference == mContentPadding) {
        //    Settings.Secure.putIntForUser(getContext().getContentResolver(), Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING,
        //            (int) newValue, UserHandle.USER_CURRENT);
        //    return true;
        } else if (preference == mRoundedFwvals) {
            restoreCorners();
            return true;
        }
        return false;
    }

    private void restoreCorners() {
        Resources res = null;
        float density = Resources.getSystem().getDisplayMetrics().density;
        Context ctx = getContext();

        try {
            res = ctx.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        int resourceIdRadius = (int) ctx.getResources().getDimension(com.android.internal.R.dimen.rounded_corner_radius);
        //int resourceIdPadding = res.getIdentifier("com.android.systemui:dimen/rounded_corner_content_padding", null, null);
        mCornerRadius.setValue((int) (resourceIdRadius / density));
        //mContentPadding.setValue((int) (res.getDimension(resourceIdPadding) / density));

    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.BLISSIFY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.blissify_themes;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, Fragment fragment) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.primary_color"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.adaptive_icon_shape"));
        controllers.add(mFontPickerPreference = new FontPickerPreferenceController(context, lifecycle));
        controllers.add(new SystemThemePreferenceController(context));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.signal_icon"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.wifi_icon"));
        return controllers;
    }

    @Override
    public void onResume() {
        super.onResume();
        final Context context = getActivity();
        context.registerReceiver(mIntentReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        final Context context = getActivity();
        context.unregisterReceiver(mIntentReceiver);
        mFontPickerPreference.stopProgress();
    }

    /**
     * For Search.
     */

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.blissify_themes);
}

