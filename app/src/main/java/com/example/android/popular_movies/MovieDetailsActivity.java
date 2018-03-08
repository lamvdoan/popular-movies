package com.example.android.popular_movies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popular_movies.data.FavoriteContract.FavoriteEntry;
import com.example.android.popular_movies.data.TrailerContract;
import com.example.android.popular_movies.data.TrailerContract.TrailerEntry;
import com.example.android.popular_movies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MovieDetailsActivity extends AppCompatActivity {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String YOUTUBE_LINK = "https://www.youtube.com/watch?v=";

    MovieInfo movieInfo;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        movieInfo = new MovieInfo();
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
            String endpoint = extras.getString("key");
            new FetchMovieDetailsTask().execute(endpoint);
        }

        isMovieFaved = false;
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
            movieInfo.setPosterPath(jsonObject.getString("poster_path"));
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
        if (isMovieFaved) {
            isMovieFaved = false;
            mFavorite.setImageResource(R.drawable.transparent_star);
            Toast.makeText(getApplicationContext(), "Removed movie from Favorites", Toast.LENGTH_SHORT).show();

            // TODO: Delete from database

        } else {
            /************** Adding to favorites *************/

            // Added to favorite database
            ContentValues favoriteContentValues = new ContentValues();
            favoriteContentValues.put(FavoriteEntry.COLUMN_ORIGINAL_TITLE, mMovieTitle.getText().toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_OVERVIEW, mOverview.getText().toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_RELEASE_DATE, mReleaseDate.getText().toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_RUNTIME, mRunTime.getText().toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_VOTE_AVERAGE, mUserRating.getText().toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_POSTER_PATH, imageRequestUri.toString());
            favoriteContentValues.put(FavoriteEntry.COLUMN_REVIEW, reviewRequestUrl.toString());

            ContentResolver favoriteContentResolver = getContentResolver();
            favoriteContentResolver.insert(FavoriteEntry.CONTENT_URI, favoriteContentValues);

            // Querying the primary key for the favorites database
            // TODO: query properly and set favorite_id
            // TODO: Add content provider to android manifest file
            Cursor cursor = getContentResolver().query(FavoriteEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);

            Long favorite_id = 1L;


            // Adding to trailer database
            ContentValues trailerContentValues;
            ContentResolver trailerContentResolver = getContentResolver();

            List<String> listOfTrailers = movieInfo.getKey();

            for (String trailerLink : listOfTrailers) {
                trailerContentValues = new ContentValues();
                trailerContentValues.put(TrailerEntry.COLUMN_YOUTUBE_LINK, trailerLink);
                trailerContentValues.put(TrailerEntry.COLUMN_FAVORITE_ID, favorite_id);

                trailerContentResolver.insert(TrailerEntry.CONTENT_URI, trailerContentValues);
            }

            mFavorite.setImageResource(R.drawable.yellow_star);
            isMovieFaved = true;
            Toast.makeText(getApplicationContext(), "Added movie to Favorites", Toast.LENGTH_SHORT).show();
        }
    }
}
