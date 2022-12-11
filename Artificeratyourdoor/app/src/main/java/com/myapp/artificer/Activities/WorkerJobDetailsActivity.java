package com.myapp.artificer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myapp.artificer.Models.JobsModel;
import com.myapp.artificer.R;
import com.myapp.artificer.Models.UserModel;

public class WorkerJobDetailsActivity extends AppCompatActivity {
    JobsModel jobsModel;
    DatabaseReference mDatabaseRef;
    TextView nameText, phoneText, statusText, ratingText;
    AppCompatButton callButton;
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_job_details);

        Intent intent = getIntent();
        jobsModel = (JobsModel) intent.getSerializableExtra("job");

        initViews();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("user");
        mDatabaseRef.orderByChild("id").equalTo(jobsModel.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("asdf", "onDataChange: 111"+dataSnapshot);
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    UserModel userModel = ds.getValue(UserModel.class);
                    nameText.setText(userModel.getName());
                    phoneText.setText(userModel.getPhone());
                    phone = userModel.getPhone();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
        if (jobsModel.getRatings() != null){
            statusText.setText("Completed");
            ratingText.setText(jobsModel.getRatings());
        }
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((ContextCompat.checkSelfPermission(WorkerJobDetailsActivity.this, Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) &&
                        (ContextCompat.checkSelfPermission(WorkerJobDetailsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(WorkerJobDetailsActivity.this,
                            new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE},
                            1);
                } else {
                    Intent intent1 = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" +phone));
                    startActivity(intent1);
                }
            }
        });

    }
    private void initViews() {
        nameText = findViewById(R.id.nameText);
        phoneText = findViewById(R.id.phoneText);
        statusText = findViewById(R.id.statusText);
        ratingText = findViewById(R.id.ratingText);
        callButton = findViewById(R.id.callButton);
    }
}