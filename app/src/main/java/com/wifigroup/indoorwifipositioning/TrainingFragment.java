package com.wifigroup.indoorwifipositioning;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wifigroup.indoorwifipositioning.BRs.WiFiReceiver;
import com.wifigroup.indoorwifipositioning.hardware.HardwareHandler;
import com.wifigroup.indoorwifipositioning.interfaces.ICsvExportCompleted;
import com.wifigroup.indoorwifipositioning.interfaces.IWiFiScanCompleted;
import com.wifigroup.indoorwifipositioning.misc.CsvExporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the training phase of the indoor positioning system.
 * <p>
 * This fragment handles the collection of RSSI measurements at predefined
 * distances from known Access Points. It provides a user interface to select
 * the target AP and distance, tracks the number of required scans, and exports
 * the collected data to a CSV file for calibration purposes.
 * </p>
 *
 * @author WiFiGroup
 * @version 1.0.0
 */
public class TrainingFragment extends Fragment implements IWiFiScanCompleted, ICsvExportCompleted {

    private final String TAG = "TrainingFragment";
    private static final LinkedHashMap<Integer, Integer> REQUIRED = new LinkedHashMap<>();

    static {
        REQUIRED.put(1, 3);
        REQUIRED.put(2, 4);
        REQUIRED.put(3, 6);
        REQUIRED.put(4, 8);
        REQUIRED.put(5, 10);
        REQUIRED.put(6, 12);
        REQUIRED.put(7, 15);
    }

    private static final String[] ACCESS_POINTS = {
            "AP1", "AP2", "AP3", "AP4"
    };

    // ── UI ────────────────────────────────────────────────────────────────────
    private Spinner spinnerAP = null;
    private Spinner spinnerDistance = null;
    private TextView tvCurrentAP = null;
    private TextView tvCurrentDistance = null;
    private TextView tvMeasureCount = null;
    private Button bttStartScan = null;
    private Button bttExportCSV = null;

    private WifiManager wifiManager   = null;
    private WiFiReceiver wiFiReceiver = null;
    private boolean onlyOneScan = false;

    private final Map<String, Map<Integer, List<Integer>>> measureData = new HashMap<>();

    /**
     * {@inheritDoc}
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
    }

    /**
     * {@inheritDoc}
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to. The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        return inflater.inflate(R.layout.training_fragments, container, false);
    }

    /**
     * {@inheritDoc}
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "Avvio training");

        initViews(view);
        setupWiFiReceiver();
        setupSpinners();
        setupButtons();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {
            if (wiFiReceiver != null) {
                requireActivity().unregisterReceiver(wiFiReceiver);
            }
        } catch (Exception e) {
            Log.i("WIFI_RECEIVER", "Receiver already disconnected, ignoring error.");
        }
    }

    /**
     * Initializes the fragment's UI components by finding views by their IDs.
     *
     * @param view the fragment's root view
     */
    private void initViews(View view) {
        spinnerAP         = view.findViewById(R.id.spinnerAP);
        spinnerDistance   = view.findViewById(R.id.spinnerDistance);
        tvCurrentAP       = view.findViewById(R.id.tvCurrentAP);
        tvCurrentDistance = view.findViewById(R.id.tvCurrentDistance);
        tvMeasureCount    = view.findViewById(R.id.tvMeasureCount);
        bttStartScan      = view.findViewById(R.id.bttStartScan);
        bttExportCSV      = view.findViewById(R.id.bttExportCSV);
    }

    /**
     * Configures the spinners for Access Point and distance selection,
     * including their adapters and selection change listeners.
     */
    private void setupSpinners() {

        ArrayAdapter<String> apAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                ACCESS_POINTS);
        apAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAP.setAdapter(apAdapter);

        List<String> distLabels = new ArrayList<>();
        for (int d : REQUIRED.keySet()) distLabels.add(d + " m");

