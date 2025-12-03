package com.example.ballerevents;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

/**
 * Helper class for uploading images to Firebase Storage.
 *
 * <p>This class provides a centralized interface for uploading profile pictures,
 * event posters, and event banners to Firebase Storage. Each uploaded image
 * receives a unique filename and is stored in an organized folder structure.</p>
 */
public class ImageUploadHelper {

    private static final String TAG = "ImageUploadHelper";

    private static final FirebaseStorage storage = FirebaseStorage.getInstance();

    private static final String PROFILE_IMAGES_PATH = "profile_images/";
    private static final String EVENT_POSTERS_PATH = "event_posters/";
    private static final String EVENT_BANNERS_PATH = "event_banners/";

    /**
     * Callback interface for image upload operations.
     */
    public interface UploadCallback {
        /**
         * Called when the upload succeeds.
         *
         * @param downloadUrl The public download URL for the uploaded image.
         */
        void onSuccess(String downloadUrl);

        /**
         * Called when the upload fails.
         *
         * @param e The exception that caused the failure.
         */
        void onFailure(Exception e);
    }

    /**
     * Uploads a profile picture to Firebase Storage.
     *
     * @param imageUri The local URI of the image to upload.
     * @param callback Callback to receive the download URL or error.
     */
    public static void uploadProfileImage(Uri imageUri, UploadCallback callback) {
        if (imageUri == null) {
            callback.onFailure(new IllegalArgumentException("Image URI cannot be null"));
            return;
        }
        uploadImage(imageUri, PROFILE_IMAGES_PATH, callback);
    }

    /**
     * Uploads an event poster to Firebase Storage.
     *
     * @param imageUri The local URI of the poster image to upload.
     * @param callback Callback to receive the download URL or error.
     */
    public static void uploadEventPoster(Uri imageUri, UploadCallback callback) {
        if (imageUri == null) {
            callback.onFailure(new IllegalArgumentException("Image URI cannot be null"));
            return;
        }
        uploadImage(imageUri, EVENT_POSTERS_PATH, callback);
    }

    /**
     * Uploads an event banner to Firebase Storage.
     *
     * @param imageUri The local URI of the banner image to upload.
     * @param callback Callback to receive the download URL or error.
     */
    public static void uploadEventBanner(Uri imageUri, UploadCallback callback) {
        if (imageUri == null) {
            callback.onFailure(new IllegalArgumentException("Image URI cannot be null"));
            return;
        }
        uploadImage(imageUri, EVENT_BANNERS_PATH, callback);
    }

    /**
     * Internal method that handles the actual upload to Firebase Storage.
     *
     * <p>Generates a unique filename using UUID and uploads the image to the
     * specified storage path. Once complete, retrieves the public download URL.</p>
     *
     * @param imageUri    The local URI of the image.
     * @param storagePath The folder path in Firebase Storage.
     * @param callback    Callback to receive results.
     */
    private static void uploadImage(Uri imageUri, String storagePath, UploadCallback callback) {
        String filename = UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storage.getReference().child(storagePath + filename);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri downloadUri) {
                                        Log.d(TAG, "Image uploaded successfully: " + downloadUri.toString());
                                        callback.onSuccess(downloadUri.toString());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "Failed to get download URL", e);
                                        callback.onFailure(e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Image upload failed", e);
                        callback.onFailure(e);
                    }
                });
    }

    /**
     * Deletes an image from Firebase Storage given its download URL.
     *
     * @param imageUrl The full download URL of the image to delete.
     * @param callback Callback to handle success or failure.
     */
    public static void deleteImage(String imageUrl, DeleteCallback callback) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("Image URL cannot be null or empty"));
            }
            return;
        }

        try {
            StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
            imageRef.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Image deleted successfully");
                            if (callback != null) {
                                callback.onSuccess();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Failed to delete image", e);
                            if (callback != null) {
                                callback.onFailure(e);
                            }
                        }
                    });
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid image URL", e);
            if (callback != null) {
                callback.onFailure(e);
            }
        }
    }

    /**
     * Callback interface for image deletion operations.
     */
    public interface DeleteCallback {
        /**
         * Called when the deletion succeeds.
         */
        void onSuccess();

        /**
         * Called when the deletion fails.
         *
         * @param e The exception that caused the failure.
         */
        void onFailure(Exception e);
    }
}