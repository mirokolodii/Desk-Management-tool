package com.unagit.deskmanagementtool.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unagit.deskmanagementtool.R;
import com.unagit.deskmanagementtool.brain.Absence;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShowAbsenceActivity extends AppCompatActivity {

    private final static String TAG = "ShowAbsenceActivity";
    Absence mAbsence;
//    String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_absence);

        getIntentExtras();
        updateUI();
    }

    // Add menu into activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_show_absence_menu, menu);
        return true;
    }

    // Handle menu items onClick events.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_menu_button:
                editAbsence();
                break;
            case R.id.delete_menu_button:
                deleteAbsence();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editAbsence() {
        Intent addAbsenceActivityIntent = new Intent(this, AddAbsenceActivity.class);
        addAbsenceActivityIntent
                .putExtra(Absence.EXTRA_SERIALIZABLE_OBJECT, mAbsence);
        startActivity(addAbsenceActivityIntent);
        this.finish();

    }

    private void deleteAbsence() {

        DocumentReference absenceRef = FirebaseFirestore.getInstance()
                .collection("absences")
                .document(mAbsence.id);
        absenceRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        Toast.makeText(ShowAbsenceActivity.this, "Absence deleted.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                        Toast.makeText(ShowAbsenceActivity.this, "Failed to delete.", Toast.LENGTH_SHORT).show();
                    }
                });

        this.finish();
    }


    private void getIntentExtras() {
        mAbsence = (Absence) this.getIntent().getSerializableExtra(Absence.EXTRA_SERIALIZABLE_OBJECT);
//        Log.d(TAG, absence.getType() + ": " + absence.id);
//        mUserId = this.getIntent().getStringExtra(Absence.EXTRA_USER_ID);
    }

    private void updateUI() {
        ((TextView) findViewById(R.id.show_absence_type)).setText(mAbsence.getType());

        Date start = new Date(mAbsence.getStartDate());
        Date end = new Date(mAbsence.getEndDate());
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMMM dd", Locale.getDefault()); /* Tue, Jan 12 */
        String startDate = format.format(start);
        String endDate = format.format(end);
        ((TextView) findViewById(R.id.show_absence_start_date)).setText(startDate);
        ((TextView) findViewById(R.id.show_absence_end_date)).setText(endDate);

        TextView approvalStatusView = findViewById(R.id.show_absence_approval_status);
        if(mAbsence.getApprovalStatus() != null) {
            approvalStatusView.setText(mAbsence.getApprovalStatus());
            if(mAbsence.getApprovalStatus().equals(Absence.PENDING_APPROVAL_LABEL)) {
                approvalStatusView.setTextColor(getResources().getColor(R.color.pendingApprovalStatus));
            } else {
                approvalStatusView.setTextColor(getResources().getColor(R.color.approvedStatus));
            }

        } else {
            approvalStatusView.setText("N/A");
        }

        TextView noteView = findViewById(R.id.show_absence_note);
        noteView.setText(
                mAbsence.getNote() != null
                ? mAbsence.getNote() : "" );

        TextView timestampView = findViewById(R.id.show_absence_timestamp);
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String timestamp = format.format(mAbsence.getTimestamp());
        timestampView.setText(timestamp);
    }
}
