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

// TODO: Add more docs to all methods
// TODO: Set caching on by querying the database first, and then pulling reviews from the API.
// TODO: Read data from cache instead of pulling from internet
// TODO: Run the test :( to see if it works
// TODO: Send a flag via intent to only read from db when faved
// TODO: Restore savedInstanceState
// TODO: Test rotating devices

public class MovieDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    private static final int FAVORITES_LOADER_ID = 0;
    private static final int TRAILER_LOADER_ID = 1;

    Cursor mFavoritesCursor = null;
    Cursor mTrailerCursor = null;

    private static final String YOUTUBE_LINK = "https://www.youtube.com/watch?v=";

    Integer movieId;
    String movieTitle;

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


        // Toolbar set up
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // UI Views
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
        LinearLayoutManager linearLayoutManagerForReviews = new LinearLayoutManager(this);
        mReviewRecyclerView.setLayoutManager(linearLayoutManagerForReviews);
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
        LinearLayoutManager linearLayoutManagerForTrailer = new LinearLayoutManager(this);
        mTrailerRecyclerView.setLayoutManager(linearLayoutManagerForTrailer);
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
            getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, MovieDetailsActivity.this);

            // Check the cache for the movie
            if (mFavoritesCursor != null) {
                mFavoritesCursor.moveToFirst();
                MovieInfo movieInfo = new MovieInfo();

                movieInfo.setId(mFavoritesCursor.getColumnIndex(FavoriteEntry.COLUMN_MOVIE_ID));
                movieInfo.setOriginalTitle(mFavoritesCursor.getString(mFavoritesCursor.getColumnIndex(FavoriteEntry.COLUMN_ORIGINAL_TITLE)));
                movieInfo.setOverview(mFavoritesCursor.getString(mFavoritesCursor.getColumnIndex(FavoriteEntry.COLUMN_OVERVIEW)));
                movieInfo.setVoteAverage(Double.parseDouble(mFavoritesCursor.getString(mFavoritesCursor.getColumnIndex(FavoriteEntry.COLUMN_VOTE_AVERAGE))));
                movieInfo.setReleaseDate(mFavoritesCursor.getString(mFavoritesCursor.getColumnIndex(FavoriteEntry.COLUMN_RELEASE_DATE)));
                movieInfo.setRuntime(mFavoritesCursor.getInt(mFavoritesCursor.getColumnIndex(FavoriteEntry.COLUMN_RUNTIME)));
                movieInfo.setPosterPath(mFavoritesCursor.getString(mFavoritesCursor.getColumnIndex(FavoriteEntry.COLUMN_POSTER_PATH)));

                // TODO: Fetch reviews and trailers
                // TODO: Create AsyncTask to fetch trailers and reviews separately
                // TODO: Pull that out of the FetchMovieDetailsTask
                String reviewLink  = mFavoritesCursor.getString(mFavoritesCursor.getColumnIndex(FavoriteEntry.COLUMN_REVIEW));

                setUIValues(movieInfo);
            } else {
                new FetchMovieDetailsTask().execute(movieId);
            }
        } else {
            Log.e(TAG, "We got a problem, intent.getExtras() is null!!!");
        }
    }

    public class FetchMovieDetailsTask extends AsyncTask<String, Void, MovieInfo> {
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "Async Task: onPreExecute");
            super.onPreExecute();
        }

        @Override
        protected MovieInfo doInBackground(String... params) {
            Log.i(TAG, "Async Task: doInBackground");
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
            Log.i(TAG, "Async Task: onPostExecute");
            setUIValues(movieInfo);
        }
    }

    /**
     * Set movie info to the View objects
     *
     * @param movieInfo
     */
    private void setUIValues(MovieInfo movieInfo) {
        // Set the view with movie details
        if (movieInfo != null) {
            // Set movie info
            movieTitle = movieInfo.getOriginalTitle();
            Log.i(TAG, "Movie Title: " + movieTitle);
            mMovieTitle.setText(movieTitle);
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

    /**
     * Wire the movie info from the json string to the movieInfo object.  Used by the API.
     * @param jsonString
     * @return
     */
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

    /**
     * When favorite button is clicked, check if it's faved.
     *
     * If faved, then delete the record from the database
     * If not faved, then add the record to the database
     *
     * @param view
     */
    public void onClickFavoriteButton(View view) {
        Log.i(TAG, "Movie is faved...");
        if (isMovieFaved) {
            // ************* Deleting from favorites ************ //
            int favoriteId;

            getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, this);

            // Get favoriteId, foreign key of Trailers table
            if (mFavoritesCursor != null && mFavoritesCursor.getCount() == 1) {
                mFavoritesCursor.moveToFirst();
                favoriteId = mFavoritesCursor.getInt(mFavoritesCursor.getColumnIndex(FavoriteEntry._ID));
            } else {
                Log.e(TAG, "favoriteCursor is null.. count: " + mFavoritesCursor.getCount());
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putInt("favoriteId", favoriteId);
            getSupportLoaderManager().restartLoader(TRAILER_LOADER_ID, bundle, this);
            Log.i(TAG, "TrailerCursor Count: " + mTrailerCursor.getCount());

            // Get cursor with all trailers that need to be deleted
            if (mTrailerCursor != null && mFavoritesCursor.getCount() >= 1) {

                // Delete from Trailers first due to Foreign Key Constraint in Favorites
                Uri trailerUri;
                long trailerId;

                while (mTrailerCursor.moveToNext()) {
                    trailerId = mTrailerCursor.getLong(mTrailerCursor.getColumnIndex(TrailerEntry._ID));

                    trailerUri = TrailerEntry.CONTENT_URI;
                    trailerUri = trailerUri.buildUpon().appendPath(String.valueOf(trailerId)).build();
                    Log.i(TAG, "Trailer URI: " + trailerUri);

                    int trailerRecordsDeleted = getContentResolver().delete(trailerUri, null, null);
                    Log.i(TAG, "Trailers Deleted: " + trailerRecordsDeleted);
                }

                // Delete from Favorites
                Uri favoritesUri = FavoriteEntry.CONTENT_URI;
                favoritesUri = favoritesUri.buildUpon().appendPath(String.valueOf(favoriteId)).build();
                Log.i(TAG, "Favorites Delete URI: " + favoritesUri);

                int favoriteRecordsDeleted = getContentResolver().delete(favoritesUri, null, null);
                Log.i(TAG, "Favorites Deleted: " + favoriteRecordsDeleted);

                movieUnfaved();
            }
        } else {
            // ************* Adding to favorites ************ //
            Log.i(TAG, "Movie is NOT faved...");

            // Added to favorite database
            ContentValues favoriteContentValues = new ContentValues();
            favoriteContentValues.put(FavoriteEntry.COLUMN_MOVIE_ID, movieId);
            favoriteContentValues.put(FavoriteEntry.COLUMN_ORIGINAL_TITLE, mMovieTitle.getText().toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_RELEASE_DATE, mReleaseDate.getText().toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_RUNTIME, mRunTime.getText().toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_VOTE_AVERAGE, mUserRating.getText().toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_POSTER_PATH, imageRequestUri.toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_REVIEW, reviewRequestUrl.toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_OVERVIEW, mOverview.getText().toString());

            Uri favoriteUri = getContentResolver().insert(FavoriteEntry.CONTENT_URI, favoriteContentValues);
            Log.i(TAG, "Favorites Insert URI: " + favoriteUri);

            if (mMovieInfo.getKey().size() > 0) {
                getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, this);
            }

            // Query favorite_id
            if (mFavoritesCursor != null && mFavoritesCursor.getCount() == 1) {
                Long favoriteId = null;

                mFavoritesCursor.moveToFirst();
                favoriteId = Long.parseLong(mFavoritesCursor.getString(mFavoritesCursor.getColumnIndex(FavoriteEntry._ID)));

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
            } else {
                Log.e(TAG, "favoriteCursor is null.. count: " + String.valueOf(mFavoritesCursor.getCount()));
                // TODO handle in this case
            }

            movieFaved();
        }
    }

    /**
     * Fave the movie and fill in the yellow star
     */
    private void movieFaved() {
        Log.i(TAG, "isMovieFaved: " + isMovieFaved + "Now: true");
        mFavorite.setImageResource(R.drawable.yellow_star);
        isMovieFaved = true;
    }

    /**
     * Unfave the movie and fill in the transparent star
     */
    private void movieUnfaved() {
        Log.i(TAG, "isMovieFaved: " + isMovieFaved + "Now: false");
        mFavorite.setImageResource(R.drawable.transparent_star);
        isMovieFaved = false;
    }

    /**
     * Get Movie Info from the database
     *
     * @param loaderId Database table type
     * @param args     Movie info
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, final Bundle args) {
        Log.i(TAG, "onCreateLoader started");
        final Bundle bundle = args;

        switch (loaderId) {
            case FAVORITES_LOADER_ID:
                return new AsyncTaskLoader<Cursor>(this) {

                    @Override
                    protected void onStartLoading() {
                        forceLoad();
                    }

                    @Override
                    public Cursor loadInBackground() {
                        try {
                            Log.i(TAG, "Movie Id (Favorite Loader): " + movieId);
                            String[] mFavoriteProjection = {FavoriteEntry._ID};
                            String[] mFavoriteSelectionArgs = {movieId.toString()};

                            Cursor cursor = getContentResolver().query(
                                    FavoriteEntry.CONTENT_URI,
                                    mFavoriteProjection,
                                    FavoriteEntry.COLUMN_MOVIE_ID + "=?",
                                    mFavoriteSelectionArgs,
                                    null);
                            Log.i(TAG, "AsyncTaskLoader Favorites Cursor: " + cursor.getCount());
                            return cursor;

                        } catch (Exception e) {
                            Log.e(TAG, "Failed to asynchronously load data.");
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    public void deliverResult(Cursor data) {
                        mFavoritesCursor = data;
                        super.deliverResult(data);
                    }
                };
            case TRAILER_LOADER_ID:
                return new AsyncTaskLoader<Cursor>(this) {

                    @Override
                    protected void onStartLoading() {
                        forceLoad();
                    }

                    @Override
                    public Cursor loadInBackground() {
                        try {
                            Log.i(TAG, "Movie Id (Trailer Loader): " + movieId);
                            String favoriteId = String.valueOf(bundle.getInt("favoriteId"));
                            String[] mTrailerSelectionArgs = {favoriteId};

                            Cursor cursor = getContentResolver().query(
                                    TrailerEntry.JOIN_CONTENT_URI,
                                    null,
                                    null,
                                    mTrailerSelectionArgs,
                                    null);
                            Log.i(TAG, "AsyncTaskLoader Trailer Cursor: " + cursor.getCount());
                            return cursor;
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to asynchronously load data.");
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    public void deliverResult(Cursor data) {
                        mTrailerCursor = data;
                        super.deliverResult(data);
                    }
                };
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(TAG, "onLoadFinished started");
        switch (loader.getId()) {
            case FAVORITES_LOADER_ID:
                mFavoritesCursor = data;
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
        Log.i(TAG, "onLoaderReset started");
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
