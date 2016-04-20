package com.hystle.zach.moviediscover.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.hystle.zach.moviediscover.R;
import com.hystle.zach.moviediscover.adapter.SearchPagerAdapter;

public class SearchActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private String[] mTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        initData();
        configViews();
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.id_search_toolbar);
        mTabLayout = (TabLayout) findViewById(R.id.id_search_tablayout);
        mViewPager = (ViewPager) findViewById(R.id.id_search_viewpager);
    }

    private void initData() {
        mTitles = getResources().getStringArray(R.array.tab_titles_search);
    }

    private void configViews() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SearchPagerAdapter searchPagerAdapter = new SearchPagerAdapter(getSupportFragmentManager(), mTitles);
        mViewPager.setAdapter(searchPagerAdapter);
        mViewPager.addOnPageChangeListener(this);

        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mToolbar.setTitle("Search");
    }

    @Override
    public void onPageSelected(int position) {}

    @Override
    public void onPageScrollStateChanged(int state) {}
}
