package com.zybooks.courtneywarneropt2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * BroadcastReceiver that triggers event reminders.
 * For Milestone Three, Toasts were replaced with Notifications
 * for a clearer and more professional reminder system.
 */
public class SmsBroadcastReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "event_reminders_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Retrieve event details from the intent
        String eventName = intent.getStringExtra("eventName");
        String eventDate = intent.getStringExtra("eventDate");
        String eventTime = intent.getStringExtra("eventTime");

        if (eventName == null || eventDate == null || eventTime == null) {
            Log.e("SmsBroadcastReceiver", "Missing event details in intent!");
            return;
        }

        Log.d("SmsBroadcastReceiver", "📅 Reminder: " + eventName
                + " on " + eventDate + " at " + eventTime);

        // Make sure channel exists (Android 8.0+)
        createNotificationChannel(context);

        // Build notification message
        String reminderMessage = context.getString(
                R.string.reminder_triggered, eventName, eventDate, eventTime);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(context.getString(R.string.success))
                .setContentText(reminderMessage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Show notification (if permission granted on Android 13+)
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int notificationId = eventName.hashCode();

        if (Build.VERSION.SDK_INT < 33 ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(notificationId, builder.build());
        } else {
            Log.w("SmsBroadcastReceiver", "Notification permission not granted!");
        }
    }

    // Creates notification channel  //
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Event Reminders";
            String description = "Notifications for scheduled event reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}