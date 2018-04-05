package com.example.android.popular_movies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popular_movies.data.FavoriteContract.FavoriteEntry;
import com.example.android.popular_movies.data.TrailerContract.TrailerEntry;
import com.example.android.popular_movies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MovieDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    private static final int FAVORITES_LOADER_ID = 0;
    private static final int TRAILER_LOADER_ID = 1;
    Cursor mFavoritesCursor = null;
    Cursor mTrailerCursor = null;

    private static final String YOUTUBE_LINK = "https://www.youtube.com/watch?v=";

    int movieId;
    MovieInfo mMovieInfo;
    TextView mMovieTitle;
    ImageView mThumbnail;
    TextView mOverview;
    TextView mReleaseDate;
    TextView mRunTime;
    TextView mUserRating;
    ImageView mFavorite;

    Uri imageRequestUri;
    URL reviewRequestUrl;
    Boolean isMovieFaved;

    RecyclerView mReviewRecyclerView;
    ReviewAdapter mReviewAdapter;
    ReviewAdapter.ReviewAdapterOnClickHandler reviewClickHandler;

    RecyclerView mTrailerRecyclerView;
    TrailerAdapter mTrailerAdapter;
    TrailerAdapter.TrailerAdapterOnClickHandler mTrailerClickHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "*** onCreate executed ***");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMovieInfo = new MovieInfo();
        mMovieTitle = (TextView) findViewById(R.id.movie_title);
        mThumbnail = (ImageView) findViewById(R.id.movie_portrait);
        mOverview = (TextView) findViewById(R.id.overview);
        mReleaseDate = (TextView) findViewById(R.id.release_date);
        mRunTime = (TextView) findViewById(R.id.run_time);
        mUserRating = (TextView) findViewById(R.id.user_rating);
        mFavorite = (ImageView) findViewById(R.id.favorite_button);

        // Initialize RecyclerView for Reviews
        mReviewRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_review);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mReviewRecyclerView.setLayoutManager(linearLayoutManager);
        mReviewRecyclerView.setHasFixedSize(true);

        reviewClickHandler = new ReviewAdapter.ReviewAdapterOnClickHandler() {
            @Override
            public void onClick(String reviewSummary) {
                Intent intent = new Intent(MovieDetailsActivity.this, ReviewActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, reviewSummary);
                startActivity(intent);
            }
        };

        mReviewAdapter = new ReviewAdapter(reviewClickHandler);
        mReviewRecyclerView.setAdapter(mReviewAdapter);

        // Initialize RecyclerView for Trailer
        mTrailerRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_trailer);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);
        mTrailerRecyclerView.setLayoutManager(linearLayoutManager2);
        mTrailerRecyclerView.setHasFixedSize(true);

        mTrailerClickHandler = new TrailerAdapter.TrailerAdapterOnClickHandler() {
            @Override
            public void onClick(String trailerLink) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerLink));
                startActivity(intent);
            }
        };

        mTrailerAdapter = new TrailerAdapter(mTrailerClickHandler);
        mTrailerRecyclerView.setAdapter(mTrailerAdapter);

        // Get the URL string from the MainActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String movieId = extras.getString("key");
            this.movieId = Integer.valueOf(movieId);
            new FetchMovieDetailsTask().execute(movieId);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "*** onStart executed ***");
        getSupportLoaderManager().initLoader(FAVORITES_LOADER_ID, null, this);
    }

    public class FetchMovieDetailsTask extends AsyncTask<String, Void, MovieInfo> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected MovieInfo doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            try {
                // Get movie details as a json
                String endpoint = params[0];
                URL movieRequestUrl = NetworkUtils.buildUrl(endpoint);
                String movieInfoDetailsJson = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
                MovieInfo movieInfo = getMovieInfoFromJson(movieInfoDetailsJson);

                // Get Reviews
                String reviewDetailsEndpoint = movieInfo.getId() + "/reviews";
                reviewRequestUrl = NetworkUtils.buildUrl(reviewDetailsEndpoint);
                String reviewDetailsJson = NetworkUtils.getResponseFromHttpUrl(reviewRequestUrl);

                // Store reviews into movieList object by extracting contents from the results array
                JSONObject reviewDetailsObject = new JSONObject(reviewDetailsJson);
                JSONArray reviewResults = reviewDetailsObject.getJSONArray("results");
                List<String> contents = new ArrayList<>();

                for (int i = 0; i < reviewResults.length(); i++) {
                    JSONObject childJSONObject = reviewResults.getJSONObject(i);
                    contents.add(childJSONObject.getString("content"));
                }

                movieInfo.setReview(contents);

                // Get Videos
                String videoDetailsEndpoint = movieInfo.getId() + "/videos";
                URL videoRequestUrl = NetworkUtils.buildUrl(videoDetailsEndpoint);
                String videoDetailsJson = NetworkUtils.getResponseFromHttpUrl(videoRequestUrl);

                // Store keys into movieList object by extracting keys from the results array
                JSONObject videosDetailObject = new JSONObject(videoDetailsJson);
                JSONArray videoResults = videosDetailObject.getJSONArray("results");
                List<String> keys = new ArrayList<>();

                for (int i = 0; i < videoResults.length(); i++) {
                    JSONObject childJSONObject = videoResults.getJSONObject(i);
                    keys.add(YOUTUBE_LINK + childJSONObject.getString("key"));
                }

                movieInfo.setKey(keys);

                return movieInfo;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(MovieInfo movieInfo) {
            // Set the view with movie details
            if (movieInfo != null) {
                // Set movie info
                mMovieTitle.setText(movieInfo.getOriginalTitle());
                mOverview.setText(movieInfo.getOverview());
                mRunTime.setText(String.valueOf(movieInfo.getRuntime()) + "min");
                mReleaseDate.setText(String.valueOf(movieInfo.getReleaseDate()).substring(0, 4));
                mUserRating.setText(String.valueOf(movieInfo.getVoteAverage()) + "/10");
                mMovieInfo = movieInfo;


                if (movieInfo.getReview() != null) {
                    // Make Reviews text appear
                    mReviewAdapter.setReviewData(movieInfo.getReview());
                }

                if (movieInfo.getKey() != null) {
                    // Make Trailers text appear
                    mTrailerAdapter.setTrailerData(movieInfo.getKey());
                }

                // Load movie thumbnail
                imageRequestUri = NetworkUtils.buildUri(movieInfo.getPosterPath());
                Context context = mThumbnail.getContext();
                Picasso.with(context)
                        .load(imageRequestUri)
                        .into(mThumbnail);
            } else {
                Log.e(TAG, "No movie details!");
            }
        }
    }

    private MovieInfo getMovieInfoFromJson(String jsonString) {
        MovieInfo movieInfo = new MovieInfo();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            movieInfo.setId(jsonObject.getInt("id"));
            movieInfo.appendPosterPath(jsonObject.getString("poster_path"));
            movieInfo.setOriginalTitle(jsonObject.getString("original_title"));
            movieInfo.setOverview(jsonObject.getString("overview"));
            movieInfo.setVoteAverage(jsonObject.getDouble("vote_average"));
            movieInfo.setReleaseDate(jsonObject.getString("release_date"));
            movieInfo.setRuntime(jsonObject.getInt("runtime"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return movieInfo;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onClickFavoriteButton(View view) {
        // TODO Fix bug on the movie details page since it think its not faved.
        // TODO Add async task for queries
        // TODO Add pulling the reviews again

        if (isMovieFaved) {
            // Delete from database
            int favoriteId;

            String[] mFavoriteProjection = {FavoriteEntry._ID};
            String[] mFavoriteSelectionArgs = {mMovieTitle.getText().toString()};

            Cursor favoriteCursor = getContentResolver().query(
                    FavoriteEntry.CONTENT_URI,
                    mFavoriteProjection,
                    FavoriteEntry.COLUMN_ORIGINAL_TITLE + "=?",
                    mFavoriteSelectionArgs,
                    null);

            // Get favoriteId, foreign key of Trailers table
            if (favoriteCursor != null && favoriteCursor.getCount() == 1) {
                int index = favoriteCursor.getColumnIndex(FavoriteEntry._ID);
                Log.i(TAG, "favoriteCursor: " + index);
                favoriteCursor.moveToFirst();
                favoriteId = favoriteCursor.getInt(index);
            } else {
                Log.e(TAG, "favoriteCursor is null.. count: " + favoriteCursor.getCount());
                return;
            }

            // Query from Trailer table with favoriteId
            String[] mTrailerSelectionArgs = {String.valueOf(favoriteId)};

            Cursor trailerCursor = getContentResolver().query(
                    TrailerEntry.JOIN_CONTENT_URI,
                    null,
                    null,
                    mTrailerSelectionArgs,
                    null);

            Log.i(TAG, "TrailerCursor Count: " + trailerCursor.getCount());

            // Get cursor with all trailers that need to be deleted
            if (trailerCursor != null && favoriteCursor.getCount() >= 1) {

                // Delete from Trailers first due to Foreign Key Constraint in Favorites
                Uri trailerUri;
                int index;
                long trailerId;

                while (trailerCursor.moveToNext()) {
                    index = trailerCursor.getColumnIndex(TrailerEntry._ID);
                    trailerId = trailerCursor.getLong(index);

                    trailerUri = TrailerEntry.CONTENT_URI;
                    trailerUri = trailerUri.buildUpon().appendPath(String.valueOf(trailerId)).build();
                    Log.i(TAG, "Trailer URI: " + trailerUri);

                    int trailerRecordsDeleted = getContentResolver().delete(trailerUri, null, null);
                    Log.i(TAG, "Trailers Deleted: " + trailerRecordsDeleted);
                }

                // Delete from Favorites
                Uri favoritesUri = FavoriteEntry.CONTENT_URI;
                favoritesUri = favoritesUri.buildUpon().appendPath(String.valueOf(favoriteId)).build();
                Log.i(TAG, "Favorites URI: " + favoritesUri);

                int favoriteRecordsDeleted = getContentResolver().delete(favoritesUri, null, null);
                Log.i(TAG, "Favorites Deleted: " + favoriteRecordsDeleted);

                movieUnfaved();
            }
        } else {
            /************** Adding to favorites *************/

            // Added to favorite database
            ContentValues favoriteContentValues = new ContentValues();
            favoriteContentValues.put(FavoriteEntry.COLUMN_MOVIE_ID, movieId);
            favoriteContentValues.put(FavoriteEntry.COLUMN_ORIGINAL_TITLE, mMovieTitle.getText().toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_OVERVIEW, mOverview.getText().toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_RELEASE_DATE, mReleaseDate.getText().toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_RUNTIME, mRunTime.getText().toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_VOTE_AVERAGE, mUserRating.getText().toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_POSTER_PATH, imageRequestUri.toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_REVIEW, reviewRequestUrl.toString());

            Uri favoriteUri = getContentResolver().insert(FavoriteEntry.CONTENT_URI, favoriteContentValues);
            Log.i(TAG, "Favorites Insert URI: " + favoriteUri);

            if (mMovieInfo.getKey().size() > 0) {
                Long favoriteId = null;

//                getSupportLoaderManager().initLoader(FAVORITES_LOADER_ID, null, this);

                String[] mProjection = {FavoriteEntry._ID};
                String[] mSelectionArgs = {mMovieTitle.getText().toString()};

                Cursor favoriteCursor = getContentResolver().query(
                        FavoriteEntry.CONTENT_URI,
                        mProjection,
                        FavoriteEntry.COLUMN_ORIGINAL_TITLE + "=?",
                        mSelectionArgs,
                        null);

                // Query favorite_id
                if (favoriteCursor != null && favoriteCursor.getCount() == 1) {
                    int index = favoriteCursor.getColumnIndex(FavoriteEntry._ID);
                    favoriteCursor.moveToFirst();
                    favoriteId = Long.parseLong(favoriteCursor.getString(index));
                } else {
                    Log.e(TAG, "favoriteCursor is null.. count: " + favoriteCursor.getCount());
                    // TODO handle in this case
                }

                // Adding to trailer database
                ContentValues trailerContentValues;
                ContentResolver trailerContentResolver = getContentResolver();

                List<String> listOfTrailers = mMovieInfo.getKey();

                for (String trailerLink : listOfTrailers) {
                    trailerContentValues = new ContentValues();
                    trailerContentValues.put(TrailerEntry.COLUMN_YOUTUBE_LINK, trailerLink);
                    trailerContentValues.put(TrailerEntry.COLUMN_FAVORITE_ID, favoriteId);

                    Uri trailerUri = trailerContentResolver.insert(TrailerEntry.CONTENT_URI, trailerContentValues);
                    Log.i(TAG, "Trailer Insert URI: " + trailerUri);
                }
            }

            movieFaved();
        }
    }

    private void movieFaved() {
        mFavorite.setImageResource(R.drawable.yellow_star);
        isMovieFaved = true;
        Log.i(TAG, "isMovieFaved: " + isMovieFaved);
    }

    private void movieUnfaved() {
        mFavorite.setImageResource(R.drawable.transparent_star);
        isMovieFaved = false;
        Log.i(TAG, "isMovieFaved: " + isMovieFaved);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {
            Cursor mFavoriteData = null;

            @Override
            protected void onStartLoading() {
                if (mFavoriteData != null) {
                    deliverResult(mFavoriteData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
                try {
                    // select * from favorites where movie_id;
                    Log.i(TAG, "movieId: " + movieId);
                    String[] mSelectionArgs = {String.valueOf(movieId)};

//                    Cursor cursor = getContentResolver().query(FavoriteEntry.CONTENT_URI,
//                            null,
//                            FavoriteEntry.COLUMN_MOVIE_ID + "=?",
//                            mSelectionArgs,
//                            null);
//
//                    Log.i(TAG, "Cursor Count: " + cursor.getCount());
//                    return cursor;
                    return getContentResolver().query(FavoriteEntry.CONTENT_URI,
                            null,
                            FavoriteEntry.COLUMN_MOVIE_ID + "=?",
                            mSelectionArgs,
                            null);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(Cursor data) {
                mFavoriteData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case FAVORITES_LOADER_ID:
                mFavoritesCursor = data;

                if (mFavoritesCursor.getCount() == 0) {
                    movieUnfaved();
                } else {
                    movieFaved();
                }
                Log.i(TAG, "Load Cursor setting isMovieFaved: " + isMovieFaved);

                break;
            case TRAILER_LOADER_ID:
                mTrailerCursor = data;
                break;
            default:
                Log.e(TAG, "Unknown ID: " + loader.getId());
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case FAVORITES_LOADER_ID:
                mFavoritesCursor = null;
                break;
            case TRAILER_LOADER_ID:
                mTrailerCursor = null;
                break;
            default:
                Log.e(TAG, "Unknown ID: " + loader.getId());
                break;
        }
    }
}
