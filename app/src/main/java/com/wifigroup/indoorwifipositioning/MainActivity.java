package com.wifigroup.indoorwifipositioning;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private Button bttTraining;
    private Button bttDemo;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.i(TAG, "Permesso di Localizzazione concesso dall'utente.");
                } else {
                    Toast.makeText(this, "WARNING: GPS permission not granted", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        checkLocationPermission();

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, new DemoFragment())
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

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.i(TAG, "Richiesta permessi in corso...");
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        } else {
            Log.i(TAG, "Permessi già garantiti in precedenza.");
        }
    }
}