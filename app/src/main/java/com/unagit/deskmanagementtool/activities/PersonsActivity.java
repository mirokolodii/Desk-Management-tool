package com.unagit.deskmanagementtool.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
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

    // Adapter for RecycleView.
    PersonsAdapter mAdapter;

    MyFilter myFilter = new MyFilter();

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

//    // Add menu into activity
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.activity_persons_menu, menu);
//
//        // Associate searchable configuration with the SearchView
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.searchView)
//                .getActionView();
//        searchView.setSearchableInfo(searchManager
//                .getSearchableInfo(getComponentName()));
//        searchView.setMaxWidth(Integer.MAX_VALUE);
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                myFilter.filter(query);
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                myFilter.filter(newText);
//                return true;
//            }
//        });
//
//        return true;
//    }

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
        mAdapter = new PersonsAdapter(persons);
        recyclerView.setAdapter(mAdapter);
    }


    class PersonsAdapter extends RecyclerView.Adapter<PersonsAdapter.PersonViewHolder> {
        ArrayList<Person> filteredPersons;
        PersonsAdapter(ArrayList<Person> filteredPersons) {
            this.filteredPersons = filteredPersons;
        }
        class PersonViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            PersonViewHolder(View view) {
                super(view);
                name = (TextView) view;
            }
        }

        void setList(ArrayList<Person> filteredPersons) {
            this.filteredPersons = filteredPersons;
        }

        @Override
        public int getItemCount() {
//            return persons.size();
            return filteredPersons.size();
        }

        @NonNull
        @Override
        public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PersonViewHolder(new TextView(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(@NonNull PersonViewHolder holder, int position) {
//            final Person person = persons.get(position);
            final Person person = filteredPersons.get(position);
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
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

    class MyFilter extends Filter{
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Person> filteredPersons = new ArrayList<>();
            String query = charSequence.toString().trim();
            if(query.isEmpty()) {
                filteredPersons = persons;
            } else {
                for(Person person : persons) {
                    if(person.getName().toLowerCase().contains(query.toLowerCase())) {
                        filteredPersons.add(person);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredPersons;
            return filterResults;

        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            ArrayList<Person> filteredPersons = (ArrayList<Person>) filterResults.values;
            mAdapter.setList(filteredPersons);
            mAdapter.notifyDataSetChanged();
        }
    }

}
