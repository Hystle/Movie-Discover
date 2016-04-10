package com.hystle.zach.moviediscover.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.hystle.zach.moviediscover.R;

public class LinearViewHolder extends RecyclerView.ViewHolder{
    public ImageView posterIV;
    public TextView headLineTV;
    public TextView contentTV;
    public TextView subContentTV;

    public LinearViewHolder(View v){
        super(v);
        posterIV = (ImageView) v.findViewById(R.id.iv_item_linear_poster);
        headLineTV = (TextView) v.findViewById(R.id.tv_theater_headline);
        contentTV = (TextView) v.findViewById(R.id.tv_item_linear_content);
        subContentTV = (TextView) v.findViewById(R.id.tv_item_linear_subcontent);
    }
}
