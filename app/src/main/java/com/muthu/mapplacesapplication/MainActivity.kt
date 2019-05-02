package com.muthu.mapplacesapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.PlacesClient
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), OnMapReadyCallback {


    var mMap: GoogleMap by Delegates.notNull()
    var listAutoCompletePrediction: List<AutocompletePrediction> by Delegates.notNull()
    var placesClient: PlacesClient by Delegates.notNull()


    var lastKnownLocation: Location by Delegates.notNull()
    var locationCallback: LocationCallback by Delegates.notNull()


    private val ZOOM_LEVEL = 18.toFloat()
    val ACTIVITY_REQUEST = 23

    var fusedLocationProviderClient: FusedLocationProviderClient by Delegates.notNull()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)

        Places.initialize(this@MainActivity, "AIzaSyCijnBibpphJDGji3PGzGrnxdHjoTbey6c")

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap?) {

        if (map != null) {
            this.mMap = map

            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true

        }


        val locationRequest = LocationRequest.create()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(this@MainActivity)

        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener { res ->

        }

        task.addOnFailureListener { res ->

            if (res is ResolvableApiException) {
                val resolvableExp = res
                resolvableExp.startResolutionForResult(this@MainActivity, ACTIVITY_REQUEST)
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    ACTIVITY_REQUEST -> {
                        getDeviceLocation()
                    }
                }
            }

        }

    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {

        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->

            if (task.isSuccessful) {
                if (task.result != null) {
                    lastKnownLocation = task.result!!

                    if (lastKnownLocation != null) {
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    lastKnownLocation.latitude,
                                    lastKnownLocation.longitude
                                ), ZOOM_LEVEL
                            )
                        )
                    } else {
                        requestLocationUpdate()
                    }
                }
            } else {
                Toast.makeText(this, "Unable to get last location", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun requestLocationUpdate() {

        val locationRequest = LocationRequest.create()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            @SuppressLint("MissingPermission")
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                if (locationResult == null) {
                    return
                }

                lastKnownLocation = locationResult.lastLocation
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            lastKnownLocation.latitude,
                            lastKnownLocation.longitude
                        ), ZOOM_LEVEL
                    )
                )


                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)


            }

        }
    }

}
