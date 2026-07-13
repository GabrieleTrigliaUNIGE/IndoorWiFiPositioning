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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.wifigroup.indoorwifipositioning.AP.AccessPoint;
import com.wifigroup.indoorwifipositioning.BRs.WiFiReceiver;
import com.wifigroup.indoorwifipositioning.constants.AppConstants;
import com.wifigroup.indoorwifipositioning.graphics.GraphManager;
import com.wifigroup.indoorwifipositioning.hardware.HardwareHandler;
import com.wifigroup.indoorwifipositioning.interfaces.ICsvReadCompleted;
import com.wifigroup.indoorwifipositioning.interfaces.IOnProcessingCompleted;
import com.wifigroup.indoorwifipositioning.interfaces.IWiFiScanCompleted;
import com.wifigroup.indoorwifipositioning.misc.CsvReader;
import com.wifigroup.indoorwifipositioning.processing.CsvDataProcessor;
import com.wifigroup.indoorwifipositioning.processing.TrilaterationEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DemoFragment extends Fragment implements IWiFiScanCompleted, IOnProcessingCompleted, ICsvReadCompleted {

    private final String TAG = "DemoFragment";

    private TextView tvPolynomial = null;
    private TextView tvLog = null;
    private Button bttStartDemo = null;
    private WifiManager wifiManager = null;
    private WiFiReceiver wiFiReceiver = null;
    private GraphManager graphManager = null;
    private Map<String, AccessPoint> roomMap = null;
    private Map<String, AccessPoint> tempCalibratedAps = null;

    private final Map<String, List<Integer>> liveScanBuffer = new ConcurrentHashMap<>();

    private boolean isScanRequested = false;
    private int scanCount = 0;
    private static final int MAX_SCANS = 5;
    private Runnable scanLoopRunnable = null;


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

        if (scanLoopRunnable != null && bttStartDemo != null) {
            bttStartDemo.removeCallbacks(scanLoopRunnable);
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

        bttStartDemo.setEnabled(false);

        GraphView graphMap = view.findViewById(R.id.graphMap);
        graphManager = new GraphManager(graphMap);
    }

    private void setupStartButton() {
        bttStartDemo.setOnClickListener(v -> {

            if (!HardwareHandler.isHardwareReady(requireContext(), wifiManager)) {
                return;
            }

            bttStartDemo.setEnabled(false);
            isScanRequested = true;
            scanCount = 0;
            liveScanBuffer.clear();
            wifiManager.startScan();

            // Il Cronometro: esegue una scansione ogni 1,2 secondi
            scanLoopRunnable = new Runnable() {
                @Override
                public void run() {
                    if (scanCount < MAX_SCANS) {
                        scanCount++;

                        tvLog.setText(getString(R.string.PollScan, scanCount, MAX_SCANS));
                        tvPolynomial.setText(getString(R.string.KeepStill));

                        wifiManager.startScan();

                        bttStartDemo.postDelayed(this, 3000);
                        Log.i(TAG, "Scansione numero: " + scanCount + "fresca:" + wifiManager.isWifiEnabled());
                    } else {
                        // Abbiamo finito le scansioni, elaboriamo i dati
                        isScanRequested = false;
                        processBufferedScans();
                    }
                }
            };

            scanLoopRunnable.run();
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
        new CsvReader(requireContext(), AppConstants.CSV_MEASUREMENTS_FILE, this).start();
    }

    private void processBufferedScans() {
        if (!isAdded() || getContext() == null) return;

        Map<String, Integer> stableRssiMap = new HashMap<>();

        // 1. Puliamo i dati delegando il calcolo al TrilaterationEngine
        for (Map.Entry<String, List<Integer>> entry : liveScanBuffer.entrySet()) {
            String ssid = entry.getKey();
            List<Integer> allReadings = entry.getValue();

            // CHIAMATA AL MOTORE MATEMATICO
            int stableValue = TrilaterationEngine.getStableRssi(allReadings);
            stableRssiMap.put(ssid, stableValue);

            Log.i(TAG, "AP: " + ssid + " | Letture: " + allReadings.toString() + " | Media Pulita: " + stableValue);
        }

        if (stableRssiMap.size() >= 3) {

            double[] posLog = TrilaterationEngine.calculatePosition(stableRssiMap, roomMap, true);
            double[] posPoly = TrilaterationEngine.calculatePosition(stableRssiMap, roomMap, false);

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

            graphManager.updatePositions(posLog, posPoly);

        } else {

            tvLog.setText(getString(R.string.LogAPLow, liveScanBuffer.size()));
            tvPolynomial.setText(getString(R.string.PolyAPLow, liveScanBuffer.size()));

            graphManager.updatePositions(null, null);

        }

        bttStartDemo.setEnabled(true);
        Toast.makeText(getContext(), "DONE!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWifiScanCompleted(String ssid, int dBm) {

        if (roomMap == null) {
            Log.i(TAG, "Scansione ricevuta");
            return;
        }

        if (!isScanRequested) return;

        if (dBm == -999 || dBm == -998) return;

        if (roomMap.containsKey(ssid)) {
            List<Integer> rssiList = liveScanBuffer.get(ssid);
            if (rssiList == null) {
                rssiList = new ArrayList<>();
                liveScanBuffer.put(ssid, rssiList);
            }
            rssiList.add(dBm);
        }
    }

    @Override
    public void onProcessingDone(Map<String, AccessPoint> calibratedAps) {

        this.tempCalibratedAps = calibratedAps;

        Log.i(TAG, "Modelli matematici calcolati. Avvio lettura coordinate...");
        new CsvReader(requireContext(), AppConstants.CSV_COORDINATES_FILE, this).start();
    }

    @Override
    public void onCsvReadDone(List<String> dataRaw, String fileName) {

        if (dataRaw.isEmpty()) {
            if(isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "ERROR: empty CSV or not found (" + fileName + ")", Toast.LENGTH_LONG).show()
                );
            }
            Log.i(TAG, "Lista dati vuota. Controlla che il file sia nella cartella assets e il nome sia corretto.");
            return;
        }

        // Abbiamo letto il file delle misure iniziali
        if (fileName.equals(AppConstants.CSV_MEASUREMENTS_FILE)) {
            if(isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Data loaded! Starting mean computing...", Toast.LENGTH_SHORT).show()
                );
            }
            CsvDataProcessor meanProcessor = new CsvDataProcessor(dataRaw, this);
            meanProcessor.start();

        }
        // Abbiamo letto il file delle coordinate dopo OnProcessingDone
        else if (fileName.equals(AppConstants.CSV_COORDINATES_FILE)) {

            DataPoint[] apDataPoints = new DataPoint[dataRaw.size()];
            int count = 0;
            double maxX = 9.0;
            double maxY = 10.0;

            // Uniamo le coordinate lette con i modelli matematici parcheggiati
            for (String line : dataRaw) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    try {
                        String ssid = parts[0];
                        double x = Double.parseDouble(parts[1]);
                        double y = Double.parseDouble(parts[2]);

                        if (tempCalibratedAps != null) {
                            AccessPoint ap = tempCalibratedAps.get(ssid);

                            if (ap != null) {
                                ap.x = x;
                                ap.y = y;
                                Log.i(TAG, "Configurato " + ssid + " alla posizione X: " + x + ", Y: " + y);

                                apDataPoints[count] = new DataPoint(x, y);
                                count++;
                            }
                        }
                    } catch (Exception e) {
                        Log.i(TAG, "Errore nella lettura dei numeri");
                    }
                }
            }

            // Salvataggio finale nella mappa ufficiale della stanza
            this.roomMap = tempCalibratedAps;

            final int finalCount = count;

            if(isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {

                    DataPoint[] validPoints = new DataPoint[finalCount];
                    System.arraycopy(apDataPoints, 0, validPoints, 0, finalCount);

                    java.util.Arrays.sort(validPoints, (p1, p2) -> Double.compare(p1.getX(), p2.getX()));
                    Log.i(TAG, "Array ordinato");

                    graphManager.drawRoomAndAPs(validPoints, maxX, maxY);
                    Log.i(TAG, "Mappa aggiornata");

                    Toast.makeText(getContext(), "Calculation completed", Toast.LENGTH_LONG).show();
                    bttStartDemo.postDelayed(() -> {
                        bttStartDemo.setEnabled(true);
                        Log.i(TAG, "Tabelle salvate in memoria");
                    }, 3000);
                });
            }
        }
    }
}
