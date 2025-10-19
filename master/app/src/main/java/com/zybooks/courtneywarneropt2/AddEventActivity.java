package com.zybooks.courtneywarneropt2;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEventActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST = 101;

    private EditText etEventName, etEventDate, etEventTime;
    private Button btnSaveEvent, btnCancel;
    private EventDatabaseHelper dbHelper;
    private Calendar calendar;
    private int userId;
    private String eventName, eventDate, eventTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        // Initialize UI components
        etEventName = findViewById(R.id.etEventName);
        etEventDate = findViewById(R.id.etEventDate);
        etEventTime = findViewById(R.id.etEventTime);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);
        btnCancel = findViewById(R.id.btnCancel);
        dbHelper = new EventDatabaseHelper(this);
        calendar = Calendar.getInstance();

        // Retrieve user ID from intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("user_id", -1);

        // Set click listeners for date and time pickers
        etEventDate.setOnClickListener(v -> showDatePicker());
        etEventTime.setOnClickListener(v -> showTimePicker());

        // Set event save and cancel actions
        btnSaveEvent.setOnClickListener(v -> saveEvent());
        btnCancel.setOnClickListener(v -> finish());
    }

    // Opens date picker dialog
    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            etEventDate.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.getDatePicker().setMinDate(System.currentTimeMillis()); // Prevent past dates
        datePicker.show();
    }

    // Opens time picker dialog
    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            etEventTime.setText(timeFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

        timePicker.show();
    }

    // Validates input and prompts for SMS reminders
    private void saveEvent() {
        eventName = etEventName.getText().toString().trim();
        eventDate = etEventDate.getText().toString().trim();
        eventTime = etEventTime.getText().toString().trim();

        // Ensure all fields are filled
        if (eventName.isEmpty() || eventDate.isEmpty() || eventTime.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Enforce event name length limit
        if (eventName.length() > 45) {
            Toast.makeText(this, "Event name cannot exceed 45 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure event date/time is in the future
        Calendar now = Calendar.getInstance();
        if (calendar.before(now)) {
            Toast.makeText(this, "Invalid: Event date/time has already passed!", Toast.LENGTH_LONG).show();
            return;
        }

        // Check for duplicate event before prompting for SMS
        if (dbHelper.eventExists(userId, eventName, eventDate, eventTime)) {
            Toast.makeText(this, " Event already exists!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prompt user for SMS reminder
        showSmsPrompt();
    }

    // Displays SMS reminder prompt
    private void showSmsPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enable SMS Reminder?")
                .setMessage("Would you like to receive an SMS reminder for this event?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (checkSmsPermission()) {
                        insertEvent(true); // Save with SMS reminder
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    insertEvent(false); // Save without SMS reminder
                })
                .setCancelable(false)
                .show();
    }

    // Inserts event into database and schedules reminder if required
    private void insertEvent(boolean scheduleReminder) {
        boolean inserted = dbHelper.insertEvent(userId, eventName, eventDate, eventTime);

        if (inserted) {
            Toast.makeText(this, " Event added successfully.", Toast.LENGTH_SHORT).show();
            if (scheduleReminder) {
                checkExactAlarmPermission();
            } else {
                finish();
            }
        } else {
            Toast.makeText(this, " Failed to add event.", Toast.LENGTH_SHORT).show();
        }
    }

    // Checks for SMS permission
    private boolean checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST);
            return false;
        }
    }

    // Handles SMS permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkExactAlarmPermission(); // Proceed with reminder setup
            } else {
                Toast.makeText(this, "SMS permission denied. No reminder will be sent.", Toast.LENGTH_SHORT).show();
                insertEvent(false);
            }
        }
    }

    // Checks if exact alarms are allowed for scheduling reminders
    private void checkExactAlarmPermission() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, " Allow exact alarms for reminders!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }

        scheduleReminder();
    }

    // Schedules an SMS reminder using AlarmManager
    private void scheduleReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, SmsBroadcastReceiver.class);
        intent.putExtra("eventName", eventName);
        intent.putExtra("eventDate", eventDate);
        intent.putExtra("eventTime", eventTime);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, eventName.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            long triggerTime = calendar.getTimeInMillis();
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            Toast.makeText(this, " SMS reminder set for " + eventTime, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, " Unable to schedule exact alarm.", Toast.LENGTH_LONG).show();
        }

        finish();
    }
}
