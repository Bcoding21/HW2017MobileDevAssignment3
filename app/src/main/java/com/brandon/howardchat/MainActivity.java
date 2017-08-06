package com.brandon.howardchat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private ProgressBar mProgressBar;
    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        GoogleApiClient.OnConnectionFailedListener listener =
                new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
                        // be available.
                        Log.d("TAG", "onConnectionFailed:" + connectionResult);
                        Toast.makeText(MainActivity.this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                };


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, listener)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            updateUI(currentUser);
        }

        Intent i = new Intent(this, MessagingAppNotificationService.class);
        stopService(i);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("DEI", "app stopped");
        // Start the service
        Intent i = new Intent(this, MessagingAppNotificationService.class);
        startService(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sign_in_menu, menu);

        MenuItem item = menu.findItem(R.id.sign_in_button);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        item.setTitle(currentUser == null ? "Sign in" : "Sign out");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sign_in_button) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) { //user is signed in, so sign out
                signOut();
            } else {  //user is signed out, so sign in
                signIn();
            }
            return true;
        } else {
            return false;
        }
    }


    private void signIn() {
//        mProgressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
//        mProgressBar.setVisibility(View.VISIBLE);
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                    }
                });
    }


    private void updateUI(FirebaseUser user) {
        invalidateOptionsMenu(); // This reloads the options menu items to display the correct string.
        //mProgressBar.setVisibility(View.INVISIBLE); // Hide the spinner.

        // Show a Toast to the user.
        String statusText = (user == null) ? "Signed out" : "Signed in as " + user.getEmail();
        Toast.makeText(MainActivity.this, statusText , Toast.LENGTH_SHORT).show();

        // Show or remove the MainFragment based on user signed-in state change.
        FragmentManager fm = getSupportFragmentManager();
        Fragment currentFragment = fm.findFragmentById(R.id.container);
        if (user != null && currentFragment == null) {
            // If the user is signed IN, and there is NOT a fragment in the container, add it.
            Fragment mainFragment = new MessageFragment();
            fm.beginTransaction().replace(R.id.container, mainFragment).commit();

        } else if (user == null && currentFragment != null) {
            // If the user is signed OUT, and there IS a fragment in the container, remove it.
            fm.beginTransaction().remove(currentFragment).commit();
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                updateUI(null);
            }
        }
    }
}
