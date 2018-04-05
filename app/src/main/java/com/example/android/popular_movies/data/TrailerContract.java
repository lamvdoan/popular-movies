package com.example.android.popular_movies.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class TrailerContract {
    public static final String AUTHORITY = "com.example.android.popular_movies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_TRAILERS = "trailers";
    public static final String PATH_TRAILERS_FAVORITES = "favorites_and_trailers";

    public static final class TrailerEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILERS).build();
        public static final Uri JOIN_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILERS_FAVORITES).build();

        public static final String TABLE_NAME = "trailers";

        public static final String COLUMN_YOUTUBE_LINK = "youtube_link";
        public static final String COLUMN_FAVORITE_ID = "favorite_id";
    }
}
