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

/**
 * Created by jackeagle on 31/12/17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Gestures extends Fragment {

    private View view;
    private ViewPager viewPager;
    private TabLayout tableLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.blissify_tablayout, container, false);

        viewPager = view.findViewById(R.id.viewpager);
        viewPager.setAdapter(new GesturesAdapter(getChildFragmentManager()));
        tableLayout = view.findViewById(R.id.sliding_tabs);
        tableLayout.post(new Runnable() {
            @Override
            public void run() {
                tableLayout.setupWithViewPager(viewPager);
            }
        });

        return view;
    }

    private class GesturesAdapter extends FragmentPagerAdapter {

        final String[] tabs= getTabsTitle();
        private final Fragment[] frags = new Fragment[tabs.length];

        public GesturesAdapter(FragmentManager fm) {
            super(fm);
            // Add Fragments Here

        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return 0;
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

                    };
            return titleString;
        }
    }
}