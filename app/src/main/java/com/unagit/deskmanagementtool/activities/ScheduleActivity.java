package com.unagit.deskmanagementtool.activities;

import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity {

    private List<Person> persons = new ArrayList<>();
//    private Map<String, Absence> absencesDic = new HashMap<>();
//    private ArrayList<DateTime> mScheduleCalendar;
    private ArrayList<ScheduleItem> mSchedule;
    private FirebaseFirestore db;
    private static final String TAG = "ScheduleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

//        db = FirebaseFirestore.getInstance();
//        updateUI();
        printScheduleCalendar();
    }

//    private void updateUI() {
//        getPersons();
//    }
//
//    // Get data from firestore.
//    private void getPersons() {
//        db.collection("persons").get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot documentSnapshots) {
////                        persons = documentSnapshots.toObjects(Person.class);
////                        Log.d(TAG, "Query: " + documentSnapshots.getDocuments().toString());
////                        Log.d(TAG, "Documents: " + documentSnapshots.getDocuments().toString());
////                        Log.d(TAG, "List: " + persons.toString());
//
//                        for (DocumentSnapshot documentSnapshot : documentSnapshots) {
//                            Person person = documentSnapshot.toObject(Person.class);
//                            person.setId(documentSnapshot.getId());
//                            persons.add(person);
//
//                        }
//                        for (Person person : persons) {
//                            Log.d(TAG, String.format("Name: %s; id: %s", person.getName(), person.withId()));
//                        }
//
//                        ScheduleActivity.this.getAbsences();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d(TAG, e.getMessage());
//                    }
//                });
//
//    }

//    private void getAbsences() {
//        for (final Person person : persons) {
//            db.collection("persons").document(person.withId()).collection("absences").get()
//                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                        @Override
//                        public void onSuccess(QuerySnapshot documentSnapshots) {
//                            for (DocumentSnapshot documentSnapshot : documentSnapshots) {
//                                absencesDic.put(person.getName(), documentSnapshot.toObject(Absence.class));
//                            }
//
//                            ScheduleActivity.this.showAbsences();
//                        }
//
//
//                    });
//
//        }
//
//    }
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
//        mScheduleCalendar = getScheduleCalendar(scheduleStart, scheduleEnd);
        mSchedule = getSchedule(scheduleStart, scheduleEnd);

        initializeRecycleView();

        for (ScheduleItem item : mSchedule) {
            Log.d(TAG, String.format("%s-%s-%s",
                    item.getDate().year().getAsShortText(),
                    item.getDate().monthOfYear().getAsString(),
                    item.getDate().dayOfMonth().getAsString())
            );
            int i = 0;
            for(Absence absence : item.getAbsences()) {
                Log.d(TAG, String.format("Absence %d: %s", ++i, absence.getType()));
            }
            Log.d(TAG, "___________________________________");
        }
    }


//    private ArrayList<DateTime> getScheduleCalendar(DateTime scheduleStart,DateTime scheduleEnd) {
//        ArrayList<DateTime> scheduleCalendar = new ArrayList<>();
//        while (scheduleStart.isBefore(scheduleEnd)) {
//            scheduleCalendar.add(scheduleStart);
//            scheduleStart = scheduleStart.plusDays(1);
//        }
//        return scheduleCalendar;
//    }

    private void initializeRecycleView() {
        RecyclerView recyclerView = findViewById(R.id.schedule_recycle_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerView.Adapter adapter = new ScheduleRVAdapter();
        recyclerView.setAdapter(adapter);

    }



    private ArrayList<ScheduleItem> getSchedule(DateTime scheduleStart, DateTime scheduleEnd) {
        ArrayList<ScheduleItem> schedule = new ArrayList<>();


        while (scheduleStart.isBefore(scheduleEnd)) {

            schedule.add(new ScheduleItem(scheduleStart));

            scheduleStart = scheduleStart.plusDays(1);
        }

        ArrayList<Absence> absences = getAbsences();
//        ArrayList<DateTime> scheduleDates = getScheduleCalendar(scheduleStart, scheduleEnd);
        for(Absence absence : absences) {
            for(ScheduleItem item : schedule) {

                if(isAbsenceInDate(absence, item.getDate())) {
                    // Put absence into ScheduleItem
                    item.addAbsence(absence);
                }
            }
        }

        return schedule;

    }

    private boolean isAbsenceInDate(Absence absence, DateTime date) {
        return !date.isBefore(absence.getStartDate()) && !date.isAfter(absence.getEndDate());
    }


    private ArrayList<Absence> getAbsences() {
        ArrayList<Absence> absencesArray = new ArrayList<>();
        absencesArray.add(new Absence(
                "Vacation",
                (new DateTime(2018, 2,14, 0, 0)).getMillis(),
                (new DateTime(2018, 2,17, 0, 0)).getMillis(),
                null,
                false
        ));

        absencesArray.add(new Absence(
                "Training",
                (new DateTime(2018, 2,16, 0, 0)).getMillis(),
                (new DateTime(2018, 2,19, 0, 0)).getMillis(),
                null,
                false
        ));
        return absencesArray;
    }

    class ScheduleRVAdapter extends RecyclerView.Adapter<ScheduleRVAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView weekDay;
            TextView month;
            TextView monthDay;
            LinearLayout layout;
            ViewHolder(View view) {
                super(view);
                weekDay = view.findViewById(R.id.schedule_rv_week_day);
                month = view.findViewById(R.id.schedule_rv_month);
                monthDay = view.findViewById(R.id.schedule_rv_month_day);
                layout = view.findViewById(R.id.absence_layout);

            }
        }

        @Override
        public int getItemCount() {
//            return mScheduleCalendar.size();
            return mSchedule.size();
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

//            holder.weekDay.setText(mScheduleCalendar.get(position).dayOfWeek().getAsShortText());
//            holder.monthDay.setText(mScheduleCalendar.get(position).dayOfMonth().getAsText());
//            holder.month.setText(mScheduleCalendar.get(position).monthOfYear().getAsShortText());

            holder.weekDay.setText(
                    mSchedule.get(position).getDate().dayOfWeek().getAsShortText()
            );
            holder.monthDay.setText(
                    mSchedule.get(position).getDate().dayOfMonth().getAsText()
            );
            holder.month.setText(
                    mSchedule.get(position).getDate().monthOfYear().getAsShortText()
            );

            holder.layout.removeAllViews();
            ArrayList<Absence> absences = mSchedule.get(position).getAbsences();
            for(Absence absence : absences) {
                TextView textView = new TextView(ScheduleActivity.this);
                textView.setText(absence.getType());
                holder.layout.addView(textView);
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


