package com.example.android.popular_movies;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popular_movies.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {
    RecyclerView mRecyclerView;
    MovieAdapter mMovieAdapter;
    TextView mErrorMessageDisplay;
    ProgressBar mLoadingIndicator;
    Toolbar myToolbar;
    public static final int SPAN_COUNT = 2;

    boolean isPopularSort = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

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
        new FetchMovieListTask().execute(parameter);
    }

    private void toggleIsPopularFlag() {
        if(isPopularSort) {
            isPopularSort = false;
        } else {
            isPopularSort = true;
        }
    }

    public class FetchMovieListTask extends AsyncTask<String, Void, List<MovieInfo>> {
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

            try {
                // Get movie list
                String endpoint = params[0];
                URL movieRequestUrl = NetworkUtils.buildUrl(endpoint);
                String jsonMovieListResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);

                // Parse the image path from the JSON object
                JSONObject object = new JSONObject(jsonMovieListResponse);
                JSONArray array = object.getJSONArray("results");

                // Store the image name and id's into the list
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

    @Override
    public void onClick(MovieInfo movieInfo) {
        // Send the movie details url to MovieDetailsActivity
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra("key", ((Integer) movieInfo.getId()).toString());
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
        getMenuInflater().inflate(R.menu.sort, menu);
        return true;
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.sort, menu);
//        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sort_button) {
            loadMovieData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
