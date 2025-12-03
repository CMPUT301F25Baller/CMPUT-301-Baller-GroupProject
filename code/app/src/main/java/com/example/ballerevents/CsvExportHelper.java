package com.example.ballerevents;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Helper class for exporting data to CSV files.
 *
 * <p>This class provides methods to export lists of entrants to CSV format
 * and save them to the device's Downloads folder. It handles both legacy
 * external storage (Android 9 and below) and scoped storage (Android 10+).</p>
 *
 * <p>Features:</p>
 * <ul>
 * <li>Exports accepted entrants with name and email</li>
 * <li>Generates timestamped filenames</li>
 * <li>Saves to Downloads folder for easy access</li>
 * <li>Handles Android version differences automatically</li>
 * </ul>
 */
public class CsvExportHelper {

    private static final String TAG = "CsvExportHelper";

    /**
     * Callback interface for CSV export operations.
     */
    public interface ExportCallback {
        /**
         * Called when export succeeds.
         *
         * @param filePath The path or URI where the file was saved.
         */
        void onSuccess(String filePath);

        /**
         * Called when export fails.
         *
         * @param error The exception that caused the failure.
         */
        void onFailure(Exception error);
    }

    /**
     * Exports a list of accepted entrants to a CSV file.
     *
     * <p>The CSV will contain two columns: Name and Email.
     * The file is saved to the Downloads folder with a timestamped filename.</p>
     *
     * @param context       Application context for file operations.
     * @param eventTitle    Title of the event (used in filename).
     * @param entrants      List of UserProfile objects who accepted.
     * @param callback      Callback to receive success/failure results.
     */
    public static void exportAcceptedEntrants(
            Context context,
            String eventTitle,
            List<UserProfile> entrants,
            ExportCallback callback
    ) {
        if (entrants == null || entrants.isEmpty()) {
            callback.onFailure(new IllegalArgumentException("No entrants to export"));
            return;
        }

        try {
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("Name,Email\n");

            for (UserProfile entrant : entrants) {
                String name = escapeCSV(entrant.getName());
                String email = escapeCSV(entrant.getEmail());
                csvContent.append(name).append(",").append(email).append("\n");
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                    .format(new Date());
            String sanitizedTitle = sanitizeFilename(eventTitle);
            String filename = "Accepted_Entrants_" + sanitizedTitle + "_" + timestamp + ".csv";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToDownloadsQ(context, filename, csvContent.toString(), callback);
            } else {
                saveToDownloadsLegacy(context, filename, csvContent.toString(), callback);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error creating CSV", e);
            callback.onFailure(e);
        }
    }

    /**
     * Saves CSV to Downloads folder on Android 10+ using MediaStore API.
     *
     * @param context  Application context.
     * @param filename Name of the file to save.
     * @param content  String content of the CSV.
     * @param callback Callback for success or failure.
     */
    private static void saveToDownloadsQ(
            Context context,
            String filename,
            String content,
            ExportCallback callback
    ) {
        try {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            if (uri == null) {
                callback.onFailure(new IOException("Failed to create file in Downloads"));
                return;
            }

            try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                if (outputStream == null) {
                    callback.onFailure(new IOException("Failed to open output stream"));
                    return;
                }

                outputStream.write(content.getBytes());
                outputStream.flush();

                Log.d(TAG, "CSV saved successfully to: " + uri.toString());
                callback.onSuccess("Downloads/" + filename);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving CSV (Android 10+)", e);
            callback.onFailure(e);
        }
    }

    /**
     * Saves CSV to Downloads folder on Android 9 and below using File API.
     *
     * @param context  Application context.
     * @param filename Name of the file to save.
     * @param content  String content of the CSV.
     * @param callback Callback for success or failure.
     */
    private static void saveToDownloadsLegacy(
            Context context,
            String filename,
            String content,
            ExportCallback callback
    ) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
            );

            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File file = new File(downloadsDir, filename);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes());
                fos.flush();

                Log.d(TAG, "CSV saved successfully to: " + file.getAbsolutePath());
                callback.onSuccess(file.getAbsolutePath());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving CSV (Legacy)", e);
            callback.onFailure(e);
        }
    }

    /**
     * Escapes special characters in CSV fields.
     *
     * <p>If a field contains commas, quotes, or newlines, it will be
     * wrapped in double quotes and internal quotes will be escaped.</p>
     *
     * @param value The string value to escape.
     * @return Escaped string safe for CSV format.
     */
    private static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }

    /**
     * Sanitizes a string to be safe for use as a filename.
     *
     * @param filename The string to sanitize.
     * @return Sanitized filename safe for all platforms.
     */
    private static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "Event";
        }

        return filename.replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_{2,}", "_")
                .substring(0, Math.min(filename.length(), 50));
    }
}