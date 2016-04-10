package com.hystle.zach.moviediscover.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {
    public static final String CONTENT_AUTHORITY = "com.hystle.zach.moviediscover";
    public static final Uri BASE_CONTENT_URL = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_RATED = "rated";
    public static final Uri CONTENT_RATED_URI = BASE_CONTENT_URL.buildUpon().appendPath(PATH_RATED).build();

    public static Uri buildInserReturnUri(int type, long id) {
        switch (type){
            case MovieProvider.RATED:
                return ContentUris.withAppendedId(CONTENT_RATED_URI, id);
        }
        return null;
    }

    public static final class RatedEntry implements BaseColumns{
        public static final String TABLE_NAME = "table_rated";
        public static final String COLM_ID = "id";
        public static final String COLM_POSTER_PATH = "poster_path";
        public static final String COLM_MY_RATE = "my_rate";

        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
