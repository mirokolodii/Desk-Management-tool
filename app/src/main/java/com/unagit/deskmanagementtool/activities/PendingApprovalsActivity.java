package com.unagit.deskmanagementtool.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.unagit.deskmanagementtool.Helpers;
import com.unagit.deskmanagementtool.R;
import com.unagit.deskmanagementtool.brain.Absence;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class is responsible for showing a list of absences, which require approval.
 * It's possible to approve absence from the view by clicking on 'approve' button.
 * Once approved, list will be updated (approved absence will be removed from it).
 */
public class PendingApprovalsActivity extends AppCompatActivity {

    // Firestore userId.
    private String mUserId;
    // Adapter for FirestoreUI RecycleView.
    private FirestoreRecyclerAdapter adapter;

    final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_approvals);


    }

    @Override
    protected void onStart() {
//        Log.d(TAG, "Enter onStart");
        super.onStart();
        if (isLoggedInUser()) {
//            Log.d(TAG, "User is logged in");
            testQuery();
           prepareRecycleView();
           enableListeningForAdapter(true);
        } else {
//            Log.d(TAG, "User is not logged in");
            launchSignInActivity();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        enableListeningForAdapter(false);
    }


    /**
     *  Verifies that user is logged into the Firebase.
     *  Saves user ID.
     */
    private boolean isLoggedInUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) {
            return false;
        }
        mUserId = user.getUid();
        return true;
    }

    /**
     * In case user is not logged in, redirect to SignInActivity.
     */
    private void launchSignInActivity() {
        Intent signInActivityIntent = new Intent(this, SignInActivity.class);
        signInActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(signInActivityIntent);
        this.finish();
    }

    /**
     * Changes listening state of adapter.
     * @param enable if true - start listening, otherwise - stop listening.
     */
    private void enableListeningForAdapter(boolean enable) {
        if(adapter != null) {
            if(enable) {
                adapter.startListening();
            } else {
                adapter.stopListening();
            }
        }
    }

    private void testQuery() {
        // Query for Firestore.

        Query absencesForUserQuery = FirebaseFirestore.getInstance()
                .collection("absences")
                .whereEqualTo("requiredApproval", true)
                .whereEqualTo("approved", false)
                .orderBy("timestamp", Query.Direction.ASCENDING);
        absencesForUserQuery.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        Log.d(TAG, "Data received from Firestore");
                        for(DocumentSnapshot document : documentSnapshots) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to receive data from Firestore");
                        Log.d(TAG, e.getMessage());
                    }
                });
    }

    /**
     * Prepares RecycleView to work with data from Firestore.
     * Includes two classes:
     * - AbsenceHolder, which binds view to java objects.
     * - FirestoreRecycleAdapter, which shows items, sets onClicksListeners etc.
     */
    private void prepareRecycleView() {
        // RecycleView
        RecyclerView mRecyclerView = findViewById(R.id.pending_approval_absences_recycle_view);

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


        // Query for Firestore.
        Query absencesForUserQuery = FirebaseFirestore.getInstance()
                .collection("absences")
                .whereEqualTo("requiredApproval", true)
                .whereEqualTo("approved", false)
                .orderBy("timestamp", Query.Direction.ASCENDING);


        // Configure recycler adapter options:
        //  * query is the Query object defined above.
        //  * Absence.class instructs the adapter to convert each DocumentSnapshot to an Absence object
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
            Button approveButton;

            AbsenceHolder(View v) {
                super(v);

                type = v.findViewById(R.id.type_recycle_view_single_item_textView);
                dates = v.findViewById(R.id.dates_recycle_view_single_item_textView);
                approveButton = v.findViewById(R.id.approve_absence_button);
            }
        }

        adapter = new FirestoreRecyclerAdapter<Absence, AbsenceHolder>(options) {

            @Override
            public void onBindViewHolder(AbsenceHolder holder, int position, final Absence model) {
                Log.d("AbsencesActivity", "onBindViewHolder triggered with " + model.getType());
                // Bind the Chat object to the ChatHolder
                holder.type.setText(model.getType());
                holder.dates.setText(getDatesString(model));

                holder.approveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        approveAbsence(model);
                        Log.d(TAG, "Approve button pressed");
                    }
                });
            }

            @Override
            public AbsenceHolder onCreateViewHolder(ViewGroup group, int i) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                Log.d("AbsencesActivity", "onCreateViewHolder triggered.");
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.content_pending_approval_absences_recycle_view_item, group, false);
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
                    datesString += " - " + format.format(end);
                }
                return datesString;
            }

            /**
             * Returns true, if it's an one-day absence.
             * @param model absence.
             * @return
             */
            private boolean oneDayAbsence(Absence model) {
                long startDays = model.getStartDate() / (24 * 60 * 60 * 1000);
                long endDays = model.getEndDate() / (24 * 60 * 60 * 1000);
                return startDays == endDays;
            }

            private void approveAbsence(Absence absence) {
                absence.approve();
                DocumentReference docRef = FirebaseFirestore.getInstance()
                        .collection("absences")
                        .document(absence.id);
                docRef.set(absence, SetOptions.merge());
            }
        };

        // Set adapter for RecycleView
        mRecyclerView.setAdapter(adapter);
    }
}
