package com.myapp.artificer.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.myapp.artificer.R;
import com.myapp.artificer.Models.WorkerModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class WorkerRegActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    EditText workerName, workerEmail, workerPhone, workerPassword, workerPassword2;
    AppCompatButton locationButton, registerButton, choose_btn;
    FirebaseAuth mAuth;
    FusedLocationProviderClient fusedLocationProviderClient;
    Spinner worksSpinner;
    ArrayAdapter<String> adapter;
    DatabaseReference mDatabaseRef;
    String workType;
    double lat, lon;
    private StorageTask mUploadTask;
    private StorageReference mStorageRef;
    Uri imageUrl;
    ProgressDialog progressDialog;
    private Uri mImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_reg);
        initViews();
        mAuth = FirebaseAuth.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("worker");
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");


        registerButton.setOnClickListener(this);
        locationButton.setOnClickListener(this);
        choose_btn.setOnClickListener(this);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView) v.findViewById(android.R.id.text1)).setText("");
                    ((TextView) v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
                }

                return v;
            }

            @Override
            public int getCount() {
                return super.getCount() - 1;
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


        worksSpinner.setAdapter(adapter);
        worksSpinner.setSelection(adapter.getCount());
        worksSpinner.setOnItemSelectedListener(this);
    }

    private void workerRegister() {
        String name = workerName.getText().toString().trim();
        String email = workerEmail.getText().toString().trim();
        String phone = workerPhone.getText().toString().trim();
        String pass1 = workerPassword.getText().toString().trim();
        String pass2 = workerPassword2.getText().toString().trim();
        if (name.isEmpty()) {
            workerName.setError("Enter a name");
            workerName.requestFocus();
            progressDialog.cancel();
            return;
        }
        if (email.isEmpty()) {
            workerEmail.setError("Email is required");
            workerEmail.requestFocus();
            progressDialog.cancel();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            workerEmail.setError("Please enter a valid email");
            workerEmail.requestFocus();
            progressDialog.cancel();
            return;
        }
        if (!pass1.equals(pass2)) {
            workerPassword2.setError("Password does not match");
            workerPassword2.requestFocus();
            progressDialog.cancel();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, pass1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(WorkerRegActivity.this, "Register Successful", Toast.LENGTH_SHORT).show();
                    final String uploadId = mDatabaseRef.push().getKey();
                    WorkerModel workerModel;
                    if(imageUrl!=null) {
                        workerModel = new WorkerModel(uploadId, name, workType, email, phone, lat, lon, imageUrl.toString());
                    }
                    else {
                        workerModel = new WorkerModel(uploadId, name, workType, email, phone, lat, lon, null);

                    }
                    mDatabaseRef.child(uploadId).setValue(workerModel);

                    SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                    prefEditor.putString("profile", "worker");
                    prefEditor.putString("id", uploadId);
                    prefEditor.apply();
                    progressDialog.cancel();

                    finish();
                    Intent intent = new Intent(WorkerRegActivity.this, WorkerHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {

                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT).show();
                        progressDialog.cancel();
                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.cancel();
                    }

                }
            }
        });

    }

    private void initViews() {
        workerName = findViewById(R.id.worker_name);
        workerEmail = findViewById(R.id.worker_email);
        workerPhone = findViewById(R.id.worker_phone);
        workerPassword = findViewById(R.id.worker_password);
        workerPassword2 = findViewById(R.id.worker_re_password);
        locationButton = findViewById(R.id.location_btn);
        registerButton = findViewById(R.id.registerButton);
        worksSpinner = findViewById(R.id.work_spinner);
        choose_btn = findViewById(R.id.choose_btn);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registerButton:
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(getApplicationContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog = ProgressDialog.show(WorkerRegActivity.this, "",
                            "Registering. Please wait...", true);
                    if(mImageUri!=null) {
                        final long time = System.currentTimeMillis();
                        Bitmap bmp = null;
                        try {
                            bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                        byte[] fileInBytes = baos.toByteArray();


                        StorageReference fileReference = mStorageRef.child(time
                                + "." + "jpg");
                        mUploadTask = fileReference.putBytes(fileInBytes)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!urlTask.isSuccessful()) ;
                                        imageUrl = urlTask.getResult();
                                        workerRegister();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.cancel();
                                        Toast.makeText(WorkerRegActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                                        int progress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                        progressDialog.setMessage(progress + "% Please wait...");
                                    }
                                });
                    }
                    else {
                        workerRegister();
                    }
                }
                break;
            case R.id.location_btn:
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
                                Toast.makeText(WorkerRegActivity.this, "Location permission is needed to get location", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                            }
                        }).check();
                break;
            case R.id.choose_btn:
                openFileChooser();
                break;
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
                mImageUri = data.getData();
                choose_btn.setText(getFileName(mImageUri));
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
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
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(WorkerRegActivity.this, Locale.getDefault());
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
                        locationButton.setText(thoroughfare+subLocality+locality);
                        lat = location.getLatitude();
                        lon = location.getLongitude();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        workType = adapter.getItem(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}