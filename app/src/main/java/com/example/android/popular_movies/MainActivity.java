package com.example.android.popular_movies;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popular_movies.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {
    private static final String TAG = NetworkUtils.class.getSimpleName();
    public static final String MOVIE_URL = "https://api.themoviedb.org/3/movie/";

    RecyclerView mRecyclerView;
    MovieAdapter mMovieAdapter;
    TextView mErrorMessageDisplay;
    ProgressBar mLoadingIndicator;
    public static final int SPAN_COUNT = 2;

    boolean isPopularSort = true;

    public String movieDetailsJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // Initialize Views
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        // Initialize RecyclerView
        GridLayoutManager linearLayoutManager = new GridLayoutManager(this, SPAN_COUNT);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mMovieAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);

        // Load all movie data
        loadMovieData();
    }

    public void loadMovieData() {
        String parameter;

        if (isPopularSort) {
            parameter = "popular";
        } else {
            parameter = "top_rated";
        }

        toggleIsPopularFlag();
        showMovieDataView();
        new FetchMovieTask().execute(MOVIE_URL + parameter);
    }

    private void toggleIsPopularFlag() {
        if(isPopularSort) {
            isPopularSort = false;
        } else {
            isPopularSort = true;
        }
    }

    public class FetchMovieTask extends AsyncTask<String, Void, List<MovieInfo>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<MovieInfo> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            String movieURL = params[0];

            try {
                // Get movie list
                URL movieRequestUrl = NetworkUtils.buildUrl(movieURL);
                String jsonMovieListResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);

                // Parse the image path from the JSON object
                JSONObject object = new JSONObject(jsonMovieListResponse);
                JSONArray array = object.getJSONArray("results");

                // Store the image URLs into the list
                List<MovieInfo> movieInfoList = new ArrayList<>();
                for (int i=0; i< array.length(); i++) {
                    JSONObject childJSONObject = array.getJSONObject(i);
                    MovieInfo movieInfo = new MovieInfo(
                            childJSONObject.getInt("id"),
                            childJSONObject.getString("poster_path"));
                    movieInfoList.add(movieInfo);
                }

                return movieInfoList;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<MovieInfo> movieInfoList) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);

            if (movieInfoList != null) {
                showMovieDataView();
                mMovieAdapter.setMovieData(movieInfoList);
            } else {
                showErrorMessage();
            }
        }
    }

    public class FetchMovieDetailsTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            String movieDetailsURL = params[0];

            try {
                // Get movie details
                URL movieRequestUrl = NetworkUtils.buildUrl(movieDetailsURL);
                return NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String movieInfoDetails) {
            if (movieInfoDetails != null) {
                movieDetailsJson = movieInfoDetails;
            } else {
                Log.e(TAG, "No movie details!");
            }
        }
    }

    @Override
    public void onClick(MovieInfo movieInfo) {
        try {
            // Get movie details
            URL movieRequestUrl = NetworkUtils.buildUrl(MOVIE_URL + movieInfo.getId());
            movieDetailsJson = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        URL movieRequestUrl = NetworkUtils.buildUrl(MOVIE_URL + movieInfo.getId());
//        new FetchMovieDetailsTask().execute(movieRequestUrl.toString());

//        while (movieDetailsJson == null) {
//            try {
//                Log.i(TAG, "Sleeping for 1 second");
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }


        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, movieDetailsJson);
        startActivity(intent);
    }

    private void showMovieDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sort, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sort_button) {
            loadMovieData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
