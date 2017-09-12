package com.example.android.popular_movies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popular_movies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MovieDetailsActivity extends AppCompatActivity implements ReviewAdapter.ReviewAdapterOnClickHandler {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    MovieInfo movieInfo;

    TextView mMovieTitle;
    ImageView mThumbnail;
    TextView mOverview;
    TextView mReleaseDate;
    TextView mRunTime;
    TextView mUserRating;

    RecyclerView mReviewRecyclerView;
    ReviewAdapter mReviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        movieInfo = new MovieInfo();
        mMovieTitle = (TextView) findViewById(R.id.movie_title);
        mThumbnail = (ImageView) findViewById(R.id.movie_portrait);
        mOverview = (TextView) findViewById(R.id.overview);
        mReleaseDate = (TextView) findViewById(R.id.release_date);
        mRunTime = (TextView) findViewById(R.id.run_time);
        mUserRating = (TextView) findViewById(R.id.user_rating);

//        Initialize RecyclerView for Reviews
        mReviewRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_review);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mReviewRecyclerView.setLayoutManager(linearLayoutManager);
        mReviewRecyclerView.setHasFixedSize(true);
        mReviewAdapter = new ReviewAdapter(this);
        mReviewRecyclerView.setAdapter(mReviewAdapter);

        // Get the URL string from the MainActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String endpoint = extras.getString("key");
            new FetchMovieDetailsTask().execute(endpoint);
        }
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
                URL reviewRequestUrl = NetworkUtils.buildUrl(reviewDetailsEndpoint);
                String reviewDetailsJson = NetworkUtils.getResponseFromHttpUrl(reviewRequestUrl);

                // Store reviews into movieList object by extracting contents from the results array
                JSONObject reviewDetailsObject = new JSONObject(reviewDetailsJson);
                JSONArray reviewResults = reviewDetailsObject.getJSONArray("results");
                List<String> contents = new ArrayList<>();

                for (int i=0; i< reviewResults.length(); i++) {
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

                for (int i=0; i< videoResults.length(); i++) {
                    JSONObject childJSONObject = videoResults.getJSONObject(i);
                    keys.add(childJSONObject.getString("key"));
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
                    mReviewAdapter.setReviewData(movieInfo.getReview());
                }

                // Load movie thumbnail
                Uri imageRequestUri = NetworkUtils.buildUri(movieInfo.getPosterPath());
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
    public void onClick(String reviewSummary) {
        Intent intent = new Intent(this, ReviewActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, reviewSummary);
        startActivity(intent);
    }
}
