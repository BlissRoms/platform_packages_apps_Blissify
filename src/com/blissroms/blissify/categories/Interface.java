package com.blissroms.blissify.categories;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blissroms.blissify.R;
import com.blissroms.blissify.fragments.ui.BattLED;
import com.blissroms.blissify.fragments.ui.CallOpt;
import com.blissroms.blissify.fragments.ui.Misc;
import com.blissroms.blissify.fragments.ui.Theme;
import com.blissroms.blissify.fragments.ui.Weather;


/**
 * Created by jackeagle on 31/12/17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Interface extends Fragment {

    private View view;
    private ViewPager viewPager;
    private TabLayout tableLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.blissify_tablayout, container, false);

        viewPager = view.findViewById(R.id.viewpager);
        viewPager.setAdapter(new InterfaceAdapter(getChildFragmentManager()));
        tableLayout = view.findViewById(R.id.sliding_tabs);
        tableLayout.post(new Runnable() {
            @Override
            public void run() {
                tableLayout.setupWithViewPager(viewPager);
            }
        });

        return view;
    }

    private class InterfaceAdapter extends FragmentPagerAdapter {

        final String[] tabs= getTabsTitle();
        private final Fragment[] frags = new Fragment[tabs.length];

        public InterfaceAdapter(FragmentManager fm) {
            super(fm);
            // Add Fragments Here
            frags[0] = new Theme();
            frags[1] = new BattLED();
            frags[2] = new CallOpt();
            frags[3] = new Misc();
            frags[4] = new Weather();

        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }

        // Tab Titles
        private String[] getTabsTitle() {
            String titleString[];
            titleString = new String[]
                    {
                      // Add Tab Fragment Title
                        getString(R.string.interface_theme_title),
                        getString(R.string.interface_batteryled_title),
                        getString(R.string.interface_callopt_title),
                        getString(R.string.interface_misc_title),
                        getString(R.string.interface_weather_title)

                    };
            return titleString;
        }
    }
}
