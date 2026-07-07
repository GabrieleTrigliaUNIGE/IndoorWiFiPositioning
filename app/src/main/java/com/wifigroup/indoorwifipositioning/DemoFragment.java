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

            liveScanBuffer.clear();

            wifiManager.startScan();

            tvLog.setText("Log distance: Scansione in corso...");
            tvPolynomial.setText("Polynomial approximation: Scansione in corso...");
            Toast.makeText(getContext(), "Scansione Wi-Fi avviata...", Toast.LENGTH_SHORT).show();
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
        // Chiama il CsvReader per leggere il file dalla cartella assets
        List<String> dataRaw = CsvReader.readCsvFromAssets(requireContext(), "MISURE_AP_TOT.csv");

        if (!dataRaw.isEmpty()) {

            Toast.makeText(getContext(), "Dati caricati! Avvio calcolo medie...", Toast.LENGTH_SHORT).show();

            CsvDataProcessor meanProcessor = new CsvDataProcessor(dataRaw, this);
            meanProcessor.start();

        } else {
            Toast.makeText(getContext(), "ERRORE: Impossibile caricare il CSV", Toast.LENGTH_LONG).show();
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

        if (dBm == -999 || dBm == -998) return;

        if (roomMap.containsKey(ssid)) {
            liveScanBuffer.put(ssid, dBm);
            Log.i(TAG, "Ricevuto live: " + ssid + " -> " + dBm + " dBm");
        }

        if (liveScanBuffer.size() >= 3) {

            // Logica per multilaterazione
            double[] posLog = TrilaterationEngine.calculatePosition(liveScanBuffer, roomMap, true);
            double[] posPoly = TrilaterationEngine.calculatePosition(liveScanBuffer, roomMap, false);

            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (posLog != null) {
                        tvLog.setText(String.format("Log distance\nX: %.2f m | Y: %.2f m", posLog[0], posLog[1]));
                    } else {
                        tvLog.setText("Log distance: Errore di convergenza");
                    }

                    if (posPoly != null) {
                        tvPolynomial.setText(String.format("Polynomial approximation\nX: %.2f m | Y: %.2f m", posPoly[0], posPoly[1]));
                    } else {
                        tvPolynomial.setText("Polynomial approximation: Errore di convergenza");
                    }
                });
            }
        }
    }

    @Override
    public void onProcessingDone(Map<String, AccessPoint> calibratedAps) {

        // TODO: METTERE LE CORDINATE IN UN FILE??
        // INSERISCI QUI LE COORDINATE REALI (IN METRI) DELLA TUA STANZA!
        if (calibratedAps.containsKey("AP1")) {
            calibratedAps.get("AP1").x = 0.0;
            calibratedAps.get("AP1").y = 0.0;
        }
        if (calibratedAps.containsKey("AP2")) {
            calibratedAps.get("AP2").x = 5.0;
            calibratedAps.get("AP2").y = 0.0;
        }
        if (calibratedAps.containsKey("AP3")) {
            calibratedAps.get("AP3").x = 5.0;
            calibratedAps.get("AP3").y = 5.0;
        }
        if (calibratedAps.containsKey("AP4")) {
            calibratedAps.get("AP4").x = 0.0;
            calibratedAps.get("AP4").y = 5.0;
        }

        // Salviamo la stanza configurata
        this.roomMap = calibratedAps;

        if(isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Calcolo completato", Toast.LENGTH_LONG).show();
                bttStartDemo.setEnabled(true);
                Log.i(TAG, "Tabelle salvate in memoria");
            });
        }
    }
}
