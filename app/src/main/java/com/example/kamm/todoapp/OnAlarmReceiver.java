package com.example.kamm.todoapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.Date;

/**
 * Created by knalepa on 2018-06-14.
 */

public class OnAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int notifyID = 0;
        String CHANNEL_ID = "my_channel_01";// The id of the channel.

        Bundle bundle = intent.getBundleExtra("bundle");
        if (bundle != null) {
            Item item = (Item) bundle.getSerializable("item");

            String message = "";

            notifyID = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

            if (item.getDate() == new Date().getTime())
                message = "is going to start today!";
            else
                message = "has finished!";

            String wholeMessage = item.getTitle() + " " + message;

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_notify_sync)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setTicker("ticker")
                    .setContentTitle("TODO notification")
                    .setContentInfo("Info")
                    .setContentText(wholeMessage)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setChannelId(CHANNEL_ID);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        "TODO notification",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(notifyID, builder.build());
        }
    }
}

