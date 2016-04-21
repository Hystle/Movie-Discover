package com.hystle.zach.moviediscover.ui;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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
import com.hystle.zach.moviediscover.data.MovieContract;
import com.hystle.zach.moviediscover.entity.MovieInfo;
import com.hystle.zach.moviediscover.entity.PersonInfo;
import com.hystle.zach.moviediscover.entity.ReviewInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class DetailFragment extends Fragment implements RecyclerDetailAdapter.OnItemClickListener {
    private static final String NO_REVIEWS = "No reviews found";
    private static final String MOVIE_SHARE_HASHTAG = "#MovieWanderApp";

    private Context mContext;

    private LinearLayout mainLL;
    private FrameLayout mFrameLayout;
    private TextView titleView;
    private TextView overviewView;
    private ImageView posterView;
    private TextView voteView;
    private TextView voteCountView;
    private RatingBar ratingBar;
    private TextView dateView;

    private RecyclerView similarRV;
    private RecyclerDetailAdapter mSimilarAdapter;
    private TextView similarHintView;
    private TextView emptySimilarView;

    private TextView reviewsView;
    private TextView reviewsHintView;

    private TextView emptyCastView;
    private TextView castHintView;
    private LinearLayout castLL;

    private ProgressDialog mProgressDialog;
    private RequestQueue mQueue;
    private ContentResolver resolver;
    private String mId;
    private String mTitle;
    private String mDate;
    private View mRootView;
    private float mOldRate;
    private String mPosterPath;

    private ArrayList<MovieInfo> mMoviesList = new ArrayList<>();
    private ArrayList<ReviewInfo> mReviewsList = new ArrayList<>();
    private ArrayList<PersonInfo> mCastsList = new ArrayList<>();

    private Menu mMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // show a friendly dialog
        mProgressDialog = ProgressDialog.show(mContext, null, "Loading...");
        // based on the movie's Id to get other detail information
        Intent intent = getActivity().getIntent();
        mId = intent.getStringExtra(Constants.EXTRA_ID);
        // prepare a Volley RequestQueue
        mQueue = Volley.newRequestQueue(mContext);
        // load data
        loadMovieDetailFromServer();
        loadMyRatesFromDB();
    }

    private void loadMyRatesFromDB() {
        // get my_rate from database
        resolver = mContext.getContentResolver();
        Cursor cursor = resolver.query(
                Uri.parse(MovieContract.CONTENT_RATED_URI + "/" + mId),
                new String[]{MovieContract.RatedEntry.COLM_MY_RATE},
                null,
                null,
                null);
        if(cursor != null && cursor.moveToFirst()){
            mOldRate = cursor.getFloat(0);
        }
        if(cursor!= null) {
            cursor.close();
        }
    }

    private void loadMovieDetailFromServer() {
        Uri uri = Uri.parse(Constants.TMDB_BASE_URL_MOVIE).buildUpon()
                .appendPath(mId)
                .appendQueryParameter(Constants.API_KEY, BuildConfig.API_KEY)
                .appendQueryParameter(Constants.TMDB_APPEND_TO_RESPONSE, "trailers,reviews,casts")
                .build();
        StringRequest request = new StringRequest(
                uri.toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        updateMovieInfoFromJson(response);
                        Utility.setDataStatus(mContext, Utility.DATA_STATUS_OK);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utility.handleErrorCases(mContext, error);
                        mProgressDialog.dismiss();
                        Utility.updateEmptyView(mContext, mainLL, mFrameLayout);
                    }
                }
        );
        mQueue.add(request);
    }

    private void updateMovieInfoFromJson(String response) {
        try {
            // get movie moviesInfo
            JSONObject movieObject = new JSONObject(response);

            // 1. date
            mTitle = movieObject.getString(Constants.TMDB_ORIGINAL_TITLE);
            mDate = movieObject.getString(Constants.TMDB_RELEASE_DATE);
            if (!mDate.equals("")) {
                dateView.setText(Utility.formatDate(mContext, mDate));
            }
            MenuItem eventItem = mMenu.findItem(R.id.id_item_action_event);
            eventItem.setIntent(createEventIntent());

            // 2. title
            String[] dateSplit = mDate.split("\\-");
            String titleStr = mTitle;
            if (!mDate.equals("")){
                titleStr = titleStr + " (" + dateSplit[0] + ")";
            }
            titleView.setText(titleStr);
            MenuItem shareItem = mMenu.findItem(R.id.id_item_action_share);
            shareItem.setIntent(createShareMovieIntent());

            // 3. poster
            mPosterPath = movieObject.getString(Constants.TMDB_POSTER_PATH);
            loadPosterImageFromServer();

            // 4. overview
            String overview = movieObject.getString(Constants.TMDB_OVERVIEW);
            if (overview == null || overview.equals("null")){
                overviewView.setText("No overview found");
                overviewView.setTextColor(Color.GRAY);
                overviewView.setTextSize(16);
            }else {
                overviewView.setText(overview);
            }

            // 5. vote average
            String voteAverage = movieObject.getString(Constants.TMDB_VOTE_AVERAGE);
            String voteStr = voteAverage + "/10";
            voteView.setText(voteStr);

            // 6. vote count
            String voteCount = movieObject.getString(Constants.TMDB_VOTE_COUNT);
            String voteCountStr = "(" + voteCount + ")";
            voteCountView.setText(voteCountStr);

            // 7. trailer
            JSONObject trailersObject = movieObject.getJSONObject(Constants.TMDB_TRAILERS);
            JSONArray youtubePathObject = trailersObject.getJSONArray(Constants.TMDB_YOUTUBE);
            // set trailer dynamically
            LinearLayout linearLayout = (LinearLayout) mRootView.findViewById(R.id.ll_frag_detail_trailer);
            if(youtubePathObject.length() != 0) {
                int trailerCount = youtubePathObject.length();
                // prevent too many trailers on the screen
                if(trailerCount > 3){
                    trailerCount = 3;
                }
                for(int i=0; i<trailerCount; i++){
                    JSONObject sourcePathObject = youtubePathObject.getJSONObject(i);
                    final String trailerSourcePath = sourcePathObject.getString(Constants.TMDB_SOURCE);
                    LinearLayout subLinearLayout = buildTrailerViews(trailerSourcePath, i);
                    linearLayout.addView(subLinearLayout);
                }
            }else{
                TextView tv = buildTrailerEmptyView();
                linearLayout.addView(tv);
            }

            // 8. cast
            JSONObject castsObject = movieObject.getJSONObject(Constants.TMDB_CASTS);
            JSONArray castArray = castsObject.getJSONArray(Constants.TMDB_CAST);
            if(castArray.length() != 0) {
                for (int i = 0; i < castArray.length(); i++) {
                    JSONObject review = castArray.getJSONObject(i);
                    PersonInfo personInfo = new PersonInfo();
                    personInfo.id = review.getString(Constants.TMDB_ID);
                    personInfo.name = review.getString(Constants.TMDB_NAME);
                    personInfo.character = review.getString(Constants.TMDB_CHARACTER);
                    personInfo.profilePath = review.getString(Constants.TMDB_PROFILE_PATH);
                    mCastsList.add(personInfo);
                }
                // set cast
                final int CAST_LIMIT = 3;
                int size = mCastsList.size();
                if(size <= CAST_LIMIT){
                    buildDisplayedCasts(size);
                }else{
                    buildDisplayedCasts(CAST_LIMIT);
                }
            }else{
                emptyCastView.setVisibility(View.VISIBLE);
                castHintView.setVisibility(View.GONE);
            }

            // 9. reviews
            JSONObject reviewsObject = movieObject.getJSONObject(Constants.TMDB_REVIEWS);
            JSONArray reviewsArray = reviewsObject.getJSONArray(Constants.TMDB_RESULTS);
            if(reviewsArray.length() != 0){
                for(int i=0; i<reviewsArray.length(); i++){
                    JSONObject review = reviewsArray.getJSONObject(i);
                    ReviewInfo reviewInfo = new ReviewInfo();
                    reviewInfo.author = review.getString(Constants.TMDB_AUTHOR);
                    reviewInfo.content = review.getString(Constants.TMDB_CONTENT);
                    mReviewsList.add(reviewInfo);
                }
                // set reviews
                int REVIEWS_LIMIT = 2;
                int size = mReviewsList.size();
                String reviewsText;
                if(size <= REVIEWS_LIMIT){
                    reviewsText = buildDetailPageReviews(size);
                }else{
                    reviewsText = buildDetailPageReviews(REVIEWS_LIMIT);
                }
                reviewsView.setText(reviewsText);
            }else{
                reviewsView.setText(NO_REVIEWS);
                reviewsView.setTextColor(Color.GRAY);
                reviewsView.setTextSize(16);
                reviewsHintView.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }finally {
            mProgressDialog.dismiss();
        }
    }

    private TextView buildTrailerEmptyView() {
        TextView tv = new TextView(mContext);
        String noTrailerText = "No Trailer found";
        tv.setText(noTrailerText);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        tv.setTextSize(16);
        tv.setTextColor(Color.GRAY);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvParams.setMargins(0, 16, 16, 16);
        tv.setLayoutParams(tvParams);
        return tv;
    }

    private LinearLayout buildTrailerViews(final String trailerSourcePath, int count) {
        LinearLayout subLinearLayout = new LinearLayout(mContext);
        subLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        subLinearLayout.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llParams.setMargins(0,4,0,4);
        subLinearLayout.setLayoutParams(llParams);

        ImageView iv = new ImageView(mContext);
        iv.setImageResource(R.drawable.play);

        float widthPixels = Utility.densityToPixel(mContext, 50);
        LinearLayout.LayoutParams ivParams
                = new LinearLayout.LayoutParams((int)widthPixels, (int)widthPixels);
        iv.setLayoutParams(ivParams);
        subLinearLayout.addView(iv, ivParams);

        TextView tv = new TextView(mContext);
        String playTrailerText = "Play Trailer "  + (count + 1);
        tv.setText(playTrailerText);
        tv.setTextSize(14);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvParams.setMargins(36, 0, 0, 0);
        subLinearLayout.addView(tv, tvParams);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                watchYoutubeVideo(trailerSourcePath);
            }
        });
        return subLinearLayout;
    }

    @SuppressWarnings("unchecked")
    private void loadPosterImageFromServer() {
        Uri posterUri = Uri.parse(Constants.TMDB_BASE_URL_IMAGE_W342 + mPosterPath).buildUpon()
                .appendQueryParameter(Constants.API_KEY, BuildConfig.API_KEY)
                .build();
        String url = posterUri.toString();
        int widthDps = Utility.getPosterWidth(mContext);
        int heightDps = (int)(widthDps * Constants.PICTURE_RATIO);
        float widthPixels = Utility.densityToPixel(mContext, widthDps);
        float heightPixels = Utility.densityToPixel(mContext, heightDps);
        LinearLayout.LayoutParams ivParams
                = new LinearLayout.LayoutParams((int)widthPixels, (int)heightPixels);
        posterView.setLayoutParams(ivParams);

        Glide.with(this).load(url)
                .listener(
                        GlidePalette.with(url)
                                .use(GlidePalette.Profile.VIBRANT_DARK)
                                .intoBackground(titleView, GlidePalette.Swatch.RGB)
                                .intoTextColor(titleView, GlidePalette.Swatch.BODY_TEXT_COLOR)
                )
                .placeholder(R.drawable.placeholder_loading)
                .error(R.drawable.placeholder_error)
                .into(posterView);
    }

    private void buildDisplayedCasts(int size) {
        for(int i=0; i<size; i++) {
            LinearLayout subLL = new LinearLayout(mContext);
            subLL.setOrientation(LinearLayout.VERTICAL);
            subLL.setGravity(Gravity.CENTER);
            subLL.setPadding(6, 6, 6, 6);
            LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            subLL.setLayoutParams(llParams);

            ImageView iv = new ImageView(mContext);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            String profileUri = Constants.TMDB_BASE_URL_IMAGE_W185 + mCastsList.get(i).profilePath;
            Glide.with(mContext)
                    .load(profileUri)
                    .placeholder(R.drawable.placeholder_loading)
                    .error(R.drawable.placeholder_error)
                    .into(iv);

            int widthDps = Utility.getCastWidth(mContext);
            int heightDps = (int)(widthDps * Constants.PICTURE_RATIO);
            float widthPixels = Utility.densityToPixel(mContext, widthDps);
            float heightPixels = Utility.densityToPixel(mContext, heightDps);
            LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(
                    (int)widthPixels, (int)heightPixels);
            iv.setLayoutParams(ivParams);
            subLL.addView(iv, ivParams);
            final int finalI = i;
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, CastActivity.class);
                    intent.putExtra(Constants.EXTRA_CAST, mCastsList.get(finalI).id);
                    intent.putExtra(Constants.EXTRA_CAST_FLAG, Constants.EXTRA_CAST);
                    startActivity(intent);
                }
            });

            TextView tv = new TextView(mContext);
            String castName = mCastsList.get(i).name;
            tv.setText(castName);
            tv.setTextSize(14);
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvParams.setMargins(4, 4, 4, 4);
            subLL.addView(tv, tvParams);
            castLL.addView(subLL);
            castHintView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, CastActivity.class);
                    intent.putExtra(Constants.EXTRA_CAST_LIST, mCastsList);
                    intent.putExtra(Constants.EXTRA_CAST_FLAG, Constants.EXTRA_CAST_LIST);
                    startActivity(intent);
                }
            });
        }
    }

    private String buildDetailPageReviews(int size) {
        StringBuilder builder = new StringBuilder();
        for(int j=0; j < size - 1; j++){
            builder.append("User: ").append(mReviewsList.get(j).author).append("\n")
                    .append(mReviewsList.get(j).content).append("\n\n");
        }
        builder.append("User: ").append(mReviewsList.get(size - 1).author).append("\n")
                .append(mReviewsList.get(size - 1).content);
        return builder.toString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.frag_detail, container, false);

        initViews();

        reviewsHintView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ContentActivity.class);
                intent.putExtra(Constants.EXTRA_REVIEWS, mReviewsList);
                startActivity(intent);
            }
        });

        return mRootView;
    }

    private void initViews() {
        titleView = (TextView) mRootView.findViewById(R.id.tv_frag_detail_title);
        overviewView = (TextView) mRootView.findViewById(R.id.tv_frag_detail_overview);
        posterView = (ImageView) mRootView.findViewById(R.id.iv_frag_detail_poster);
        voteView = (TextView)mRootView.findViewById(R.id.tv_frag_detail_vote);
        voteCountView = (TextView) mRootView.findViewById(R.id.tv_frag_detail_vote_count);
        dateView = (TextView) mRootView.findViewById(R.id.tv_frag_detail_date);
        ratingBar = (RatingBar) mRootView.findViewById(R.id.rating_bar);
        reviewsView = (TextView)mRootView.findViewById(R.id.tv_frag_detail_reviews);
        ratingBar.setRating(mOldRate);
        similarRV = (RecyclerView) mRootView.findViewById(R.id.rv_frag_detail_similar);
        mainLL = (LinearLayout)mRootView.findViewById(R.id.ll_frag_detail_main);
        emptySimilarView = (TextView) mRootView.findViewById(R.id.tv_frag_detail_empty_similar);
        mFrameLayout = (FrameLayout) mRootView.findViewById(R.id.fl_frag_detail);
        emptyCastView = (TextView) mRootView.findViewById(R.id.tv_frag_detail_empty_cast);
        castLL = (LinearLayout)mRootView.findViewById(R.id.ll_frag_detail_cast);

        reviewsHintView = (TextView)mRootView.findViewById(R.id.tv_frag_detail_reviews_hint);
        similarHintView = (TextView)mRootView.findViewById(R.id.tv_frag_detail_similar_hint);
        castHintView = (TextView)mRootView.findViewById(R.id.tv_frag_detail_cast_hint);
    }

    @Override
    public void onPause() {
        super.onPause();
        persistMyRate();
    }

    private void persistMyRate() {
        Float newRate = ratingBar.getRating();
        if(!newRate.equals(mOldRate)){
            ContentValues values = new ContentValues();
            values.put(MovieContract.RatedEntry.COLM_ID, mId);
            values.put(MovieContract.RatedEntry.COLM_POSTER_PATH, mPosterPath);
            values.put(MovieContract.RatedEntry.COLM_MY_RATE, newRate);
            // replace with the new one
            resolver.delete(MovieContract.CONTENT_RATED_URI, null, new String[]{mId});
            resolver.insert(MovieContract.CONTENT_RATED_URI, values);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // handle similar movies
        updateSimilarMovies();
        configSimilarMovies();
    }

    private void configSimilarMovies() {
        RecyclerView.LayoutManager similarLM
                = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        similarRV.setLayoutManager(similarLM);
        mSimilarAdapter = new RecyclerDetailAdapter(mContext, mMoviesList);
        mSimilarAdapter.setOnItemClickListener(this);
        similarRV.setAdapter(mSimilarAdapter);
    }

    private void updateSimilarMovies() {
        Uri uri = Uri.parse(Constants.TMDB_BASE_URL_MOVIE).buildUpon()
                .appendPath(mId)
                .appendPath(Constants.TMDB_SIMILAR)
                .appendQueryParameter(Constants.API_KEY, BuildConfig.API_KEY)
                .build();
        StringRequest request = new StringRequest(
                uri.toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getSimilarMoviesFromJson(response);
                        // handle empty similar movies' case
                        if(mMoviesList.size() != 0){
                            similarRV.setVisibility(View.VISIBLE);
                            emptySimilarView.setVisibility(View.GONE);
                        }else{
                            similarRV.setVisibility(View.GONE);
                            emptySimilarView.setVisibility(View.VISIBLE);
                            similarHintView.setVisibility(View.GONE);
                        }
                        mSimilarAdapter.notifyDataSetChanged();
                        Utility.setDataStatus(mContext, Utility.DATA_STATUS_OK);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utility.handleErrorCases(mContext, error);
                        mProgressDialog.dismiss();
                        Utility.updateEmptyView(mContext, mainLL, mFrameLayout);
                    }
                });
        mQueue.add(request);
    }

    private void getSimilarMoviesFromJson(String response) {
        try {
            JSONObject moviesObject = new JSONObject(response);
            JSONArray moviesArray = moviesObject.getJSONArray(Constants.TMDB_RESULTS);

            for(int i=0; i<moviesArray.length(); i++){
                JSONObject movieObject = moviesArray.getJSONObject(i);
                MovieInfo movieInfo = new MovieInfo();
                movieInfo.id = movieObject.getString(Constants.TMDB_ID);
                movieInfo.posterPath = movieObject.getString(Constants.TMDB_POSTER_PATH);
                movieInfo.title = movieObject.getString(Constants.TMDB_ORIGINAL_TITLE);
                mMoviesList.add(movieInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void watchYoutubeVideo(String id){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + id));
            startActivity(intent);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(mContext, DetailActivity.class);
        intent.putExtra(Constants.EXTRA_ID, mMoviesList.get(position).id);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_frag_detail, menu);
        mMenu = menu;
    }

    private Intent createEventIntent() {
        if (mDate != null && !mDate.equals("")) {
            String[] array = mDate.split("\\-");
            int year = Integer.parseInt(array[0]);
            int month = Integer.parseInt(array[1]) - 1;
            int day = Integer.parseInt(array[2]);
            Calendar movieTime = Calendar.getInstance();
            movieTime.set(year, month, day);

            return new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, movieTime.getTimeInMillis())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, movieTime.getTimeInMillis())
                    .putExtra(CalendarContract.Events.TITLE, "Movie - " + mTitle)
                    .putExtra(CalendarContract.Events.DESCRIPTION, "Added by Movie Discover");

        }
        return null;
    }
    private Intent createShareMovieIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mTitle + " is an awesome movie. Check it out! " + MOVIE_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
}
