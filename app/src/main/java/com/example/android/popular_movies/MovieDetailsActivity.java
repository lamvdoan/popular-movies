package com.example.android.popular_movies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popular_movies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

public class MovieDetailsActivity extends AppCompatActivity {
    private static final String TAG = NetworkUtils.class.getSimpleName();
    MovieInfo movieInfo;

    TextView mMovieTitle;
    ImageView mThumbnail;
    TextView mOverview;
    TextView mReleaseDate;
    TextView mRunTime;
    TextView mUserRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        movieInfo = new MovieInfo();

        mMovieTitle = (TextView) findViewById(R.id.movie_title);
        mThumbnail = (ImageView) findViewById(R.id.movie_thumbnail);
        mOverview = (TextView) findViewById(R.id.overview);
        mReleaseDate = (TextView) findViewById(R.id.release_date);
        mRunTime = (TextView) findViewById(R.id.run_time);
        mUserRating = (TextView) findViewById(R.id.user_rating);

        // Get the URL string from the MainActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String movieDetailsUrl = extras.getString("key");
            new FetchMovieDetailsTask().execute(movieDetailsUrl);
            new FetchMovieDetailsTask().execute(movieDetailsUrl);
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
                String movieDetailsURL = params[0];
                URL movieRequestUrl = NetworkUtils.buildUrl(movieDetailsURL);
                String movieInfoDetailsJson = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
                return getMovieInfoFromJson(movieInfoDetailsJson);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(MovieInfo movieInfo) {
            // Set the view with movie details
            if(movieInfo != null) {
                // Set movie info
                mMovieTitle.setText(movieInfo.getOriginalTitle());
                mOverview.setText(movieInfo.getOverview());
                mRunTime.setText(String.valueOf(movieInfo.getRuntime()) + "min");
                mReleaseDate.setText(String.valueOf(movieInfo.getReleaseDate()).substring(0, 4));
                mUserRating.setText(String.valueOf(movieInfo.getVoteAverage()) + "/10");

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
}
