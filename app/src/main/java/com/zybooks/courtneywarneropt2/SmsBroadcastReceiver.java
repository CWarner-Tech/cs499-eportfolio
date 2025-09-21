package com.zybooks.courtneywarneropt2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

// Broadcast receiver that triggers event reminders
public class SmsBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Retrieve event details from the intent
        String eventName = intent.getStringExtra("eventName");
        String eventDate = intent.getStringExtra("eventDate");
        String eventTime = intent.getStringExtra("eventTime");

        //Added null-safety check for extras
        if (eventName == null || eventDate == null || eventTime == null) {
            Log.e("SmsBroadcastReceiver", "Missing event details in intent!");
            return;
        }

        // Log the reminder event
        Log.d("SmsBroadcastReceiver", "📅 Reminder Triggered: " + eventName + " on " + eventDate + " at " + eventTime);

        // Display a toast notification when the reminder triggers
        // Use string resource
        String reminderMessage = context.getString(R.string.reminder_triggered, eventName, eventDate, eventTime);
        Toast.makeText(context, reminderMessage, Toast.LENGTH_LONG).show();
    }
}
