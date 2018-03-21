package com.unagit.deskmanagementtool.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.unagit.deskmanagementtool.R;
import com.unagit.deskmanagementtool.brain.Absence;
import com.unagit.deskmanagementtool.brain.AbsenceType;

import java.util.ArrayList;

import static com.unagit.deskmanagementtool.activities.AddAbsenceActivity.EXTRA_USER_ID;

public class AbsencesActivity extends AppCompatActivity {

    private String mUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__absences);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        FloatingActionButton addAbsenceButton = (FloatingActionButton) findViewById(R.id.fab);
        addAbsenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent absenceIntent = new Intent(AbsencesActivity.this, AddAbsenceActivity.class);
                startActivity(absenceIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getIntentData();
        getAbsences();
    }

    /**
     *  Gets data from intent.
     * Firebase user ID from intent. If not available, uses current one instead.
     */
    private void getIntentData() {
        String uid = getIntent().getStringExtra(EXTRA_USER_ID);
        if(uid == null) {
            // Check if user is signed in (non-null) and update UI accordingly.
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user == null) {
                // Redirect to login activity.
                launchSignInActivity();
            } else {
                uid = user.getUid();
            }
        }
        mUserId  = uid;
    }

    private void launchSignInActivity() {
        Intent signInActivityIntent = new Intent(this, SignInActivity.class);
        startActivity(signInActivityIntent);
        finish();
    }

    /**
     * Gets absences for user with id == mUserId from db
     * and populate them into ListView.
     */
    private void getAbsences() {

        final ArrayList<Absence> absences = new ArrayList<>();
        // Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference absencesColRef = db.collection("persons").document(mUserId).collection("absences");
        absencesColRef
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                for(DocumentSnapshot document : documentSnapshots) {
//                    Log.d("AddAbsenceActivity", document.getData().toString());
                    Absence absence = document.toObject(Absence.class);
                    absences.add(absence);
                }

                Log.d("AddAbsenceActivity", "absenceTypes: " + absences.toString());
                for(Absence absence : absences) {
                    Log.d("AbsencesActivity", String.format("Absence: %s", absence.getType()));
                }

            }
        });

    }

}
