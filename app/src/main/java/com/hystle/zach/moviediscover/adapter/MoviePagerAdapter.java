package com.hystle.zach.moviediscover.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.hystle.zach.moviediscover.Constants;
import com.hystle.zach.moviediscover.ui.MovieFragment;

public class MoviePagerAdapter extends FragmentStatePagerAdapter {

    private String[] mTitles;
    private String mSection;

    public MoviePagerAdapter(FragmentManager fm, String[] titles, String section) {
        super(fm);
        this.mTitles = titles;
        this.mSection = section;
    }

    @Override
    public Fragment getItem(int position) {
        switch(mSection){
            case Constants.CHARTS:
            case Constants.THEATER:
            case Constants.PERSONS:
            case Constants.MY_RATES: {
                Bundle args = new Bundle();
                args.putString(Constants.SECTION, mSection);
                args.putInt(Constants.PAGE_POSITION, position);
                MovieFragment movieFragment = new MovieFragment();
                movieFragment.setArguments(args);
                return movieFragment;
            }
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(mSection){
            case Constants.CHARTS:
            case Constants.THEATER:
                return mTitles[position];
            case Constants.PERSONS:
                return "HOT PERSONS";
            case Constants.MY_RATES:
                return "MY RATED MOVIES";
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }
}
