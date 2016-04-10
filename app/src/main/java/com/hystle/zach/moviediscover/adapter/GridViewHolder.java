package com.hystle.zach.moviediscover.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.hystle.zach.moviediscover.R;

public class GridViewHolder extends RecyclerView.ViewHolder {

    public ImageView mImageView;

    public GridViewHolder(View itemView) {
        super(itemView);
        mImageView = (ImageView) itemView.findViewById(R.id.iv_item_grid);
    }
}
