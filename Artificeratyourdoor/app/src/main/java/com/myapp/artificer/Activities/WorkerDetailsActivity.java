package com.myapp.artificer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.myapp.artificer.Models.JobsModel;
import com.myapp.artificer.R;
import com.myapp.artificer.Models.WorkerModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkerDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    WorkerModel workerModel;
    TextView nameText,profText, locationText, phoneText, emailText, ratingText, ratingCountText;
    ImageView imageView;
    AppCompatButton hireButton, callButton;
    private DatabaseReference jobDatabaseRef, userDatabaseRef, workerDatabaseRef;
    List<String> workerJoblist, userJobList;
    int ratings = 0, ratingsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_details);

        initViews();
        Intent intent = getIntent();
        workerModel = (WorkerModel) intent.getSerializableExtra("worker");

        jobDatabaseRef = FirebaseDatabase.getInstance().getReference("jobs");
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("user");
        workerDatabaseRef = FirebaseDatabase.getInstance().getReference("worker");


        userJobList = new ArrayList<>();
        workerJoblist = new ArrayList<>();

        if (workerModel.getImageUrl() != null) {
            Glide.with(getApplicationContext()).load(workerModel.getImageUrl()).into(imageView);
        }
        nameText.setText(workerModel.getName());
        profText.setText(workerModel.getWorkType());
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = null;
            addresses = geocoder.getFromLocation(workerModel.getLat(), workerModel.getLon(), 1);
            if(addresses.size() != 0) {
                String subLocality = "";
                if (addresses.get(0).getSubLocality() != null) {
                    subLocality = addresses.get(0).getSubLocality() + ", ";
                }
                String thoroughfare = "";
                if (addresses.get(0).getThoroughfare() != null) {
                    thoroughfare = addresses.get(0).getThoroughfare() + ", ";
                }
                String locality = "";
                if (addresses.get(0).getLocality() != null) {
                    locality = addresses.get(0).getLocality();
                }
                locationText.setText(thoroughfare + subLocality + locality);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        phoneText.setText(workerModel.getPhone());
        emailText.setText(workerModel.getEmail());

        jobDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    JobsModel jobsModel = ds.getValue(JobsModel.class);
                    if (jobsModel.getWorkerId().equals(workerModel.getId())) {
                        if (jobsModel.getRatings() != null) {
                            ratingsCount++;
                            ratings += Integer.parseInt(jobsModel.getRatings());
                        }
                    }
                }
                if (ratingsCount != 0) {
                    float avgRating = ratings / ratingsCount;

                    ratingText.setText("" + avgRating);
                    ratingCountText.setText("" + ratingsCount);
                } else {
                    ratingText.setText("0");
                    ratingCountText.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        hireButton.setOnClickListener(this);
        callButton.setOnClickListener(this);
    }

    private void initViews() {
        nameText = findViewById(R.id.nameText);
        profText = findViewById(R.id.profText);
        locationText = findViewById(R.id.locationText);
        phoneText = findViewById(R.id.phoneText);
        emailText = findViewById(R.id.emailText);
        ratingText = findViewById(R.id.ratingText);
        ratingCountText = findViewById(R.id.ratingCountText);
        imageView = findViewById(R.id.imageView);
        hireButton = findViewById(R.id.hireButton);
        callButton = findViewById(R.id.callButton);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hireButton:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Do you really want to hire this artificer?")
                        .setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(WorkerDetailsActivity.this, "Confirmed", Toast.LENGTH_SHORT).show();

                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                String userId = prefs.getString("id", "0");


                                final String uploadId = jobDatabaseRef.push().getKey();
                                JobsModel jobsModel = new JobsModel(uploadId, userId, workerModel.getId(), workerModel.getName());
                                jobDatabaseRef.child(uploadId).setValue(jobsModel);

                                userDatabaseRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        userJobList.clear();
                                        GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                                        };
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            if (ds.getKey().equals("jobs") && ds.getValue() != null) {
                                                userJobList = ds.getValue(t);
                                            }
                                        }
                                        userJobList.add(uploadId);
                                        userDatabaseRef.child(userId).child("jobs").setValue(userJobList);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                workerDatabaseRef.child(workerModel.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        workerJoblist.clear();
                                        GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                                        };
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            if (ds.getKey().equals("jobs") && ds.getValue() != null) {
                                                workerJoblist = ds.getValue(t);

                                            }
                                        }
                                        workerJoblist.add(uploadId);
                                        workerDatabaseRef.child(workerModel.getId()).child("jobs").setValue(workerJoblist);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                Toast.makeText(WorkerDetailsActivity.this, "canceled", Toast.LENGTH_SHORT).show();
                            }
                        });
                // Create the AlertDialog object and return it
                builder.create();
                builder.show();
                break;
            case R.id.callButton:

                if ((ContextCompat.checkSelfPermission(WorkerDetailsActivity.this, Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) &&
                        (ContextCompat.checkSelfPermission(WorkerDetailsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(WorkerDetailsActivity.this,
                            new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE},
                            1);
                } else {
                    Intent intent1 = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + workerModel.getPhone()));
                    startActivity(intent1);
                }
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Intent intent1 = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + workerModel.getPhone()));
        startActivity(intent1);
    }
}