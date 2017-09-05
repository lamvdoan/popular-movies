package com.example.android.popular_movies;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by lamvdoan on 9/4/17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    MovieAdapterOnClickHandler mClickHandler;

    public MovieAdapter(MovieAdapterOnClickHandler mClickHandler) {
        this.mClickHandler = mClickHandler;
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new MovieAdapterViewHolder(null);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder movieAdapterViewHolder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public interface MovieAdapterOnClickHandler {
        void onClick();
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public MovieAdapterViewHolder(View view) {
            super(view);
            // TODO find view by id
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
