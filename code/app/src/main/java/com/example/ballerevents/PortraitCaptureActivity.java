package com.example.ballerevents;

import com.journeyapps.barcodescanner.CaptureActivity;

/**
 * A custom CaptureActivity used to force the QR scanner into Portrait orientation.
 * <p>
 * This class extends {@link CaptureActivity} and relies on the manifest declaration:
 * <code>android:screenOrientation="portrait"</code> to enforce the orientation.
 * </p>
 */
public class PortraitCaptureActivity extends CaptureActivity {
    // No custom logic needed; the manifest attribute handles the rotation.
}