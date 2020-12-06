/*
 * Copyright (C) 2019 The BlissRoms Project
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

package com.blissroms.blissify;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import nl.joery.animatedbottombar.AnimatedBottomBar;

import com.blissroms.blissify.fragments.statusbar.StatusBar;
import com.blissroms.blissify.fragments.buttons.ButtonSettings;
import com.blissroms.blissify.fragments.gestures.Gestures;
import com.blissroms.blissify.fragments.notifications.Notifications;

import java.util.List;
import java.util.ArrayList;

public class Blissify extends SettingsPreferenceFragment {

    Context mContext;
    View view;
    AnimatedBottomBar animatedBottomBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mContext = getActivity();
        Resources res = getResources();
        
        view = inflater.inflate(R.layout.blissify, container, false);

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.blissify_title);
        }

        animatedBottomBar = (AnimatedBottomBar) view.findViewById(R.id.nav_view);
        
        if (savedInstanceState == null)
        {
            animatedBottomBar.selectTabById(R.id.navigation_home, true);
            Fragment HomeFragment = new com.blissroms.blissify.fragments.statusbar.StatusBar();
            launchFragment(HomeFragment);
        }
        
        animatedBottomBar.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {
            @Override
            public void onTabReselected(int i, AnimatedBottomBar.Tab tab) {

            }

            @Override
            public void onTabSelected(int lastIndex, AnimatedBottomBar.Tab lastTab, int newIndex, AnimatedBottomBar.Tab newTab) {
                Fragment fragment = null;
                int id = newTab.getId();
                
                if (id == R.id.navigation_home)
                {
                        fragment = new com.blissroms.blissify.fragments.statusbar.StatusBar();
                } else if (id == R.id.navigation_dashboard) {
                        fragment = new com.blissroms.blissify.fragments.buttons.ButtonSettings();
                } else if (id == R.id.navigation_notifications) {
                        fragment = new com.blissroms.blissify.fragments.gestures.Gestures();
                } else if (id == R.id.test1) {
                        fragment = new com.blissroms.blissify.fragments.notifications.Notifications();
                } else if (id == R.id.test2) {
                        fragment = new com.blissroms.blissify.fragments.statusbar.StatusBar();
                }

                if (fragment!=null){
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragmentContainer, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                } else{
                    Log.e("TAG", "Error");
                }
            }
        });
        
        return view;
  }
  
    private void launchFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    
    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
    }
}
