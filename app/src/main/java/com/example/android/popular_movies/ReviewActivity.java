package com.example.android.popular_movies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class ReviewActivity extends AppCompatActivity {
    TextView mReviewDetail;
    String mReviewText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mReviewDetail = (TextView) findViewById(R.id.review_text);
        Intent intent = getIntent();

        if (intent != null) {
            if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                mReviewText = intent.getStringExtra(Intent.EXTRA_TEXT);
                mReviewDetail.setText(mReviewText);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
