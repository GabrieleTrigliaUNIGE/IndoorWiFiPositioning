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
import com.wifigroup.indoorwifipositioning.graphics.GraphManager;
import com.wifigroup.indoorwifipositioning.hardware.HardwareHandler;
import com.wifigroup.indoorwifipositioning.interfaces.ICsvReadCompleted;
import com.wifigroup.indoorwifipositioning.interfaces.IOnProcessingCompleted;
import com.wifigroup.indoorwifipositioning.interfaces.IWiFiScanCompleted;
import com.wifigroup.indoorwifipositioning.misc.CsvReader;
import com.wifigroup.indoorwifipositioning.processing.CsvDataProcessor;
import com.wifigroup.indoorwifipositioning.processing.TrilaterationEngine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the main user interface for the indoor Wi-Fi positioning demonstration.
 * <p>
 * This fragment manages the UI components, handles real-time Wi-Fi scanning operations,
 * reads calibration data from CSV assets, and orchestrates the trilateration engine
 * to compute the device's coordinates based on RSSI measurements.
 * </p>
 *
 * @author WiFiGroup
 * @version 1.0.0
 */
public class DemoFragment extends Fragment implements IWiFiScanCompleted, IOnProcessingCompleted, ICsvReadCompleted {


    private final String TAG = "DemoFragment";

    private TextView tvPolynomial = null;

    private TextView tvLog = null;

    private Button bttStartDemo = null;

    private WifiManager wifiManager = null;

    private WiFiReceiver wiFiReceiver = null;

    private Map<String, AccessPoint> roomMap = null;

    private Map<String, Integer> liveScanBuffer = new ConcurrentHashMap<>();

    private Map<String, AccessPoint> tempCalibratedAps = null;

    private boolean isScanRequested = false;

    private Runnable calculationRunnable = null;

    private GraphManager graphManager = null;


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
     * @return the fragment's view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        return inflater.inflate(R.layout.demo_fragments, container, false);
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
        Log.i(TAG, "Avvio demo");

        initViews(view);
        setupWiFiReceiver();
        setupStartButton();
        readCsv();
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

    /**
     * Initializes the fragment's UI components.
     * * @param view the fragment's root view
     */
    private void initViews(@NonNull View view) {
        tvPolynomial      = view.findViewById(R.id.tvPolynomial);
        tvLog             = view.findViewById(R.id.tvLog);
        bttStartDemo      = view.findViewById(R.id.bttStartDemo);

        bttStartDemo.setEnabled(false);

        GraphView graphMap = view.findViewById(R.id.graphMap);
        graphManager = new GraphManager(graphMap);
    }

    /**
     * Sets up the start button listener and its initial state.
     */
    private void setupStartButton() {
        bttStartDemo.setOnClickListener(v -> {

            if (!HardwareHandler.isHardwareReady(requireContext(), wifiManager)) {
                return;
            }

            bttStartDemo.setEnabled(false);
            isScanRequested = true;
            liveScanBuffer.clear();
            wifiManager.startScan();

            tvLog.setText(getString(R.string.LogPH, getString(R.string.Scanning)));
            tvPolynomial.setText(getString(R.string.PolynomialPH, getString(R.string.Scanning)));
            Toast.makeText(getContext(), "Wi-Fi scanning started...", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Initializes and registers the Wi-Fi broadcast receiver to listen for scan results.
     */
    private void setupWiFiReceiver() {
        wifiManager = (WifiManager) requireActivity().getSystemService(Context.WIFI_SERVICE);
        wiFiReceiver = new WiFiReceiver(wifiManager, this);
        requireActivity().registerReceiver(
                wiFiReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    /**
     * Reads the Access Point calibration data from the CSV file located in the assets folder.
     */
    private void readCsv(){
        new CsvReader(requireContext(), "MISURE_AP_TOT.csv", this).start();
    }

    /**
     * {@inheritDoc}
     *
     * @param ssid AP's SSID
     * @param dBm AP's RSSI
     */
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

        // Cancella il calcolo in coda se arriva un nuovo Access Point
        if (calculationRunnable != null && bttStartDemo != null) {
            bttStartDemo.removeCallbacks(calculationRunnable);
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

                        graphManager.updatePositions(posLog, posPoly);

                        bttStartDemo.setEnabled(true);
                    });
                }
            } else {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvLog.setText(getString(R.string.LogAPLow, liveScanBuffer.size()));
                        tvPolynomial.setText(getString(R.string.PolyAPLow, liveScanBuffer.size()));

                        bttStartDemo.setEnabled(true);
                    });
                }
            }
        };

        bttStartDemo.postDelayed(calculationRunnable, 250);
    }

    /**
     * {@inheritDoc}
     * * @param calibratedAps the map of the APs configured with their physical coordinates
     */
    @Override
    public void onProcessingDone(Map<String, AccessPoint> calibratedAps) {

        this.tempCalibratedAps = calibratedAps;

        Log.i(TAG, "Modelli matematici calcolati. Avvio lettura coordinate...");
        new CsvReader(requireContext(), "AP_COORDINATES.csv", this).start();
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
        if (fileName.equals("MISURE_AP_TOT.csv")) {
            if(isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Data loaded! Starting mean computing...", Toast.LENGTH_SHORT).show()
                );
            }
            CsvDataProcessor meanProcessor = new CsvDataProcessor(dataRaw, this);
            meanProcessor.start();

        }
        // Abbiamo letto il file delle coordinate dopo OnProcessingDone
        else if (fileName.equals("AP_COORDINATES.csv")) {

            DataPoint[] apDataPoints = new DataPoint[dataRaw.size()];
            int count = 0;
            double maxX = 9.0;
            double maxY = 10.0;

            // Uniamo le coordinate lette con i modelli matematici parcheggiati
            for (String line : dataRaw) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String ssid = parts[0];
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);

                    if (tempCalibratedAps != null) {
                        AccessPoint ap = tempCalibratedAps.get(ssid);

                        if (ap != null) {
                            ap.x = x;
                            ap.y = y;
                            Log.i(TAG, "Configurato " + ssid + " alla posizione X: " + x + ", Y: " + y);
                        }
                    }
                }
            }

            // Salvataggio finale nella mappa ufficiale della stanza
            this.roomMap = tempCalibratedAps;

            if(isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {

                    DataPoint[] validPoints = new DataPoint[count];
                    System.arraycopy(apDataPoints, 0, validPoints, 0, count);
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