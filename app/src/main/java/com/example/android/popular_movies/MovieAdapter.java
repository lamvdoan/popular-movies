package com.example.android.popular_movies;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popular_movies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

/**
 * Created by lamvdoan on 9/4/17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {
    private static final String TAG = NetworkUtils.class.getSimpleName();
    private static final String IMAGE_URL = "https://image.tmdb.org/t/p/w185/";

    MovieAdapterOnClickHandler mClickHandler;
    List<String> imageUrlList;

    public MovieAdapter(MovieAdapterOnClickHandler mClickHandler) {
        this.mClickHandler = mClickHandler;
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mMovieThumbnail;

        public MovieAdapterViewHolder(View view) {
            super(view);
            mMovieThumbnail = (ImageView) view.findViewById(R.id.movie_thumbnail);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // TODO Fill in the details
        }
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder movieAdapterViewHolder, int position) {
        Uri imageRequestUri = NetworkUtils.buildUri(IMAGE_URL + imageUrlList.get(position));
        Context context = movieAdapterViewHolder.itemView.getContext();
        Picasso.with(context)
                .load(imageRequestUri)
                .resize(540, 540)
                .centerCrop()
                .into(movieAdapterViewHolder.mMovieThumbnail);
    }

    @Override
    public int getItemCount() {
        if (null == imageUrlList) {
            Log.e(TAG, "Size of imageUrlList is 0!");
            return 0;
        }

        return imageUrlList.size();
    }

    public interface MovieAdapterOnClickHandler {
        void onClick();
    }

    public void setMovieData(List<String> movieData) {
        imageUrlList = movieData;
        notifyDataSetChanged();
    }
}
