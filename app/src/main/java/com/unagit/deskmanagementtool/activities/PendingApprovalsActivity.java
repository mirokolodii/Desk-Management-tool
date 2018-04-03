package com.unagit.deskmanagementtool.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.unagit.deskmanagementtool.R;

public class PendingApprovalsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_approvals);


    }

    @Override
    protected void onStart() {
        super.onStart();

//        updateUI();
    }
}
