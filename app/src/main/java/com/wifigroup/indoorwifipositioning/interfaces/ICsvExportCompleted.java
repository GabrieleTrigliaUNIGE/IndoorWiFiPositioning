package com.wifigroup.indoorwifipositioning.interfaces;

/**
 * A callback interface to handle the completion of a CSV export operation.
 * <p>
 * Implementing this interface allows a class (such as a UI Fragment or Activity)
 * to asynchronously receive the result of the background export task and update
 * the user interface accordingly.
 * </p>
 *
 * @author WiFiGroup
 * @version 1.1.0-alpha
 */
public interface ICsvExportCompleted {

    /**
     * Invoked when the background CSV export process has finished.
     *
     * @param isSuccess {@code true} if the CSV file was successfully saved to the device;
     * {@code false} if an I/O error occurred during the process
     * @param message   a descriptive string containing either the name of the saved file
     * (on success) or an error message explaining the failure
     */
    void onExportDone(boolean isSuccess, String message);
}