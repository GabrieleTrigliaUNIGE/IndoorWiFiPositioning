package com.wifigroup.indoorwifipositioning;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private Button bttTraining;
    private Button bttDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, new TrainingFragment())
                    .commit();
        }

        bttTraining.setOnClickListener( v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new TrainingFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void initViews() {
        bttTraining = findViewById(R.id.bttTraining);
        bttDemo = findViewById(R.id.bttDemo);
    }
}