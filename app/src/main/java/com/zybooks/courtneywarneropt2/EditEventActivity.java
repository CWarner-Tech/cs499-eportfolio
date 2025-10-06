package com.zybooks.courtneywarneropt2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditEventActivity extends AppCompatActivity {

    private EditText etEventName, etEventDate, etEventTime;
    private Button btnUpdateEvent, btnBackToEvents;
    private EventDatabaseHelper dbHelper;
    private int eventId, userId;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // Initialize UI components
        etEventName = findViewById(R.id.etEventName);
        etEventDate = findViewById(R.id.etEventDate);
        etEventTime = findViewById(R.id.etEventTime);
        btnUpdateEvent = findViewById(R.id.btnUpdateEvent);
        btnBackToEvents = findViewById(R.id.btnBackToEvents);
        dbHelper = new EventDatabaseHelper(this);
        calendar = Calendar.getInstance();

        // Retrieve event details from intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("user_id", -1);
        eventId = intent.getIntExtra("event_id", -1);

        String eventName = intent.getStringExtra("event_name");
        String eventDate = intent.getStringExtra("event_date");
        String eventTime = intent.getStringExtra("event_time");

        // Populate fields with existing event data
        etEventName.setText(eventName);
        etEventDate.setText(eventDate);
        etEventTime.setText(eventTime);

        // Set click listeners for date and time pickers
        etEventDate.setOnClickListener(v -> showDatePicker());
        etEventTime.setOnClickListener(v -> showTimePicker());

        // Set actions for update and back buttons
        btnUpdateEvent.setOnClickListener(v -> updateEvent());
        btnBackToEvents.setOnClickListener(v -> finish());
    }

    // Opens date picker dialog
    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            etEventDate.setText((month + 1) + "/" + dayOfMonth + "/" + year);

            //Clear error if user selects valid date
            etEventDate.setError(null);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        // Prevent selecting past dates
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
        datePicker.show();
    }

    // Opens time picker dialog
    private void showTimePicker() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, selectedMinute) -> {
            Calendar selectedTime = Calendar.getInstance();
            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedTime.set(Calendar.MINUTE, selectedMinute);

            // Prevent selecting past times if today is selected
            if (etEventDate.getText().toString().equals(new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(now.getTime()))
                    && selectedTime.before(now)) {
                //Replaced Toast with inline + dialog
                //Replaced hardcoded error with string resource
                etEventTime.setError(getString(R.string.invalid_event_time));
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.invalid_event_time_title)) //FIXED
                        .setMessage(getString(R.string.invalid_event_time_dialog)) //FIXED
                        .setPositiveButton(getString(R.string.ok), null) //FIXED
                        .show();
                return;
            }

            // Format time in 12-hour format with AM/PM
            String amPm;
            int displayHour = hourOfDay;

            if (hourOfDay >= 12) {
                amPm = getString(R.string.pm); //Use string resource
                if (hourOfDay > 12) displayHour -= 12;
            } else {
                amPm = getString(R.string.am); //Use string resource
                if (hourOfDay == 0) displayHour = 12;
            }


            String formattedTime = String.format(Locale.US, "%02d:%02d %s", displayHour, selectedMinute, amPm);
            etEventTime.setText(formattedTime);

            //Clear error when valid time is chosen
            etEventTime.setError(null);
        }, hour, minute, false);

        timePicker.show();
    }

    // Validates and updates event details
    private void updateEvent() {
        String updatedName = etEventName.getText().toString().trim();
        String updatedDate = etEventDate.getText().toString().trim();
        String updatedTime = etEventTime.getText().toString().trim();

        // Ensure all fields are filled
        if (updatedName.isEmpty()) {
            etEventName.setError(getString(R.string.error_event_name_required));
            return;
        }

        if (updatedDate.isEmpty()) {
            etEventDate.setError(getString(R.string.error_select_date));
            return;
        }

        if (updatedTime.isEmpty()) {
            etEventTime.setError(getString(R.string.error_select_time));
            return;
        }

        // Enforce event name length limit
        if (updatedName.length() > 45) {
            etEventName.setError(getString(R.string.error_event_name_too_long));
            return;
        }

        // Validate date and time before updating
        Calendar selectedCalendar = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US);
            Date selectedDateTime = sdf.parse(updatedDate + " " + updatedTime);
            selectedCalendar.setTime(selectedDateTime);

            Calendar now = Calendar.getInstance();

            // Prevent updating to a past date/time
            if (selectedCalendar.before(now)) {
                //Replace Toast with dialog, and use string resource
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.invalid_event_time_title))
                        .setMessage(getString(R.string.invalid_event_time_dialog))
                        .setPositiveButton(getString(R.string.ok), null)
                        .show();
                return;
            }

        } catch (ParseException e) {
            // Dialog box and string resource
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.error))
                    .setMessage(getString(R.string.error_schedule_alarm))
                    .setPositiveButton(getString(R.string.ok), null)
                    .show();
            return;
        }
        // Convert updatedDate and updatedTime into epoch values before saving
        long dateEpoch = 0;
        long timeEpoch = 0;
        try {
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            SimpleDateFormat tf = new SimpleDateFormat("hh:mm a", Locale.US);
            Date parseDate = df.parse(updatedDate);
            Date parseTime = tf.parse(updatedTime);
            if (parseDate != null) dateEpoch = parseDate.getTime();
            if (parseTime != null) timeEpoch = parseTime.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Update event in database
        boolean isUpdated = dbHelper.updateEvent(eventId, userId, updatedName, dateEpoch, timeEpoch);

        if (isUpdated) {
            //Replace Toast with dialog and use string resource
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.success))
                    .setMessage(getString(R.string.event_updated))
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        // Navigate back to "My Events" screen
                        Intent intent = new Intent(EditEventActivity.this, EventListActivity.class);
                        intent.putExtra("user_id", userId);
                        startActivity(intent);
                        finish();
                    })
                    .show();
        } else {
            // Show failure message if update fails
            //Replace Toast with dialog and use string resource
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.error))
                    .setMessage(getString(R.string.error_event_add_failed))
                    .setPositiveButton(getString(R.string.ok), null)
                    .show();
        }
    }
}