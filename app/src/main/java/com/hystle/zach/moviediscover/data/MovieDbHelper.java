package com.hystle.zach.moviediscover.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MovieDbHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "moviewander.db";
    private static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_RATED_MOVIE_TABLE = "CREATE TABLE " + MovieContract.RatedEntry.TABLE_NAME + " (" +
                MovieContract.RatedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieContract.RatedEntry.COLM_ID + " TEXT NOT NULL UNIQUE, " +
                MovieContract.RatedEntry.COLM_POSTER_PATH + " TEXT NOT NULL, " +
                MovieContract.RatedEntry.COLM_MY_RATE + " REAL DEFAULT 0)";
        db.execSQL(SQL_CREATE_RATED_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.RatedEntry.TABLE_NAME);
        onCreate(db);
    }
}
