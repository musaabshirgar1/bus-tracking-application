package com.example.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverRegistrationActivity extends AppCompatActivity {

    EditText driverNameEditText;
    EditText driverPhoneNumberEditText;
    EditText driverEmailEditText;
    EditText driverPasswordEditText;
    TextView driverSigninTextView;
    Spinner busNamesSpinner;
    String driverName;
    String driverPhoneNumber;
    String driverEmail;
    String driverPassword;
    String busName;
    String driverId;
    int type;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseDriver;

    public void registerAsDriver(View view){
        driverName = driverNameEditText.getText().toString();
        driverPhoneNumber = driverPhoneNumberEditText.getText().toString();
        driverEmail = driverEmailEditText.getText().toString();
        driverPassword = driverPasswordEditText.getText().toString();
        busName = busNamesSpinner.getSelectedItem().toString();

        if(!TextUtils.isEmpty(driverName) && !TextUtils.isEmpty(driverPhoneNumber) && !TextUtils.isEmpty(driverEmail) && !TextUtils.isEmpty(driverPassword)){
            driverSignup();
        } else {
            Toast.makeText(this, "Please enter all the credentials", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_driver_registration);

        driverNameEditText = findViewById(R.id.driverNameEditText);
        driverPhoneNumberEditText = findViewById(R.id.driverPhoneNumberEditText);
        driverEmailEditText = findViewById(R.id.driverEmailEditText);
        driverPasswordEditText = findViewById(R.id.driverPasswordEditText);
        busNamesSpinner = findViewById(R.id.driverBusNameSpinner);
        driverSigninTextView = findViewById(R.id.driverSigninTextView);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseDriver = FirebaseDatabase.getInstance().getReference("Driver");
       //firebaseFireStore = FirebaseFirestore.getInstance();

        driverSigninTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.putExtra("userType", "driver");
                startActivity(intent);
            }
        });

    }

    public void addDriverDetailsToFirebase() {

        if(driverId!=null) {
            Driver driverDetails = new Driver(driverId, driverName, driverPhoneNumber, driverEmail, busName);
            databaseDriver.child(driverId).setValue(driverDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i("Info", "DDA");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(DriverRegistrationActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "There was an internal error", Toast.LENGTH_SHORT).show();
        }
    }

    public void driverSignup() {
            if (driverPassword.length() >= 6) {
                firebaseAuth.createUserWithEmailAndPassword(driverEmail, driverPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            driverId= firebaseUser.getUid();
                            addDriverDetailsToFirebase();
                            //emailVerification();
                            //finish();
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            intent.putExtra("userType", "driver");
                            startActivity(intent);
                        } else {
                            Toast.makeText(DriverRegistrationActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Please enter a password which has at least 6 characters", Toast.LENGTH_SHORT).show();
            }
    }
    public void emailVerification(){
        firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser!=null){
            firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(DriverRegistrationActivity.this, "Driver Registered. Verification Email has been sent.", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(DriverRegistrationActivity.this, "Failed To Register. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
