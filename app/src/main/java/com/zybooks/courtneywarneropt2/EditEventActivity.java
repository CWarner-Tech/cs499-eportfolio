package com.zybooks.courtneywarneropt2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
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
                Toast.makeText(this, "Cannot select a past time", Toast.LENGTH_SHORT).show();
                return;
            }

            // Format time in 12-hour format with AM/PM
            String amPm;
            int displayHour = hourOfDay;

            if (hourOfDay >= 12) {
                amPm = "PM";
                if (hourOfDay > 12) displayHour -= 12;
            } else {
                amPm = "AM";
                if (hourOfDay == 0) displayHour = 12;
            }

            String formattedTime = String.format("%02d:%02d %s", displayHour, selectedMinute, amPm);
            etEventTime.setText(formattedTime);
        }, hour, minute, false);

        timePicker.show();
    }

    // Validates and updates event details
    private void updateEvent() {
        String updatedName = etEventName.getText().toString().trim();
        String updatedDate = etEventDate.getText().toString().trim();
        String updatedTime = etEventTime.getText().toString().trim();

        // Ensure all fields are filled
        if (updatedName.isEmpty() || updatedDate.isEmpty() || updatedTime.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Enforce event name length limit
        if (updatedName.length() > 45) {
            Toast.makeText(this, "Event name cannot exceed 45 characters", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Invalid time or date for modification", Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (ParseException e) {
            Toast.makeText(this, "Error parsing date/time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update event in database
        boolean isUpdated = dbHelper.updateEvent(eventId, userId, updatedName, updatedDate, updatedTime);

        if (isUpdated) {
            Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show();

            // Navigate back to "My Events" screen
            Intent intent = new Intent(EditEventActivity.this, EventListActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            finish();
        } else {
            // Show failure message if update fails
            Toast.makeText(this, "Failed to update event. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
