package com.example.android.popular_movies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.popular_movies.data.FavoriteContract.FavoriteEntry;
import com.example.android.popular_movies.data.TrailerContract.TrailerEntry;

public class FavoriteDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favoritesDb.db";
    private static final int VERSION = 1;

    public FavoriteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_FAVORITES_TABLE = "CREATE TABLE " + FavoriteEntry.TABLE_NAME + " (" +
                FavoriteEntry._ID + " INTEGER PRIMARY KEY, " +
                FavoriteEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL " +
                FavoriteEntry.COLUMN_RUNTIME + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_VOTE_AVERAGE + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_REVIEW + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_OVERVIEW + " TEXT NOT NULL);";
        db.execSQL(CREATE_FAVORITES_TABLE);

        final String CREATE_TRAILER_TABLE = "CREATE TABLE " + TrailerEntry.TABLE_NAME + "(" +
                TrailerEntry._ID + " INTEGER PRIMARY KEY, " +
                TrailerEntry.COLUMN_YOUTUBE_LINK + " TEXT NOT NULL);" +
                TrailerEntry.COLUMN_FAVORITE_ID + " INTEGER," +
                " FOREIGN KEY (" + TrailerEntry.COLUMN_FAVORITE_ID + ") REFERENCES " +
                FavoriteEntry.TABLE_NAME + "(" + FavoriteEntry._ID + ")";

        db.execSQL(CREATE_TRAILER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        onCreate(db);
    }
}
