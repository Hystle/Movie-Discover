package com.hystle.zach.moviediscover.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.github.florent37.glidepalette.GlidePalette;
import com.hystle.zach.moviediscover.BuildConfig;
import com.hystle.zach.moviediscover.Constants;
import com.hystle.zach.moviediscover.R;
import com.hystle.zach.moviediscover.Utility;
import com.hystle.zach.moviediscover.adapter.RecyclerDetailAdapter;
import com.hystle.zach.moviediscover.entity.MovieInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class CastDetailFragment extends Fragment implements RecyclerDetailAdapter.OnItemClickListener {
    private static final String ARG_CAST = "arg_cast";
    private Context mContext;
    private String mCastId;
    private TextView nameTV;
    private ImageView profileIV;
    private TextView birthplaceTV;
    private TextView birthdayTV;
    private TextView deathdayTV;
    private TextView deathdayTextTV;
    private TextView bioTV;
    private RequestQueue mQueue;
    private ProgressDialog progressDialog;
    private RecyclerView castCreditsRV;
    private RecyclerDetailAdapter mCastCreditsAdapter;
    private TextView emptyCastCreditsTV;
    private TextView castCreditsHintTV;
    private LinearLayout mainViewLL;
    private FrameLayout emptyViewFL;

    private ArrayList<MovieInfo> mMoviesList = new ArrayList<>();

    public CastDetailFragment() {}

    public static CastDetailFragment newInstance(String s) {
        CastDetailFragment fragment = new CastDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CAST, s);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCastId = getArguments().getString(ARG_CAST);
            progressDialog = ProgressDialog.show(mContext, null, "Loading...");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mQueue = Volley.newRequestQueue(mContext);
        // get cast basic info
        updateCastInfo();
        // get cast's more movie credits
        updateCastCredits();
        configRecyclerView();
    }

    private void configRecyclerView() {
        RecyclerView.LayoutManager castCreditsLM
                = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        castCreditsRV.setLayoutManager(castCreditsLM);
        mCastCreditsAdapter = new RecyclerDetailAdapter(mContext, mMoviesList);
        mCastCreditsAdapter.setOnItemClickListener(this);
        castCreditsRV.setAdapter(mCastCreditsAdapter);
    }

    private void updateCastCredits() {
        Uri uri = Uri.parse(Constants.TMDB_BASE_URL_PERSON).buildUpon()
                .appendPath(mCastId)
                .appendPath(Constants.TMDB_MOVIE_CREDITS)
                .appendQueryParameter(Constants.API_KEY, BuildConfig.API_KEY)
                .build();
        StringRequest request = new StringRequest(
                uri.toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getCastCreditsFromJson(response);
                        // handle empty view
                        if(mMoviesList.size() != 0){
                            castCreditsRV.setVisibility(View.VISIBLE);
                            emptyCastCreditsTV.setVisibility(View.GONE);
                        }else{
                            castCreditsRV.setVisibility(View.GONE);
                            emptyCastCreditsTV.setVisibility(View.VISIBLE);
                            castCreditsHintTV.setVisibility(View.GONE);
                        }
                        mCastCreditsAdapter.notifyDataSetChanged();
                        Utility.setDataStatus(mContext, Utility.DATA_STATUS_OK);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        castCreditsRV.setVisibility(View.GONE);
                        emptyCastCreditsTV.setVisibility(View.VISIBLE);
                        castCreditsHintTV.setVisibility(View.GONE);
                    }
                }
        );
        mQueue.add(request);
    }

    private void getCastCreditsFromJson(String response) {
        try {
            JSONObject castsObject = new JSONObject(response);
            JSONArray castsArray = castsObject.getJSONArray(Constants.TMDB_CAST);
            for(int i=0; i<castsArray.length(); i++){
                JSONObject castObject = castsArray.getJSONObject(i);
                MovieInfo movieInfo = new MovieInfo();
                movieInfo.id = castObject.getString(Constants.TMDB_ID);
                movieInfo.title = castObject.getString(Constants.TMDB_ORIGINAL_TITLE);
                movieInfo.posterPath = castObject.getString(Constants.TMDB_POSTER_PATH);
                mMoviesList.add(movieInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateCastInfo() {
        Uri uri = Uri.parse(Constants.TMDB_BASE_URL_PERSON).buildUpon()
                .appendPath(mCastId)
                .appendQueryParameter(Constants.API_KEY, BuildConfig.API_KEY)
                .build();
        StringRequest request = new StringRequest(
                uri.toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getCastInfoFromJson(response);
                        Utility.setDataStatus(mContext, Utility.DATA_STATUS_OK);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utility.handleErrorCases(mContext, error);
                        progressDialog.dismiss();
                        Utility.updateEmptyView(mContext, mainViewLL, emptyViewFL);
                    }
                }
        );
        mQueue.add(request);
    }

    @SuppressWarnings("unchecked")
    private void getCastInfoFromJson(String jsonStr) {
        final String NO_RECORD = "No record";
        try {
            JSONObject castObject = new JSONObject(jsonStr);

            // 1. name
            String name = castObject.getString(Constants.TMDB_NAME);
            nameTV.setText(name);

            // 2. place of birth
            String place_of_birth = castObject.getString(Constants.TMDB_PLACE_OF_BIRTH);
            if(!place_of_birth.equals("") && !place_of_birth.equals("null")){
                String[] birthplaceArray = place_of_birth.split("\\s-\\s");
                int length = birthplaceArray.length;
                if(length == 1){
                    birthplaceTV.setText(place_of_birth);
                }else{
                    StringBuilder builder = new StringBuilder();
                    for(int i=0; i<length-1; i++){
                        builder.append(birthplaceArray[i]).append(", ");
                    }
                    builder.append(birthplaceArray[length-1]);
                    birthplaceTV.setText(builder);
                }
            }else{
                birthplaceTV.setText(NO_RECORD);
            }

            // 3. birthday
            String birthday = castObject.getString(Constants.TMDB_BIRTHDAY);
            if(!birthday.equals("") && !birthday.equals("null")){
                birthdayTV.setText(Utility.formatDate(mContext, birthday));
            }else{
                birthdayTV.setText(NO_RECORD);
            }

            // 4. bio
            String bio = castObject.getString(Constants.TMDB_BIOGRAPHY);
            if(!bio.equals("") && !bio.equals("null")){
                bioTV.setText(bio);
            }else{
                bioTV.setText(NO_RECORD);
            }

            // 5. deathday
            String deathday = castObject.getString(Constants.TMDB_DEATHDAY);
            if(!deathday.equals("") && !bio.equals("null")){
                deathdayTextTV.setVisibility(View.VISIBLE);
                deathdayTV.setVisibility(View.VISIBLE);
                deathdayTV.setText(Utility.formatDate(mContext, deathday));
            }

            // 6. profile image
            String profile_path = castObject.getString(Constants.TMDB_PROFILE_PATH);
            String profilePath = Constants.TMDB_BASE_URL_IMAGE_W185 + profile_path;
            Glide.with(mContext).load(profilePath)
                    .listener(
                            GlidePalette.with(profilePath)
                                    .use(GlidePalette.Profile.VIBRANT_DARK)
                                    .intoBackground(nameTV, GlidePalette.Swatch.RGB)
                                    .intoTextColor(nameTV, GlidePalette.Swatch.BODY_TEXT_COLOR)
                    )
                    .placeholder(R.drawable.placeholder2)
                    .error(R.drawable.placeholder3)
                    .into(profileIV);
        } catch (JSONException e) {
            e.printStackTrace();
        }finally{
            progressDialog.dismiss();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_cast_detail, container, false);
        nameTV = (TextView) rootView.findViewById(R.id.tv_frag_cast_detail_name);
        birthdayTV = (TextView) rootView.findViewById(R.id.tv_frag_cast_detail_birthday);
        birthplaceTV = (TextView) rootView.findViewById(R.id.tv_frag_cast_detail_birthplace);
        deathdayTextTV = (TextView) rootView.findViewById(R.id.id_frag_cast_detail_deathday);
        deathdayTV = (TextView) rootView.findViewById(R.id.tv_frag_cast_detail_deathday);
        bioTV = (TextView) rootView.findViewById(R.id.tv_frag_cast_detail_bio);
        profileIV = (ImageView) rootView.findViewById(R.id.iv_frag_cast_detail_poster);
        castCreditsRV = (RecyclerView) rootView.findViewById(R.id.rv_frag_cast_detail_credits);
        emptyCastCreditsTV = (TextView) rootView.findViewById(R.id.tv_frag_cast_detail_empty_credits);
        castCreditsHintTV = (TextView) rootView.findViewById(R.id.tv_frag_cast_detail_credits_hint);
        mainViewLL = (LinearLayout) rootView.findViewById(R.id.ll_frag_cast_detail);
        emptyViewFL = (FrameLayout) rootView.findViewById(R.id.fl_frag_cast_detail);
        return rootView;
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(mContext, DetailActivity.class);
        intent.putExtra(Constants.EXTRA_ID, mMoviesList.get(position).id);
        intent.putExtra(Constants.EXTRA_TITLE, mMoviesList.get(position).title);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {}
}
