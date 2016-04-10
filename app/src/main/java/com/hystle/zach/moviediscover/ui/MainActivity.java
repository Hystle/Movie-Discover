package com.hystle.zach.moviediscover.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.hystle.zach.moviediscover.Constants;
import com.hystle.zach.moviediscover.R;
import com.hystle.zach.moviediscover.adapter.MoviePagerAdapter;

public class MainActivity extends AppCompatActivity
        implements ViewPager.OnPageChangeListener, NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private NavigationView mNavigationView;

    private String[] mTitles;
    private String mSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // init UI component
        initViews();
        // retrieve the right section when user rotate the screen
        mSection = (String) getLastCustomNonConfigurationInstance();
        if(mSection == null) {
            // no saved section selection: start from discovery section
            initData(Constants.CHARTS);
        }else{
            // recover the selected section
            initData(mSection);
        }
        // setup toolbar, drawer, viewPaper
        configViews();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mSection;
    }

    private void initViews() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.id_drawerlayout);
        mToolbar = (Toolbar) findViewById(R.id.id_toolbar_main);
        mTabLayout = (TabLayout) findViewById(R.id.id_tablayout);
        mViewPager = (ViewPager) findViewById(R.id.id_viewpager);
        mNavigationView = (NavigationView) findViewById(R.id.id_navigationview);
    }

    private void initData(String section) {
        // init tab titles
        switch(section){
            case Constants.CHARTS:
                mTitles = getResources().getStringArray(R.array.tab_titles_discovery);
                mSection = Constants.CHARTS;
                break;
            case Constants.THEATER:
                mTitles = getResources().getStringArray(R.array.tab_titles_theaters);
                mSection = Constants.THEATER;
                break;
            case Constants.PERSONS:
                mTitles = new String[]{"Hot Persons"};
                mSection = Constants.PERSONS;
                break;
            case Constants.MY_RATES:
                mTitles = new String[]{"My Rates"};
                mSection = Constants.MY_RATES;
                break;
        }
    }

    private void configViews() {
        // Toolbar
        setSupportActionBar(mToolbar);

        // DrawerToggle
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        // NavigationView
        mNavigationView.setNavigationItemSelectedListener(this);

        // ViewPager & Adapter
        MoviePagerAdapter mMoviePagerAdapter = new MoviePagerAdapter(getSupportFragmentManager(), mTitles, mSection);
        mViewPager.setAdapter(mMoviePagerAdapter);
        mViewPager.addOnPageChangeListener(this);

        // TabLayout
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_charts:
                switchSection(Constants.CHARTS);
                break;
            case R.id.action_in_theaters:
                switchSection(Constants.THEATER);
                break;
            case R.id.action_popular_persons:
                switchSection(Constants.PERSONS);
                break;
            case R.id.action_search:
                startActivity(new Intent(this, FinderActivity.class));
                break;
            case R.id.action_rated:
                switchSection(Constants.MY_RATES);
                break;
            case R.id.action_watch_list:
                Toast.makeText(this, "action_watch_list", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_rate_app:
                Toast.makeText(this, "action_rate_app", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_feedback:
                Toast.makeText(this, "action_feedback", Toast.LENGTH_SHORT).show();
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void switchSection(String section) {
        initData(section);
        configViews();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mToolbar.setTitle(mTitles[position]);
    }

    @Override
    public void onPageSelected(int position) {}

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_setting:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_about:
                new AlertDialog.Builder(this)
                        .setTitle("About")
                        .setMessage(R.string.about)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // press twice to exit function
    private boolean exit = false;
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            if(!exit){
                exit = true;
                Toast.makeText(MainActivity.this, "Press again to exit", Toast.LENGTH_SHORT).show();
                handler.sendEmptyMessageDelayed(1, 2000);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @SuppressWarnings("handlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1){
                exit = false;
            }
        }
    };

}
