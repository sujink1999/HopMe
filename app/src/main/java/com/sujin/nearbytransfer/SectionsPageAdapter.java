package com.sujin.nearbytransfer;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;


public class SectionsPageAdapter extends FragmentStatePagerAdapter {

    private  List<Fragment> fragments = new ArrayList<>();
    public SectionsPageAdapter(FragmentManager fm) {
        super(fm);
    }


    public void addFragment(Fragment fragment)
    {
        fragments.add(fragment);
    }

    public void removeFragments()
    {
        fragments.clear();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
