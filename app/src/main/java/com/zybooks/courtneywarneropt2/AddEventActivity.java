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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import java.text.ParseException;
import java.util.Date;

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

            //Specify Locale.US
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            etEventDate.setText(dateFormat.format(calendar.getTime()));

            //Clear any previous error if a valid date is chosen
            etEventDate.setError(null);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.getDatePicker().setMinDate(System.currentTimeMillis()); // Prevent past dates
        datePicker.show();
    }

    // Opens time picker dialog
    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            //Specify Locale.US
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            etEventTime.setText(timeFormat.format(calendar.getTime()));

            //Clears previous error if a valid time is selected
            etEventTime.setError(null);

        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

        timePicker.show();
    }

    // Validates input and prompts for SMS reminders
    private void saveEvent() {
        eventName = etEventName.getText().toString().trim();
        eventDate = etEventDate.getText().toString().trim();
        eventTime = etEventTime.getText().toString().trim();

        // Ensure all fields are filled
        //Using setError() so the error is visible on the input fields
        //Uses string resources for localization
        if (eventName.isEmpty()) {
            etEventName.setError(getString(R.string.error_event_name_required));
            return;
        }

        if (eventDate.isEmpty()) {
            etEventDate.setError(getString(R.string.error_select_date));
            return;
        }

        if (eventTime.isEmpty()) {
            etEventTime.setError(getString(R.string.error_select_time));
            return;
        }

        //Added a minimum length check to prevent 1 letter event names
        //Uses string resources for localization instead of hardcoded text
        if (eventName.length() < 3) {
            etEventName.setError(getString(R.string.error_event_name_too_short));
            return;
        }
        //Enforce event name length limit - removed Toast
        //Uses string resources for localization instead of hardcoded text
        if (eventName.length() > 45) {
            etEventName.setError(getString(R.string.error_event_name_too_long));
            return;
        }

        //Separated past - time validation (only if the date is today)
        Calendar now = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        String todayStr = dateFormat.format(now.getTime());

        if (eventDate.equals(todayStr) && calendar.before(now)) {
            //Uses string resources for localization instead of hardcoded text
            etEventTime.setError(getString(R.string.invalid_event_time));
            // Provides feedback using an AlertDialog instead of a Toast for better user clarity
            new AlertDialog.Builder(this) //Added dialog for clearer feedback
                    //Uses string resources for localization instead of hardcoded text
                    .setTitle(getString(R.string.invalid_event_time_title))
                    .setMessage(getString(R.string.invalid_event_time_dialog))
                    .setPositiveButton(getString(R.string.ok), null)
                    .show();

            return;
        }
        // Converts date and time inputs into epoch values before saving to database
        long dateEpoch = 0;
        long timeEpoch = 0;
        try {
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            SimpleDateFormat tf = new SimpleDateFormat("hh:mm a", Locale.US);
            Date parseDate = df.parse(eventDate);
            Date parseTime = tf.parse(eventTime);
            if (parseDate != null) dateEpoch = parseDate.getTime();
            if (parseTime != null) timeEpoch = parseTime.getTime();}
        catch (ParseException e) {
            e.printStackTrace();
        }// This replaces text-based date/time storage with numeric timestamps.

        // Updated duplicate event check to use epoch timestamps
        if (dbHelper.eventExists(userId, eventName, dateEpoch, timeEpoch)) {
            // Provides feedback using an AlertDialog
            //Uses string resources for localization
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.duplicate_event_title))
                    .setMessage(getString(R.string.duplicate_event_message))
                    .setPositiveButton(getString(R.string.ok), null)
                    .show();
            return;
        }

        // Prompt user for SMS reminder
        showSmsPrompt(dateEpoch, timeEpoch);
    }

    // Displays SMS reminder prompt
    // Method updated to accept dateEpoch/timeEpoch
    private void showSmsPrompt(long dateEpoch, long timeEpoch) {
        //Replaced Toast with Dialog box
        // Uses string resources for localization instead of hardcoded text
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.enable_sms_reminder_title))
                .setMessage(getString(R.string.enable_sms_reminder_message))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    if (checkSmsPermission()) {
                        insertEvent(true, dateEpoch, timeEpoch);
                    }
                })
                .setNegativeButton(getString(R.string.no), (dialog, which) -> {
                    insertEvent(false, dateEpoch, timeEpoch);
                })
                .setCancelable(false)
                .show();
    }

    // Inserts event into database and schedules reminder if required
    // Updated to accept epoch parameters
    private void insertEvent(boolean scheduleReminder, long dateEpoch, long timeEpoch) {
        boolean inserted = dbHelper.insertEvent(userId, eventName, dateEpoch, timeEpoch);

        if (inserted) {
            // Uses string resources for localization
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.success))
                    .setMessage(getString(R.string.event_saved))
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        if (scheduleReminder) {
                            checkExactAlarmPermission();
                        } else {
                            finish();
                        }
                    })
                    .show();
        } else {


            // Uses string resources for localization
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.error))
                    .setMessage(getString(R.string.error_event_add_failed))
                    .setPositiveButton(getString(R.string.ok), null)
                    .show();
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
                // Uses string resources
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.permission_denied_title))
                        .setMessage(getString(R.string.permission_denied_message))
                        .setPositiveButton(getString(R.string.ok), (dialog, which) -> insertEvent(false,0,0))
                        .show();
            }
        }
    }

    // Checks if exact alarms are allowed
    private void checkExactAlarmPermission() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Uses string resources for localization instead of hardcoded text
                new AlertDialog.Builder(this) //Dialog prompt for alam permission
                        .setTitle(getString(R.string.permission_required_title))
                        .setMessage(getString(R.string.permission_required_message))
                        .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        })
                        .show();
                return;
            }
        }

        scheduleReminder(); // Only call if permission is allowed
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
            // Uses string resources for localization instead of hardcoded text
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.reminder_scheduled_title))
                    .setMessage(getString(R.string.reminder_scheduled_message, eventTime))
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> finish())
                    .show();
        } catch (SecurityException e) {
            // Uses string resources for localization instead of hardcoded text
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.error))
                    .setMessage(getString(R.string.error_schedule_alarm))
                    .setPositiveButton(getString(R.string.ok), null)
                    .show();
        }
    }
}
