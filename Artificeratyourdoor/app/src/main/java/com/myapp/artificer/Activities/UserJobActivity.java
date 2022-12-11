package com.myapp.artificer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.myapp.artificer.Models.JobsModel;
import com.myapp.artificer.R;
import com.myapp.artificer.Adapters.UserPreviousJobsAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserJobActivity extends AppCompatActivity implements UserPreviousJobsAdapter.OnItemClickListener {

    RecyclerView mRecyclerView;
    ProgressBar mProgressCircle;
    TextView newText;
    DatabaseReference mDatabaseRef, jobsDatabaseRef;
    List<String> joblist;
    List<JobsModel> jobsModels;
    UserPreviousJobsAdapter previousJobsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_job);

        initViews();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String profile = prefs.getString("profile", "0");
        String id = prefs.getString("id", "0");

        LinearLayoutManager mlayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mlayoutManager);
        jobsModels = new ArrayList<>();
        loadPreviousJobs(profile, id);
    }

    private void initViews() {
        mRecyclerView = findViewById(R.id.recyclerViewId);
        newText = findViewById(R.id.newText);
        mProgressCircle = findViewById(R.id.progressbarId);
    }

    private void loadPreviousJobs(String profile, String id) {
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(profile);

        mDatabaseRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                };
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.getKey().equals("jobs") && ds.getValue() != null) {
                        joblist = ds.getValue(t);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        jobsDatabaseRef = FirebaseDatabase.getInstance().getReference("jobs");
        jobsDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                jobsModels.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    JobsModel jobsModel = postSnapshot.getValue(JobsModel.class);
                    if (joblist != null && joblist.contains(jobsModel.getJobId())) {
                        jobsModels.add(jobsModel);
                    }
                }

                if (jobsModels.size() != 0) {
                    Collections.reverse(jobsModels);
                    previousJobsAdapter = new UserPreviousJobsAdapter(UserJobActivity.this, jobsModels);
                    mRecyclerView.setAdapter(previousJobsAdapter);
                    previousJobsAdapter.setOnItemClickListener(UserJobActivity.this);
                    mProgressCircle.setVisibility(View.INVISIBLE);
                }
                else {
                    newText.setText("No previous jobs");
                    mProgressCircle.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onItemClick(int position) {

        if (jobsModels.get(position).getRatings() == null) {
            itemRatingPopup(position);
        }
        else {
            Toast.makeText(this, "You have already gave ratings to this job", Toast.LENGTH_SHORT).show();
        }

    }


    public void itemRatingPopup(int position) {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(
                this);
        final RatingBar rating = new RatingBar(this);
        rating.setNumStars(5);
        rating.setStepSize(1f);
        rating.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout parent = new LinearLayout(this);
        parent.setGravity(Gravity.CENTER);
        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        parent.addView(rating);

        // popDialog.setIcon(android.R.drawable.btn_star_big_on);
        popDialog.setTitle("Give ratings");
        popDialog.setView(parent);

        // Button OK
        popDialog.setPositiveButton("submit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(UserJobActivity.this, "Ratings: " + rating.getRating(), Toast.LENGTH_SHORT).show();
                        int rates = (int) rating.getRating();
                        jobsDatabaseRef.child(jobsModels.get(position).getJobId()).child("ratings").setValue(""+rates);
                        jobsModels.get(position).setRatings(""+rates);
                        dialog.dismiss();
                    }
                }).setNegativeButton("cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        popDialog.create();
        popDialog.show();
    }
}