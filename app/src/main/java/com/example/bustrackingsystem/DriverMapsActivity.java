package com.example.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

public class DriverMapsActivity extends FragmentActivity implements OnMapReadyCallback{

    public static class SLocation{
        private double lat;
        private double lon;

        SLocation(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        @NonNull
        @Override
        public String toString() {
            return "StudentLocations{" +"lat=" + lat +
                    ", lon=" + lon + ")";
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }
    }

    private GoogleMap mMap;

    LocationManager locationManager;
    LocationListener locationListener;


    DatabaseReference databaseDriverLocation;
    DatabaseReference databaseStudentLocation;
    DatabaseReference databaseCentroidLocation;
    FirebaseUser driverFirebaseUser;

    String driverId;
    double driverLat;
    double driverLon;
    double studentLat;
    double studentLon;
    HashMap<String, SLocation> studentsLocation;
    boolean driverDataAdded = false;
    boolean doubleBackToExitPressedOnce = false;
    boolean isCentroidFormed = false;
    boolean removeOtherMarkers = false;

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        databaseDriverLocation = FirebaseDatabase.getInstance().getReference("DriverLocation");
        databaseStudentLocation = FirebaseDatabase.getInstance().getReference("StudentLocation");
        databaseCentroidLocation = FirebaseDatabase.getInstance().getReference("CentroidLocation");
        studentsLocation = new HashMap<>();

        driverFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (driverFirebaseUser != null) {
            driverId = driverFirebaseUser.getUid();
        }

        getAllStudentLocation();
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


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(location.getLatitude()!=0 && location.getLongitude()!=0) {
                    updateDriverLocationOnFirebase(location);
                    if(driverDataAdded){
                        driverLat = location.getLatitude();
                        driverLon = location.getLongitude();
                        updateDriverMap(location);
                        formCentroid();
                        //Toast.makeText(DriverMapsActivity.this, location.getLatitude() + ", "+ location.getLongitude(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {
                Toast.makeText(DriverMapsActivity.this, "GPS Enabled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String s) {
                Toast.makeText(DriverMapsActivity.this, "GPS Disabled", Toast.LENGTH_SHORT).show();
            }
        };

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location driversLastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (driversLastKnownLocation != null) {
                updateDriverLocationOnFirebase(driversLastKnownLocation);
                updateDriverMap(driversLastKnownLocation);
            }
        }

    }

