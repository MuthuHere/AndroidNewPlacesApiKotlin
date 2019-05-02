package com.muthu.mapplacesapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_permission.*


class PermissionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startActivity(Intent(this@PermissionActivity, MainActivity::class.java))

            finish()
            return
        }


        btnEnable.setOnClickListener {
            Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener {
                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()

                    }

                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        startActivity(Intent(this@PermissionActivity, MainActivity::class.java))
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        if (response.isPermanentlyDenied) {
                            val alertDialog = AlertDialog.Builder(this@PermissionActivity)
                            alertDialog.setTitle("Permission Denied")
                            alertDialog.setMessage("Grant permission to access location from settings")
                            alertDialog.setNegativeButton("Cancel", null)
                            alertDialog.setPositiveButton("Enable") { dialog, which ->
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                intent.data = Uri.fromParts("package", packageName, null)
                                startActivity(intent)
                            }
                            alertDialog.show()
                        } else {
                            Toast.makeText(this@PermissionActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
                        }
                    }


                }).check()
        }


    }


}