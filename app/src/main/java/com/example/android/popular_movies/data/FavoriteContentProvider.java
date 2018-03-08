package com.example.android.popular_movies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.example.android.popular_movies.data.FavoriteContract.FavoriteEntry;
import static com.example.android.popular_movies.data.TrailerContract.TrailerEntry;

public class FavoriteContentProvider extends ContentProvider {
    public static final int FAVORITES = 100;
    public static final int FAVORITES_WITH_ID = 101;
    public static final int TRAILERS = 200;
    public static final int TRAILERS_WITH_ID = 201;

    private FavoriteDbHelper mFavoriteDbHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(FavoriteContract.AUTHORITY, FavoriteContract.PATH_FAVORITES, FAVORITES);
        uriMatcher.addURI(FavoriteContract.AUTHORITY, FavoriteContract.PATH_FAVORITES + "/#", FAVORITES_WITH_ID);
        uriMatcher.addURI(TrailerContract.AUTHORITY, TrailerContract.PATH_TRAILERS, TRAILERS);
        uriMatcher.addURI(TrailerContract.AUTHORITY, TrailerContract.PATH_TRAILERS + "/#", TRAILERS_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mFavoriteDbHelper = new FavoriteDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        final SQLiteDatabase db = mFavoriteDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case FAVORITES:
                retCursor =  db.query(FavoriteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TRAILERS:
                retCursor =  db.query(TrailerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // not needed
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mFavoriteDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case FAVORITES:
                long favorite_id = db.insert(FavoriteEntry.TABLE_NAME, null, values);
                if (favorite_id > 0) {
                    returnUri = ContentUris.withAppendedId(FavoriteEntry.CONTENT_URI, favorite_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case TRAILERS:
                long trailer_id = db.insert(TrailerEntry.TABLE_NAME, null, values);
                if (trailer_id > 0) {
                    returnUri = ContentUris.withAppendedId(TrailerEntry.CONTENT_URI, trailer_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mFavoriteDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int recordsDeleted;

        switch (match) {
            case FAVORITES_WITH_ID:
                String favorite_id = uri.getPathSegments().get(1);
                recordsDeleted = db.delete(FavoriteEntry.TABLE_NAME, "_id=?", new String[]{favorite_id});
                break;
            case TRAILERS_WITH_ID:
                String trailer_id = uri.getPathSegments().get(1);
                recordsDeleted = db.delete(FavoriteEntry.TABLE_NAME, "_id=?", new String[]{trailer_id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (recordsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return recordsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Not needed
        return 0;
    }
}
