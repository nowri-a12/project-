package com.myapp.artificer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.myapp.artificer.Models.JobsModel;
import com.myapp.artificer.R;
import com.myapp.artificer.Adapters.WorkerPreviousJobsAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorkerJobActivity extends AppCompatActivity implements WorkerPreviousJobsAdapter.OnItemClickListener {

    RecyclerView mRecyclerView;
    ProgressBar mProgressCircle;
    TextView newText;
    DatabaseReference mDatabaseRef, jobsDatabaseRef;
    List<String> joblist;
    List<JobsModel> jobsModels;
    WorkerPreviousJobsAdapter workerPreviousJobsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_job);
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
                Log.d("asdf", "onDataChange: " + snapshot);
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                };
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.getKey().equals("jobs") && ds.getValue() != null) {
                        joblist = ds.getValue(t);
                        Log.d("asdf", "onDataChange: 5"+joblist);
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
                    workerPreviousJobsAdapter = new WorkerPreviousJobsAdapter(WorkerJobActivity.this, jobsModels);
                    mRecyclerView.setAdapter(workerPreviousJobsAdapter);
                    workerPreviousJobsAdapter.setOnItemClickListener(WorkerJobActivity.this);
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
        Intent intent = new Intent(getApplicationContext(), WorkerJobDetailsActivity.class);
        intent.putExtra("job", jobsModels.get(position));
        startActivity(intent);
    }
}