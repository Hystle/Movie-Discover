package com.hystle.zach.moviediscover.ui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.hystle.zach.moviediscover.R;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if(savedInstanceState == null){
            getFragmentManager().beginTransaction()
                    .replace(R.id.id_activity_settings, new SettingsFragment()).commit();
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

    // Registers a shared preference change listener that gets notified when preferences change
    @Override
    protected void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    // Unregisters a shared preference change listener
    @Override
    protected void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_enable_notifications_key))){
            notifyNotificationChanged();
        }
    }

    private void notifyNotificationChanged(){
        final int MOVIE_NOTIFICATION_ID = 0;
//        final long SYNC_INTERVAL = 1000 * 60 * 60 * 24;  // one day
//        final long SYNC_INTERVAL = 0;
        // check if displayNotification is enabled
        String notificationKey = this.getString(R.string.pref_enable_notifications_key);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean displayNotification = sp.getBoolean(
                notificationKey,
                Boolean.parseBoolean(this.getString(R.string.pref_enable_notifications_default)));
        NotificationManager mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if(displayNotification) {
            // check when is the last time sync notification
            String lastNotificationKey = this.getString(R.string.pref_last_notification);
            long lastSync = sp.getLong(lastNotificationKey, 0);

//            if (System.currentTimeMillis() - lastSync >= SYNC_INTERVAL) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.app_icon_64)
                            .setContentTitle("Movie Wander")
                            .setContentText("Click here back to Movie Discover");
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(this, MainActivity.class);
            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            // PendingIntent
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            // NotificationManager
            mNotificationManager.notify(MOVIE_NOTIFICATION_ID, mBuilder.build());
            // refresh last sync state
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(lastNotificationKey, System.currentTimeMillis());
            editor.apply();
        }else{
            mNotificationManager.cancel(MOVIE_NOTIFICATION_ID);
        }
    }
}