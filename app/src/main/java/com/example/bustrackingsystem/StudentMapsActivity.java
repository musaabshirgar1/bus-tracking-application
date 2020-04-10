package com.example.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

public class StudentMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final String CHANNEL_ID = "driver_notification";
    private final int NOTIFICATION_ID = 001;
    private GoogleMap mMap;

    LocationManager locationManager;
    LocationListener locationListener;

    DatabaseReference databaseDriverLocation;
    DatabaseReference databaseStudentLocation;
    DatabaseReference databaseCentroidLocation;

    FirebaseUser firebaseUser;
    LatLng driverLatLng;
    LatLng studentLatLng;
    LatLng centroidLatLng;
    TextView driverDistanceTextView;
    LinearLayout driverDistanceLinearLayout;

    String studentId;
    double driverLat;
    double driverLon;
    double studentLat;
    double studentLon;
    double centroidLat;
    double centroidLon;
    boolean studentDataAdded = false;
    boolean doubleBackToExitPressedOnce = false;
    boolean isCentroidFormed;
    boolean isDriverNear = false;

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_student_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        driverDistanceLinearLayout = findViewById(R.id.driverDistanceLinearLayout);
        driverDistanceTextView = findViewById(R.id.driverDistanceTextView);
        databaseDriverLocation = FirebaseDatabase.getInstance().getReference("DriverLocation");
        databaseStudentLocation = FirebaseDatabase.getInstance().getReference("StudentLocation");
        databaseCentroidLocation = FirebaseDatabase.getInstance().getReference("CentroidLocation");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            studentId = firebaseUser.getUid();
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getDriverLocationOnMap();
        getCentroidLocation();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(location.getLatitude()!=0 && location.getLongitude()!=0){
                    studentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    studentLat = location.getLatitude();
                    studentLon=location.getLatitude();
                    updateStudentLocationOnFirebase(location);
                    if(studentDataAdded){
                        if(isCentroidFormed){
                            updateMapWhenCentroidFormed(centroidLat, centroidLon, location);
                        } else {
                            updateMapForStudent(driverLat, driverLon, location);
                            //Log.i("Info", isCentroidFormed + ".");
                        }
                    }
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {
                Toast.makeText(StudentMapsActivity.this, "GPS Enabled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String s) {
                Toast.makeText(StudentMapsActivity.this, "GPS Disabled. Please Enable", Toast.LENGTH_SHORT).show();
            }
        };

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location studentLastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (studentLastKnownLocation != null) {
                updateStudentLocationOnFirebase(studentLastKnownLocation);
                if(isCentroidFormed){
                    updateMapWhenCentroidFormed(centroidLat, centroidLon,studentLastKnownLocation);
                } else {
                    updateMapForStudent(driverLat, driverLon, studentLastKnownLocation);
                }
            }
        }

    }

    public void updateStudentLocationOnFirebase(Location location){
        StudentLocation studentLocation = new StudentLocation();
        HashMap<String, Object> updateStudentLocation = new HashMap<>();
        updateStudentLocation.put("studentLat", studentLocation.setStudentLat(location.getLatitude()));
        updateStudentLocation.put("studentLon", studentLocation.setStudentLon(location.getLongitude()));
        databaseStudentLocation.child(studentId).updateChildren(updateStudentLocation).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                studentDataAdded = true;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(StudentMapsActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getDriverLocationOnMap() {
        databaseStudentLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot studentLocationDataSnapshot : dataSnapshot.getChildren()) {
                        final StudentLocation studentLocation = studentLocationDataSnapshot.getValue(StudentLocation.class);
                        if (studentLocation != null && studentLocation.getStudentId().equals(studentId)) {
                            databaseDriverLocation.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot driverLocationDataSnapshot : dataSnapshot.getChildren()) {
                                            DriverLocation driverLocation = driverLocationDataSnapshot.getValue(DriverLocation.class);
                                            if (driverLocation != null && driverLocation.getDriverId().equals(studentLocation.getDriverId())) {
                                                driverLat = driverLocation.getDriverLat();
                                                driverLon = driverLocation.getDriverLon();
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void updateMapForStudent(double dLat, double dLon, Location location){
        mMap.clear();
        studentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        driverLatLng = new LatLng(dLat, dLon);
        ArrayList<Marker> markers = new ArrayList<>();
        if(studentLatLng.longitude != 0.0d && studentLatLng.latitude != 0.0d && driverLatLng.latitude != 0.0d && driverLatLng.longitude != 0.0d ) {
            markers.add(mMap.addMarker(new MarkerOptions().position(studentLatLng).title("You are here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))));
            markers.add(mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Driver").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();

            int padding = 80;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.moveCamera(cu);
            double distanceToDriver = 6371 *
                    acos(cos(toRadians(location.getLatitude())) *
                            cos(toRadians(dLat)) *
                            cos(toRadians(dLon) -
                                    toRadians(location.getLongitude())) +
                            sin(toRadians(location.getLatitude())) *
                                    sin(toRadians(dLat)));
            DecimalFormat decimalFormat = new DecimalFormat("#.##");

            if (distanceToDriver <= 0.45 && distanceToDriver >= 0.1d) {
                driverDistanceTextView.setText("Your driver is nearly here");
            } else if (distanceToDriver < 0.1d) {
                driverDistanceTextView.setText("Your driver is here");
            } else {
                driverDistanceTextView.setText(String.format("Your driver is %s km's away", decimalFormat.format(distanceToDriver)));
            }
            //isDriverNear(dLat, dLon, location);
            Toast.makeText(StudentMapsActivity.this, driverLat + ", " + driverLon, Toast.LENGTH_SHORT).show();
        }
    }

    public void updateMapWhenCentroidFormed(double cLat, double cLon, Location location){
        mMap.clear();
        studentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        centroidLatLng = new LatLng(cLat,cLon);
        ArrayList<Marker> markers = new ArrayList<>();
        if(studentLatLng.longitude != 0.0d && studentLatLng.latitude != 0.0d && centroidLatLng.latitude != 0.0d && centroidLatLng.longitude != 0.0d ) {
            markers.add(mMap.addMarker(new MarkerOptions().position(studentLatLng).title("You Are Here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))));
            markers.add(mMap.addMarker(new MarkerOptions().position(centroidLatLng).title("Your Driver").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();

            int padding = 80;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.moveCamera(cu);

            double distanceToDriver = 6371 *
                    acos(cos(toRadians(location.getLatitude())) *
                            cos(toRadians(cLat)) *
                            cos(toRadians(cLon) -
                                    toRadians(location.getLongitude())) +
                            sin(toRadians(location.getLatitude())) *
                                    sin(toRadians(cLat)));
            DecimalFormat decimalFormat = new DecimalFormat("#.##");

            if (distanceToDriver >= 30  && distanceToDriver < 50) {
                driverDistanceTextView.setText("Your driver is nearly here");
            } else if (distanceToDriver < 0.1d) {
                driverDistanceTextView.setText("Your driver is here");
            } else {
                driverDistanceTextView.setText(String.format("Your driver is %s km's away", decimalFormat.format(distanceToDriver)));
            }
            //isDriverNear(cLat, cLon, location);
            //Toast.makeText(StudentMapsActivity.this, "Centroid formed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    public void getCentroidLocation(){
        databaseStudentLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot studentLocationDataSnapshot : dataSnapshot.getChildren()) {
                        StudentLocation studentLocation = studentLocationDataSnapshot.getValue(StudentLocation.class);
                        if (studentLocation != null && studentLocation.getStudentId().equals(studentId)) {
                            databaseCentroidLocation.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        for(DataSnapshot centroidLocationDataSnapshot: dataSnapshot.getChildren()){
                                            CentroidLocation centroidLocation = centroidLocationDataSnapshot.getValue(CentroidLocation.class);
                                            if(centroidLocation != null  && centroidLocation.getDriverId().equals(studentLocation.getDriverId())){
                                                centroidLat = centroidLocation.getCentroidLat();
                                                centroidLon = centroidLocation.getCentroidLon();
                                                isCentroidFormed =!centroidLocation.isCentroidFormed();
                                                Log.i("Info", centroidLat + ", " + centroidLon + ", " + isCentroidFormed );
                                            }
                                        }

                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void showNotification(){
        NotificationCompat.Builder builder= new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.buslogo);
        builder.setContentTitle("Nirma Bus Tracking App");
        builder.setContentText("Your driver is five minutes away!");
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    public void isDriverNear(double dLat, double dLon, Location location){
        double distanceToDriver = 6371 *
                acos(cos(toRadians(location.getLatitude())) *
                        cos(toRadians(dLat)) *
                        cos(toRadians(dLon) -
                                toRadians(location.getLongitude())) +
                        sin(toRadians(location.getLatitude())) *
                                sin(toRadians(dLat)));
        if(distanceToDriver < 1000 && !isDriverNear){
            showNotification();
            isDriverNear = true;
        }
    }
}
