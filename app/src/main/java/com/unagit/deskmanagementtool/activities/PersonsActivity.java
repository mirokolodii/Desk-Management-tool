package com.unagit.deskmanagementtool.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.unagit.deskmanagementtool.Helpers;
import com.unagit.deskmanagementtool.R;
import com.unagit.deskmanagementtool.brain.Absence;
import com.unagit.deskmanagementtool.brain.Person;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class PersonsActivity extends AppCompatActivity {

    // Firebase user id.
    private String mUserId;

    // Array of persons from Firestore.
    private ArrayList<Person> persons = new ArrayList<>();

    //TAG
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persons);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUserId = Helpers.getLoggedInFirebaseUser();
        if (mUserId != null) {
            prepareRecycleView();
            getPersons();
        } else {
            Helpers.launchActivity(this, SignInActivity.class);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void prepareRecycleView() {
        // RecycleView
        RecyclerView recyclerView = findViewById(R.id.persons_recycle_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        // Add divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Set adapter.
        PersonsAdapter adapter = new PersonsAdapter();
        recyclerView.setAdapter(adapter);
    }


    class PersonsAdapter extends RecyclerView.Adapter<PersonsAdapter.PersonViewHolder> {

        class PersonViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            PersonViewHolder(View view) {
                super(view);
                name = (TextView) view;
            }
        }

        @Override
        public int getItemCount() {
            return persons.size();
        }

        @NonNull
        @Override
        public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PersonViewHolder(new TextView(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(@NonNull PersonViewHolder holder, int position) {
            final Person person = persons.get(position);
            holder.name.setText(person.getName());
            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(PersonsActivity.this, AbsencesActivity.class);
                    intent.putExtra(Absence.EXTRA_USER_ID, person.withId());
                    startActivity(intent);
                }
            });
        }
    }

    private void getPersons() {
        FirebaseFirestore.getInstance()
                .collection("persons")
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        for(DocumentSnapshot snapshot : documentSnapshots) {
                            if(snapshot != null && snapshot.exists()) {
                                Person person = snapshot.toObject(Person.class);
                                person.setId(snapshot.getId());
                                persons.add(person);
                            }
                        }
                    }
                });
    }

}
