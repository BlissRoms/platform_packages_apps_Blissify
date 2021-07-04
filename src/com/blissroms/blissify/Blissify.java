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

import com.android.internal.logging.nano.MetricsProto;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.ContentResolver;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.view.Surface;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.R;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import androidx.fragment.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.blissroms.blissify.fragments.statusbar.StatusBar;
import com.blissroms.blissify.fragments.qs.QuickSettings;
import com.blissroms.blissify.fragments.navigation.NavigationSettings;
import com.blissroms.blissify.fragments.lockscreen.Lockscreen;
import com.blissroms.blissify.fragments.system.SystemSettings;
import com.blissroms.blissify.stats.Constants;
import com.blissroms.blissify.stats.RequestInterface;
import com.blissroms.blissify.stats.models.ServerRequest;
import com.blissroms.blissify.stats.models.ServerResponse;
import com.blissroms.blissify.stats.models.StatsData;

import nl.joery.animatedbottombar.AnimatedBottomBar;

import java.util.List;
import java.util.ArrayList;
import android.util.Log;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

public class Blissify extends SettingsPreferenceFragment {

    Context mContext;
    View view;
    AnimatedBottomBar animatedBottomBar;
    private CompositeDisposable mCompositeDisposable;
    private SharedPreferences pref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        Resources res = getResources();
        Window win = getActivity().getWindow();

        mCompositeDisposable = new CompositeDisposable();
        pref = getActivity().getSharedPreferences("BlissStatsPref", Context.MODE_PRIVATE);
        if (!pref.getString(Constants.LAST_BUILD_DATE, "null").equals(Build.DATE)
                || pref.getBoolean(Constants.IS_FIRST_LAUNCH, true)) {
            pushStats();
        }

        win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        win.setNavigationBarColor(res.getColor(R.color.bottombar_bg));
        win.setNavigationBarDividerColor(res.getColor(R.color.bottombar_bg));

        view = inflater.inflate(R.layout.layout_blissify, container, false);

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.blissify_title);
        }

        animatedBottomBar = (AnimatedBottomBar) view.findViewById(R.id.bottom_navigation);

        Fragment lockscreen = new com.blissroms.blissify.fragments.lockscreen.Lockscreen();
        Fragment navsettings = new com.blissroms.blissify.fragments.navigation.NavigationSettings();
        Fragment qspanel = new com.blissroms.blissify.fragments.qs.QuickSettings();
        Fragment statusbar = new com.blissroms.blissify.fragments.statusbar.StatusBar();
        Fragment system = new com.blissroms.blissify.fragments.system.SystemSettings();

        Fragment fragment = (Fragment) getFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, statusbar);
            transaction.addToBackStack(null);
            transaction.commit();
        }

        animatedBottomBar.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {
            @Override
            public void onTabReselected(int i, AnimatedBottomBar.Tab tab) {

            }

            @Override
            public void onTabSelected(int lastIndex, AnimatedBottomBar.Tab lastTab, int newIndex, AnimatedBottomBar.Tab newTab) {
                int id = newTab.getId();
                
                if (id == R.id.status_bar_category)
                {
                       switchFrag(statusbar);
                } else if (id == R.id.qspanel_category) 
				{
                       switchFrag(qspanel);
                } else if (id == R.id.navigation_category) 
				{
                       switchFrag(navsettings);
                } else if (id == R.id.lockscreen_category) 
				{
                       switchFrag(lockscreen);
                } else if (id == R.id.system_category) 
				{
                       switchFrag(system);
                }
            }
        });

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    private void switchFrag(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
    }

    @Override
    public void onResume() {
        super.onResume();

        view = getView();
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP &&
                    keyCode == KeyEvent.KEYCODE_BACK) {
                getActivity().finish();
                return true;
            }
            return false;
        });
    }

    private void pushStats() {
        //Anonymous Stats

        if (!TextUtils.isEmpty(SystemProperties.get(Constants.KEY_DEVICE))) {
            RequestInterface requestInterface = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(RequestInterface.class);

            StatsData stats = new StatsData();
            stats.setDevice(stats.getDevice());
            stats.setModel(stats.getModel());
            stats.setBlissVersion(stats.getBlissVersion());
            stats.setBuildType(stats.getBuildType());
            stats.setBuildName(stats.getBuildName());
            stats.setCountryCode(stats.getCountryCode(getActivity()));
            stats.setBuildDate(stats.getBuildDate());
            stats.setAndroidVersion(stats.getAndroidVersion());
            stats.setManufacturer(stats.getManufacturer());

            ServerRequest request = new ServerRequest();
            request.setOperation(Constants.PUSH_OPERATION);
            request.setStats(stats);
            mCompositeDisposable.add(requestInterface.operation(request)
                    .observeOn(AndroidSchedulers.mainThread(),false,100)
                    .subscribeOn(Schedulers.io())
                    .subscribe(this::handleResponse, this::handleError));
        } else {
            Log.d(Constants.TAG, "This ain't BlissRoms!");
        }

    }

    private void handleResponse(ServerResponse resp) {

        if (resp.getResult().equals(Constants.SUCCESS)) {

            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(Constants.IS_FIRST_LAUNCH, false);
            editor.putString(Constants.LAST_BUILD_DATE, Build.DATE);
            editor.apply();
            Log.d(Constants.TAG, "push successful");

        } else {
            Log.d(Constants.TAG, resp.getMessage());
        }

    }

    private void handleError(Throwable error) {

        Log.d(Constants.TAG, error.toString());

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    public static int getThemeAccentColor (final Context context) {
        final TypedValue value = new TypedValue ();
        context.getTheme ().resolveAttribute (android.R.attr.colorAccent, value, true);
        return value.data;
    }

}
