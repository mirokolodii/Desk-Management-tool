package com.unagit.deskmanagementtool.brain;

import android.support.annotation.Nullable;

/**
 * Instance of absence, used with Firestore Firebase.
 */

public class Absence {
    private final static String PENDING_APPROVAL_LABEL = "Pending Approval";
    private final static String APPROVED_LABEL = "Approved";
    private String type;
    private String note;
    private String approvalStatus;
    private long startDate;
    private long endDate;
    private boolean requiredApproval;
    private boolean isApproved;



    public Absence() {}

    public Absence(String type, long startDate, long endDate, @Nullable String note) {
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        if(note != null) {
            this.note = note;
        }
    }

    public Absence(String type, long startDate, long endDate, @Nullable String note,  boolean requiredApproval) {
        this(type, startDate, endDate, note);
        if(requiredApproval) {
            this.requiredApproval = requiredApproval;
            this.isApproved = false;
            this.approvalStatus = PENDING_APPROVAL_LABEL;
        }
    }

//    public static String getPendingApprovalLabel() {
//        return PENDING_APPROVAL_LABEL;
//    }
//
//    public static String getApprovedLabel() {
//        return APPROVED_LABEL;
//    }

    public String getType() {
        return type;
    }

    public String getNote() {
        return note;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public long getStartDate() {
        return startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public boolean isRequiredApproval() {
        return requiredApproval;
    }

    public boolean isApproved() {
        return isApproved;
    }
}
