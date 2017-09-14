package com.example.android.popular_movies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popular_movies.utils.NetworkUtils;

import java.util.List;

/**
 * Created by lamvdoan on 9/13/17.
 */


public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerAdapterViewHolder> {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    TrailerAdapter.TrailerAdapterOnClickHandler mClickHandler;
    List<String> mTrailerList;

    public TrailerAdapter(TrailerAdapterOnClickHandler mClickHandler) {
        this.mClickHandler = mClickHandler;
    }

    public class TrailerAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mTrailer;
        public final View mLine;

        public TrailerAdapterViewHolder(View view) {
            super(view);
            mTrailer = (TextView) view.findViewById(R.id.review_preview);
            mLine = view.findViewById(R.id.trailer_horizontal_line);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mClickHandler.onClick(mTrailerList.get(adapterPosition));
        }
    }

    @Override
    public TrailerAdapter.TrailerAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(R.layout.review_item_list, viewGroup, shouldAttachToParentImmediately);
        return new TrailerAdapter.TrailerAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerAdapter.TrailerAdapterViewHolder trailerAdapterViewHolder, int position) {
        trailerAdapterViewHolder.mTrailer.setText("Trailer " + (position + 1));
    }

    @Override
    public int getItemCount() {
        if (null == mTrailerList) {
            Log.w(TAG, "Size of mTrailerList is 0!");
            return 0;
        }

        return mTrailerList.size();
    }

    public interface TrailerAdapterOnClickHandler {
        void onClick(String review);
    }

    public void setTrailerData(List<String> trailerData) {
        mTrailerList = trailerData;
        notifyDataSetChanged();
    }
}
