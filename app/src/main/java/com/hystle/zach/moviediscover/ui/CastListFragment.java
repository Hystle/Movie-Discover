package com.hystle.zach.moviediscover.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hystle.zach.moviediscover.Constants;
import com.hystle.zach.moviediscover.R;
import com.hystle.zach.moviediscover.adapter.RecyclerCastAdapter;
import com.hystle.zach.moviediscover.entity.PersonInfo;

import java.util.ArrayList;

public class CastListFragment extends Fragment implements RecyclerCastAdapter.OnItemClickListener {
    private static final String ARG_CASTS = "arg_casts";

    private ArrayList castsList;
    private RecyclerView mRecyclerView;
    private Context mContext;

    // Required empty public constructor
    public CastListFragment() {}

    public static CastListFragment newInstance(ArrayList list) {
        CastListFragment fragment = new CastListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_CASTS, list);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            castsList = getArguments().getParcelableArrayList(ARG_CASTS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_cast_list, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_frag_detail_cast);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        RecyclerCastAdapter recyclerCastAdapter = new RecyclerCastAdapter(mContext, castsList);
        recyclerCastAdapter.setOnItemClickListener(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(recyclerCastAdapter);

    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(mContext, CastActivity.class);
        intent.putExtra(Constants.EXTRA_CAST, ((PersonInfo)castsList.get(position)).id);
        intent.putExtra(Constants.EXTRA_CAST_FLAG, Constants.EXTRA_CAST);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
}
