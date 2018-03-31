package com.unagit.deskmanagementtool.brain;

import android.support.annotation.Nullable;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

/**
 * Instance of absence, used with Firestore Firebase.
 */

@IgnoreExtraProperties
public class Absence implements Serializable {
    // Intent extras.
    public final static String EXTRA_SERIALIZABLE_OBJECT = "serializableObject";
    public final static String EXTRA_USER_ID = "uId";
    public final static String EXTRA_ABSENCE_ID = "absenceId";
    public final static String EXTRA_START_DATE = "startDate";
    public final static String EXTRA_END_DATE = "endDate";
    public final static String EXTRA_TYPE = "type";
    public final static String EXTRA_NOTE = "note";
    public final static String EXTRA_APPROVAL_STATUS = "approvalStatus";
    public final static String EXTRA_TIMESTAMP = "timestamp";


    public final static String PENDING_APPROVAL_LABEL = "Pending Approval";
    public final static String APPROVED_LABEL = "Approved";
    private String type;
    private String note;
    private String approvalStatus;
    private long startDate;
    private long endDate;
    private boolean requiredApproval;
    private boolean approved;
    private String userId;

    @Exclude
    public String id;

    @ServerTimestamp
    private Date timestamp;


    public Absence() {}

    public Absence(String type,
                   long startDate,
                   long endDate,
                   @Nullable String note,
                   boolean requiredApproval,
                   String userId) {
        this(type, startDate, endDate, note, userId);
        if(requiredApproval) {
            this.requiredApproval = requiredApproval;
            this.approved = false;
            this.approvalStatus = PENDING_APPROVAL_LABEL;
        }
    }

    private Absence(String type, long startDate, long endDate, @Nullable String note, String userId) {
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userId = userId;
        if(note != null) {
            this.note = note;
        }
    }


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
        return approved;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getUserId() { return userId; }

//    public String getId() { return id; }
//
//    public void setId(String id) { this.id = id; }

}
