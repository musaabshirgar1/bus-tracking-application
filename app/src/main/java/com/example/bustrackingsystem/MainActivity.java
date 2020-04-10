package com.example.bustrackingsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    public void driver(View view){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.putExtra("userType", "driver");
            startActivity(intent);
    }
    public void student(View view){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.putExtra("userType", "student");
            startActivity(intent);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
    }
}
