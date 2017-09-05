package com.example.android.popular_movies;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popular_movies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

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
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(R.layout.movie_list_item, viewGroup, shouldAttachToParentImmediately);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder movieAdapterViewHolder, int position) {
        Uri imageRequestUri = NetworkUtils.buildUri(IMAGE_URL + imageUrlList.get(position));
        Context context = movieAdapterViewHolder.itemView.getContext();
        Picasso.with(context)
                .load(imageRequestUri)
//                .transform(new CropSquareTransformation())
                .resize(540, 540)
                .centerCrop()
                .into(movieAdapterViewHolder.mMovieThumbnail);
    }

    public class CropSquareTransformation implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;
            Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
            if (result != source) {
                source.recycle();
            }
            return result;
        }

        @Override
        public String key() {
            return "square()";
        }
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
