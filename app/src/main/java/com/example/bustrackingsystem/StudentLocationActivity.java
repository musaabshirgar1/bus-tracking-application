package com.example.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;



import android.annotation.SuppressLint;

import android.content.Intent;
import android.content.pm.ActivityInfo;

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


import java.util.HashMap;

public class StudentLocationActivity extends AppCompatActivity {
    String studentId;
    String studentBusRoute;
    String studentChangedBusRoute;
    Student driverId;

    FirebaseUser studentFirebaseUser;
    DatabaseReference databaseStudent;
    DatabaseReference databaseStudentLocation;
    DatabaseReference databaseDriverLocation;

    TextView studentBusRouteTextView;
    Spinner studentBusChangeSpinner;

    public void studentOpenMap(View view){
        Intent intent = new Intent(getApplicationContext(),StudentMapsActivity.class);
        startActivity(intent);
    }

    public void studentChangeBusRoute(View view){
        studentChangedBusRoute = studentBusChangeSpinner.getSelectedItem().toString();
        Student changeStudentDetails = new Student();
        HashMap<String, Object> studentDetailsUpdate = new HashMap<>();
        studentDetailsUpdate.put("studentBusRoute", changeStudentDetails.setStudentBusRoute(studentChangedBusRoute));
        databaseStudent.child(studentId).updateChildren(studentDetailsUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(StudentLocationActivity.this, "Changed bus route to " + studentChangedBusRoute, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(StudentLocationActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        StudentLocation changeStudentLocationDetails = new StudentLocation();
        HashMap<String, Object> studentLocationDetailsUpdate = new HashMap<>();
        studentLocationDetailsUpdate.put("studentBusRoute", changeStudentLocationDetails.setStudentBusRoute(studentChangedBusRoute));
        databaseStudentLocation.child(studentId).updateChildren(studentLocationDetailsUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                studentBusRouteTextView.setText(String.format("You're riding the bus %s", studentChangedBusRoute));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(StudentLocationActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        getDriverId(studentChangedBusRoute);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_student_location);

        studentBusRouteTextView = findViewById(R.id.studentBusRouteTextView);
        studentBusChangeSpinner = findViewById(R.id.studentChangeBusSpinner);
        databaseStudent = FirebaseDatabase.getInstance().getReference("Student");
        databaseStudentLocation = FirebaseDatabase.getInstance().getReference("StudentLocation");
        databaseDriverLocation = FirebaseDatabase.getInstance().getReference("DriverLocation");
        studentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (studentFirebaseUser != null) {
            studentId = studentFirebaseUser.getUid();
        }

        getStudentBusRoute();

    }
    public void getStudentBusRoute(){
        databaseStudent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                        Student studentDetails = studentSnapshot.getValue(Student.class);

                        if (studentDetails != null) {
                            if (studentDetails.getStudentId().equals(studentId)) {
                                studentBusRoute = studentDetails.getStudentBusRoute();
                                getDriverId(studentBusRoute);
                                StudentLocation studentLocation = new StudentLocation(studentBusRoute, studentId, 0, 0,"");
                                databaseStudentLocation.child(studentId).setValue(studentLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.i("Info", "SLA");
                                        } else {
                                            Toast.makeText(StudentLocationActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                studentBusRouteTextView.setText(String.format("You're currently riding the bus %s", studentBusRoute));
                            }
                        }
                    }
                }else {
                    Toast.makeText(StudentLocationActivity.this, "No student data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getDriverId(final String busName){
        databaseDriverLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot driverDataSnapshot: dataSnapshot.getChildren()){
                    final DriverLocation driverLocation = driverDataSnapshot.getValue(DriverLocation.class);
                    if(driverLocation.getDriverBusName().equals(busName)){
                        StudentLocation studentLocation = new StudentLocation();
                        HashMap<String, Object> updateDriverId = new HashMap<>();
                        updateDriverId.put("driverId", studentLocation.setDriverId(driverLocation.getDriverId()));
                        databaseStudentLocation.child(studentId).updateChildren(updateDriverId).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Log.i("Info", "Driver Id added in student location table");
                                }else {
                                    Toast.makeText(StudentLocationActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
