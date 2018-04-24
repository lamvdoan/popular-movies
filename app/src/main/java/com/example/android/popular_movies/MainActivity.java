package com.example.android.popular_movies;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popular_movies.data.FavoriteContract;
import com.example.android.popular_movies.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int FAVORITES_LOADER_ID = 0;
    Cursor mFavoriteCursor = null;

    RecyclerView mRecyclerView;
    MovieAdapter mMovieAdapter;
    TextView mErrorMessageDisplay;
    ProgressBar mLoadingIndicator;
    Toolbar myToolbar;
    public static final int SPAN_COUNT = 2;

    boolean isPopularSort = true;
    boolean isFavoriteSort = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "*** onCreate executed ***");
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

        Log.i(TAG, "Deleting from Database");
        this.deleteDatabase("favoritesDb.db");

        // Load all movie data
        if (isFavoriteSort) {
            Log.i(TAG, "Favorites View");
            getSupportLoaderManager().initLoader(FAVORITES_LOADER_ID, null, this);
        } else {
            Log.i(TAG, "Loading Movies from onCreate");
            loadMovieData();
        }
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "*** onStart executed ***");
        super.onStart();

        if(isFavoriteSort) {
            getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, this);
        }

    }

    @Override
    protected void onResume() {
        Log.i(TAG, "*** onResume executed ***");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "*** onPause executed ***");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "*** onStop executed ***");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "*** onRestart executed ***");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "*** onDestroy executed ***");
        super.onDestroy();
    }


    private void loadMovieData() {
        String parameter;
        Log.i(TAG, "isPopularSort: " + isPopularSort);
        Log.i(TAG, "isFavoriteSort: " + isFavoriteSort);
        setIsFavoriteFalse();
        Log.i(TAG, "isFavoriteSort: " + isFavoriteSort);

        if (isPopularSort) {
            parameter = "popular";
        } else {
            parameter = "top_rated";
        }

        showMovieDataView();
        new FetchMovieListTask().execute(parameter);
    }

    private void loadFavoritesData() {
        showMovieDataView();
        getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, this);
    }

    private void toggleIsPopularFlag() {
        if (isPopularSort) {
            isPopularSort = false;
        } else {
            isPopularSort = true;
        }
        Log.i(TAG, "Setting isPopularSort: " + isPopularSort);
    }

    private void setIsFavoriteTrue() {
        Log.i(TAG, "State of isFavoriteSort: " + isFavoriteSort);
            isFavoriteSort = true;
        Log.i(TAG, "Setting isFavoriteSort: " + isFavoriteSort);
    }

    private void setIsFavoriteFalse() {
        Log.i(TAG, "State of isFavoriteSort: " + isFavoriteSort);
            isFavoriteSort = false;
        Log.i(TAG, "Setting isFavoriteSort: " + isFavoriteSort);
    }

    public class FetchMovieListTask extends AsyncTask<String, Void, List<MovieInfo>> {
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "Async Task Start");
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<MovieInfo> doInBackground(String... params) {
            Log.i(TAG, "Async Task Middle");
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

                for (int i = 0; i < array.length(); i++) {
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
            Log.i(TAG, "Async Task Finish");
            displayMovieList(movieInfoList);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {
        Log.i(TAG, "OnCreateLoader started");
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
                    Cursor cursor = getContentResolver().query(FavoriteContract.FavoriteEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

                    Log.i(TAG, "OnCreateLoader Cursor: " + cursor.getCount());
                    return cursor;
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor favoriteCursor) {
        Log.i(TAG, "onLoadFinished started");
        List<MovieInfo> movieInfoList = new ArrayList<>();

        int movieIdIndex;
        int posterPathIndex;

        int movieId;
        String posterPath;

        if (favoriteCursor.getCount() > 0) {
            favoriteCursor.moveToFirst();

            do {
                movieIdIndex = favoriteCursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID);
                movieId = favoriteCursor.getInt(movieIdIndex);

                posterPathIndex = favoriteCursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_POSTER_PATH);
                posterPath = favoriteCursor.getString(posterPathIndex);

                MovieInfo movieInfo = new MovieInfo();
                movieInfo.setId(movieId);
                movieInfo.setPosterPath(posterPath);

                movieInfoList.add(movieInfo);
            } while (favoriteCursor.moveToNext());

            Log.i(TAG, "onLoadFinished Cursor: " + favoriteCursor.getCount());
            Log.i(TAG, "ID: " + movieInfoList.get(0).getId());
            displayMovieList(movieInfoList);
        } else {
            if (isFavoriteSort) {
                Log.i(TAG, "Loading Movies from onLoadFinished");
                loadMovieData();
            } else {
                Toast.makeText(getApplicationContext(),
                        "No movies are faved.  Add a favorite movie to the list",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(TAG, "onLoaderReset started");
        mFavoriteCursor = null;
    }

    private void displayMovieList(List<MovieInfo> movieInfoList) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);

        if (movieInfoList != null) {
            Log.i(TAG, "Show movie!");
            showMovieDataView();
            mMovieAdapter.setMovieData(movieInfoList);
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onClick(MovieInfo movieInfo) {
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggle_sort_button:
                toggleIsPopularFlag();
                Log.i(TAG, "Loading Movies from onOptionItemSelected");
                loadMovieData();
                return true;
            case R.id.favorite_sort_button:
                loadFavoritesData();
                setIsFavoriteTrue();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
