package com.example.android.popular_movies;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    RecyclerView mRecyclerView;
    MovieAdapter mMovieAdapter;
    TextView mErrorMessageDisplay;
    ProgressBar mLoadingIndicator;

    boolean isPopularSort = true;

    public static final int SPAN_COUNT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        final String MOVIE_URL = "https://api.themoviedb.org/3/movie/" + parameter;

        toggleIsPopularFlag();
        showMovieDataView();
        new FetchMovieTask().execute(MOVIE_URL);
    }

    private void toggleIsPopularFlag() {
        if(isPopularSort) {
            isPopularSort = false;
        } else {
            isPopularSort = true;
        }
    }

    public class FetchMovieTask extends AsyncTask<String, Void, List<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<String> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            String movieURL = params[0];

            try {
                // Get movie list
                URL movieRequestUrl = NetworkUtils.buildUrl(movieURL);
                String jsonMovieResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);

                // Parse the image path from the JSON object
                JSONObject object = new JSONObject(jsonMovieResponse);
                JSONArray array = object.getJSONArray("results");

                // Store the image URLs into the list
                List<String> imageUrlList = new ArrayList<>();
                for (int i=0; i< array.length(); i++) {
                    JSONObject childJSONObject = array.getJSONObject(i);
                    imageUrlList.add(childJSONObject.getString("poster_path"));
                }

                return imageUrlList;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<String> imageUrlList) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);

            if (imageUrlList != null) {
                showMovieDataView();
                mMovieAdapter.setMovieData(imageUrlList);
            } else {
                showErrorMessage();
            }
        }
    }

    @Override
    public void onClick() {
        // TODO add intent to go to details
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
