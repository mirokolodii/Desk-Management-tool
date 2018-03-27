package com.unagit.deskmanagementtool.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.unagit.deskmanagementtool.R;
import com.unagit.deskmanagementtool.brain.Absence;
import com.unagit.deskmanagementtool.brain.Person;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity {

    private List<Person> persons = new ArrayList<>();
    private Map<String, Absence> absencesDic = new HashMap<>();
    private FirebaseFirestore db;
    private static final String TAG = "ScheduleActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        db = FirebaseFirestore.getInstance();
        updateUI();
    }

    private void updateUI() {
        getPersons();
    }

    private void getPersons() {
        db.collection("persons").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
//                        persons = documentSnapshots.toObjects(Person.class);
//                        Log.d(TAG, "Query: " + documentSnapshots.getDocuments().toString());
//                        Log.d(TAG, "Documents: " + documentSnapshots.getDocuments().toString());
//                        Log.d(TAG, "List: " + persons.toString());

                        for(DocumentSnapshot documentSnapshot : documentSnapshots) {
                            Person person = documentSnapshot.toObject(Person.class);
                            person.setId(documentSnapshot.getId());
                            persons.add(person);

                        }
                        for(Person person : persons) {
                            Log.d(TAG, String.format("Name: %s; id: %s", person.getName(), person.withId()));
                        }

                        ScheduleActivity.this.getAbsences();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                });

    }

    private void getAbsences() {
        for(final Person person : persons) {
            db.collection("persons").document(person.withId()).collection("absences").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            for(DocumentSnapshot documentSnapshot : documentSnapshots) {
                                absencesDic.put(person.getName(), documentSnapshot.toObject(Absence.class));
                            }

                            ScheduleActivity.this.showAbsences();
                        }


                    });

        }

    }

    private void showAbsences() {
        for(Map.Entry<String, Absence> entry : absencesDic.entrySet()) {
            Absence absence = entry.getValue();
            Log.d(TAG, String.format("Name: %s, absence type: %s", entry.getKey(), absence.getType()));

            DateTime time = new DateTime(absence.getStartDate());
            Log.d(TAG, String.format("date: %s, week: %d", time.dayOfMonth().toString(), time.getWeekOfWeekyear()));
            Log.d(TAG, "______________________");

        }


    }
}
