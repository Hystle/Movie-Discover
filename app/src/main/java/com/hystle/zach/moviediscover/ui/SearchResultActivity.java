package com.hystle.zach.moviediscover.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hystle.zach.moviediscover.Constants;
import com.hystle.zach.moviediscover.R;
import com.hystle.zach.moviediscover.adapter.RecyclerCastAdapter;
import com.hystle.zach.moviediscover.adapter.RecyclerViewAdapter;
import com.hystle.zach.moviediscover.entity.MovieInfo;
import com.hystle.zach.moviediscover.entity.PersonInfo;

import java.util.ArrayList;

public class SearchResultActivity extends AppCompatActivity
        implements RecyclerViewAdapter.OnItemClickListener, RecyclerCastAdapter.OnItemClickListener {

    private ArrayList mList;
    private RecyclerView recyclerView;
    private String FLAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        Intent intent = getIntent();
        mList = (ArrayList) intent.getSerializableExtra(Constants.EXTRA_SEARCH_RESULT);
        FLAG = intent.getStringExtra(Constants.EXTRA_SEARCH_RESULT_FLAG);

        recyclerView = (RecyclerView) findViewById(R.id.rv_frag_finder);
        RecyclerView.LayoutManager mLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);

        switch(FLAG){
            case Constants.TMDB_MOVIE:
                RecyclerViewAdapter searchMovieAdapter
                        = new RecyclerViewAdapter(this, Constants.SEARCH_MOVIE, 0, mList);
                searchMovieAdapter.setOnItemClickListener(this);
                recyclerView.setAdapter(searchMovieAdapter);
                break;
            case Constants.TMDB_PERSON:
                RecyclerViewAdapter searchPersonAdapter = new RecyclerViewAdapter(this, Constants.PERSONS, 0, mList);
                searchPersonAdapter.setOnItemClickListener(this);
                recyclerView.setAdapter(searchPersonAdapter);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent;
        switch(FLAG){
            case Constants.TMDB_MOVIE:
                intent = new Intent(this, DetailActivity.class);
                MovieInfo movieInfo = (MovieInfo) mList.get(position);
                intent.putExtra(Constants.EXTRA_ID, movieInfo.id);
                intent.putExtra(Constants.EXTRA_TITLE, movieInfo.title);
                startActivity(intent);
                break;
            case Constants.TMDB_PERSON:
                intent = new Intent(this, CastActivity.class);
                PersonInfo personInfo = (PersonInfo) mList.get(position);
                intent.putExtra(Constants.EXTRA_CAST, personInfo.id);
                intent.putExtra(Constants.EXTRA_CAST_FLAG, Constants.EXTRA_CAST);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }
}
