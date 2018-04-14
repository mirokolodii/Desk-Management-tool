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
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

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
    private DocumentSnapshot mLastVisibleAbsence;
    private final static long QUERY_LIMIT = 100;
    private DateTime mScheduleStart;
    private DateTime mScheduleEnd;
    private DateTime mToday;
    private final static int SCROLL_POSITION = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        db = FirebaseFirestore.getInstance();
        initializeScheduleDates();
        getPersons();

//        testAbsenceQuery(
//                new DateTime(2018, 4,2, 0, 0),
//                new DateTime(2018, 4,11, 0, 0)
//        );


    }

    private void initializeScheduleDates() {
        mToday = new DateTime()
                .withHourOfDay(0)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);
        mScheduleStart = mToday.minusWeeks(2);
        mScheduleEnd = mToday.plusMonths(1);
    }


    // Get all persons form Firestore.
    private void getPersons() {
        db.collection("persons").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        // Add persons into class array field.
                        for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                            Person person = documentSnapshot.toObject(Person.class);
                            person.setId(documentSnapshot.getId());
                            mPersons.add(person);
                        }
                        // We have persons now. Now we need absences.
                        getAbsences();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                });
    }

    private void getAbsences() {
        Query query = getAbsencesQuery(mScheduleStart.getMillis(), mScheduleEnd.getMillis());
        query
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {

                        // Save last document.
                        mLastVisibleAbsence = documentSnapshots.getDocuments()
                                .get(documentSnapshots.size()-1);

                        // Save query results into mAbsences.
                        updateAbsences(documentSnapshots);
                        // Debug
                        printAbsences();

                        // Prepare schedule.
                        showSchedule();

                    }
                })
                .addOnFailureListener(getOnFailureListener());
    }

    private Query getAbsencesQuery(long start, long end) {
        // First query
        if(mLastVisibleAbsence == null) {
            return db.collection("absences")
                    .whereGreaterThanOrEqualTo("endDate", start)
                    .orderBy("endDate") /* first orderBy should be on 'where' filter field */
                    .orderBy("startDate")
                    .limit(QUERY_LIMIT);
        } else { // Next query(-ies)
            return db.collection("absences")
                    .whereGreaterThanOrEqualTo("endDate", start)
                    .orderBy("endDate") /* first orderBy should be on 'where' filter field */
                    .orderBy("startDate")
                    .startAfter(mLastVisibleAbsence)
                    .limit(QUERY_LIMIT);
        }
    }

    private void updateAbsences(QuerySnapshot documentSnapshots) {
        for (DocumentSnapshot documentSnapshot : documentSnapshots) {
            Absence absence = documentSnapshot.toObject(Absence.class);
            absence.id = (documentSnapshot.getId());
            mAbsences.add(absence);
        }
    }


    private void showSchedule() {
        DateTime scheduleStart = mScheduleStart;
        DateTime scheduleEnd = mScheduleEnd;
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

    /**
     * Prepares an array of ScheduleItems with dates range from scheduleStart to scheduleEnd.
     * Loops through all absences and put into ScheduleItem, in case absence is in corresponding date.
     * @param scheduleStart
     * @param scheduleEnd
     * @return
     */
    private ArrayList<ScheduleItem> getSchedule(DateTime scheduleStart, DateTime scheduleEnd) {
        ArrayList<ScheduleItem> schedule = new ArrayList<>();
        // Put items into array
        while (scheduleStart.isBefore(scheduleEnd)) {
            ScheduleItem scheduleItem = new ScheduleItem(scheduleStart);
            schedule.add(scheduleItem);
            // Put absences into ScheduleItem.
            for(Absence absence : mAbsences) {
                if(isAbsenceInDate(absence, scheduleItem.getDate())) {
                    scheduleItem.addAbsence(absence);
                }
            }
            // Continue with next day. If current is Friday, skip weekend and jump to Monday.
            int jump;
            if(scheduleStart.getDayOfWeek() == DateTimeConstants.FRIDAY) {
                jump = 3;
            } else {
                jump = 1;
            }
            scheduleStart = scheduleStart.plusDays(jump);
        }
        return schedule;
    }


    private void initializeRecycleView() {
        RecyclerView recyclerView = findViewById(R.id.schedule_recycle_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerView.Adapter adapter = new ScheduleRVAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(SCROLL_POSITION);
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




    /**
     * Verifies whether absence is in provided date.
     * @param absence
     * @param date
     * @return true if absence is in date.
     */
    private boolean isAbsenceInDate(Absence absence, DateTime date) {
        Log.d(TAG, String.format("Absence: %s. Date: %d, absence.start: %d, absence.end: %d", absence.getType(), date.getMillis(), absence.getStartDate(), absence.getEndDate()));
        return !date.plusDays(1).isBefore(absence.getStartDate()) && !date.isAfter(absence.getEndDate());
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
            return mSchedule.size();
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            ScheduleItem item = mSchedule.get(position);
            holder.weekDay.setText(
                    item.getDate().dayOfWeek().getAsShortText());
            holder.monthDay.setText(
                    item.getDate().dayOfMonth().getAsText());
            holder.month.setText(
                    item.getDate().monthOfYear().getAsShortText());

            if(item.getDate().isEqual(mToday)) {
                holder.absencesLayout.setBackgroundColor(
                        getResources().getColor(R.color.recycle_view_item_background_today));

            } else {
                holder.absencesLayout.setBackgroundColor(
                        getResources().getColor(R.color.recycle_view_item_background));
            }


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
