package com.zybooks.courtneywarneropt2;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.util.Calendar;
import java.util.Locale;

// Time picker fragment for selecting event time
public class TimePickerFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Get the current time
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create and return a TimePickerDialog
        return new TimePickerDialog(getActivity(), (view, hourOfDay, minute1) -> {
            EditText etEventTime = getActivity().findViewById(R.id.etEventTime);

            // Convert 24-hour format to 12-hour format with AM/PM
            String amPm;
            int hourFormatted;

            if (hourOfDay >= 12) {
                amPm = getString(R.string.pm); // FIXED
                hourFormatted = (hourOfDay == 12) ? 12 : hourOfDay - 12;
            } else {
                amPm = getString(R.string.am); // FIXED
                hourFormatted = (hourOfDay == 0) ? 12 : hourOfDay;
            }

            // Format and set selected time
            String formattedTime = String.format(Locale.US, "%02d:%02d %s", hourFormatted, minute1, amPm); // FIXED
            if (etEventTime != null) {
                etEventTime.setText(formattedTime);
                etEventTime.setError(null); // FIXED: Clear previous error if valid time is selected
            }
        }, hour, minute, false); // false enforces 12-hour format
    }
}
