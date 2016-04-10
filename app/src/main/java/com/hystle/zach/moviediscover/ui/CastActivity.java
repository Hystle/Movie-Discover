package com.hystle.zach.moviediscover.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.hystle.zach.moviediscover.Constants;
import com.hystle.zach.moviediscover.R;
import com.hystle.zach.moviediscover.entity.CastInfo;

import java.util.ArrayList;

public class CastActivity extends AppCompatActivity {

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cast);
        String flag = getIntent().getStringExtra(Constants.EXTRA_CAST_FLAG);
        switch (flag){
            case Constants.EXTRA_CAST:
                String mCastId = getIntent().getStringExtra(Constants.EXTRA_CAST);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_activity_cast, CastDetailFragment.newInstance(mCastId))
                        .commit();
                break;
            case Constants.EXTRA_CAST_LIST:
                ArrayList<CastInfo> castsList
                        = (ArrayList<CastInfo>) getIntent().getSerializableExtra(Constants.EXTRA_CAST_LIST);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_activity_cast, CastListFragment.newInstance(castsList))
                        .commit();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_setting:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_about:
                new AlertDialog.Builder(this)
                        .setTitle("About")
                        .setMessage(R.string.about)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
