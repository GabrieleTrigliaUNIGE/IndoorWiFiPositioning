package com.wifigroup.indoorwifipositioning.interfaces;

import java.util.List;

/**
 * A callback interface to handle the completion of a CSV reading operation.
 * <p>
 * Implementing this interface allows a class to asynchronously receive the raw data
 * extracted from a CSV file (e.g., from the application's assets folder) once the
 * background thread finishes reading and parsing it.
 * </p>
 *
 * @author WiFiGroup
 * @version 1.1.0-alpha
 */
public interface ICsvReadCompleted {

    /**
     * Invoked when the background CSV reading process has successfully finished.
     *
     * @param dataRaw  a {@link List} of strings, where each string represents a single
     * raw data row extracted from the CSV file
     * @param fileName the name of the CSV file that was processed
     */
    void onCsvReadDone(List<String> dataRaw, String fileName);
}