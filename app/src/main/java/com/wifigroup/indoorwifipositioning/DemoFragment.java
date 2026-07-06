package com.wifigroup.indoorwifipositioning;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wifigroup.indoorwifipositioning.BRs.WiFiReceiver;
import com.wifigroup.indoorwifipositioning.interfaces.IWiFiScanCompleted;
import com.wifigroup.indoorwifipositioning.misc.CsvReader;

import java.util.List;

public class DemoFragment extends Fragment implements IWiFiScanCompleted {

    private final String TAG = "DemoFragment";

    private TextView tvPolynomial = null;

    private TextView tvLog = null;

    private Button bttStartDemo = null;

    private WiFiReceiver wiFiReceiver = null;


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

        initViews(view);

        Log.i(TAG, "Avvio demo");

        // Chiama il CsvReader per leggere il file dalla cartella assets
        List<String> dataRaw = CsvReader.readCsvFromAssets(requireContext(), "MISURE_AP_TOT.csv");

        // 2. Controlla se ha trovato i dati
        /*
        if (!dataRaw.isEmpty()) {

            Toast.makeText(getContext(), "Dati caricati! Avvio calcolo medie...", Toast.LENGTH_SHORT).show();

            // 3. Passa la lista al tuo Thread per calcolare le medie
            CsvToMean calcolatoreMedie = new CsvToMean(dataRaw);
            calcolatoreMedie.start();

        } else {
            // Se la lista è vuota, significa che il file non c'era o aveva un nome sbagliato
            Toast.makeText(getContext(), "ERRORE: Impossibile caricare il CSV di calibrazione", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Lista dati vuota. Controlla che il file sia nella cartella assets e il nome sia corretto.");
        }
        */
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    // CoNTROLLA SE NECESSARIO
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

    private void initViews(View view) {
        tvPolynomial      = view.findViewById(R.id.tvPolynomial);
        tvLog             = view.findViewById(R.id.tvLog);
        bttStartDemo      = view.findViewById(R.id.bttStartDemo);
    }

    // CONTROLLAAAAA
    @Override
    public void onWifiScanCompleted(String ssid, int dBm) {

    }
}
