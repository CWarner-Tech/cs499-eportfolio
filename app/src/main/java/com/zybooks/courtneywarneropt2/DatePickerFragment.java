package com.zybooks.courtneywarneropt2;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.util.Calendar;
import java.util.Locale;//Added import for locale

public class DatePickerFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Get current date
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create and return DatePickerDialog
        // Declare DatePickerDialog and assign it to a variable
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(), (view, year1, month1, dayOfMonth) -> {
            EditText etEventDate = requireActivity().findViewById(R.id.etEventDate);

            // Add null-safety
            if (etEventDate != null) {
                // Use Locale.US explicitly to avoid default locale bug
                String formattedDate = String.format(Locale.US, "%02d/%02d/%d", month1 + 1, dayOfMonth, year1);
                etEventDate.setText(formattedDate);

                // Clear error if a valid date is chosen
                etEventDate.setError(null);
            }
        }, year, month, day);

        // Prevent past dates
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        return datePickerDialog; //return the correct variable
    }
}