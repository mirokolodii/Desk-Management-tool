package com.unagit.deskmanagementtool.activities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.unagit.deskmanagementtool.R;
import com.unagit.deskmanagementtool.brain.Absence;
import com.unagit.deskmanagementtool.brain.Person;
import com.unagit.deskmanagementtool.brain.ScheduleItem;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity {

    private HashSet<Person> mPersons = new HashSet<>();
    private HashSet<Absence> mAbsences = new HashSet<>();
    private ArrayList<ScheduleItem> mSchedule;
    private FirebaseFirestore db;
    private static final String TAG = "ScheduleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        db = FirebaseFirestore.getInstance();
        getPersons();

        getAbsences(
                new DateTime(2018, 4,2, 0, 0),
                new DateTime(2018, 4,11, 0, 0)
        );


    }

    // Get all persons form Firestore.
    private void getPersons() {
        db.collection("persons").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
//                        persons = documentSnapshots.toObjects(Person.class);
//                        Log.d(TAG, "Query: " + documentSnapshots.getDocuments().toString());
//                        Log.d(TAG, "Documents: " + documentSnapshots.getDocuments().toString());
//                        Log.d(TAG, "List: " + persons.toString());

                        // Add persons into class array field.
                        for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                            Person person = documentSnapshot.toObject(Person.class);
                            person.setId(documentSnapshot.getId());
                            mPersons.add(person);

                        }

                        // Debug.
//                        for (Person person : mPersons) {
//                            Log.d(TAG, String.format("Name: %s; id: %s", person.getName(), person.withId()));
//                        }

                        // We have persons now. Continue with preparing schedule.
                        ScheduleActivity.this.printScheduleCalendar();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                });

    }

    private void getAbsences(final DateTime start, final DateTime end) {

        Query query = getAbsencesBetweenDatesQuery("startDate", start.getMillis(), end.getMillis());
        query
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        updateAbsences(documentSnapshots);

                        Query query = getAbsencesBetweenDatesQuery("endDate", start.getMillis(), end.getMillis());
                        query
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot documentSnapshots) {
                                        updateAbsences(documentSnapshots);

                                        // Debug
                                        printAbsences();
                                    }
                                })
                                .addOnFailureListener(getOnFailureListener());

                    }
                })
                .addOnFailureListener(getOnFailureListener());
    }

    private Query getAbsencesBetweenDatesQuery(String dateField, long start, long end) {
        return db.collection("absences")
                .whereGreaterThanOrEqualTo(dateField, start)
                .whereLessThanOrEqualTo(dateField, end);
    }

    private void updateAbsences(QuerySnapshot documentSnapshots) {
        for (DocumentSnapshot documentSnapshot : documentSnapshots) {
            Absence absence = documentSnapshot.toObject(Absence.class);
            absence.id = (documentSnapshot.getId());
            mAbsences.add(absence);
        }
    }

    private OnFailureListener getOnFailureListener() {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
            }
        };

    }

    private void printAbsences() {
        Log.d(TAG, "Absences:");
        for (Absence absence : mAbsences) {
            Log.d(TAG, String.format("Start: %d, end: %d, type: %s", absence.getStartDate(), absence.getEndDate(), absence.getType()));
        }
    }
