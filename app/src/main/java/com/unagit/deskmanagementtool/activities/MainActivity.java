package com.unagit.deskmanagementtool.activities;

import android.content.Intent;
import android.graphics.drawable.LayerDrawable;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.unagit.deskmanagementtool.R;
import com.unagit.deskmanagementtool.activities.SignInActivity;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private GoogleSignInClient mGoogleSignInClient;

    // Instance of FirebaseAuth for Firebase Authentication
    private FirebaseAuth mAuth;

    /**
     * Firebase authentication state change listener.
     */
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        // Initialize auth state change listener.
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user == null) {
                    launchSignInActivity();
                }
            }
        };

        initializeListeners();

    }

    private void initializeListeners() {
        // Buttons
        findViewById(R.id.manage_absences_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchAbsencesActivity();
            }
        });

        findViewById(R.id.schedule_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchScheduleActivity();
            }
        });
    }

    // Add menu into activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    // Handle menu items onClick events.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_button:
                signOut();
                break;
            case R.id.revoke_button:
                revokeAccess();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Try to sign in into Google account silently.
//        mGoogleSignInClient.silentSignIn();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null) {
            // Redirect to login activity.
            launchSignInActivity();
        } else {
            initializeUserProfile(currentUser);
        }

        // Add auth change listener.
        mAuth.addAuthStateListener(mAuthStateListener);

    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove auth change listener.
        if(mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    private void initializeUserProfile(FirebaseUser user) {
        String name = user.getDisplayName();
        String email = user.getEmail();
        Uri photoUrl = user.getPhotoUrl();

        // Set name
        TextView userNameTextView = findViewById(R.id.user_name);
        userNameTextView.setText(
                (name != null ? name : "")
                );

        // Set email
        TextView userEmailTextView = findViewById(R.id.user_email);
        userEmailTextView.setText(
                (email != null ? email : "")
        );

        // Set photo
        if(photoUrl != null) {
            Picasso.get().load(photoUrl)
                    .transform(new CropCircleTransformation())
                    .into((ImageView) findViewById(R.id.user_photo));
        }
    }

    private void updateUI(FirebaseUser currentUser) {
        String message = (currentUser == null ? "FirebaseUser is null" : currentUser.getEmail());
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        message = (googleSignInAccount == null ? "Google account is null" : googleSignInAccount.getDisplayName());
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    //TODO: refactor methods for launching activities to use single method.
    private void launchSignInActivity() {
        Intent signInActivityIntent = new Intent(this, SignInActivity.class);
        startActivity(signInActivityIntent);
        finish();
    }

    private void launchAbsencesActivity() {
        Intent signInActivityIntent = new Intent(this, AbsencesActivity.class);
        startActivity(signInActivityIntent);
    }

    private void launchScheduleActivity() {
        Intent intent = new Intent(this, ScheduleActivity.class);
        startActivity(intent);
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(mAuth.getCurrentUser());
                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(mAuth.getCurrentUser());
                    }
                });
    }
}
