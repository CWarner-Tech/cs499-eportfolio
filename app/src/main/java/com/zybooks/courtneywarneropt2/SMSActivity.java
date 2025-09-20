package com.zybooks.courtneywarneropt2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// Handles SMS permission requests and sending event reminders
public class SMSActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 101;
    private String eventName, eventDate, eventTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve event details from intent
        eventName = getIntent().getStringExtra("eventName");
        eventDate = getIntent().getStringExtra("eventDate");
        eventTime = getIntent().getStringExtra("eventTime");

        requestSmsPermission();
    }

    // Checks and requests SMS permission
    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            sendSmsNotification(); // Permission granted, send SMS
        } else {
            // Show rationale if the user previously denied permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                showPermissionExplanation();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
            }
        }
    }

    // Displays an alert dialog explaining why SMS permission is needed
    private void showPermissionExplanation() {
        new AlertDialog.Builder(this)
                .setTitle("SMS Permission Required")
                .setMessage("This app needs SMS permission to send event reminders. Please allow SMS access.")
                .setPositiveButton("OK", (dialog, which) ->
                        ActivityCompat.requestPermissions(SMSActivity.this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE)
                )
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "SMS Permission Denied!", Toast.LENGTH_SHORT).show();
                    returnToPreviousScreen();
                })
                .show();
    }

    // Simulates sending an SMS notification
    private void sendSmsNotification() {
        Toast.makeText(this, "📩 SMS Reminder Sent!", Toast.LENGTH_SHORT).show();
        returnToPreviousScreen();
    }

    // Returns to the previous screen after handling SMS permission
    private void returnToPreviousScreen() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();  // Closes SMSActivity and returns to AddEventActivity
    }

    // Handles SMS permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSmsNotification();
            } else {
                Toast.makeText(this, "SMS Permission Denied!", Toast.LENGTH_SHORT).show();
            }
            returnToPreviousScreen();
        }
    }
}
