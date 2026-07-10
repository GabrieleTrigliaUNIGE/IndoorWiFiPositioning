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

/**
 * The main entry point of the application.
 * <p>
 * This activity serves as the host container for the application's fragments.
 * It manages the navigation between the {@link TrainingFragment} (for data collection)
 * and the {@link DemoFragment} (for real-time positioning demonstration) using
 * button interactions to replace the displayed fragment.
 * </p>
 *
 * @author WiFiGroup
 * @version 1.0.0
 */
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

    /**
     * {@inheritDoc}
     * <p>
     * Initializes the layout, sets up the initial fragment (TrainingFragment),
     * and configures the click listeners for switching between the application's modes.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in {@link #onSaveInstanceState}. <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        checkLocationPermission();

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

    /**
     * Initializes the activity's UI components by finding views by their IDs.
     */
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