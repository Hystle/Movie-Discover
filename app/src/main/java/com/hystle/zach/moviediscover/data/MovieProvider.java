package com.hystle.zach.moviediscover.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


@SuppressWarnings("ALL")
public class MovieProvider extends ContentProvider{

    private MovieDbHelper mHelper;
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final String sRatedSelectionWithId =
            MovieContract.RatedEntry.TABLE_NAME + "." + MovieContract.RatedEntry.COLM_ID + " = ?";

    public static final int RATED = 100;
    public static final int RATED_WITH_SEGMENT = 101;

    static{
        final String authority = MovieContract.CONTENT_AUTHORITY;
        sUriMatcher.addURI(authority, MovieContract.PATH_RATED, RATED);
        sUriMatcher.addURI(authority, MovieContract.PATH_RATED + "/*", RATED_WITH_SEGMENT);
    }

    @Override
    public boolean onCreate() {
        mHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor rCursor;
        switch (sUriMatcher.match(uri)){
            case RATED:
                rCursor = getAllRate(sortOrder);
                break;
            case RATED_WITH_SEGMENT:
                rCursor = getRateById(uri, projection);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if (rCursor != null) {
            rCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return rCursor;
    }

    private Cursor getAllRate(String sortOrder) {
        return mHelper.getReadableDatabase().query(
                MovieContract.RatedEntry.TABLE_NAME,
                null,
                MovieContract.RatedEntry.COLM_MY_RATE + " > 0",
                null,
                null,
                null,
                MovieContract.RatedEntry.COLM_MY_RATE + " DESC");
    }

    private Cursor getRateById(Uri uri, String[] projection) {
        String[] selectionArgs = new String[]{MovieContract.RatedEntry.getIdFromUri(uri)};
        return mHelper.getReadableDatabase().query(
                MovieContract.RatedEntry.TABLE_NAME,
                projection,
                sRatedSelectionWithId,
                selectionArgs,
                null,
                null,
                null);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        Uri rUri;
        switch(sUriMatcher.match(uri)){
            case RATED:
                long _id = db.insert(MovieContract.RatedEntry.TABLE_NAME, null, values);
                rUri = MovieContract.buildInserReturnUri(RATED, _id);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        db.close();
        getContext().getContentResolver().notifyChange(uri, null);
        return rUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        int dCount;
        switch (sUriMatcher.match(uri)){
            case RATED:
                dCount = db.delete(MovieContract.RatedEntry.TABLE_NAME, sRatedSelectionWithId, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if(dCount != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        db.close();
        return dCount;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        int uCount;
        switch (sUriMatcher.match(uri)) {
            case RATED:
                uCount = db.update(MovieContract.RatedEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if(uCount != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        db.close();
        return uCount;
    }
}
