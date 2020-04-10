package com.example.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText loginEmailEditText;
    EditText loginPasswordEditText;
    String loginEmail;
    String loginPassword;
    String userType;

    FirebaseAuth firebaseAuth;

    TextView registerTextView;
    ImageView busImageView;
    TextView fadingText;

    public void login(View view){
        loginEmail = loginEmailEditText.getText().toString().trim();
        loginPassword = loginPasswordEditText.getText().toString().trim();
        if(!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword)){
            signUserIn();
        } else {
            Toast.makeText(this, "Please enter all the credentials", Toast.LENGTH_SHORT).show();
        }
    }


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_login);

        loginEmailEditText = findViewById(R.id.loginEmailEditText);
        loginPasswordEditText = findViewById(R.id.loginPasswordEditText);
        registerTextView = findViewById(R.id.registerTextView);
        busImageView = findViewById(R.id.busImageView);
        fadingText = findViewById(R.id.fadingText);


        firebaseAuth = FirebaseAuth.getInstance();

        Intent userTypeIntent = getIntent();
        userType = userTypeIntent.getStringExtra("userType");
        if (userType != null) {
            Log.i("UserType", userType);
        }

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userType != null) {
                    if(userType.equals("driver")) {
                        Intent intent = new Intent(getApplicationContext(), DriverRegistrationActivity.class);
                        startActivity(intent);
                    } else if (userType.equals("student")){
                        Intent intent = new Intent(getApplicationContext(), StudentRegistrationActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });

    }

    public void signUserIn(){
        firebaseAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    if(userType!=null) {
                        if (userType.equals("student")) {
                            Toast.makeText(LoginActivity.this, "Logged in as Student", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), StudentLocationActivity.class);
                            startActivity(intent);
                        } else if (userType.equals("driver")) {
                            Toast.makeText(LoginActivity.this, "Logged in as Driver", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), DriverLocationActivity.class);
                            startActivity(intent);
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    boolean hasAnimationStarted;

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !hasAnimationStarted) {
            hasAnimationStarted=true;
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            ObjectAnimator translationY = ObjectAnimator.ofFloat(busImageView, "x", metrics.widthPixels / 2 - busImageView.getWidth()/ 2); // metrics.heightPixels or root.getHeight()
            translationY.setDuration(1800);
            translationY.start();
        }
    }

}
