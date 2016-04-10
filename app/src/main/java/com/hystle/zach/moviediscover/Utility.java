package com.hystle.zach.moviediscover;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.VolleyError;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utility {

    public static int densityToPixel(Context context, int dps){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    public static int getPosterWidth(Context context){
        final float scale = context.getResources().getDisplayMetrics().density;
        if(scale < 1.5){
            return 92;
        }else if(scale <= 2.5){
            return 154;
        }else if(scale <= 3.5){
            return 185;
        }else{
            return 342;
        }
    }

    public static int getCastWidth(Context context){
        final float scale = context.getResources().getDisplayMetrics().density;

        if(scale <= 1.5){
            return 90;
        }else if(scale <= 3.5){
            return 120;
        }else{
            return 150;
        }
    }

    public static int getDisplayWidthInPixel(Context context){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static float getDisplayWidthInDensity(Context context){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float density = context.getResources().getDisplayMetrics().density;
        return displayMetrics.widthPixels / density;
    }

    public static float getItemWidthInPixel(Context context, int columNum){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        float density = context.getResources().getDisplayMetrics().density;
        return displayMetrics.widthPixels / columNum;
    }

    public static String formatDate(Context context, String dateStr) {
        SimpleDateFormat stringToDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        try {
            date = stringToDate.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(date != null) {
            SimpleDateFormat dateToString = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
            return dateToString.format(date);
        }
        Utility.setDataStatus(context, DATA_STATUS_SERVER_INVALID);
        return null;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DATA_STATUS_OK, DATA_STATUS_SERVER_DOWN, DATA_STATUS_SERVER_INVALID, DATA_STATUS_NO_CONNECTION, DATA_STATUS_UNKNOWN})
    public @interface DataStatus {}

    public static final int DATA_STATUS_OK = 0;
    public static final int DATA_STATUS_SERVER_DOWN = 1;
    public static final int DATA_STATUS_SERVER_INVALID = 2;
    public static final int DATA_STATUS_NO_CONNECTION = 3;
    public static final int DATA_STATUS_UNKNOWN = 4;

    public static void setDataStatus(Context c, @DataStatus int locationStatus){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_data_status_key), locationStatus);
        spe.commit();
    }

    @SuppressWarnings("ResourceType")
    public static @DataStatus int getLocationStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_data_status_key), DATA_STATUS_UNKNOWN);
    }

    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static void resetLocationStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_data_status_key), DATA_STATUS_UNKNOWN);
        spe.apply();
    }

    /**
     * addView to parent and make oldView invisible to implement error cases
     * @param context
     * @param oldView
     * @param parent
     */
    public static void updateEmptyView(Context context, View oldView, ViewGroup parent) {
        int message = R.string.empty_movie_list;
        @Utility.DataStatus int status = Utility.getLocationStatus(context);
        switch (status) {
            case Utility.DATA_STATUS_SERVER_DOWN:
                message = R.string.empty_movie_list_server_down;
                break;
            case Utility.DATA_STATUS_SERVER_INVALID:
                message = R.string.empty_movie_list_server_error;
                break;
            case Utility.DATA_STATUS_NO_CONNECTION:
                message = R.string.empty_movie_list_no_network;
                break;
            case Utility.DATA_STATUS_UNKNOWN:
                message = R.string.empty_movie_list_unknown_error;
        }
        TextView emptyView = new TextView(context);
        emptyView.setTextSize(16);
        emptyView.setTextColor(Color.BLACK);
        emptyView.setText(message);
        emptyView.setPadding(0, 200, 0, 0);
        emptyView.setGravity(Gravity.CENTER_HORIZONTAL);
        emptyView.setVisibility(View.VISIBLE);
        oldView.setVisibility(View.GONE);
        parent.addView(emptyView);
    }

    public static void handleErrorCases(Context context, VolleyError error) {
        // no connection
        if(error instanceof NoConnectionError){
            Utility.setDataStatus(context, Utility.DATA_STATUS_NO_CONNECTION);
            // error from api
        }else{
            NetworkResponse response = error.networkResponse;
            if(response != null && response.data != null){
                switch(response.statusCode){
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        Utility.setDataStatus(context, Utility.DATA_STATUS_SERVER_INVALID);
                        break;
                    case HttpURLConnection.HTTP_INTERNAL_ERROR:
                        Utility.setDataStatus(context, Utility.DATA_STATUS_SERVER_DOWN);
                        break;
                    default:
                        Utility.setDataStatus(context, Utility.DATA_STATUS_UNKNOWN);
                }
            }
        }
    }
}
