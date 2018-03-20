package com.unagit.deskmanagementtool.brain;

/**
 * Created by myroslavkolodii on 20/03/2018.
 */

public class AbsenceType {
    private String name;
    private boolean requiredApproval;

    public AbsenceType() {}

//        public AbsenceType(String name, boolean requiredApproval) {
//            this.name = name;
//            this.requiredApproval = requiredApproval;
//        }

    public AbsenceType(String name, boolean requiredApproval) {
        this.name = name;
        this.requiredApproval = requiredApproval;
    }

    public String getName() {
        return this.name;
    }

    public boolean isRequiredApproval() {
        return this.requiredApproval;
    }



}