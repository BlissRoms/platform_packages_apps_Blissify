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
import com.blissroms.blissify.fragments.qs.Panel;

/**
 * Created by jackeagle on 31/12/17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class QuickSettings extends Fragment {

    private View view;
    private ViewPager viewPager;
    private TabLayout tableLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.blissify_fixed_tablayout, container, false);

        viewPager = view.findViewById(R.id.viewpager);
        viewPager.setAdapter(new QuickSettingsAdapter(getChildFragmentManager()));
        tableLayout = view.findViewById(R.id.fixed_tabs);
        tableLayout.post(new Runnable() {
            @Override
            public void run() {
                tableLayout.setupWithViewPager(viewPager);
            }
        });

        return view;
    }

    private class QuickSettingsAdapter extends FragmentPagerAdapter {

        final String[] tabs= getTabsTitle();
        private final Fragment[] frags = new Fragment[tabs.length];

        public QuickSettingsAdapter(FragmentManager fm) {
            super(fm);
            // Add Fragments Here
            frags[0] = new Panel();

        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return 1;
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
                        getString(R.string.qs_panel_title)

                    };
            return titleString;
        }
    }
}
