package com.wifigroup.indoorwifipositioning;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

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
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (!(currentFragment instanceof TrainingFragment)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new TrainingFragment())
                        .commit();
                Log.i(TAG, "Cliccato Training");
            }
        });

        bttDemo.setOnClickListener( v -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (!(currentFragment instanceof DemoFragment)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new DemoFragment())
                        .commit();

                Log.i(TAG, "Cliccato Demo");
            }
        });
    }

    private void initViews() {
        bttTraining = findViewById(R.id.bttTraining);
        bttDemo = findViewById(R.id.bttDemo);
    }
}