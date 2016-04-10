package com.hystle.zach.moviediscover.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.hystle.zach.moviediscover.Constants;
import com.hystle.zach.moviediscover.R;
import com.hystle.zach.moviediscover.entity.CastInfo;

import java.util.ArrayList;

public class RecyclerCastAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    public OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mOnItemClickListener = listener;
    }

    private Context mContext;
    private ArrayList<CastInfo> mCastsList;

    public RecyclerCastAdapter(Context context, ArrayList castsList){
        this.mContext = context;
        this.mCastsList = castsList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_linear, parent, false);
        return new LinearViewHolder(view);
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

        LinearViewHolder linearViewHolder = (LinearViewHolder)holder;
        linearViewHolder.headLineTV.setText(mCastsList.get(position).name);
        linearViewHolder.contentTV.setText(mCastsList.get(position).character);
        String posterUrl = Constants.TMDB_BASE_URL_IMAGE_W185 + mCastsList.get(position).profilePath;
        Glide.with(mContext)
                .load(posterUrl)
                .placeholder(R.drawable.placeholder2)
                .error(R.drawable.placeholder3)
                .into(linearViewHolder.posterIV);
    }

    @Override
    public int getItemCount() {
        return mCastsList.size();
    }
}
