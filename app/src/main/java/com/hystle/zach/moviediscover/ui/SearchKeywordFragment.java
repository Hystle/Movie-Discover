package com.hystle.zach.moviediscover.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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
import com.hystle.zach.moviediscover.entity.PersonInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchKeywordFragment extends Fragment {
    private String FLAG;
    private View mRootView;
    private ArrayList mList = new ArrayList<>();
    private ImageView mDeleteIV;
    private EditText mKeywordET;
    private Button mSearchBT;
    private RadioButton mMovieRB;
    private RadioButton mPersonRB;
    private FrameLayout mFrameLayout;
    private LinearLayout mLinearLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.frag_search_keyword, container, false);
        mDeleteIV = (ImageView) mRootView.findViewById(R.id.iv_frag_search_keyword);
        mKeywordET = (EditText) mRootView.findViewById(R.id.et_frag_search_keyword);
        mSearchBT = (Button) mRootView.findViewById(R.id.bt_frag_search_keyword);
        mMovieRB = (RadioButton) mRootView.findViewById(R.id.rb_movie_frag_search_keyword);
        mPersonRB = (RadioButton) mRootView.findViewById(R.id.rb_person_frag_search_keyword);
        mFrameLayout = (FrameLayout) mRootView.findViewById(R.id.fl_frag_search_keyword);
        mLinearLayout = (LinearLayout) mRootView.findViewById(R.id.ll_frag_search_keyword);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDeleteIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKeywordET.setText("");
            }
        });

        mKeywordET.setTextColor(Color.BLACK);
        mKeywordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    mDeleteIV.setVisibility(View.GONE);
                } else {
                    mDeleteIV.setVisibility(View.VISIBLE);
                }
            }
        });

        mSearchBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mKeywordStr = mKeywordET.getText().toString().trim();
                if(mKeywordStr.equals("")){
                    Toast.makeText(getActivity(), "Please enter some keywords", Toast.LENGTH_SHORT).show();
                    return;
                }
                getResultByKeyword();
            }
        });

    }

    private void getResultByKeyword() {
        RequestQueue mQueue = Volley.newRequestQueue(getActivity());
        StringRequest request = new StringRequest(
                buildUriByConditions(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch(FLAG){
                            case Constants.TMDB_MOVIE:
                                getMovieDataFromJson(response);
                                break;
                            case Constants.TMDB_PERSON:
                                getPersonDataFromJson(response);
                                break;
                        }

                        if(mList.size() != 0) {
                            Utility.setDataStatus(getActivity(), Utility.DATA_STATUS_OK);
                            Intent intent = new Intent(getActivity(), SearchResultActivity.class);
                            intent.putExtra(Constants.EXTRA_SEARCH_RESULT, mList);
                            intent.putExtra(Constants.EXTRA_SEARCH_RESULT_FLAG, FLAG);
                            startActivity(intent);

                        }else{
                            Toast.makeText(getActivity(), "Oops, no result found", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utility.handleErrorCases(getActivity(), error);
                        Utility.updateEmptyView(getActivity(), mLinearLayout, mFrameLayout);
                    }
                }
        );
        mQueue.add(request);
    }

    private String buildUriByConditions() {
        Uri uri = Uri.parse(Constants.TMDB_BASE_URL_SEARCH).buildUpon().build();

        if(mMovieRB.isChecked()){
            uri = uri.buildUpon()
                    .appendPath(Constants.TMDB_MOVIE)
                    .build();
            FLAG = Constants.TMDB_MOVIE;
        }else if(mPersonRB.isChecked()){
            uri = uri.buildUpon()
                    .appendPath(Constants.TMDB_PERSON)
                    .build();
            FLAG = Constants.TMDB_PERSON;
        }

        // obtain user input string
        String mKeywordStr = mKeywordET.getText().toString().trim();

        uri = uri.buildUpon()
                .appendQueryParameter(Constants.TMDB_QUERY, mKeywordStr)
                .appendQueryParameter(Constants.API_KEY, BuildConfig.API_KEY)
                .build();

        return uri.toString();
    }

    private void getMovieDataFromJson(String response) {
        try {
            JSONObject moviesObject = new JSONObject(response);
            JSONArray moviesArray = moviesObject.getJSONArray(Constants.TMDB_RESULTS);

            // remove last search results
            mList.clear();
            // get required data from JSON response
            for(int i=0; i<moviesArray.length(); i++){
                JSONObject movieObject = moviesArray.getJSONObject(i);

                MovieInfo movieInfo = new MovieInfo();
                movieInfo.id = movieObject.getString(Constants.TMDB_ID);
                movieInfo.posterPath = movieObject.getString(Constants.TMDB_POSTER_PATH);
                movieInfo.title = movieObject.getString(Constants.TMDB_ORIGINAL_TITLE);
                movieInfo.date = movieObject.getString(Constants.TMDB_RELEASE_DATE);
                movieInfo.vote = movieObject.getString(Constants.TMDB_VOTE_AVERAGE);
                mList.add(movieInfo);
            }
        } catch (JSONException e) {
            Utility.setDataStatus(getActivity(), Utility.DATA_STATUS_SERVER_INVALID);
            e.printStackTrace();
        }
    }


    private void getPersonDataFromJson(String response) {
        try {
            JSONObject personsObject = new JSONObject(response);
            JSONArray personsArray = personsObject.getJSONArray(Constants.TMDB_RESULTS);

            // remove last search results
            mList.clear();

            for(int i=0; i<personsArray.length(); i++){
                JSONObject personObject = personsArray.getJSONObject(i);
                PersonInfo personInfo = new PersonInfo();
                personInfo.id = personObject.getString(Constants.TMDB_ID);
                personInfo.name = personObject.getString(Constants.TMDB_NAME);
                personInfo.profilePath = personObject.getString(Constants.TMDB_PROFILE_PATH);
                mList.add(personInfo);
            }
        } catch (JSONException e) {
            Utility.setDataStatus(getActivity(), Utility.DATA_STATUS_SERVER_INVALID);
            e.printStackTrace();
        }
    }

}
