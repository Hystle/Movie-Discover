package com.hystle.zach.moviediscover.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.hystle.zach.moviediscover.BuildConfig;
import com.hystle.zach.moviediscover.Constants;
import com.hystle.zach.moviediscover.R;
import com.hystle.zach.moviediscover.Utility;
import com.hystle.zach.moviediscover.entity.MovieInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FinderActivity extends AppCompatActivity {

    private static final String NOT_SELECTED = "not_selected";
    private static final String DESC = "desc";
    private static final String ASC = "asc";
    private static final String POPULARITY = "popularity";
    private static final String REVENUE = "revenue";
    private static final String RELEASE_DATE = "release_date";
    private static final String ORIGINAL_TITLE = "original_title";
    private static final String VOTE_AVERAGE = "vote_average";

    private Spinner genreSP;
    private Spinner startYearSP;
    private Spinner endYearSP;
    private SeekBar voteSB;
    private TextView voteTV;
    private Spinner sortbySP;
    private Spinner sortorderSP;
    private Button searchBT;
    private Button clearBT;

    private String mGenre;
    private String mStartYear;
    private String mEndYear;
    private String mSortBy;
    private String mSortOrder;
    private String mVoteAverage;

    ArrayList<MovieInfo> mMoviesList = new ArrayList<>();
    FinderFragment fragment;
    private FrameLayout mFrameLayout;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finder);

        // init views
        initViews();

        // setup spinners
        configSpinner(genreSP, R.array.genre_array);
        configSpinner(startYearSP, R.array.year_array);
        configSpinner(endYearSP, R.array.year_array);
        configSpinner(sortbySP, R.array.sort_by);
        configSpinner(sortorderSP, R.array.sort_order);

        // handle spinner choice
        addSpinnerListener();

        // set up listener for seekbar change
        voteSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setSelectedVoteText(seekBar);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                setSelectedVoteText(seekBar);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setSelectedVoteText(seekBar);
                mVoteAverage = seekBar.getProgress() + "";
            }
            // Set display text
            private void setSelectedVoteText(SeekBar seekBar) {
                String text;
                if (seekBar.getProgress() == 10) {
                    text = seekBar.getProgress() + "";
                } else {
                    text = seekBar.getProgress() + " +";
                }
                voteTV.setText(text);
            }
        });

        // handle buttons click
        searchBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getResultByConditions();
            }
        });
        clearBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genreSP.setSelection(0);
                startYearSP.setSelection(0);
                endYearSP.setSelection(0);
                sortbySP.setSelection(0);
                sortorderSP.setSelection(0);
                voteSB.setProgress(0);
                voteTV.setText("");
                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }
            }
        });
    }

    private void initViews() {
        genreSP = (Spinner) findViewById(R.id.spinner_genre);
        startYearSP = (Spinner) findViewById(R.id.spinner_start_year);
        endYearSP = (Spinner) findViewById(R.id.spinner_end_year);
        voteSB = (SeekBar) findViewById(R.id.seekbar_vote_average);
        voteTV = (TextView) findViewById(R.id.tv_frag_picker_vote_average);
        sortbySP = (Spinner) findViewById(R.id.spinner_sort_by);
        sortorderSP = (Spinner) findViewById(R.id.spinner_sort_order);
        searchBT = (Button) findViewById(R.id.bt_frag_picker_search);
        clearBT = (Button) findViewById(R.id.bt_frag_picker_clear);

        mFrameLayout = (FrameLayout) findViewById(R.id.fl_finder_activity);
        scrollView = (ScrollView) findViewById(R.id.sv_activity_finder);
    }

    private void configSpinner(Spinner spinner, int arrayResId) {
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, arrayResId, R.layout.item_spinner);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setVisibility(View.VISIBLE);
    }

    /**
     * request data from server by applying conditions user made
     */
    private void getResultByConditions() {
        Uri uri = buildUriByConditions();
        RequestQueue mQueue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(
                uri.toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getMovieDataFromJson(response);
                        // pass moviesInfo to FinderFragment for display
                        if(mMoviesList.size() != 0) {
                            Utility.setDataStatus(FinderActivity.this, Utility.DATA_STATUS_OK);
                            Bundle args = new Bundle();
                            args.putSerializable("BUNDLE", mMoviesList);
                            fragment = new FinderFragment();
                            fragment.setArguments(args);

                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.ll_activity_finder, fragment).commit();
                        }else{
                            Toast.makeText(FinderActivity.this, "Oops, please check your input", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utility.handleErrorCases(FinderActivity.this, error);
                        Utility.updateEmptyView(FinderActivity.this, scrollView, mFrameLayout);
                    }
                }
        );
        mQueue.add(request);
    }

    private void getMovieDataFromJson(String jsonStr) {
        try {
            JSONObject moviesObject = new JSONObject(jsonStr);
            JSONArray moviesArray = moviesObject.getJSONArray(Constants.TMDB_RESULTS);

            // remove last search results
            mMoviesList.clear();
            // get required data from JSON response
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
            Utility.setDataStatus(FinderActivity.this, Utility.DATA_STATUS_SERVER_INVALID);
            e.printStackTrace();
        }
    }

    private Uri buildUriByConditions() {
        final String GENRE = "with_genres";
        final String START_YEAR = "primary_release_date.gte";
        final String END_YEAR = "primary_release_date.lte";
        final String VOTE_AVERAGE_GTE = "vote_average.gte";
        final String SORT_BY = Constants.TMDB_SORT_BY;

        final String POPULARITY_DESC = "popularity.desc";
        final String POPULARITY_ASC = "popularity.asc";
        final String RELEASE_DATE_DESC = "primary_release_date.desc";
        final String RELEASE_DATE_ASC = "primary_release_date.asc";
        final String REVENUE_DESC = "revenue.desc";
        final String REVENUE_ASC = "revenue.asc";
        final String ORIGINAL_TITLE_DESC = "original_title.desc";
        final String ORIGINAL_TITLE_ASC = "original_title.asc";
        final String VOTE_AVERAGE_DESC = "vote_average.desc";
        final String VOTE_AVERAGE_ASC = "vote_average.asc";

        Uri uri = Uri.parse(Constants.TMDB_BASE_URL_DISCOVER_MOVIE).buildUpon().build();

        if(!mStartYear.equals(NOT_SELECTED)){
            uri = uri.buildUpon().appendQueryParameter(START_YEAR, mStartYear).build();
        }
        if(!mEndYear.equals(NOT_SELECTED)){
            uri = uri.buildUpon().appendQueryParameter(END_YEAR, mEndYear).build();
        }
        if(!mSortBy.equals(NOT_SELECTED)) {
            switch (mSortBy) {
                case POPULARITY:
                    if (mSortOrder.equals(ASC)) {
                        uri = uri.buildUpon().appendQueryParameter(SORT_BY, POPULARITY_ASC).build();
                    } else if (mSortOrder.equals(DESC)) {
                        uri = uri.buildUpon().appendQueryParameter(SORT_BY, POPULARITY_DESC).build();
                    }
                    break;
                case REVENUE:
                    if (mSortOrder.equals(ASC)) {
                        uri = uri.buildUpon().appendQueryParameter(SORT_BY, REVENUE_ASC).build();
                    } else if (mSortOrder.equals(DESC)) {
                        uri = uri.buildUpon().appendQueryParameter(SORT_BY, REVENUE_DESC).build();
                    }
                    break;
                case RELEASE_DATE:
                    if (mSortOrder.equals(ASC)) {
                        uri = uri.buildUpon().appendQueryParameter(SORT_BY, RELEASE_DATE_ASC).build();
                    } else if (mSortOrder.equals(DESC)) {
                        uri = uri.buildUpon().appendQueryParameter(SORT_BY, RELEASE_DATE_DESC).build();
                    }
                    break;
                case ORIGINAL_TITLE:
                    if (mSortOrder.equals(ASC)) {
                        uri = uri.buildUpon().appendQueryParameter(SORT_BY, ORIGINAL_TITLE_ASC).build();
                    } else if (mSortOrder.equals(DESC)) {
                        uri = uri.buildUpon().appendQueryParameter(SORT_BY, ORIGINAL_TITLE_DESC).build();
                    }
                    break;
                case VOTE_AVERAGE:
                    if (mSortOrder.equals(ASC)) {
                        uri = uri.buildUpon().appendQueryParameter(SORT_BY, VOTE_AVERAGE_ASC).build();
                    } else if (mSortOrder.equals(DESC)) {
                        uri = uri.buildUpon().appendQueryParameter(SORT_BY, VOTE_AVERAGE_DESC).build();
                    }
                    break;
            }
        }
        if(mVoteAverage != null){
            uri = uri.buildUpon().appendQueryParameter(VOTE_AVERAGE_GTE, mVoteAverage).build();
        }
        if(!mGenre.equals(NOT_SELECTED)){
            uri = uri.buildUpon().appendQueryParameter(GENRE, mGenre).build();
        }
        uri = uri.buildUpon().appendQueryParameter(Constants.API_KEY, BuildConfig.API_KEY).build();
        return  uri;
    }

    private void addSpinnerListener() {
        genreSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedText = parent.getItemAtPosition(position).toString();
                switch(selectedText){
                    case "Any":
                        mGenre = NOT_SELECTED;
                        break;
                    case "Action":
                        mGenre = "28";
                        break;
                    case "Adventure":
                        mGenre = "12";
                        break;
                    case "Animation":
                        mGenre = "16";
                        break;
                    case "Comedy":
                        mGenre = "35";
                        break;
                    case "Crime":
                        mGenre = "80";
                        break;
                    case "Documentary":
                        mGenre = "99";
                        break;
                    case "Drama":
                        mGenre = "18";
                        break;
                    case "Family":
                        mGenre = "10751";
                        break;
                    case "Fantasy":
                        mGenre = "14";
                        break;
                    case "Foreign":
                        mGenre = "10769";
                        break;
                    case "History":
                        mGenre = "36";
                        break;
                    case "Horror":
                        mGenre = "27";
                        break;
                    case "Music":
                        mGenre = "10402";
                        break;
                    case "Mystery":
                        mGenre = "9648";
                        break;
                    case "Romance":
                        mGenre = "10749";
                        break;
                    case "Science Fiction":
                        mGenre = "878";
                        break;
                    case "TV Movie":
                        mGenre = "10770";
                        break;
                    case "Thriller":
                        mGenre = "53";
                        break;
                    case "War":
                        mGenre = "10752";
                        break;
                    case "Western":
                        mGenre = "37";
                        break;
                    default:
                        mGenre = NOT_SELECTED;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        startYearSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedText = parent.getItemAtPosition(position).toString();
                switch(selectedText){
                    case "Any":
                        mStartYear = NOT_SELECTED;
                        break;
                    case "1950s":
                        mStartYear = "1950-01-01";
                        endYearSP.setSelection(2);
                        break;
                    case "1960s":
                        mStartYear = "1960-01-01";
                        endYearSP.setSelection(3);
                        break;
                    case "1970s":
                        mStartYear = "1970-01-01";
                        endYearSP.setSelection(4);
                        break;
                    case "1980s":
                        mStartYear = "1980-01-01";
                        endYearSP.setSelection(5);
                        break;
                    case "1990s":
                        mStartYear = "1990-01-01";
                        endYearSP.setSelection(6);
                        break;
                    case "2000s":
                        mStartYear = "2000-01-01";
                        endYearSP.setSelection(7);
                        break;
                    case "Nowadays":
                        mStartYear = "2010-01-01";
                        endYearSP.setSelection(7);
                        break;
                    default:
                        mStartYear = NOT_SELECTED;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        endYearSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedText = parent.getItemAtPosition(position).toString();
                switch(selectedText){
                    case "Any":
                        mEndYear = NOT_SELECTED;
                        break;
                    case "1950s":
                        mEndYear = "1950-12-31";
                        break;
                    case "1960s":
                        mEndYear = "1960-12-31";
                        break;
                    case "1970s":
                        mEndYear = "1970-12-31";
                        break;
                    case "1980s":
                        mEndYear = "1980-12-31";
                        break;
                    case "1990s":
                        mEndYear = "1990-12-31";
                        break;
                    case "2000s":
                        mEndYear = "2000-12-31";
                        break;
                    case "Nowadays":
                        mEndYear = "2016-03-31";
                        break;
                    default:
                        mEndYear = NOT_SELECTED;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        sortbySP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedText = parent.getItemAtPosition(position).toString();
                switch(selectedText){
                    case "Any":
                        mSortBy = NOT_SELECTED;
                        break;
                    case "Popularity":
                        mSortBy = POPULARITY;
                        break;
                    case "Release Date":
                        mSortBy = RELEASE_DATE;
                        break;
                    case "Revenue":
                        mSortBy = REVENUE;
                        break;
                    case "Original Title":
                        mSortBy = ORIGINAL_TITLE;
                        break;
                    case "Vote Average":
                        mSortBy = VOTE_AVERAGE;
                        break;
                    default:
                        mSortBy = NOT_SELECTED;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        sortorderSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedText = parent.getItemAtPosition(position).toString();
                switch(selectedText){
                    case "High to low":
                        mSortOrder = DESC;
                        break;
                    case "Low to high":
                        mSortOrder = ASC;
                        break;
                    default:
                        mSortOrder = NOT_SELECTED;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (item.getItemId()){
            case R.id.action_setting:
                startActivity(new Intent(this, SettingsActivity.class));
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
}
