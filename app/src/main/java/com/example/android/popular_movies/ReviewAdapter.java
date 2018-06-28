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

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    ReviewAdapter.ReviewAdapterOnClickHandler mClickHandler;
    List<String> mReviewList;

    public ReviewAdapter(ReviewAdapterOnClickHandler mClickHandler) {
        this.mClickHandler = mClickHandler;
    }

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mReview;
        public final View mLine;

        public ReviewAdapterViewHolder(View view) {
            super(view);
            mReview = (TextView) view.findViewById(R.id.review_preview);
            mLine = view.findViewById(R.id.review_horizontal_line);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mClickHandler.onClick(mReviewList.get(adapterPosition));
        }
    }

    @Override
    public ReviewAdapter.ReviewAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(R.layout.review_item_list, viewGroup, shouldAttachToParentImmediately);
        return new ReviewAdapter.ReviewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewAdapter.ReviewAdapterViewHolder reviewAdapterViewHolder, int position) {
        reviewAdapterViewHolder.mReview.setText("Review " + (position + 1));
    }

    @Override
    public int getItemCount() {
        if (null == mReviewList) {
            Log.w(TAG, "Size of mTrailerList is 0!");
            return 0;
        }

        return mReviewList.size();
    }

    public interface ReviewAdapterOnClickHandler {
        void onClick(String review);
    }

    public void setReviewData(List<String> reviewData) {
        mReviewList = reviewData;
        notifyDataSetChanged();
    }
}
