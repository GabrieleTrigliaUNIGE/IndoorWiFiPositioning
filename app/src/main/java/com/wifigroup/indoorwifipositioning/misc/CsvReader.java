package com.wifigroup.indoorwifipositioning.misc;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides utility methods for reading CSV files from the application's assets folder.
 * <p>
 * This class handles the extraction of raw data lines from CSV files bundled
 * with the Android application. It handles the input stream and logs the
 * read process for debugging purposes.
 * </p>
 *
 * @author WiFiGroup
 * @version 1.0.0
 */
public class CsvReader {

    private static final String TAG = "CsvReader";

    // TODO: FARE IL BOOLEAN PER IL SALTO DELLA RIGA
    // TODO: FARE INTERFACCIA ??

    /**
     * Reads the contents of a CSV file located in the application's assets folder.
     * <p>
     * Note: This current implementation automatically skips the first line of the CSV file,
     * assuming it to be a header row.
     * </p>
     *
     * @param context the application context used to access the AssetManager
     * @param fileName the name of the CSV file to read (e.g., "data.csv")
     * @return a list of strings, where each string represents a single line from the CSV file (excluding the header)
     */
    public static List<String> readCsvFromAssets(Context context, String fileName) {
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

        return rowCsv;
    }
}