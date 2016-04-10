package com.hystle.zach.moviediscover.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hystle.zach.moviediscover.Constants;
import com.hystle.zach.moviediscover.R;
import com.hystle.zach.moviediscover.entity.MovieInfo;

import java.util.ArrayList;

public class RecyclerDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    public OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    private Context mContext;
    private ArrayList<MovieInfo> mMoviesList;

    public RecyclerDetailAdapter(Context context, ArrayList<MovieInfo> mMoviesList) {
        this.mContext = context;
        this.mMoviesList = mMoviesList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_similar, parent, false);
        return new GridSimilarHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder.itemView, position);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onItemLongClick(holder.itemView, position);
                    return true;
                }
            });
        }
        if (mMoviesList.size() != 0) {
            String posterUrl = Constants.TMDB_BASE_URL_IMAGE_W185 + mMoviesList.get(position).posterPath;
            GridSimilarHolder gridViewHolder = (GridSimilarHolder) holder;
            Glide.with(mContext)
                    .load(posterUrl)
                    .placeholder(R.drawable.placeholder1)
                    .error(R.drawable.placeholder2)
                    .into(gridViewHolder.mImageView);
            TextView tv = gridViewHolder.mTextView;
            tv.setTextSize(13);
            tv.setText(mMoviesList.get(position).title);
        }
    }

    @Override
    public int getItemCount() {
        return mMoviesList.size();
    }

    class GridSimilarHolder extends RecyclerView.ViewHolder {

        public ImageView mImageView;
        public TextView mTextView;

        public GridSimilarHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.iv_item_similar);
            mTextView = (TextView) itemView.findViewById(R.id.tv_item_similar);
        }
    }
}
