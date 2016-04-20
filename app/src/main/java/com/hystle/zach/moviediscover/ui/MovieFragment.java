package com.hystle.zach.moviediscover.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.hystle.zach.moviediscover.BuildConfig;
import com.hystle.zach.moviediscover.Constants;
import com.hystle.zach.moviediscover.R;
import com.hystle.zach.moviediscover.Utility;
import com.hystle.zach.moviediscover.adapter.RecyclerViewAdapter;
import com.hystle.zach.moviediscover.data.MovieContract;
import com.hystle.zach.moviediscover.entity.MovieInfo;
import com.hystle.zach.moviediscover.entity.MySwipeRefreshLayout;
import com.hystle.zach.moviediscover.entity.PersonInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MovieFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener,
        RecyclerViewAdapter.OnItemClickListener{

    private static final int SPAN_COUNT = 2;
    private View mRootView;
    private MySwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private FrameLayout mFrameLayout;
    private String mSection;
    private int mPageNo;
    RequestQueue mQueue;
    private Context mContext;
    ArrayList<MovieInfo> mMoviesList = new ArrayList<>();
    ArrayList<PersonInfo> mPersonsList = new ArrayList<>();

    private int mLastVisibleItem;
    private int mResponsePageNo = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSection = (String)getArguments().get(Constants.SECTION);
        mPageNo = (int)getArguments().get(Constants.PAGE_POSITION);
        mRootView = inflater.inflate(R.layout.frag_main, container, false);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // FrameLayout for emptyView
        mFrameLayout = (FrameLayout) mRootView.findViewById(R.id.fl_frag_main);

        // MySwipeRefreshLayout overrides the default
        // SwipeRefreshLayout to enable setRefreshing when started
        mSwipeRefreshLayout = (MySwipeRefreshLayout) mRootView.findViewById(R.id.id_swiperefreshlayout);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.MAGENTA, Color.YELLOW, Color.BLUE, Color.GREEN);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // scrollListener for loading more movies when user scrolls to the bottom and still drags for more
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.id_recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_DRAGGING
                        && mLastVisibleItem + 1 == mRecyclerViewAdapter.getItemCount()){
                    if(!mSection.equals(Constants.MY_RATES)) {
                        Snackbar.make(mRootView, "Loading more...", Snackbar.LENGTH_SHORT).show();
                        mResponsePageNo ++;
                        loadMovieDataFromServer(mResponsePageNo);
                    }
                }
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mLastVisibleItem = ((LinearLayoutManager)mLayoutManager).findLastVisibleItemPosition();
            }
        });

        // start to send request to server for data according to section selected
        switch(mSection){
            case Constants.CHARTS:
            case Constants.THEATER:
            case Constants.PERSONS:
                mQueue = Volley.newRequestQueue(mContext);
                loadMovieDataFromServer(mResponsePageNo);
                break;
            case Constants.MY_RATES:
                loadMyRatesFromDb();
                break;
        }
        // config recyclerViewAdapter depending on needs
        configRecyclerView();
    }

    /**
     * Helper function to load data from local database
     */
    private void loadMyRatesFromDb() {
        ContentResolver resolver = mContext.getContentResolver();
        Cursor cursor = resolver.query(MovieContract.CONTENT_RATED_URI, null, null, null, null);
        if(cursor != null && cursor.moveToFirst()) {
            // cursor has been moved to the 1st row at this point for empty check
            cursor.moveToPrevious();
            mMoviesList.clear();
            while (cursor.moveToNext()) {
                MovieInfo movieInfo = new MovieInfo();
                movieInfo.id = cursor.getString(1);
                movieInfo.posterPath = cursor.getString(2);
                mMoviesList.add(movieInfo);
            }
            cursor.close();
        }else{
            displayEmptyView();
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void displayEmptyView() {
        TextView emptyView = new TextView(mContext);
        emptyView.setTextSize(16);
        emptyView.setTextColor(Color.BLACK);
        emptyView.setText(R.string.empty_rated_list);
        emptyView.setPadding(0, 200, 0, 0);
        emptyView.setGravity(Gravity.CENTER_HORIZONTAL);
        emptyView.setVisibility(View.VISIBLE);
        mSwipeRefreshLayout.setVisibility(View.GONE);
        mFrameLayout.addView(emptyView);
    }

    /**
     * helper function load movie data from TMDB server
     * @param responsePageNo
     */
    private void loadMovieDataFromServer(int responsePageNo) {
        StringRequest request = new StringRequest(
                buildRequestUrl(responsePageNo),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch(mSection){
                            case Constants.CHARTS:
                            case Constants.THEATER:
                                getMovieDataFromJson(response);
                                break;
                            case Constants.PERSONS:
                                getPersonDataFromJson(response);
                                break;
                        }
                        mRecyclerViewAdapter.notifyDataSetChanged();

                        Utility.setDataStatus(mContext, Utility.DATA_STATUS_OK);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utility.handleErrorCases(mContext, error);
                        Utility.updateEmptyView(mContext, mSwipeRefreshLayout, mFrameLayout);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
        mQueue.add(request);
    }

    private String buildRequestUrl(int responsePageNo) {
        Uri uri = null;
        switch(mSection) {
            case Constants.CHARTS: {
                switch (mPageNo) {
                    case Constants.POPULAR:
                        uri = Uri.parse(Constants.TMDB_BASE_URL_MOVIE).buildUpon()
                                .appendPath(Constants.TMDB_POPULAR)
                                .appendQueryParameter(Constants.TMDB_PAGE, responsePageNo+"")
                                .appendQueryParameter(Constants.API_KEY, BuildConfig.API_KEY)
                                .build();
                        break;
                    case Constants.REVENUE:
                        uri = Uri.parse(Constants.TMDB_BASE_URL_DISCOVER_MOVIE).buildUpon()
                                .appendQueryParameter(Constants.TMDB_PAGE, responsePageNo + "")
                                .appendQueryParameter(Constants.TMDB_SORT_BY, Constants.TMDB_REVENUE_DESC)
                                .appendQueryParameter(Constants.API_KEY, BuildConfig.API_KEY)
                                .build();
                        break;
                    case Constants.VOTE:
                        uri = Uri.parse(Constants.TMDB_BASE_URL_MOVIE).buildUpon()
                                .appendPath(Constants.TMDB_TOP_RATED)
                                .appendQueryParameter(Constants.TMDB_PAGE, responsePageNo+"")
                                .appendQueryParameter(Constants.API_KEY, BuildConfig.API_KEY)
                                .build();
                        break;
                }
                break;
            }
            case Constants.THEATER: {
                uri = Uri.parse(Constants.TMDB_BASE_URL_MOVIE).buildUpon()
                        .appendQueryParameter(Constants.TMDB_PAGE, responsePageNo+"").build();
                switch (mPageNo) {
                    case Constants.NOW_PLAYING:
                        uri = uri.buildUpon()
                                .appendPath(Constants.TMDB_NOW_PLAYING)
                                .appendQueryParameter(Constants.API_KEY, BuildConfig.API_KEY)
                                .build();
                        break;
                    case Constants.UPCOMING:
                        uri = uri.buildUpon()
                                .appendPath(Constants.TMDB_UPCOMING)
                                .appendQueryParameter(Constants.API_KEY, BuildConfig.API_KEY)
                                .build();
                        break;
                }
                break;
            }
            case Constants.PERSONS:{
                uri = Uri.parse(Constants.TMDB_BASE_URL_PERSON).buildUpon()
                        .appendPath(Constants.TMDB_POPULAR)
                        .appendQueryParameter(Constants.TMDB_PAGE, responsePageNo+"")
                        .appendQueryParameter(Constants.API_KEY, BuildConfig.API_KEY)
                        .build();
            }
        }
        if(uri == null){
            return null;
        }
        return uri.toString();
    }

    private void getMovieDataFromJson(String jsonStr) {
        try {
            JSONObject moviesObject = new JSONObject(jsonStr);
            JSONArray moviesArray = moviesObject.getJSONArray(Constants.TMDB_RESULTS);

            for(int i=0; i<moviesArray.length(); i++){
                JSONObject movieObject = moviesArray.getJSONObject(i);

                MovieInfo movieInfo = new MovieInfo();
                movieInfo.id = movieObject.getString(Constants.TMDB_ID);
                movieInfo.posterPath = movieObject.getString(Constants.TMDB_POSTER_PATH);
                movieInfo.title = movieObject.getString(Constants.TMDB_ORIGINAL_TITLE);
                movieInfo.date = movieObject.getString(Constants.TMDB_RELEASE_DATE);
                movieInfo.vote = movieObject.getString(Constants.TMDB_VOTE_AVERAGE);
                mMoviesList.add(movieInfo);
            }
        } catch (JSONException e) {
            // returning data is invalid
            Utility.setDataStatus(mContext, Utility.DATA_STATUS_SERVER_INVALID);
            e.printStackTrace();
        }
    }


    private void getPersonDataFromJson(String response) {
        try {
            JSONObject personsObject = new JSONObject(response);
            JSONArray personsArray = personsObject.getJSONArray(Constants.TMDB_RESULTS);
            for(int i=0; i<personsArray.length(); i++){
                JSONObject personObject = personsArray.getJSONObject(i);

                PersonInfo personInfo = new PersonInfo();
                personInfo.id = personObject.getString(Constants.TMDB_ID);
                personInfo.name = personObject.getString(Constants.TMDB_NAME);
                personInfo.profilePath = personObject.getString(Constants.TMDB_PROFILE_PATH);

//                JSONArray creditsArr = personObject.getJSONArray(Constants.TMDB_KNOWN_FOR);
//                for(int j=0; j<creditsArr.length(); j++){
//                   JSONObject creditObj = creditsArr.getJSONObject(j);
//                    String credit = creditObj.getString(Constants.TMDB_ORIGINAL_TITLE);
//                    personInfo.creditsList.add(credit);
//                }
                mPersonsList.add(personInfo);
            }
        } catch (JSONException e) {
            Utility.setDataStatus(mContext, Utility.DATA_STATUS_SERVER_INVALID);
            e.printStackTrace();
        }
    }

    /**
     * Set recyclerViewAdapter
     */
    private void configRecyclerView(){
        switch(mSection){
            case Constants.CHARTS:
            case Constants.MY_RATES:
                mLayoutManager = new GridLayoutManager(mContext, SPAN_COUNT, GridLayoutManager.VERTICAL, false);
                mRecyclerViewAdapter = new RecyclerViewAdapter(mContext, mSection, mPageNo, mMoviesList);
                break;
            case Constants.THEATER:
                mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
                mRecyclerViewAdapter = new RecyclerViewAdapter(mContext, mSection, mPageNo, mMoviesList);
                break;
            case Constants.PERSONS:
                mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
                mRecyclerViewAdapter = new RecyclerViewAdapter(mContext, mSection, mPageNo, mPersonsList);
                break;
        }
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerViewAdapter.setOnItemClickListener(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    @Override
    public void onRefresh() {
        mMoviesList.clear();
        switch(mSection){
            case Constants.CHARTS:
            case Constants.THEATER:
                mResponsePageNo = 1;
                loadMovieDataFromServer(mResponsePageNo);
                break;
            case Constants.MY_RATES:
                loadMyRatesFromDb();
                break;
        }
    }

    /**
     * Movie is selected: go to DetailActivity
     * @param view
     * @param position
     */
    @Override
    public void onItemClick(View view, int position) {
       if(!mSection.equals(Constants.PERSONS)) {
            Intent intent = new Intent(mContext, DetailActivity.class);
            intent.putExtra(Constants.EXTRA_ID, mMoviesList.get(position).id);
            intent.putExtra(Constants.EXTRA_TITLE, mMoviesList.get(position).title);
            startActivity(intent);
        }else{
           Intent intent = new Intent(mContext, CastActivity.class);
           intent.putExtra(Constants.EXTRA_CAST, (mPersonsList.get(position)).id);
           intent.putExtra(Constants.EXTRA_CAST_FLAG, Constants.EXTRA_CAST);
           startActivity(intent);
       }
    }

    @Override
    public void onItemLongClick(View view, int position) {
    }

    /**
     * save context object: better than getActivity()
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
}
