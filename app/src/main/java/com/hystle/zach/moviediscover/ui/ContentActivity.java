package com.hystle.zach.moviediscover.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.hystle.zach.moviediscover.Constants;
import com.hystle.zach.moviediscover.R;
import com.hystle.zach.moviediscover.entity.ReviewInfo;

import java.util.ArrayList;

public class ContentActivity extends AppCompatActivity {

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        TextView reviewsView = (TextView) findViewById(R.id.tv_activity_content);
        ArrayList<ReviewInfo> reviewsList = (ArrayList<ReviewInfo>) getIntent()
                .getSerializableExtra(Constants.EXTRA_REVIEWS);

        StringBuilder reviewsBuilder = new StringBuilder();
        for (ReviewInfo reviewInfo : reviewsList) {
            reviewsBuilder.append("User: ").append(reviewInfo.author).append("\n")
                    .append(reviewInfo.content).append("\n")
                    .append("---------------------------").append("\n\n");
        }
        if (reviewsView != null) {
            reviewsView.setText(reviewsBuilder.toString());
        }
    }
}