//
//    private void showAbsences() {
//        for (Map.Entry<String, Absence> entry : absencesDic.entrySet()) {
//            Absence absence = entry.getValue();
//            Log.d(TAG, String.format("Name: %s, absence type: %s", entry.getKey(), absence.getType()));
//
//            DateTime time = new DateTime(absence.getStartDate());
//            Log.d(TAG, String.format("date: %s, week: %d", time.dayOfMonth().toString(), time.getWeekOfWeekyear()));
//            Log.d(TAG, "______________________");
//        }
//    }


    private void printScheduleCalendar() {
        DateTime scheduleStart = new DateTime(2018, 2, 12, 0, 0);
        DateTime scheduleEnd = new DateTime(2018, 3, 12, 0, 0);
        mSchedule = getSchedule(scheduleStart, scheduleEnd);

        initializeRecycleView();

//        for (ScheduleItem item : mSchedule) {
//            Log.d(TAG, String.format("%s-%s-%s",
//                    item.getDate().year().getAsShortText(),
//                    item.getDate().monthOfYear().getAsString(),
//                    item.getDate().dayOfMonth().getAsString())
//            );
//            int i = 0;
//            for(Absence absence : item.getAbsences()) {
//                Log.d(TAG, String.format("Absence %d: %s", ++i, absence.getType()));
//            }
//            Log.d(TAG, "___________________________________");
//        }
    }

    private void initializeRecycleView() {
        RecyclerView recyclerView = findViewById(R.id.schedule_recycle_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerView.Adapter adapter = new ScheduleRVAdapter();
        recyclerView.setAdapter(adapter);
    }

    /**
     * Prepares an array of ScheduleItems with dates range from scheduleStart to scheduleEnd.
     * Loops through all absences and put into ScheduleItem, in case absence is in corresponding date.
     * @param scheduleStart
     * @param scheduleEnd
     * @return
     */
    private ArrayList<ScheduleItem> getSchedule(DateTime scheduleStart, DateTime scheduleEnd) {
        ArrayList<ScheduleItem> schedule = new ArrayList<>();
        HashSet<Absence> absences = getAbsences2();


        // Put items into array
        while (scheduleStart.isBefore(scheduleEnd)) {
            ScheduleItem scheduleItem = new ScheduleItem(scheduleStart);
            schedule.add(scheduleItem);

            // Put absences into ScheduleItem.
            for(Absence absence : absences) {
                if(isAbsenceInDate(absence, scheduleItem.getDate())) {
                    // Put absence into ScheduleItem
                    scheduleItem.addAbsence(absence);
                }
            }

            // Continue with next day.
            scheduleStart = scheduleStart.plusDays(1);
        }
        return schedule;
    }

    /**
     * Verifies whether absence is in provided date.
     * @param absence
     * @param date
     * @return true if absence is in date.
     */
    private boolean isAbsenceInDate(Absence absence, DateTime date) {
        return !date.isBefore(absence.getStartDate()) && !date.isAfter(absence.getEndDate());
    }


    /**
     * Gets example absences.
     * @return Array of absences.
     */
    private HashSet<Absence> getAbsences2() {
        HashSet<Absence> absences = new HashSet<>();
        Absence ab1 = new Absence(
                "Vacation",
                (new DateTime(2018, 2,14, 0, 0)).getMillis(),
                (new DateTime(2018, 2,17, 0, 0)).getMillis(),
                null,
                false,
                "sdfdsf"
        );
        ab1.id = "123";

        Absence ab2 = new Absence(
                "Training",
                (new DateTime(2018, 2,16, 0, 0)).getMillis(),
                (new DateTime(2018, 2,19, 0, 0)).getMillis(),
                null,
                false,
                "jlvHsE9D5bbn1BMhbBrarMxhSsy2"
        );
        ab2.id = "123";


        Absence ab3 = new Absence(
                "Training",
                (new DateTime(2018, 2,16, 0, 0)).getMillis(),
                (new DateTime(2018, 2,19, 0, 0)).getMillis(),
                null,
                false,
                "jlvHsE9D5bbn1BMhbBrarMxhSsy2"
        );
        ab3.id = "1234";

        absences.add(ab1);
        absences.add(ab2);
        absences.add(ab3);

//        Log.d(TAG, "absences size: " + String.valueOf(absences.size()));

        return absences;
    }

    @Nullable
    private Person getPersonWithId(String id) {
        for(Person person : mPersons) {
            if (person.withId().equals(id)) {
                return person;
            }
        }
        return null;
    }

    /**
     * Adapter for RecycleView.
     */
    class ScheduleRVAdapter extends RecyclerView.Adapter<ScheduleRVAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView weekDay;
            TextView month;
            TextView monthDay;
            LinearLayout absencesLayout;
            ViewHolder(View view) {
                super(view);
                weekDay = view.findViewById(R.id.schedule_rv_week_day);
                month = view.findViewById(R.id.schedule_rv_month);
                monthDay = view.findViewById(R.id.schedule_rv_month_day);
                absencesLayout = view.findViewById(R.id.absence_layout);

            }
        }

        @Override
        public int getItemCount() {
//            return mScheduleCalendar.size();
            return mSchedule.size();
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            holder.weekDay.setText(
                    mSchedule.get(position).getDate().dayOfWeek().getAsShortText());
            holder.monthDay.setText(
                    mSchedule.get(position).getDate().dayOfMonth().getAsText());
            holder.month.setText(
                    mSchedule.get(position).getDate().monthOfYear().getAsShortText());

            // Remove possible absences-leftovers.
            holder.absencesLayout.removeAllViews();
            // Put absences into view.
            ArrayList<Absence> absences = mSchedule.get(position).getAbsences();
            for(Absence absence : absences) {
//                Log.d(TAG, "Absence.userId: " + absence.getUserId());
                String text = "";
                Person person = getPersonWithId(absence.getUserId());
                if(person != null) {
                    text += person.getName() + ": ";
                } else {
                    text += "unknown user: ";
                }
                text += absence.getType();
                TextView textView = new TextView(ScheduleActivity.this);
                textView.setText(text);
                holder.absencesLayout.addView(textView);
            }

        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.content_schedule_recycle_view_item, parent, false);
            return new ViewHolder(view);
        }
    }
}


