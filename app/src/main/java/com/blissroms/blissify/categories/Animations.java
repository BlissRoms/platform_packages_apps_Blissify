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
import com.blissroms.blissify.fragments.SystemAnimation;
import com.blissroms.blissify.fragments.ToastAnimation;

/**
 * Created by jackeagle on 31/12/17.
 */

public class Animations extends Fragment {

    View view;
    ViewPager viewPager;
    TabLayout tableLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.blissify_tablayout, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setAdapter(new AnimationAdapter(getChildFragmentManager()));
        tableLayout = (TabLayout) view.findViewById(R.id.sliding_tabs);
        tableLayout.post(new Runnable() {
            @Override
            public void run() {
                tableLayout.setupWithViewPager(viewPager);
            }
        });

        return view;
    }

    private class AnimationAdapter extends FragmentPagerAdapter {

        String tabs[]= getTabsTitle();
        private Fragment frags[] = new Fragment[tabs.length];

        public AnimationAdapter(FragmentManager fm) {
            super(fm);
            // Add Fragments Here
            frags[0] = new SystemAnimation();
            frags[1] = new ToastAnimation();
        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return 2;
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
                            getString(R.string.system_animation_title),
                            getString(R.string.toast_animation_title)
                    };
            return titleString;
        }
    }
}
