package com.unagit.deskmanagementtool.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.unagit.deskmanagementtool.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddAbsenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_absence);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeDatePickers();
    }

    private void initializeDatePickers() {
        EditText startDateEditText = findViewById(R.id.start_date_editText);
        EditText endDateEditText = findViewById(R.id.end_date_editText);


        SimpleDateFormat format = new SimpleDateFormat("EEE, MMMM dd, yyyy", Locale.getDefault());
        String now = format.format(new Date());

        startDateEditText.setText(now);
        endDateEditText.setText(now);
    }
}
