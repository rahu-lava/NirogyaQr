package com.example.nirogyaqr

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.journeyapps.barcodescanner.CaptureActivity


class MainActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scanButton = findViewById<Button>(R.id.scanButton)

        scanButton.setOnClickListener(){
            // Check for camera permission before starting the QR Code Scanner
            if (hasCameraPermission()) {
                startQrScanner()
            } else {
                requestCameraPermission()
            }
        }


    }
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start the QR Code Scanner
                startQrScanner()
            } else {
                // Permission denied, handle accordingly (show a message, request again, etc.)
                // You may want to inform the user that the camera permission is required.
            }
        }
    }

    private fun startQrScanner() {
        val integrator = IntentIntegrator(this)

        // Customize the prompt message
        integrator.setPrompt("Volume Up for Flash Light")

        // Use front camera by default
        integrator.setCameraId(0)

        // Set beep on successful scan
        integrator.setBeepEnabled(true)

        // Set orientation to portrait
//        integrator.setOrientationLocked(true)
//        integrator.setCaptureActivity(VerticalCaptureActivity::class.java)

        // Initiate the QR code scan
        integrator.initiateScan()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Parse the result of the QR code scan
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                // Handle the case where the user canceled the scan
                // You may want to show a message or take some action
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show()
            } else {
                // Handle the QR code result
                val scannedData: String = result.contents
                Log.d("ScannedData", scannedData)
                if (scannedData.startsWith("NIR", ignoreCase = true)) {
                    val database = FirebaseDatabase.getInstance().reference
                    val messageRef = database.child("Shopkeeper").child("medicine").child("code")

                    messageRef.setValue(scannedData).addOnSuccessListener {
//                        inputText.text.clear()
                        Toast.makeText(this, "Message saved successfully!", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        // Handle failure here
                        Toast.makeText(this, "Message is Not Saved", Toast.LENGTH_SHORT).show()
                    }
                    Toast.makeText(this, "Scan Successful!", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle other cases (e.g., invalid codes)
                    Log.d("InvalidData", scannedData)
                    Toast.makeText(this, "Invalid code scanned", Toast.LENGTH_SHORT).show()
                }
//                Toast.makeText(this, "Scan Successful!$scannedData", Toast.LENGTH_LONG).show()
                // Do something with the scanned data, for example, display it
                // in a TextView or perform an action based on the content.
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}