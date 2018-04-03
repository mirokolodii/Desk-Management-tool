package com.unagit.deskmanagementtool.brain;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Created by a264889 on 27.03.2018.
 */
@IgnoreExtraProperties
public class Person {
    private String name;
    private String email;

    @ServerTimestamp
    private Date timestamp;

    @Exclude
    private String id;

    public Person() {}

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String withId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
