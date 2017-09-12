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

import java.util.List;

/**
 * Created by lamvdoan on 9/4/17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    MovieAdapterOnClickHandler mClickHandler;
    List<MovieInfo> movieInfoList;

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
            int adapterPosition = getAdapterPosition();
            mClickHandler.onClick(movieInfoList.get(adapterPosition));
        }
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(R.layout.movie_list_item, viewGroup, shouldAttachToParentImmediately);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder movieAdapterViewHolder, int position) {
        Uri imageRequestUri = NetworkUtils.buildUri(movieInfoList.get(position).getPosterPath());
        Context context = movieAdapterViewHolder.itemView.getContext();
        Picasso.with(context)
                .load(imageRequestUri)
                .into(movieAdapterViewHolder.mMovieThumbnail);
    }

    @Override
    public int getItemCount() {
        if (null == movieInfoList) {
            Log.w(TAG, "Size of movieInfoList is 0!");
            return 0;
        }

        return movieInfoList.size();
    }

    public interface MovieAdapterOnClickHandler {
        void onClick(MovieInfo movieInfo);
    }

    public void setMovieData(List<MovieInfo> movieData) {
        movieInfoList = movieData;
        notifyDataSetChanged();
    }
}
