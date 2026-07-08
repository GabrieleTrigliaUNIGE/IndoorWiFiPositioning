package com.wifigroup.indoorwifipositioning;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wifigroup.indoorwifipositioning.AP.AccessPoint;
import com.wifigroup.indoorwifipositioning.BRs.WiFiReceiver;
import com.wifigroup.indoorwifipositioning.interfaces.IOnProcessingCompleted;
import com.wifigroup.indoorwifipositioning.interfaces.IWiFiScanCompleted;
import com.wifigroup.indoorwifipositioning.misc.CsvReader;
import com.wifigroup.indoorwifipositioning.processing.CsvDataProcessor;
import com.wifigroup.indoorwifipositioning.processing.TrilaterationEngine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DemoFragment extends Fragment implements IWiFiScanCompleted, IOnProcessingCompleted {

    private final String TAG = "DemoFragment";

    private TextView tvPolynomial = null;

    private TextView tvLog = null;

    private Button bttStartDemo = null;

    private WifiManager wifiManager = null;

    private WiFiReceiver wiFiReceiver = null;

    private Map<String, AccessPoint> roomMap = null;

    private Map<String, Integer> liveScanBuffer = new ConcurrentHashMap<>();

    private boolean isScanRequested = false;

    private Runnable calculationRunnable = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");

        return inflater.inflate(R.layout.demo_fragments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "Avvio demo");

        initViews(view);
        setupWiFiReceiver();
        setupStartButton();
        readCsv();
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

        if (calculationRunnable != null && bttStartDemo != null) {
            bttStartDemo.removeCallbacks(calculationRunnable);
        }

        try {
            if (wiFiReceiver != null) {
                requireActivity().unregisterReceiver(wiFiReceiver);
            }
        } catch (Exception e) {
            Log.i("WIFI_RECEIVER", "Il ricevitore era già scollegato, ignoro l'errore.");
        }
    }

    private void initViews(@NonNull View view) {
        tvPolynomial      = view.findViewById(R.id.tvPolynomial);
        tvLog             = view.findViewById(R.id.tvLog);
        bttStartDemo      = view.findViewById(R.id.bttStartDemo);
    }

    private void setupStartButton() {
        bttStartDemo.setEnabled(false);

        bttStartDemo.setOnClickListener(v -> {
            isScanRequested = true;
            liveScanBuffer.clear();
            wifiManager.startScan();

            tvLog.setText(getString(R.string.LogPH, getString(R.string.Scanning)));
            tvPolynomial.setText(getString(R.string.PolynomialPH, getString(R.string.Scanning)));
            Toast.makeText(getContext(), "Wi-Fi scanning started...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupWiFiReceiver() {
        wifiManager = (WifiManager) requireActivity().getSystemService(Context.WIFI_SERVICE);
        wiFiReceiver = new WiFiReceiver(wifiManager, this);
        requireActivity().registerReceiver(
                wiFiReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private void readCsv(){
        List<String> dataRaw = CsvReader.readCsvFromAssets(requireContext(), "MISURE_AP_TOT.csv");

        if (!dataRaw.isEmpty()) {

            Toast.makeText(getContext(), "Data loaded! Starting mean computing...", Toast.LENGTH_SHORT).show();

            CsvDataProcessor meanProcessor = new CsvDataProcessor(dataRaw, this);
            meanProcessor.start();

        } else {
            Toast.makeText(getContext(), "ERROR: CSV loading failed!", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Lista dati vuota. Controlla che il file sia nella cartella assets e il nome sia corretto.");
        }

    }

    @Override
    public void onWifiScanCompleted(String ssid, int dBm) {
        if(!isAdded() || getContext() == null) {
            return;
        }

        if (roomMap == null) {
            Log.i(TAG, "Scansione ricevuta");
            return;
        }

        if (!isScanRequested) return;

        if (dBm == -999 || dBm == -998) return;

        if (roomMap.containsKey(ssid)) {
            liveScanBuffer.put(ssid, dBm);
            Log.i(TAG, "Ricevuto live: " + ssid + " -> " + dBm + " dBm");
        }

        calculationRunnable = () -> {

            isScanRequested = false;

            if (liveScanBuffer.size() >= 3) {

                double[] posLog = TrilaterationEngine.calculatePosition(liveScanBuffer, roomMap, true);
                double[] posPoly = TrilaterationEngine.calculatePosition(liveScanBuffer, roomMap, false);

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (posLog != null) {
                            tvLog.setText(getString(R.string.LogResults, posLog[0], posLog[1]));
                        } else {
                            tvLog.setText(getString(R.string.LogErr));
                        }

                        if (posPoly != null) {
                            tvPolynomial.setText(getString(R.string.PolyResults, posPoly[0], posPoly[1]));
                        } else {
                            tvPolynomial.setText(getString(R.string.PolyErr));
                        }
                    });
                }
            } else {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvLog.setText(getString(R.string.LogAPLow, liveScanBuffer.size()));
                        tvPolynomial.setText(getString(R.string.PolyAPLow, liveScanBuffer.size()));
                    });
                }
            }
        };

        bttStartDemo.postDelayed(calculationRunnable, 250);
    }

    @Override
    public void onProcessingDone(Map<String, AccessPoint> calibratedAps) {

        // TODO: UTILIZZARE LA FUNZIONE READCSV PER LEGGERE IL FILE DELLE COORDINATE?
        List<String> coordinateLines = CsvReader.readCsvFromAssets(requireContext(), "AP_COORDINATES.csv");

        for (String line : coordinateLines) {
            String[] parts = line.split(",");
            if (parts.length == 3) {
                String ssid = parts[0];
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);

                AccessPoint ap = calibratedAps.get(ssid);

                if (ap != null) {
                    ap.x = x;
                    ap.y = y;
                    Log.i(TAG, "Configurato " + ssid + " alla posizione X: " + x + ", Y: " + y);
                }
            }
        }

        this.roomMap = calibratedAps;

        if(isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Calculation completed", Toast.LENGTH_LONG).show();
                bttStartDemo.setEnabled(true);
                Log.i(TAG, "Tabelle salvate in memoria");
            });
        }
    }
}
