package com.hystle.zach.moviediscover.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
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

public class SearchConditionFragment extends Fragment {
    private View mRootView;

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
    private FrameLayout mFrameLayout;
    private NestedScrollView mScrollView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.frag_search_condition, container, false);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
            }
        });
    }

    private void initViews() {
        genreSP = (Spinner) mRootView.findViewById(R.id.spinner_genre);
        startYearSP = (Spinner) mRootView.findViewById(R.id.spinner_start_year);
        endYearSP = (Spinner) mRootView.findViewById(R.id.spinner_end_year);
        voteSB = (SeekBar) mRootView.findViewById(R.id.seekbar_vote_average);
        voteTV = (TextView) mRootView.findViewById(R.id.tv_frag_picker_vote_average);
        sortbySP = (Spinner) mRootView.findViewById(R.id.spinner_sort_by);
        sortorderSP = (Spinner) mRootView.findViewById(R.id.spinner_sort_order);
        searchBT = (Button) mRootView.findViewById(R.id.bt_frag_picker_search);
        clearBT = (Button) mRootView.findViewById(R.id.bt_frag_picker_clear);

        mFrameLayout = (FrameLayout) mRootView.findViewById(R.id.fl_frag_search_condition);
        mScrollView = (NestedScrollView) mRootView.findViewById(R.id.sv_frag_search_condition);
    }

    private void configSpinner(Spinner spinner, int arrayResId) {
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), arrayResId, R.layout.item_spinner);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setVisibility(View.VISIBLE);
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

    /**
     * request data from server by applying conditions user made
     */
    private void getResultByConditions() {
        RequestQueue mQueue = Volley.newRequestQueue(getActivity());
        StringRequest request = new StringRequest(
                buildUriByConditions(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getMovieDataFromJson(response);
                        // pass moviesInfo to FinderFragment for display
                        if(mMoviesList.size() != 0) {
                            Utility.setDataStatus(getActivity(), Utility.DATA_STATUS_OK);

                            Intent intent = new Intent(getActivity(), SearchResultActivity.class);
                            intent.putExtra(Constants.EXTRA_SEARCH_RESULT, mMoviesList);
                            intent.putExtra(Constants.EXTRA_SEARCH_RESULT_FLAG, Constants.TMDB_MOVIE);
                            startActivity(intent);

                        }else{
                            Toast.makeText(getActivity(), "Oops, please check your input", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utility.handleErrorCases(getActivity(), error);
                        Utility.updateEmptyView(getActivity(), mScrollView, mFrameLayout);
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

                JSONArray genreArray = movieObject.getJSONArray(Constants.TMDB_GENRE_IDS);
                if (genreArray.length() != 0) {
                    movieInfo.genre = genreArray.get(0).toString();
                }

                mMoviesList.add(movieInfo);
            }
        } catch (JSONException e) {
            Utility.setDataStatus(getActivity(), Utility.DATA_STATUS_SERVER_INVALID);
            e.printStackTrace();
        }
    }

    private String buildUriByConditions() {
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
        return  uri.toString();
    }
}
