package com.unagit.deskmanagementtool.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unagit.deskmanagementtool.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddAbsenceActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private final static String START_DATE_TAG = "startDatePicker";
    private final static String END_DATE_TAG = "endDatePicker";
    android.support.v4.app.DialogFragment fragment;
    private static Date startDate;
    private static Date endDate;

    // Firebase
    FirebaseFirestore db;

    private enum DateEditText {
        startDateEditText,
        endDateEditText
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_absence);

        db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeDatePickers();
    }

    /**
     * Puts current date into both date picker EditTexts.
     * Sets onClick listeners.
     * Saves Dates from these EditTexts into instance variables, so that they can be access
     * with other methods.
      */

    private void initializeDatePickers() {
        // Add current date to both pickers.
        setDateInEditText(DateEditText.startDateEditText, new Date());
        setDateInEditText(DateEditText.endDateEditText, new Date());

        // Set onClickListeners for both pickers to open date pickers.
        ((EditText) findViewById(R.id.start_date_editText)).setOnClickListener(getDateOnClickListener(START_DATE_TAG));
        ((EditText) findViewById(R.id.end_date_editText)).setOnClickListener(getDateOnClickListener(END_DATE_TAG));
    }

    private View.OnClickListener getDateOnClickListener(final String tag) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment = new DatePickerDialogFragment();
                fragment.show(getSupportFragmentManager(), tag);
            }
        };
    }

    // From DatePickerDialog.OnDateSetListener interface.
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Date datePickerDate = getDateFromDatePicker(datePicker);

        if(START_DATE_TAG.equals(fragment.getTag())) {
            setDateInEditText(DateEditText.startDateEditText, datePickerDate);

        } else if (END_DATE_TAG.equals(fragment.getTag())) {
            setDateInEditText(DateEditText.endDateEditText, datePickerDate);
        }

        updateUI();
    }

    private boolean areCorrectDates() {
        return (
                startDate != null
                && endDate != null
                && startDate.getTime() <= endDate.getTime()
        );
    }

    private void setDateInEditText(DateEditText editText, Date date) {
        EditText dateView;
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMMM dd, yyyy", Locale.getDefault()); /* Tue, Jan 12, 2018 */
        String dateStr = format.format(date);

        switch(editText) {
            case startDateEditText:
                dateView = findViewById(R.id.start_date_editText);
                dateView.setText(dateStr);
                startDate = date;
                break;

            case endDateEditText:
                dateView = findViewById(R.id.end_date_editText);
                dateView.setText(dateStr);
                endDate = date;
                break;
        }
    }

    /**
     * Gets Date instance from DatePicker.
     * @param datePicker
     * @return instance of Date.
     */
    private java.util.Date getDateFromDatePicker(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar.getTime();
    }

    /**
     * Alert with red color in startEditText, in case when end date is earlier than start date.
     */
    private void updateUI() {
        int color;

        if(!areCorrectDates()) {
            color = Color.RED;

        } else {
            color = Color.BLACK;
        }

        EditText startDateEditText = findViewById(R.id.start_date_editText);
        startDateEditText.setTextColor(color);
    }


    public static class DatePickerDialogFragment extends android.support.v4.app.DialogFragment {

        DatePickerDialog.OnDateSetListener listener;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            listener = (DatePickerDialog.OnDateSetListener) context;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            Date date = getDate();
            final Calendar c = Calendar.getInstance();
            c.setTime(date);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), listener, year, month, day);
        }

        private Date getDate() {

            if(START_DATE_TAG.equals(getTag())) {
                return startDate;

            } else if (END_DATE_TAG.equals(getTag())) {
                return endDate;

            }

            // Default value, shouldn't occur.
            return new Date();
        }
    }
}
