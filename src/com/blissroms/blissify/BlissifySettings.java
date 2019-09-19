/*Copyright (C) 2015 The ResurrectionRemix Project
     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
*/
package com.blissroms.blissify;

import android.app.AlertDialog;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.design.widget.Snackbar;

import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;

import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewAnimationUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.blissroms.blissify.tabs.AnimationsSettings;
import com.blissroms.blissify.tabs.ButtonsSettings;
import com.blissroms.blissify.tabs.GestureSettings;
import com.blissroms.blissify.tabs.QSSettings;
import com.blissroms.blissify.tabs.LockScreenSettings;
import com.blissroms.blissify.tabs.RecentsSettings;
import com.blissroms.blissify.tabs.StatusBarSettings;
import com.blissroms.blissify.tabs.UISettings;

import com.android.settings.dashboard.SummaryLoader;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;


public class BlissifySettings extends SettingsPreferenceFragment {
    private static final String TAG = "BlissifySettings";
    ViewPager mViewPager;
    ViewGroup mContainer;
    PagerSlidingTabStrip mTabs;
    SectionsPagerAdapter mSectionsPagerAdapter;
    protected Context mContext;
	private LinearLayout mLayout;

 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContainer = container;
        View view = inflater.inflate(R.layout.bliss_main, container, false);
        mLayout = (LinearLayout) view.findViewById(R.id.main_content);
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mTabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabs.setViewPager(mViewPager);
    return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle saveState) {
        super.onSaveInstanceState(saveState);
    }

    class SectionsPagerAdapter extends FragmentPagerAdapter {

        String titles[] = getTitles();
        private Fragment frags[] = new Fragment[titles.length];

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            frags[0] = new StatusBarSettings();
            frags[1] = new ButtonsSettings();
            frags[2] = new QSSettings();
            frags[3] = new LockScreenSettings();
            frags[4] = new RecentsSettings();
            frags[5] = new GestureSettings();
            frags[6] = new AnimationsSettings();
            frags[7] = new UISettings();
        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return frags.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    private String[] getTitles() {
        String titleString[];
        titleString = new String[]{
                getString(R.string.statusbar_category),
                getString(R.string.button_category),
                getString(R.string.qs_category),
                getString(R.string.lockscreen_category),
                getString(R.string.recents_category),
                getString(R.string.gestures_category),
                getString(R.string.animation_category),
                getString(R.string.interface_category)};
        return titleString;
    }

    @Override
     public int getMetricsCategory() {
        return MetricsEvent.BLISSIFY;
     }

}
