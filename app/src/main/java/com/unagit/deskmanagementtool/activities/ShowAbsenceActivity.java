package com.unagit.deskmanagementtool.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.unagit.deskmanagementtool.R;
import com.unagit.deskmanagementtool.brain.Absence;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShowAbsenceActivity extends AppCompatActivity {

    private final static String LOG = "ShowAbsenceActivity";
    Absence absence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_absence);

        getModel();
        updateUI();
    }

    private void getModel() {
        absence = (Absence) this.getIntent().getSerializableExtra(Absence.EXTRA_SERIALIZABLE_OBJECT);
        Log.d(LOG, absence.getType() + ": " + absence.id);
    }

    private void updateUI() {
        ((TextView) findViewById(R.id.show_absence_type)).setText(absence.getType());

        Date start = new Date(absence.getStartDate());
        Date end = new Date(absence.getEndDate());
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMMM dd", Locale.getDefault()); /* Tue, Jan 12 */
        String startDate = format.format(start);
        String endDate = format.format(end);
        ((TextView) findViewById(R.id.show_absence_start_date)).setText(startDate);
        ((TextView) findViewById(R.id.show_absence_end_date)).setText(endDate);

        TextView approvalStatusView = findViewById(R.id.show_absence_approval_status);
        if(absence.getApprovalStatus() != null) {
            approvalStatusView.setText(absence.getApprovalStatus());
            if(absence.getApprovalStatus().equals(Absence.PENDING_APPROVAL_LABEL)) {
                approvalStatusView.setTextColor(getResources().getColor(R.color.pendingApprovalStatus));
            } else {
                approvalStatusView.setTextColor(getResources().getColor(R.color.approvedStatus));
            }

        } else {
            approvalStatusView.setText("N/A");
        }

        TextView noteView = findViewById(R.id.show_absence_note);
        noteView.setText(
                absence.getNote() != null
                ? absence.getNote() : "" );

        TextView timestampView = findViewById(R.id.show_absence_timestamp);
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String timestamp = format.format(absence.getTimestamp());
        timestampView.setText(timestamp);
    }
}
