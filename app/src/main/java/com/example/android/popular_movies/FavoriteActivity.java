package com.example.android.popular_movies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FavoriteActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {
    RecyclerView mRecyclerView;
    MovieAdapter mMovieAdapter;

    private static final int SPAN_COUNT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);

        // Initialize RecyclerView
        GridLayoutManager linearLayoutManager = new GridLayoutManager(this, SPAN_COUNT);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mMovieAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);

        loadMovieData();
    }

    public void loadMovieData() {
        String parameter;
//        new MainActivity.FetchMovieListTask().execute();
    }

    @Override
    public void onClick(MovieInfo movieInfo) {
        Intent intent = getIntent();
    }
}