        ArrayAdapter<String> distAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                distLabels);
        distAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistance.setAdapter(distAdapter);

        AdapterView.OnItemSelectedListener onChange = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                refreshUI();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        };
        spinnerAP.setOnItemSelectedListener(onChange);
        spinnerDistance.setOnItemSelectedListener(onChange);

        refreshUI();
    }

    /**
     * Configures the click listeners for the scanning and CSV export buttons.
     */
    private void setupButtons() {

        bttStartScan.setOnClickListener((v) -> {

            if (!HardwareHandler.isHardwareReady(requireContext(), wifiManager)) {
                return;
            }

            // Dice al receiver quale SSID cercare
            wiFiReceiver.setTargetSSID(getSelectedAP());

            onlyOneScan = true;
            wifiManager.startScan();
            Log.i(TAG, "Scan started for: " + getSelectedAP());
            tvMeasureCount.setText(R.string.Scanning);
        });

        bttExportCSV.setOnClickListener((v) -> {
            // Disabilitiamo il bottone per evitare salvataggi multipli
            bttExportCSV.setEnabled(false);
            Toast.makeText(getContext(), "Exporting...", Toast.LENGTH_SHORT).show();
            new CsvExporter(
                    ACCESS_POINTS,
                    REQUIRED,
                    measureData,
                    this
            ).start();
        });
    }

    /**
     * Initializes and registers the Wi-Fi broadcast receiver to listen for scan results.
     */
    private void setupWiFiReceiver(){
        wifiManager = (WifiManager) requireActivity()
                .getSystemService(Context.WIFI_SERVICE);
        wiFiReceiver = new WiFiReceiver(wifiManager, this);
        requireActivity().registerReceiver(
                wiFiReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }


    private void refreshUI() {
        String ap       = getSelectedAP();
        int    distance = getSelectedDistance();
        int    done     = getMeasureCount(ap, distance);
        int    required = REQUIRED.get(distance);
        boolean completo = done >= required;

        tvCurrentAP.setText(getString(R.string.AccessPointPH, ap));
        tvCurrentDistance.setText(getString(R.string.DistancePH, distance));
        tvMeasureCount.setText(getString(R.string.MeasuresPH,done,required));

        bttStartScan.setEnabled(!completo);
        bttStartScan.setText(completo ? "✓ Completed" : "Start scanning");
    }

    private String getSelectedAP() {
        return ACCESS_POINTS[spinnerAP.getSelectedItemPosition()];
    }

    private int getSelectedDistance() {
        int pos = spinnerDistance.getSelectedItemPosition();
        return new ArrayList<>(REQUIRED.keySet()).get(pos);
    }

    private int getMeasureCount(String ap, int distance) {
        Map<Integer, List<Integer>> byDist = measureData.get(ap);
        if (byDist == null) return 0;
        List<Integer> list = byDist.get(distance);
        return list == null ? 0 : list.size();
    }

    /**
     * {@inheritDoc}
     *
     * @param ssid AP's SSID
     * @param dBm AP's RSSI (Received Signal Strength Indicator)
     */
    @Override
    public void onWifiScanCompleted(String ssid, int dBm) {

        if (!onlyOneScan) {
            Toast.makeText(getContext(),
                    "Automatic Android Scan",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (dBm == -998) {
            Toast.makeText(getContext(),
                    "Old scan (cache)\n retry",
                    Toast.LENGTH_LONG).show();
            refreshUI();
            return;
        }

        onlyOneScan = false;

        if (dBm == -999) {
            Toast.makeText(getContext(),
                    "\"" + ssid + "\" not found in scan",
                    Toast.LENGTH_SHORT).show();
            refreshUI();
            return;
        }

        String ap       = getSelectedAP();
        int    distance = getSelectedDistance();
        int    required = REQUIRED.get(distance);
        int    done     = getMeasureCount(ap, distance);

        if (done >= required) {
            Toast.makeText(getContext(),
                    "Measurements already completed for this distance!",
                    Toast.LENGTH_SHORT).show();
            refreshUI();
            return;
        }

        measureData
                .computeIfAbsent(ap,       k -> new HashMap<>())
                .computeIfAbsent(distance, k -> new ArrayList<>())
                .add(dBm);

        Log.i(TAG, "Saved → AP=" + ap + " dist=" + distance + "m dBm=" + dBm);
        Toast.makeText(getContext(), "✓ dBm: " + dBm, Toast.LENGTH_SHORT).show();

        refreshUI();
    }
    @Override
    public void onExportDone(boolean isSuccess, String message) {
        if(isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (isSuccess) {
                    Toast.makeText(getContext(), "CSV saved in Download:\n" + message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "Saving error: " + message, Toast.LENGTH_LONG).show();
                }
                Log.i(TAG, "CSV savato in Download:\n" + message);
                bttExportCSV.setEnabled(true);
            });
        }
    }
}