package com.unagit.deskmanagementtool.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.unagit.deskmanagementtool.R;
import com.unagit.deskmanagementtool.brain.Absence;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AbsencesActivity extends AppCompatActivity {

    private String mUserId;
    private FirestoreRecyclerAdapter adapter;
    private final static String APPROVED_STATUS = "Approved";
    private final static String PENDING_APPROVAL_STATUS = "Pending Approval";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__absences);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        FloatingActionButton addAbsenceButton = (FloatingActionButton) findViewById(R.id.fab);
        addAbsenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent absenceIntent = new Intent(AbsencesActivity.this, AddAbsenceActivity.class);
                startActivity(absenceIntent);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        if(isCorrectUserId()) {
//            getAbsences();
            prepareRecycleView();
            if(adapter != null) {
                adapter.startListening();
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if(adapter != null) {
            adapter.stopListening();
        }
    }

    /**
     *  Gets data from intent.
     * Firebase user ID from intent. If not available, uses current one instead.
     */
    private boolean isCorrectUserId() {
        // Firstly, try to get user ID from intent.
        String uid = getIntent().getStringExtra(Absence.EXTRA_USER_ID);
        if(uid == null) {
            // Secondly, check if user is signed in.
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user == null) {
                // Redirect to login activity.
                Log.d("AbsencesActivity", "UserID is null. Launching SignInActivity");
                launchSignInActivity();
                return false;

            } else {
                uid = user.getUid();
            }
        }
        mUserId  = uid;
        return true;
    }

    private void launchSignInActivity() {
        Intent signInActivityIntent = new Intent(this, SignInActivity.class);
        signInActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(signInActivityIntent);
        this.finish();
    }

    private void launchShowAbsenceActivity(Absence model) {
        Intent showAbsenceActivityIntent = new Intent(this, ShowAbsenceActivity.class);
        showAbsenceActivityIntent
                .putExtra(Absence.EXTRA_SERIALIZABLE_OBJECT, model);
//                .putExtra(Absence.EXTRA_USER_ID, mUserId);

//                .putExtra(Absence.EXTRA_ABSENCE_ID, model.id)
//                .putExtra(Absence.EXTRA_TYPE, model.getType())
//                .putExtra(Absence.EXTRA_START_DATE, model.getStartDate())
//                .putExtra(Absence.EXTRA_END_DATE, model.getEndDate())
//                .putExtra(Absence.EXTRA_APPROVAL_STATUS, model.getApprovalStatus())
//                .putExtra(Absence.EXTRA_NOTE, model.getNote())
//                .putExtra(Absence.EXTRA_TIMESTAMP, model.getTimestamp());

        startActivity(showAbsenceActivityIntent);
    }



    private void prepareRecycleView() {
        // RecycleView
        RecyclerView mRecyclerView = findViewById(R.id.absences_recycle_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);


        // Add divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);


        // specify an adapter
        Query absencesForUserQuery = FirebaseFirestore.getInstance()
                .collection("absences")
                .whereEqualTo("userId", mUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        // Configure recycler adapter options:
        //  * query is the Query object defined above.
        //  * Chat.class instructs the adapter to convert each DocumentSnapshot to a Chat object
        FirestoreRecyclerOptions<Absence> options = new FirestoreRecyclerOptions.Builder<Absence>()
//                .setQuery(absencesForUserQuery, Absence.class)
                .setQuery(absencesForUserQuery, new SnapshotParser<Absence>() {
                    @NonNull
                    @Override
                    public Absence parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        Absence absence = snapshot.toObject(Absence.class);
                        absence.id = snapshot.getId();
                        return absence;
                    }
                })
                .build();




        class AbsenceHolder extends RecyclerView.ViewHolder {
            TextView type;
            TextView dates;
            TextView approvalStatus;
            View view;

            AbsenceHolder(View v) {
                super(v);

                type = v.findViewById(R.id.type_recycle_view_single_item_textView);
                dates = v.findViewById(R.id.dates_recycle_view_single_item_textView);
                approvalStatus = v.findViewById(R.id.approval_status_recycle_view_single_item_textView);
                view = v;
            }
        }

        adapter = new FirestoreRecyclerAdapter<Absence, AbsenceHolder>(options) {

            @Override
            public void onBindViewHolder(AbsenceHolder holder, int position, final Absence model) {
                Log.d("AbsencesActivity", "onBindViewHolder triggered with " + model.getType());
                // Bind the Chat object to the ChatHolder
                holder.type.setText(model.getType());
                holder.dates.setText(getDatesString(model));
                setApprovalStatus(holder.approvalStatus, model.getApprovalStatus());

//                final String id = model.id;
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchShowAbsenceActivity(model);
//                        Toast.makeText(AbsencesActivity.this, id, Toast.LENGTH_SHORT).show();
                    }
                });

            }



            @Override
            public AbsenceHolder onCreateViewHolder(ViewGroup group, int i) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                Log.d("AbsencesActivity", "onCreateViewHolder triggered.");
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.content_absence_recycle_view_item, group, false);

                return new AbsenceHolder(view);
            }

            /**
             * Prepares a text string from start and end dates.
             * @param model Absence instance, from which we can get start and end dates.
             * @return string of dates in specified format.
             */
            private String getDatesString(Absence model) {
                Date start = new Date(model.getStartDate());
                Date end = new Date(model.getEndDate());

                SimpleDateFormat format = new SimpleDateFormat("EEE, MMMM dd", Locale.getDefault()); /* Tue, Jan 12 */
                String datesString = format.format(start);
                if(!oneDayAbsence(model)) {
//                if(true) {
                    datesString += " - " + format.format(end);
                }
                return datesString;
            }

            private boolean oneDayAbsence(Absence model) {
                long startDays = model.getStartDate() / (24 * 60 * 60 * 1000);
                long endDays = model.getEndDate() / (24 * 60 * 60 * 1000);
                return startDays == endDays;
            }

            private void setApprovalStatus(TextView view, String status) {
                if (status != null) {
//                    view.setVisibility(View.VISIBLE);
                    view.setText(status);
                    if(status.equals(APPROVED_STATUS)) { // Approved
                        view.setTextColor(getResources().getColor(R.color.approvedStatus));
                    } else { // Pending Approval
                        view.setTextColor(getResources().getColor(R.color.pendingApprovalStatus));
                    }

                } else {
//                    view.setVisibility(View.GONE);
                    view.setText("");
                }
            }
        };

        // Set adapter for RecycleView
        mRecyclerView.setAdapter(adapter);

    }
}