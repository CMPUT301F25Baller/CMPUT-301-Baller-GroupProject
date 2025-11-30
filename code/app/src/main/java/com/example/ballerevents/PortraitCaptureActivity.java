package com.example.ballerevents;

import com.journeyapps.barcodescanner.CaptureActivity;

/**
 * A custom CaptureActivity used to force the QR scanner into Portrait orientation.
 * <p>
 * This class must be declared in AndroidManifest.xml with:
 * <code>android:screenOrientation="portrait"</code>
 * </p>
 */
public class PortraitCaptureActivity extends CaptureActivity {
    // No custom logic needed; the manifest attribute handles the rotation.
}