package com.unagit.deskmanagementtool.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.unagit.deskmanagementtool.R;
import com.unagit.deskmanagementtool.brain.AbsenceType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddAbsenceActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private final static String START_DATE_TAG = "startDatePicker";
    private final static String END_DATE_TAG = "endDatePicker";
    android.support.v4.app.DialogFragment fragment;
    private static Date startDate;
    private static Date endDate;
    final ArrayList<AbsenceType> absenceTypes = new ArrayList<>();

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

        // Create instance of database.
        db = FirebaseFirestore.getInstance();

        updateAbsenceTypesSpinner();


    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeDatePickers();
    }

    // Add menu into activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_add_absence_menu, menu);
        return true;
    }

    // Handle menu items onClick events.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_menu_button:
                saveAbsence();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveAbsence() {

    }

    /**
     * Gets absence types from db and populates spinner with received data.
     */
    private void updateAbsenceTypesSpinner() {


        // Get items from db and put into array.
        db.collection("absence_types")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                for(DocumentSnapshot document : documentSnapshots) {
                    Log.d("AddAbsenceActivity", document.getData().toString());
                    AbsenceType absenceType = document.toObject(AbsenceType.class);
                    absenceTypes.add(absenceType);
                }

//                Log.d("AddAbsenceActivity", "absenceTypes: " + absenceTypes.toString());
                for(AbsenceType type : absenceTypes) {
                    Log.d("AddAbsenceActivity", "absenceTypes: " + type.getName() + ": " + type.isRequiredApproval() + "\n");
                }

                Spinner spinner = findViewById(R.id.absence_type_spinner);
                AbsenceSpinnerAdapter adapter = new AbsenceSpinnerAdapter();
                spinner.setAdapter(adapter);
//                spinner.getSelectedItem();
            }
        });




        //
    }

    /**
     * Puts current date into both date picker EditTexts.
     * Sets onClick listeners.
     * Saves Dates from these EditTexts into instance variables, so that they can be accessed
     * by other methods.
     */
    private void initializeDatePickers() {
        // Add current date to both pickers.
        setDateInEditText(DateEditText.startDateEditText, new Date());
        setDateInEditText(DateEditText.endDateEditText, new Date());

        // Set onClickListeners for both pickers to open date pickers.
        ((EditText) findViewById(R.id.start_date_editText)).setOnClickListener(getDateOnClickListener(START_DATE_TAG));
        ((EditText) findViewById(R.id.end_date_editText)).setOnClickListener(getDateOnClickListener(END_DATE_TAG));
    }

    /**
     * Creates a new DatePickerFragment.
     * @param tag representing source view, from which listener has been triggered.
     * @return OnClickListener.
     */
    private View.OnClickListener getDateOnClickListener(final String tag) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment = new DatePickerDialogFragment();
                fragment.show(getSupportFragmentManager(), tag);
            }
        };
    }

    // Implementation of DatePickerDialog.OnDateSetListener interface.
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

    /**
     * Compares startDate and endDate Dates.
     * @return true, if endDate is grater than startDate, otherwise return false.
     */
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
     * Alerts user with red color in startEditText, in case when end date is earlier than start date.
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

    private class AbsenceSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

        @Override
        public int getCount() {
            return absenceTypes.size();
        }

        @Override
        public Object getItem(int i) {
//            return null;
            return absenceTypes.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View listView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.support_simple_spinner_dropdown_item, viewGroup, false);
            TextView textView = listView.findViewById(android.R.id.text1);
            textView.setText(absenceTypes.get(i).getName());
            return listView;
        }
    }
}
