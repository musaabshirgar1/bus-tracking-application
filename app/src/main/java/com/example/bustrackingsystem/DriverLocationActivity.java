package com.example.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class DriverLocationActivity extends AppCompatActivity {

    FirebaseUser driverFirebaseUser;
    DatabaseReference databaseDriverLocation;
    DatabaseReference databaseDriver;

    String driverId;
    String driverBusName;
    String driverChangedBus;

    TextView driverBusNameTextView;
    Spinner driverChangeBusSpinner;

    public void driverOpenMap(View view){
        Intent intent = new Intent(getApplicationContext(), DriverMapsActivity.class);
        startActivity(intent);
    }

    public void driverChangeBus(View view){
        driverChangedBus = driverChangeBusSpinner.getSelectedItem().toString();
        Driver changeDriverDetails = new Driver();
        HashMap<String, Object> driverDetailsUpdate = new HashMap<>();
        driverDetailsUpdate.put("driverBusName", changeDriverDetails.setDriverBusName(driverChangedBus));
        databaseDriver.child(driverId).updateChildren(driverDetailsUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(DriverLocationActivity.this, "Changed Bus to " + driverChangedBus, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DriverLocationActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        DriverLocation changeDriverLocationDetails = new DriverLocation();
        HashMap<String, Object> driverLocationDetailsUpdate  = new HashMap<>();
        driverLocationDetailsUpdate.put("driverBusName", changeDriverLocationDetails.setDriverBusName(driverChangedBus));
        databaseDriverLocation.child(driverId).updateChildren(driverLocationDetailsUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                driverBusNameTextView.setText(String.format("You're driving the bus %s", driverChangedBus));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DriverLocationActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_driver_location);

        driverChangeBusSpinner = findViewById(R.id.driverChangeBusSpinner);
        driverBusNameTextView = findViewById(R.id.driverBusNameTextView);
        driverFirebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        databaseDriver = FirebaseDatabase.getInstance().getReference("Driver");
        databaseDriverLocation = FirebaseDatabase.getInstance().getReference("DriverLocation");
        driverId = driverFirebaseUser.getUid();

        getDriverBusName();
    }

   public void getDriverBusName(){
        databaseDriver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
                        Driver driverDetails = driverSnapshot.getValue(Driver.class);

                        if (driverDetails != null  && driverDetails.getDriverId().equals(driverId)) {
                            driverBusName = driverDetails.getDriverBusName();
                            DriverLocation driverLocation = new DriverLocation(driverBusName, driverId,0 , 0);
                            databaseDriverLocation.child(driverId).setValue(driverLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Log.i("Info", "DLA");
                                    }else {
                                        Toast.makeText(DriverLocationActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            driverBusNameTextView.setText(String.format("You're driving the bus %s", driverBusName));
                        }
                    }
                } else {
                    Toast.makeText(DriverLocationActivity.this, "There is no data existing", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
   }

}
