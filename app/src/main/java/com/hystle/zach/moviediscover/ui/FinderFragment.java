package com.hystle.zach.moviediscover.ui;


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
import com.hystle.zach.moviediscover.adapter.RecyclerViewAdapter;
import com.hystle.zach.moviediscover.entity.MovieInfo;

import java.util.ArrayList;


public class FinderFragment extends Fragment implements RecyclerViewAdapter.OnItemClickListener {

    private ArrayList<MovieInfo> mMoviesList;

    private RecyclerView recyclerView;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        mMoviesList = (ArrayList<MovieInfo>) args.getSerializable("BUNDLE");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_finder, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_frag_finder);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);

        RecyclerViewAdapter mFinderAdapter = new RecyclerViewAdapter(getActivity(), Constants.ADVANCED_SEARCH, 0, mMoviesList);
        mFinderAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mFinderAdapter);

    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(Constants.EXTRA_ID, mMoviesList.get(position).id);
        intent.putExtra(Constants.EXTRA_TITLE, mMoviesList.get(position).title);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {
    }
}
