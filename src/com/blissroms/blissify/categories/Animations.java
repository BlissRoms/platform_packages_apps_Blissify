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
import android.provider.Settings;
import com.android.internal.util.bliss.AwesomeAnimationHelper;
import com.blissroms.blissify.R;
import com.blissroms.blissify.fragments.animations.SystemAnimation;
import com.blissroms.blissify.fragments.animations.ToastAnimation;
import com.blissroms.blissify.fragments.animations.QSTiles;

/**
 * Created by jackeagle on 31/12/17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Animations extends Fragment {

    private View view;
    private ViewPager viewPager;
    private TabLayout tableLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.blissify_fixed_tablayout, container, false);

        viewPager = view.findViewById(R.id.viewpager);
        viewPager.setAdapter(new AnimationAdapter(getChildFragmentManager()));
        tableLayout = view.findViewById(R.id.fixed_tabs);
        tableLayout.post(new Runnable() {
            @Override
            public void run() {
                tableLayout.setupWithViewPager(viewPager);
            }
        });

        return view;
    }

    private class AnimationAdapter extends FragmentPagerAdapter {

        final String[] tabs= getTabsTitle();
        private final Fragment[] frags = new Fragment[tabs.length];

        public AnimationAdapter(FragmentManager fm) {
            super(fm);
            // Add Fragments Here
            frags[0] = new SystemAnimation();
            frags[1] = new ToastAnimation();
            frags[2] = new QSTiles();
        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return 3;
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
                            getString(R.string.toast_animation_title),
                            getString(R.string.qstiles_animation_title)
                    };
            return titleString;
        }
    }
}
