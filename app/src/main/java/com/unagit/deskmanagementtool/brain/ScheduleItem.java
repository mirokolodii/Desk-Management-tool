package com.unagit.deskmanagementtool.brain;

import org.joda.time.DateTime;

import java.util.ArrayList;

public class ScheduleItem {

    private DateTime date;
    private ArrayList<Absence> absences;

    public ScheduleItem(DateTime date) {
        this.date = date;
        absences = new ArrayList<>();
    }

    public void addAbsence(Absence absence) {
        this.absences.add(absence);
    }

    public DateTime getDate() {
        return this.date;
    }

    public ArrayList<Absence> getAbsences() {
        return this.absences;
    }
}
