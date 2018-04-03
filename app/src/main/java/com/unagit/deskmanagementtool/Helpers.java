package com.unagit.deskmanagementtool;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.unagit.deskmanagementtool.activities.SignInActivity;

public class Helpers {

    public static void launchActivity(Context context, Class<?> cls) {
        Intent signInActivityIntent = new Intent(context, cls);
        signInActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(signInActivityIntent);
    }

    /**
     *  Verifies that user is logged into the Firebase.
     *  Saves user ID.
     */
    @Nullable
    public static String getLoggedInFirebaseUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) {
            return null;
        }
        return user.getUid();
//        return true;
    }

    /**
     * Changes listening state of adapter.
     * @param enable if true - start listening, otherwise - stop listening.
     */
    public static void enableListeningForAdapter(boolean enable, FirestoreRecyclerAdapter adapter) {
        if(adapter != null) {
            if(enable) {
                adapter.startListening();
            } else {
                adapter.stopListening();
            }
        }
    }
}
