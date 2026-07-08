package com.wifigroup.indoorwifipositioning.misc;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {
    private static final String TAG = "CsvReader";

    // TODO: FARE IL BOOLEAN PER IL SALTO DELLA RIGA
    // TODO: FARE INTERFACCIA ??

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