package com.unagit.deskmanagementtool.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;

import static com.unagit.deskmanagementtool.activities.AddAbsenceActivity.EXTRA_USER_ID;

public class AbsencesActivity extends AppCompatActivity {

    private String mUserId;
    private FirestoreRecyclerAdapter adapter;

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
        getIntentData();

        getAbsences();

        prepareRecycleView();

        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();

        adapter.stopListening();
    }

    private void prepareRecycleView() {
        // RecycleView
        RecyclerView mRecyclerView = findViewById(R.id.absences_recycle_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

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
            public TextView type;
            public TextView dates;
            public TextView approvalStatus;

            public AbsenceHolder(View v) {
                super(v);

                //TODO: set onClickListener on v.

                type = v.findViewById(R.id.type_recycle_view_single_item_textView);
                dates = v.findViewById(R.id.dates_recycle_view_single_item_textView);
                approvalStatus = findViewById(R.id.approval_status_recycle_view_single_item_textView);
                Log.d("AbsencesActivity", "AbsenceHolder constructor triggered.");
            }
        }

        adapter = new FirestoreRecyclerAdapter<Absence, AbsenceHolder>(options) {

            @Override
            public void onBindViewHolder(AbsenceHolder holder, int position, Absence model) {
                Log.d("AbsencesActivity", "onBindViewHolder triggered with " + model.getType());
                // Bind the Chat object to the ChatHolder
                holder.type.setText(model.getType());
//                holder.approvalStatus.setText(
//                        (model.getApprovalStatus() != null)
//                        ? model.getApprovalStatus() : ""
//                );
//                holder.approvalStatus.setText(model.getApprovalStatus());

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
        };


        // Set adapter for RecycleView
        mRecyclerView.setAdapter(adapter);


    }



    /**
     *  Gets data from intent.
     * Firebase user ID from intent. If not available, uses current one instead.
     */
    private void getIntentData() {
        String uid = getIntent().getStringExtra(EXTRA_USER_ID);
        if(uid == null) {
            // Check if user is signed in (non-null) and update UI accordingly.
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user == null) {
                // Redirect to login activity.
                launchSignInActivity();
            } else {
                uid = user.getUid();
            }
        }
        mUserId  = uid;
    }

    private void launchSignInActivity() {
        Intent signInActivityIntent = new Intent(this, SignInActivity.class);
        startActivity(signInActivityIntent);
        finish();
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