    public void updateDriverLocationOnFirebase (Location location){
        final DriverLocation driverLocation = new DriverLocation();
        HashMap<String, Object> updateDriverLocation = new HashMap<>();
        updateDriverLocation.put("driverLat", driverLocation.setDriverLat(location.getLatitude()));
        updateDriverLocation.put("driverLon", driverLocation.setDriverLon(location.getLongitude()));
        databaseDriverLocation.child(driverId).updateChildren(updateDriverLocation).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                             driverDataAdded = true;
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DriverMapsActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public  void updateDriverMap(Location location){

        mMap.clear();
        LatLng driverLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        Collection<SLocation> values = studentsLocation.values();
        ArrayList<SLocation> studentLatLngs = new ArrayList<>(values);
        ArrayList<LatLng> sLatLngs = new ArrayList<>();

        for(SLocation sLocation: studentLatLngs){
            sLatLngs.add(new LatLng(sLocation.lat, sLocation.lon));
        }

        if(!removeOtherMarkers) {
            ArrayList<Marker> markers = new ArrayList<>();
            markers.add(mMap.addMarker(new MarkerOptions().position(driverLatLng).title("You are here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))));
            for (LatLng latLng : sLatLngs) {
                if(latLng.latitude != 0.0d && latLng.longitude != 0.0d) {
                    markers.add(mMap.addMarker(new MarkerOptions().position(latLng).title("Students").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))));
                }
            }
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();

            int padding = 80;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.moveCamera(cu);
        }
    }

    public void getAllStudentLocation(){
        databaseStudentLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot studentLocationDataSnapShot : dataSnapshot.getChildren()){
                        StudentLocation studentLocation = studentLocationDataSnapShot.getValue(StudentLocation.class);
                        if (studentLocation != null && studentLocation.getDriverId().equals(driverId)) {
                            String studentId = studentLocation.getStudentId();
                            studentLat = studentLocation.getStudentLat();
                            studentLon = studentLocation.getStudentLon();
                            studentsLocation.put(studentId, new SLocation(studentLat,studentLon));
                        }
                    }
                } else {
                    Toast.makeText(DriverMapsActivity.this, "There is currently no student riding", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void formCentroid(){
        double x = 0.0, y = 0.0, z = 0.0;
        if(driverLat!=0.0d && driverLon!=0.0d){
            Iterator iterator = studentsLocation.entrySet().iterator();
            int total = studentsLocation.size();
            while (iterator.hasNext()){
                Map.Entry mapElement = (Map.Entry) iterator.next();
                SLocation k = (SLocation) mapElement.getValue();
                if(getDistance(driverLat,driverLon,k.getLat(),k.getLon()) < 10){
                    Log.i("Less than 10", isCentroidFormed + ", " + removeOtherMarkers);
                    isCentroidFormed = true;
                    removeOtherMarkers = true;
                    double latitude = k.getLat() * PI / 180;
                    double longitude = k.getLon() * PI / 180;

                    x += cos(latitude) * cos(longitude);
                    y += cos(latitude) * sin(longitude);
                    z += sin(latitude);
                } else if(getDistance(driverLat,driverLon,k.getLat(),k.getLon()) >= 10){
                    Log.i("More than 10", isCentroidFormed + ", " + removeOtherMarkers);
                    ArrayList<LatLng> discardedLatLng = new ArrayList<>();
                    discardedLatLng.add(new LatLng(k.getLat(), k.getLon()));
                    for(LatLng dLatLng: discardedLatLng){
                        if(dLatLng.latitude != 0.0d && dLatLng.longitude != 0.0d) {
                            mMap.addMarker(new MarkerOptions().position(dLatLng).title("Students").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                        }
                    }
                }
            }
            x = x / total;
            y = y / total;
            z = z / total;

            double centralLongitude = atan2(y, x);
            double centralSquareRoot = sqrt(x * x + y * y);
            double centralLatitude = atan2(z, centralSquareRoot);

            double centroidLatitude = centralLatitude * 180 / PI;
            double centroidLongitude = centralLongitude * 180 / PI;

            CentroidLocation centroidLocation = new CentroidLocation();
            HashMap<String, Object> updateCentroidLocation = new HashMap<>();
            updateCentroidLocation.put("driverId", centroidLocation.setDriverId(driverId));
            LatLng centroidLatLng = new LatLng(centroidLatitude, centroidLongitude);
            if(centralLatitude != 0.0d && centralLongitude != 0.0d && isCentroidFormed) {
                Toast.makeText(this, "Yes centroid is formed", Toast.LENGTH_SHORT).show();
                updateCentroidLocation.put("centroidLat", centroidLocation.setCentroidLat(centroidLatitude));
                updateCentroidLocation.put("centroidLon", centroidLocation.setCentroidLon(centroidLongitude));

                mMap.addMarker(new MarkerOptions().position(centroidLatLng).title("Centroid").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            } else {
                removeOtherMarkers = false;
                isCentroidFormed = false;
            }
            updateCentroidLocation.put("isCentroidFormed", centroidLocation.setCentroidFormed(isCentroidFormed));
            databaseCentroidLocation.child(driverId).updateChildren(updateCentroidLocation);
        }
    }

    public double getDistance(double dLat, double dLon, double sLat, double sLon ){
        return 6371000 *
                acos(cos(toRadians(dLat)) *
                        cos(toRadians(sLat)) *
                        cos(toRadians(sLon) -
                                toRadians(dLon)) +
                        sin(toRadians(dLat)) *
                                sin(toRadians(sLat)));
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

}
