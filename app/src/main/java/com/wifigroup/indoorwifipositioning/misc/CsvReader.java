package com.wifigroup.indoorwifipositioning.misc;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {
    private static final String TAG = "CsvReader";

    // TODO: FARE IL BOOLEAN PER IL SALTO DELLA RIGA; PRIMA VOLEVAMO TESTARE SE FUNZIONASSE COSI

    public static List<String> readCsvFromAssets(Context context, String fileName) {
        List<String> rowCsv = new ArrayList<>();

        // Usiamo context.getAssets().open() per dire ad Android di andare a cercare nella cartella assets
        try (BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)))) {
            String line;
            Log.i(TAG, "Caricamento file negli assets...");

            // Legge e scarta la prima riga (l'header: AP,Distanza_m,Misura_n,dBm)
            br.readLine();

            // Legge i dati veri riga per riga e li aggiunge alla lista
            while ((line = br.readLine()) != null) {
                rowCsv.add(line);
            }
            Log.i(TAG, "File caricato correttamente dagli assets. Righe lette: " + rowCsv.size());

            Log.i("VERIFICA_DATI", "--- INIZIO LETTURA CSV: " + fileName + " ---");
            Log.i("VERIFICA_DATI", "Totale righe salvate nella lista: " + rowCsv.size());

            // Scorre tutta la lista dall'inizio (indice 0) alla fine
            for (int i = 0; i < rowCsv.size(); i++) {
                // Stampa la posizione e il contenuto esatto
                Log.i("VERIFICA_DATI", "Indice " + i + " -> " + rowCsv.get(i));
            }

            Log.i("VERIFICA_DATI", "--- FINE LETTURA CSV ---");

        } catch (Exception e) {
            Log.i(TAG, "Errore durante la lettura del file negli assets: " + e.getMessage());
        }

        return rowCsv;
    }
}