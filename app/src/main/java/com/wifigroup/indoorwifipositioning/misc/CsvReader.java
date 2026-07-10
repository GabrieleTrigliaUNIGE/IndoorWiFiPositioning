package com.wifigroup.indoorwifipositioning.misc;

import android.content.Context;
import android.util.Log;

import com.wifigroup.indoorwifipositioning.interfaces.ICsvReadCompleted;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * A background thread designed to read CSV files from the application's assets folder asynchronously.
 * <p>
 * This class extends {@link Thread} to prevent blocking the main UI thread during file I/O operations.
 * It handles the extraction of raw data lines from bundled CSV files and delivers the resulting
 * list of strings back to the caller through a callback interface.
 * </p>
 *
 * @author WiFiGroup
 * @version 1.1.0
 */
public class CsvReader extends Thread {

    // TODO: FARE IL BOOLEAN PER IL SALTO DELLA RIGA
    private static final String TAG = "CsvReader";

    private final Context context;
    private final String fileName;
    private final ICsvReadCompleted listener;

    /**
     * Initializes a new background thread to read the specified CSV file.
     *
     * @param context the application context used to access the AssetManager
     * @param fileName the name of the CSV file to read (e.g., "data.csv")
     * @param listener the callback interface to be notified when the reading process finishes
     */
    public CsvReader(Context context, String fileName, ICsvReadCompleted listener) {
        this.context = context;
        this.fileName = fileName;
        this.listener = listener;
    }

    /**
     * Executes the file reading operation in a background thread.
     * <p>
     * Note: This current implementation automatically skips the first line of the CSV file,
     * assuming it to be a header row. Upon completion, it triggers the
     * {@link ICsvReadCompleted#onCsvReadDone(List, String)} callback with the extracted data.
     * </p>
     */
    @Override
    public void run() {
        List<String> rowCsv = new ArrayList<>();

        // context.getAssets().open() per dire ad Android di cercare nel giusto path
        try (BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)))) {
            String line;
            Log.i(TAG, "Caricamento file negli assets...");

            br.readLine();

            while ((line = br.readLine()) != null) {
                rowCsv.add(line);
            }
            Log.i(TAG, "File caricato correttamente dagli assets. Righe lette: " + rowCsv.size());

            Log.i("VERIFICA_DATI", "--- INIZIO LETTURA CSV: " + fileName + " ---");
            Log.i("VERIFICA_DATI", "Totale righe salvate nella lista: " + rowCsv.size());

            for (int i = 0; i < rowCsv.size(); i++) {
                Log.i("VERIFICA_DATI", "Indice " + i + " -> " + rowCsv.get(i));
            }

            Log.i("VERIFICA_DATI", "--- FINE LETTURA CSV ---");

        } catch (Exception e) {
            Log.i(TAG, "Errore durante la lettura del file negli assets: " + e.getMessage());
        }

        if (listener != null) {
            listener.onCsvReadDone(rowCsv, fileName);
        }
    }
}