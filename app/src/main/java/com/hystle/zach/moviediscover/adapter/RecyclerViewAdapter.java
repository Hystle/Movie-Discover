package com.hystle.zach.moviediscover.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hystle.zach.moviediscover.Constants;
import com.hystle.zach.moviediscover.R;
import com.hystle.zach.moviediscover.Utility;
import com.hystle.zach.moviediscover.entity.MovieInfo;
import com.hystle.zach.moviediscover.entity.PersonInfo;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    public OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(OnItemClickListener listener){
        this.mOnItemClickListener = listener;
    }

    private Context mContext;
    private String mSection;
    private int mPageNo;    // potentially used for populate different views for tabs in a same section
    public LayoutInflater mLayoutInflater;
    private ArrayList mList;

    public RecyclerViewAdapter(Context context, String section, int pageNo, ArrayList list){
        this.mContext = context;
        this.mSection = section;
        this.mPageNo = pageNo;
        mLayoutInflater = LayoutInflater.from(mContext);

        this.mList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardView;
        switch (mSection) {
            case Constants.CHARTS:
            case Constants.MY_RATES:
                cardView = mLayoutInflater.inflate(R.layout.item_grid, parent, false);
                return new GridViewHolder(cardView);
            case Constants.THEATER:
            case Constants.PERSONS:
            case Constants.SEARCH_MOVIE:
                cardView = mLayoutInflater.inflate(R.layout.item_linear, parent, false);
                return new LinearViewHolder(cardView);
        }
        return null;
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

        if(mList.size() != 0){
            if (mSection.equals(Constants.CHARTS) || mSection.equals(Constants.MY_RATES)) {
                String posterUrl = Constants.TMDB_BASE_URL_IMAGE_W342
                        + ((MovieInfo) mList.get(position)).posterPath;
                // set poster size to half of the screen width
                ImageView imageView = ((GridViewHolder) holder).mImageView;
                float widthPixels = Utility.getItemWidthInPixel(mContext, 2);
                float heightPixels = widthPixels * Constants.PICTURE_RATIO;
                FrameLayout.LayoutParams ivParams = new FrameLayout.LayoutParams(
                        (int) widthPixels, (int) heightPixels);
                imageView.setLayoutParams(ivParams);
                Glide.with(mContext)
                        .load(posterUrl)
                        .placeholder(R.drawable.placeholder_loading)
                        .error(R.drawable.placeholder_error)
                        .into(imageView);
            }else if (mSection.equals(Constants.THEATER) || mSection.equals(Constants.SEARCH_MOVIE)){
                MovieInfo movieInfo = (MovieInfo) mList.get(position);
                String posterUrl = Constants.TMDB_BASE_URL_IMAGE_W185 + movieInfo.posterPath;
                final LinearViewHolder linearViewHolder = (LinearViewHolder) holder;
                Glide.with(mContext)
                        .load(posterUrl)
                        .placeholder(R.drawable.placeholder_loading)
                        .error(R.drawable.placeholder_error)
                        .into(linearViewHolder.posterIV);
                linearViewHolder.headLineTV.setText(movieInfo.title);
                linearViewHolder.contentTV.setText(Utility.formatDate(mContext, movieInfo.date));

                String vote = movieInfo.vote;
                if(!vote.equals("0")){
                    String voteStr =  vote + "/" + 10;
                    linearViewHolder.subContentTV.setText(voteStr);
                }
            }else if (mSection.equals(Constants.PERSONS)){
                PersonInfo personInfo = (PersonInfo) mList.get(position);
                String posterUrl = Constants.TMDB_BASE_URL_IMAGE_W185 + personInfo.profilePath;
                final LinearViewHolder linearViewHolder = (LinearViewHolder) holder;
                Glide.with(mContext)
                        .load(posterUrl)
                        .placeholder(R.drawable.placeholder_loading)
                        .error(R.drawable.placeholder_error)
                        .into(linearViewHolder.posterIV);
                linearViewHolder.headLineTV.setText(personInfo.name);
//                StringBuilder builder = new StringBuilder();
//                for (String s: personInfo.creditsList){
//                    if (builder.length() < 10){
//                        builder.append(s).append("\n");
//                    }
//                }
//                linearViewHolder.contentTV.setText(builder.toString());
            }
        }
    }
    @Override
    public int getItemCount() {
        return mList.size();
    }
}
