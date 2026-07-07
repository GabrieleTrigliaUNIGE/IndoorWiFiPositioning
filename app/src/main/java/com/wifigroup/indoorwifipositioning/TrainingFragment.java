package com.wifigroup.indoorwifipositioning;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
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
import com.wifigroup.indoorwifipositioning.interfaces.IWiFiScanCompleted;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TrainingFragment extends Fragment implements IWiFiScanCompleted {

    private final String TAG = "TrainingFragment";

    // ── Number requested measures per distance ──────────────────────────────────
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

    // ── I 4 Access Point (sostituisci con i tuoi SSID reali) ─────────────────
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

    // ── WiFi ──────────────────────────────────────────────────────────────────
    private WifiManager wifiManager   = null;
    private WiFiReceiver wiFiReceiver = null;
    private boolean onlyOneScan = false;

    // ── Dati: AP → (distance → list dBm) ────────────────────────────────────
    private final Map<String, Map<Integer, List<Integer>>> measureData = new HashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");

        return inflater.inflate(R.layout.training_fragments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "Avvio training");

        initViews(view);
        setupWiFiReceiver();
        setupSpinners();
        setupButtons();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

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

    private void initViews(View view) {
        spinnerAP         = view.findViewById(R.id.spinnerAP);
        spinnerDistance   = view.findViewById(R.id.spinnerDistance);
        tvCurrentAP       = view.findViewById(R.id.tvCurrentAP);
        tvCurrentDistance = view.findViewById(R.id.tvCurrentDistance);
        tvMeasureCount    = view.findViewById(R.id.tvMeasureCount);
        bttStartScan      = view.findViewById(R.id.bttStartScan);
        bttExportCSV      = view.findViewById(R.id.bttExportCSV);
    }

    private void setupSpinners() {

        // Spinner AP
        ArrayAdapter<String> apAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                ACCESS_POINTS);
        apAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAP.setAdapter(apAdapter);

        // Spinner distanze  →  "1 m", "2 m", …
        List<String> distLabels = new ArrayList<>();
        for (int d : REQUIRED.keySet()) distLabels.add(d + " m");

        ArrayAdapter<String> distAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                distLabels);
        distAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistance.setAdapter(distAdapter);

        // Aggiorna le TextView ogni volta che l'utente cambia selezione
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

    private void setupButtons() {

        bttStartScan.setOnClickListener((v) -> {
            if (!wifiManager.isWifiEnabled()) {
                Toast.makeText(getContext(),
                        "WiFi OFF", Toast.LENGTH_LONG).show();
                wifiManager.setWifiEnabled(true);
            }

            // Dice al receiver quale SSID cercare
            wiFiReceiver.setTargetSSID(getSelectedAP());

            onlyOneScan = true;
            wifiManager.startScan();
            Log.i(TAG, "Scan started for: " + getSelectedAP());
            tvMeasureCount.setText("Scanning...");
        });

        bttExportCSV.setOnClickListener((v) -> {
            com.wifigroup.indoorwifipositioning.misc.CsvExporter.exportToDownloads(
                    requireContext(),
                    ACCESS_POINTS,
                    REQUIRED,
                    measureData
            );
        });
    }

    private void setupWiFiReceiver(){
        wifiManager = (WifiManager) requireActivity()
                .getSystemService(Context.WIFI_SERVICE);
        wiFiReceiver = new WiFiReceiver(wifiManager, this);
        requireActivity().registerReceiver(
                wiFiReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }


    @Override
    public void onWifiScanCompleted(String ssid, int dBm) {

        // Scansione automatica di Android/altre app: la scartiamo
        if (!onlyOneScan) {
            Toast.makeText(getContext(),
                    "Automatic Android Scan",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Risultato dalla cache — non salviamo nulla
        if (dBm == -998) {
            Toast.makeText(getContext(),
                    "Old scan (cache)\n retry",
                    Toast.LENGTH_LONG).show();
            refreshUI();
            return;
        }

        onlyOneScan = false;

        // AP non trovato nella scansione fresca
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

        // Solo qui salviamo — scansione fresca + AP trovato + misure non complete
        measureData
                .computeIfAbsent(ap,       k -> new HashMap<>())
                .computeIfAbsent(distance, k -> new ArrayList<>())
                .add(dBm);

        Log.i(TAG, "Saved → AP=" + ap + " dist=" + distance + "m dBm=" + dBm);
        Toast.makeText(getContext(), "✓ dBm: " + dBm, Toast.LENGTH_SHORT).show();

        refreshUI();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI
    // ─────────────────────────────────────────────────────────────────────────

    private void refreshUI() {
        String ap       = getSelectedAP();
        int    distance = getSelectedDistance();
        int    done     = getMeasureCount(ap, distance);
        int    required = REQUIRED.get(distance);
        boolean completo = done >= required;

        tvCurrentAP.setText("Access Point: " + ap);
        tvCurrentDistance.setText("Distance: " + distance + " m");
        tvMeasureCount.setText("Measures: " + done + " / " + required);

        bttStartScan.setEnabled(!completo);
        bttStartScan.setText(completo ? "✓ Completed" : "Start scanning");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private String getSelectedAP() {
        return ACCESS_POINTS[spinnerAP.getSelectedItemPosition()];
    }

    private int getSelectedDistance() {
        int pos = spinnerDistance.getSelectedItemPosition();
        return new ArrayList<>(REQUIRED.keySet()).get(pos);
    }

    private int getMeasureCount(String ap, int distanza) {
        Map<Integer, List<Integer>> byDist = measureData.get(ap);
        if (byDist == null) return 0;
        List<Integer> list = byDist.get(distanza);
        return list == null ? 0 : list.size();
    }

}
