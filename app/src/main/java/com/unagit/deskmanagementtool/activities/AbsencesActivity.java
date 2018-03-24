package com.unagit.deskmanagementtool.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.unagit.deskmanagementtool.R;
import com.unagit.deskmanagementtool.brain.Absence;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.unagit.deskmanagementtool.activities.AddAbsenceActivity.EXTRA_USER_ID;


public class AbsencesActivity extends AppCompatActivity {

    private String mUserId;
    private FirestoreRecyclerAdapter adapter;
    private final static String APPROVED_STATUS = "Approved";

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
            getAbsences();
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
        String uid = getIntent().getStringExtra(EXTRA_USER_ID);
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
                .collection("persons")
                .document(mUserId)
                .collection("absences");

        // Configure recycler adapter options:
        //  * query is the Query object defined above.
        //  * Chat.class instructs the adapter to convert each DocumentSnapshot to a Chat object
        FirestoreRecyclerOptions<Absence> options = new FirestoreRecyclerOptions.Builder<Absence>()
                .setQuery(absencesForUserQuery, Absence.class)
                .build();


        class AbsenceHolder extends RecyclerView.ViewHolder {
            TextView type;
            TextView dates;
            TextView approvalStatus;

            AbsenceHolder(View v) {
                super(v);

                //TODO: set onClickListener on v.

                type = v.findViewById(R.id.type_recycle_view_single_item_textView);
                dates = v.findViewById(R.id.dates_recycle_view_single_item_textView);
                approvalStatus = v.findViewById(R.id.approval_status_recycle_view_single_item_textView);
                Log.d("AbsencesActivity", "AbsenceHolder constructor triggered.");
            }
        }

        adapter = new FirestoreRecyclerAdapter<Absence, AbsenceHolder>(options) {

            @Override
            public void onBindViewHolder(AbsenceHolder holder, int position, Absence model) {
                Log.d("AbsencesActivity", "onBindViewHolder triggered with " + model.getType());
                // Bind the Chat object to the ChatHolder
                holder.type.setText(model.getType());
                holder.dates.setText(getDatesString(
                        new Date(model.getStartDate()),
                        new Date(model.getEndDate() )));
                setApprovalStatus(holder.approvalStatus, model.getApprovalStatus());

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
             * @param start date
             * @param end date
             * @return string of dates in specified format.
             */
            private String getDatesString(Date start, Date end) {
                SimpleDateFormat format = new SimpleDateFormat("EEE, MMMM dd", Locale.getDefault()); /* Tue, Jan 12 */
                String datesString = format.format(start);
                if(end.getTime() > start.getTime()) {
                    datesString += " - " + format.format(end);
                }
                return datesString;
            }

            private void setApprovalStatus(TextView view, String status) {
                if (status != null) {
                    view.setVisibility(View.VISIBLE);
                    view.setText(status);
                    if(status.equals(APPROVED_STATUS)) { // Approved
                        view.setTextColor(Color.GREEN);
                    } else { // Pending Approval
                        view.setTextColor(Color.RED);
                    }

                } else {
                    view.setVisibility(View.GONE);
                    view.setText("");
                }
            }
        };




        // Set adapter for RecycleView
        mRecyclerView.setAdapter(adapter);


    }




    /**
     * Gets absences for user with id == mUserId from db
     * and populate them into RecycleView.
     */
    private void getAbsences() {

        final ArrayList<Absence> absences = new ArrayList<>();
        // Firebase

        Query absencesForUserQuery = FirebaseFirestore.getInstance()
                .collection("persons")
                .document(mUserId)
                .collection("absences");

//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        CollectionReference absencesColRef = db.collection("persons").document(mUserId).collection("absences");
//        absencesColRef

        absencesForUserQuery
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    int i = 0;
                    public void onEvent(@Nullable QuerySnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            // Handle error
                            //...
                            return;
                        }

                        // Convert query snapshot to a list of chats

                        for (DocumentSnapshot document : snapshot) {
                            Absence absence = document.toObject(Absence.class);
                            absences.add(absence);
                            Log.d("AbsencesActivity", String.format("%d: Absence: %s", i++, absence.getType()));
                        }
                    }
                });
    }
}