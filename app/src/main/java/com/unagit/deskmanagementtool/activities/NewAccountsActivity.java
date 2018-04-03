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

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.unagit.deskmanagementtool.Helpers;
import com.unagit.deskmanagementtool.R;
import com.unagit.deskmanagementtool.brain.Absence;
import com.unagit.deskmanagementtool.brain.Person;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class lists all new accounts, which are pending verification.
 */
public class NewAccountsActivity extends AppCompatActivity {

    // Firestore userId.
    private String mUserId;
    // Adapter for FirestoreUI RecycleView.
    private FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_approvals);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mUserId = Helpers.getLoggedInFirebaseUser();
        if (mUserId != null) {
            prepareRecycleView();
            Helpers.enableListeningForAdapter(true, adapter);
        } else {
//            launchSignInActivity();
            Helpers.launchActivity(this, SignInActivity.class);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Helpers.enableListeningForAdapter(false, adapter);
    }


//    /**
//     *  Verifies that user is logged into the Firebase.
//     *  Saves user ID.
//     */
//    private boolean isLoggedInUser() {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if(user == null) {
//            return false;
//        }
//        mUserId = user.getUid();
//        return true;
//    }

//    /**
//     * In case user is not logged in, redirect to SignInActivity.
//     */
//    private void launchSignInActivity() {
//        Intent signInActivityIntent = new Intent(this, SignInActivity.class);
//        signInActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(signInActivityIntent);
//        finish();
//    }

//    /**
//     * Changes listening state of adapter.
//     * @param enable if true - start listening, otherwise - stop listening.
//     */
//    private void enableListeningForAdapter(boolean enable) {
//        if(adapter != null) {
//            if(enable) {
//                adapter.startListening();
//            } else {
//                adapter.stopListening();
//            }
//        }
//    }

    /**
     * Prepares RecycleView to work with data from Firestore.
     * Includes two classes:
     * - AbsenceHolder, which binds view to java objects.
     * - FirestoreRecycleAdapter, which shows items, sets onClicksListeners etc.
     */
    private void prepareRecycleView() {
        // RecycleView
        RecyclerView mRecyclerView = findViewById(R.id.new_accounts_recycle_view);

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
        Query newAccountsQuery = FirebaseFirestore.getInstance()
                .collection("new_accounts")
                .orderBy("timestamp", Query.Direction.ASCENDING);

        // Configure recycler adapter options:
        //  * query is the Query object defined above.
        //  * Person.class instructs the adapter to convert each DocumentSnapshot to a Person object
        FirestoreRecyclerOptions<Person> options = new FirestoreRecyclerOptions.Builder<Person>()
//                .setQuery(absencesForUserQuery, Absence.class)
                .setQuery(newAccountsQuery, new SnapshotParser<Person>() {
                    @NonNull
                    @Override
                    public Person parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        Person person = snapshot.toObject(Person.class);
                        person.setId(snapshot.getId());
                        return person;
                    }
                })
                .build();

        class PersonHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView email;
            TextView date;

            PersonHolder(View v) {
                super(v);

                name = v.findViewById(R.id.person_name_recycle_view_item_textView);
                email = v.findViewById(R.id.person_email_recycle_view_item_textView);
                date = v.findViewById(R.id.person_date_recycle_view_item_textView);
            }
        }

        adapter = new FirestoreRecyclerAdapter<Person, PersonHolder>(options) {

            @Override
            public void onBindViewHolder(PersonHolder holder, int position, final Person person) {
                // Bind the Person object to the PersonHolder
                holder.name.setText(person.getName());
                holder.email.setText(person.getEmail());
                holder.date.setText(getDateString(person.getTimestamp()));
            }

            @Override
            public PersonHolder onCreateViewHolder(ViewGroup group, int i) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.content_new_accounts_recycle_view_item for each item
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.content_new_accounts_recycle_view_item, group, false);
                return new PersonHolder(view);
            }

            /**
             * Prepares a text string date.
             */
            private String getDateString(Date date) {
                SimpleDateFormat format = new SimpleDateFormat("EEE, MMMM dd, hh:mm", Locale.getDefault()); /* Tue, Jan 12, 12:23 */
                return format.format(date);
            }
        };

        // Set adapter for RecycleView
        mRecyclerView.setAdapter(adapter);
    }
}

