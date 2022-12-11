package com.myapp.artificer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.myapp.artificer.R;
import com.myapp.artificer.Models.WorkerModel;
import com.myapp.artificer.Adapters.WorkersAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserHomeActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener, WorkersAdapter.OnItemClickListener, PopupMenu.OnMenuItemClickListener {

    AppCompatButton hirebutton,location_btn;
    ArrayAdapter<String> adapter;
    SwipeRefreshLayout swipeLayout;
    RecyclerView mRecyclerView;
    ProgressBar mProgressCircle;
    TextView newText;
    List<WorkerModel> workerModels;
    WorkersAdapter workersAdapter;
    DatabaseReference mDatabaseRef;
    ImageButton menu;
    String search = "all";
    String TAG = "UserHomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        initViews();

        hirebutton.setOnClickListener(this);
        menu.setOnClickListener(this);

        LinearLayoutManager mlayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mlayoutManager);
        workerModels = new ArrayList<>();
        loadWorkersList("all");
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadWorkersList("all");
                workersAdapter.notifyDataSetChanged();
                swipeLayout.setRefreshing(false);
            }
        });


        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView)v.findViewById(android.R.id.text1)).setText("");
                    ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount()));
                }
                return v;
            }

            @Override
            public int getCount() {
                return super.getCount()-1;
            }

        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add("TV");
        adapter.add("Refrigerator");
        adapter.add("AC");
        adapter.add("Water Line");
        adapter.add("Furniture repair");
        adapter.add("Painting");
        adapter.add("PC services");
        adapter.add("Work Type");
    }
    private void initViews() {
        swipeLayout = findViewById(R.id.hire_swipe_container);
        mRecyclerView = findViewById(R.id.recyclerViewId);
        newText = findViewById(R.id.newText);
        mProgressCircle = findViewById(R.id.progressbarId);
        hirebutton = findViewById(R.id.hireButton);
        menu = findViewById(R.id.menu);
    }

    private void loadWorkersList(String search) {
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("worker");
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                workerModels.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    WorkerModel workerModel = postSnapshot.getValue(WorkerModel.class);
                    if (search.equals("all")) {
                        workerModels.add(workerModel);
                    }
                    else {
                        if (workerModel.getWorkType().equals(search)){
                            workerModels.add(workerModel);
                        }
                    }
                }
                if (workerModels.size() != 0) {
                    workersAdapter = new WorkersAdapter(UserHomeActivity.this, workerModels);
                    mRecyclerView.setAdapter(workersAdapter);
                    workersAdapter.setOnItemClickListener(UserHomeActivity.this);
                    mProgressCircle.setVisibility(View.INVISIBLE);
                }
                else {
                    newText.setText("No worker available");
                    mProgressCircle.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserHomeActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hireButton:
                openSerachPopup();
                break;
            case R.id.menu:
                showMenu(v);

                break;
        }
    }
    private void showMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.main_menu);
        popup.show();
    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                Intent intent = new Intent(UserHomeActivity.this, UserJobActivity.class);
                startActivity(intent);
                return true;
            case R.id.item2:
                SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                prefEditor.putString("profile", "0");
                prefEditor.putString("id", "0");
                prefEditor.apply();
                Intent intent1 = new Intent(UserHomeActivity.this, MainActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
                finish();
                return true;
            default:
                return false;
        }
    }
    private void openSerachPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        builder.setTitle("You are seeking");

        builder.setCancelable(false);
        View dialogView = inflater.inflate(R.layout.hire_dialog, null);
        location_btn = dialogView.findViewById(R.id.location_btn);
        location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadLocation();
            }
        });
        Spinner worksSpinner = dialogView.findViewById(R.id.work_spinner);
        worksSpinner.setAdapter(adapter);
        worksSpinner.setSelection(adapter.getCount()); //set the hint the default selection so it appears on launch.
        worksSpinner.setOnItemSelectedListener(this);
        builder.setView(dialogView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        loadWorkersList(search);
                    }
                });
        builder.create();
        builder.show();
    }

    private void loadLocation() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            buildAlertMessageNoGps();

                        } else {
                            getLocationAddress();
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(UserHomeActivity.this, "Location permission is needed to get location", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();
    }

    private void getLocationAddress() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        FusedLocationProviderClient fusedLocationProviderClient;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(UserHomeActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        String subLocality = "";
                        if(addresses.get(0).getSubLocality()!=null){
                            subLocality = addresses.get(0).getSubLocality() +", ";
                        }
                        String thoroughfare = "";
                        if(addresses.get(0).getThoroughfare()!=null){
                            thoroughfare = addresses.get(0).getThoroughfare() +", ";
                        }
                        String locality = "";
                        if(addresses.get(0).getLocality()!=null){
                            locality = addresses.get(0).getLocality();
                        }
                        location_btn.setText(thoroughfare+subLocality+locality);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position != 7) {
            search = adapter.getItem(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(UserHomeActivity.this, WorkerDetailsActivity.class);
        intent.putExtra("worker", workerModels.get(position));
        startActivity(intent);
    }


}