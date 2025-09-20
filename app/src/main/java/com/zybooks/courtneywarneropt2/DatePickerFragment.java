package com.zybooks.courtneywarneropt2;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.util.Calendar;

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
        return new DatePickerDialog(getActivity(), (view, year1, month1, dayOfMonth) -> {
            EditText etEventDate = getActivity().findViewById(R.id.etEventDate);
            // Format date as MM/DD/YYYY and set to EditText
            String formattedDate = String.format("%02d/%02d/%d", month1 + 1, dayOfMonth, year1);
            etEventDate.setText(formattedDate);
        }, year, month, day);
    }
}
