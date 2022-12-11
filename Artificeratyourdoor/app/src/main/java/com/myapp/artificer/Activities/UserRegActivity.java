package com.myapp.artificer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.myapp.artificer.R;
import com.myapp.artificer.Models.UserModel;

public class UserRegActivity extends AppCompatActivity {
    EditText userName,userEmail,userPhone, userPassword, userPassword2;
    AppCompatButton registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_reg);

        initViews();
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("user");

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRegister();
            }
        });

    }

    private void userRegister() {
        String name = userName.getText().toString().trim();
        String email = userEmail.getText().toString().trim();
        String phone = userPhone.getText().toString().trim();
        String pass1 = userPassword.getText().toString().trim();
        String pass2 = userPassword2.getText().toString().trim();
        if (name.isEmpty()) {
            userName.setError("Enter a name");
            userName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            userEmail.setError("Email is required");
            userEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            userEmail.setError("Please enter a valid email");
            userEmail.requestFocus();
            return;
        }
        if(!pass1.equals(pass2)){
            userPassword2.setError("Password does not match");
            userPassword2.requestFocus();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, pass1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    finish();
                    Toast.makeText(UserRegActivity.this, "Register Successful", Toast.LENGTH_SHORT).show();
                    final String uploadId = mDatabaseRef.push().getKey();
                    UserModel userModel = new UserModel(uploadId,name,email,phone);
                    mDatabaseRef.child(uploadId).setValue(userModel);
                    SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                    prefEditor.putString("profile", "user");
                    prefEditor.putString("id", uploadId);
                    prefEditor.apply();
                    FirebaseMessaging.getInstance().subscribeToTopic(uploadId);
                    Intent intent = new Intent(UserRegActivity.this, UserHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {

                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

    }

    private void initViews() {
        userName = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);
        userPhone = findViewById(R.id.user_phone);
        userPassword = findViewById(R.id.user_password);
        userPassword2 = findViewById(R.id.user_re_password);
        registerButton = findViewById(R.id.registerButton);
    }
}