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

public class StudentRegistrationActivity extends AppCompatActivity {

    EditText studentNameEditText;
    EditText studentRollNumberEditText;
    EditText studentEmailEditText;
    EditText studentPasswordEditText;
    Spinner studentBranchSpinner;
    Spinner busNamesSpinner;
    String studentName;
    String studentRollNumber;
    String studentEmail;
    String studentPassword;
    String studentBranch;
    String studentBusRoute;
    String studentId;
    TextView studentSigninTextView;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseStudent;
    FirebaseUser firebaseUser;

    public void registerAsStudent(View view){
        studentName = studentNameEditText.getText().toString();
        studentRollNumber = studentRollNumberEditText.getText().toString();
        studentEmail = studentEmailEditText.getText().toString();
        studentPassword = studentPasswordEditText.getText().toString();
        studentBranch = studentBranchSpinner.getSelectedItem().toString();
        studentBusRoute = busNamesSpinner.getSelectedItem().toString();

        if(!TextUtils.isEmpty(studentName) && !TextUtils.isEmpty(studentRollNumber) && !TextUtils.isEmpty(studentEmail) && !TextUtils.isEmpty(studentPassword)){
            studentSignup();
        } else {
            Toast.makeText(this, "Please enter all the credentials", Toast.LENGTH_SHORT).show();
        }

    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_student_registration);

        studentNameEditText = findViewById(R.id.studentNameEditText);
        studentRollNumberEditText = findViewById(R.id.studentRollNumberEditText);
        studentEmailEditText = findViewById(R.id.studentEmailEditText);
        studentPasswordEditText = findViewById(R.id.studentPasswordEditText);
        studentBranchSpinner = findViewById(R.id.studentBranchSpinner);
        busNamesSpinner = findViewById(R.id.busNamesSpinner);
        studentSigninTextView = findViewById(R.id.studentSigninTextView);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseStudent = FirebaseDatabase.getInstance().getReference("Student");

        studentSigninTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.putExtra("userType", "student");
                startActivity(intent);
            }
        });

    }

    public void addStudentDetailsToFirebase(){
        if(studentId!=null){
            Student studentDetails = new Student(studentId, studentName, studentRollNumber, studentEmail, studentBranch, studentBusRoute);
            databaseStudent.child(studentId).setValue(studentDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i("Info", "SDA");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(StudentRegistrationActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(this, "There was an internal error", Toast.LENGTH_SHORT).show();
        }
    }

    public void studentSignup(){
        if(studentPassword.length() >=6){
            firebaseAuth.createUserWithEmailAndPassword(studentEmail, studentPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            studentId = firebaseUser.getUid();
                            addStudentDetailsToFirebase();
                            //emailVerification();
                            finish();
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            intent.putExtra("userType", "student");
                            startActivity(intent);
                        } else {
                            Toast.makeText(StudentRegistrationActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                }
            });
        } else {
            Toast.makeText(this, "Password must at least be of 6 characters", Toast.LENGTH_SHORT).show();
        }
    }
    public void emailVerification(){
        firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser!=null){
            firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(StudentRegistrationActivity.this, "Student Registered. Verification Email has been sent.", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(StudentRegistrationActivity.this, "Failed To Register. Please try again later.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
