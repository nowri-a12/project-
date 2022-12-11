package com.myapp.artificer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.myapp.artificer.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    AppCompatButton hireButton, workButton;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRef;
    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        initViews();
        mAuth = FirebaseAuth.getInstance();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String profile = prefs.getString("profile", "0");
        String id = prefs.getString("id", "0");

        if (!profile.equals("0") && !id.equals("0")) {
            if (profile.equals("user")) {
                finish();
                Intent intent = new Intent(MainActivity.this, UserHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else if (profile.equals("worker")) {
                finish();
                Intent intent = new Intent(MainActivity.this, WorkerHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }

        hireButton.setOnClickListener(this);
        workButton.setOnClickListener(this);
    }

    private void initViews() {
        hireButton = findViewById(R.id.hireButton);
        workButton = findViewById(R.id.workButton);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hireButton:
                openLogInPopup(1);
                break;
            case R.id.workButton:
                openLogInPopup(2);
                break;
        }
    }

    private void openLogInPopup(int option) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the
        // dialog layout
        builder.setTitle("Login");
        builder.setCancelable(false);
        View dialogView = inflater.inflate(R.layout.login_dialog, null);
        AppCompatButton btn_signup = dialogView.findViewById(R.id.btn_signup);
        EditText editTextEmail = (EditText) dialogView.findViewById(R.id.email);
        EditText editTextPassword = (EditText) dialogView.findViewById(R.id.password);
        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String email = editTextEmail.getText().toString().trim();
                        String password = editTextPassword.getText().toString().trim();

                        if (email.isEmpty()) {
                            editTextEmail.setError("Email is required");
                            editTextEmail.requestFocus();
                            return;
                        }

                        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            editTextEmail.setError("Please enter a valid email");
                            editTextEmail.requestFocus();
                            return;
                        }

                        if (password.isEmpty()) {
                            editTextPassword.setError("Password is required");
                            editTextPassword.requestFocus();
                            return;
                        }

                        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    if (option == 1) {
                                        mDatabaseRef = FirebaseDatabase.getInstance().getReference("user");
                                        mDatabaseRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChildren()) {
                                                    // retrieve the data into an object: kon here
                                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                                                        prefEditor.putString("profile", "user");
                                                        prefEditor.putString("id", postSnapshot.getKey());
                                                        prefEditor.apply();

                                                        Intent intent = new Intent(MainActivity.this, UserHomeActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                                else {
                                                    Toast.makeText(MainActivity.this, "Enter your Hire account", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }

                                        });

                                    } else {
                                        mDatabaseRef = FirebaseDatabase.getInstance().getReference("worker");
                                        mDatabaseRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                // retrieve the data into an object: kon here
                                                if (dataSnapshot.hasChildren()) {
                                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                                                        prefEditor.putString("profile", "worker");
                                                        prefEditor.putString("id", postSnapshot.getKey());
                                                        prefEditor.apply();

                                                        FirebaseMessaging.getInstance().subscribeToTopic(postSnapshot.getKey());

                                                        Intent intent = new Intent(MainActivity.this, WorkerHomeActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                                else {
                                                    Toast.makeText(MainActivity.this, "Enter your Work account", Toast.LENGTH_SHORT).show();

                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }

                                        });

                                    }


                                } else {
                                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create();
        final AlertDialog alertDialog = builder.show();
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (option == 1) {
                    Intent intent = new Intent(MainActivity.this, UserRegActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, WorkerRegActivity.class);
                    startActivity(intent);
                }
                alertDialog.dismiss();
            }
        });
    }
}