package com.hystle.zach.moviediscover.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.hystle.zach.moviediscover.ui.SearchConditionFragment;
import com.hystle.zach.moviediscover.ui.SearchKeywordFragment;

public class SearchPagerAdapter extends FragmentStatePagerAdapter {

    private String[] mTitles;

    public SearchPagerAdapter(FragmentManager fm, String[] titles) {
        super(fm);
        this.mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return new SearchConditionFragment();
            case 1:
                return new SearchKeywordFragment();
            default:
                return new SearchConditionFragment();
        }
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }
}
