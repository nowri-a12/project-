package com.myapp.artificer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;

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
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.myapp.artificer.R;
import com.myapp.artificer.Models.WorkerModel;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class WorkerHomeActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    ImageButton nameEdit, typeEdit, phoneEdit, locationEdit, passwordEdit;
    TextView nameText, typeText, phoneText, locationText, passwordText;
    ImageButton menu;
    DatabaseReference mDatabaseRef;
    ImageView imageView;
    String id;
    FirebaseUser user;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_home);

        initViews();
        loadData();
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();

        menu.setOnClickListener(this);
        nameEdit.setOnClickListener(this);
        typeEdit.setOnClickListener(this);
        phoneEdit.setOnClickListener(this);
        locationEdit.setOnClickListener(this);
        passwordEdit.setOnClickListener(this);
    }

    private void loadData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        id = prefs.getString("id", "0");
        FirebaseMessaging.getInstance().subscribeToTopic(id);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("worker");
        mDatabaseRef.orderByChild("id").equalTo(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // retrieve the data into an object: kon here
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    WorkerModel workerModel = postSnapshot.getValue(WorkerModel.class);
                    if (workerModel.getImageUrl() != null) {
                        Glide.with(getApplicationContext()).load(workerModel.getImageUrl()).into(imageView);
                    }
                    nameText.setText(workerModel.getName());
                    typeText.setText(workerModel.getWorkType());
                    phoneText.setText(workerModel.getPhone());

                    try {
                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        List<Address> addresses = null;
                        addresses = geocoder.getFromLocation(workerModel.getLat(), workerModel.getLon(), 1);
                        if (addresses.size() != 0) {
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
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    private void initViews() {
        nameEdit = findViewById(R.id.nameEdit);
        typeEdit = findViewById(R.id.typeEdit);
        phoneEdit = findViewById(R.id.phoneEdit);
        locationEdit = findViewById(R.id.locationEdit);
        passwordEdit = findViewById(R.id.passwordEdit);

        nameText = findViewById(R.id.nameText);
        typeText = findViewById(R.id.typeText);
        phoneText = findViewById(R.id.phoneText);
        locationText = findViewById(R.id.locationText);
        passwordText = findViewById(R.id.passwordText);

        menu = findViewById(R.id.menu);
        imageView = findViewById(R.id.imageView);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu:
                showMenu(v);
                break;
            case R.id.nameEdit:
                openEditBox("name");
                break;
            case R.id.typeEdit:
                showWorkList();
                break;
            case R.id.phoneEdit:
                openEditBox("phone");
                break;
            case R.id.locationEdit:
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
                                Toast.makeText(WorkerHomeActivity.this, "Location permission is needed to get location", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                            }
                        }).check();
                break;
            case R.id.passwordEdit:
                resetPassword();
                break;
        }
    }

    private void resetPassword() {
        final EditText resetPassword = new EditText(this);

        final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(this);
        passwordResetDialog.setTitle("Reset Password ?");
        passwordResetDialog.setMessage("Enter New Password");
        passwordResetDialog.setView(resetPassword);

        passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // extract the email and send reset link
                String newPassword = resetPassword.getText().toString();
                user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(WorkerHomeActivity.this, "Password Reset Successfully.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(WorkerHomeActivity.this, "Password Reset Failed." + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // close
            }
        });

        passwordResetDialog.create().show();

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
                        Geocoder geocoder = new Geocoder(WorkerHomeActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
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
                        mDatabaseRef.child(id).child("lat").setValue(location.getLatitude());
                        mDatabaseRef.child(id).child("lon").setValue(location.getLongitude());

                        Toast.makeText(WorkerHomeActivity.this, "Current Location: " + thoroughfare + subLocality + locality, Toast.LENGTH_SHORT).show();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    private void showWorkList() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(WorkerHomeActivity.this);

        builderSingle.setTitle("Select Work Type: ");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(WorkerHomeActivity.this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("TV");
        arrayAdapter.add("Refrigerator");
        arrayAdapter.add("AC");
        arrayAdapter.add("Water Line");
        arrayAdapter.add("Furniture repair");
        arrayAdapter.add("Painting");
        arrayAdapter.add("PC services");

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String workType = arrayAdapter.getItem(which);
//                AlertDialog.Builder builderInner = new AlertDialog.Builder(CraftsmanHomeActivity.this);
//                builderInner.setMessage(strName);
//                builderInner.setTitle("Your Selected Item is");
//                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog,int which) {
//                        dialog.dismiss();
//                    }
//                });
//                builderInner.show();
                mDatabaseRef.child(id).child("workType").setValue(workType);

            }
        });
        builderSingle.show();
    }

    private void openEditBox(String item) {
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        input.setLayoutParams(lp);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit your " + item)
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String editTextInput = input.getText().toString();
                        mDatabaseRef.child(id).child(item).setValue(editTextInput);

                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
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
                Intent intent = new Intent(WorkerHomeActivity.this, WorkerJobActivity.class);
                startActivity(intent);
                return true;
            case R.id.item2:
                SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                prefEditor.putString("profile", "0");
                prefEditor.putString("id", "0");
                prefEditor.apply();
                Intent intent1 = new Intent(WorkerHomeActivity.this, MainActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
                finish();
                return true;
            default:
                return false;
        }
    }
}