package com.muthu.mapplacesapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Log.d
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.*
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.content_main.*
import java.lang.Exception
import java.util.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnCameraMoveListener {

    override fun onCameraMove() {
        d("mmmm ","moving camera")

    }


    var mMap: GoogleMap by Delegates.notNull()
    var listAutoCompletePrediction: List<AutocompletePrediction> by Delegates.notNull()
    var placesClient: PlacesClient by Delegates.notNull()


    var lastKnownLocation: Location by Delegates.notNull()
    var locationCallback: LocationCallback by Delegates.notNull()


    private val ZOOM_LEVEL = 18.toFloat()
    val ACTIVITY_REQUEST = 23

    var fusedLocationProviderClient: FusedLocationProviderClient by Delegates.notNull()
    var sheetBehavior: BottomSheetBehavior<View> by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)


        //for places API
        Places.initialize(this@MainActivity, resources.getString(R.string.api_key))
        placesClient = Places.createClient(this)
        val token = AutocompleteSessionToken.newInstance()

        //search bar control
        searchBar.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener {
            override fun onButtonClicked(buttonCode: Int) {
                when (buttonCode) {
                    MaterialSearchBar.BUTTON_BACK -> {
                        searchBar.disableSearch()
                    }
                }
            }

            override fun onSearchStateChanged(enabled: Boolean) {

            }

            override fun onSearchConfirmed(text: CharSequence?) {
                startSearch(text.toString(), true, null, true)

            }

        })

        //text change control
        searchChangeListener(token)


        btnFind.setOnClickListener {
            val currentMarker = mMap.cameraPosition.target
            rippleBg.startRippleAnimation()
            Handler().postDelayed(object : Runnable {
                override fun run() {
                    rippleBg.stopRippleAnimation()

                }

            }, 3000)
        }





        //bottom sheet
        sheetBehavior = BottomSheetBehavior.from(llBottomSheet)

        sheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {


            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {

                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {

                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }

        })

    }


    /**
     * Map movement
     */



   /*private final OnCameraChangeListener mOnCameraChangeListener =
        new OnCameraChangeListener() {

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (!mMapIsTouched) {
            refreshClustering(false);
        }
    }
};*/


    private fun searchChangeListener(token: AutocompleteSessionToken) {

        searchBar.addTextChangeListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {


            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val autoCompleteBuilder = FindAutocompletePredictionsRequest.builder()
                    .setCountry("IN")
                    .setTypeFilter(TypeFilter.ADDRESS)
                    .setSessionToken(token)
                    .setQuery(s.toString())
                    .build()
                placesClient.findAutocompletePredictions(autoCompleteBuilder).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val autoCompletionResponse = task.result
                        if (autoCompletionResponse != null) {
                            listAutoCompletePrediction = autoCompletionResponse.autocompletePredictions

                            val listOfString = arrayListOf<String>()
                            listAutoCompletePrediction.forEachIndexed { index, autocompletePrediction ->
                                listOfString.add(autocompletePrediction.getFullText(null).toString())
                            }

                            searchBar.updateLastSuggestions(listOfString)

                            if (!searchBar.isSuggestionsVisible) {
                                searchBar.showSuggestionsList()
                            }


                        }

                    } else {
                        d("MMMM ", "Address failed to load")
                    }

                    searchBar.setSuggestionsClickListener(object : SuggestionsAdapter.OnItemViewClickListener {
                        override fun OnItemDeleteListener(position: Int, v: View?) {


                        }

                        override fun OnItemClickListener(position: Int, v: View?) {
                            if (position >= listAutoCompletePrediction.size) {
                                return
                            }

                            val item = listAutoCompletePrediction[position]


                            val suggsion = searchBar.lastSuggestions[position].toString()
                            searchBar.text = suggsion

                            Handler().postDelayed(object : Runnable {
                                override fun run() {

                                    searchBar.clearSuggestions()
                                }

                            }, 1000)


                            closeKeyBoard()

                            val placeID = item.placeId
                            val placeFields = Arrays.asList(Place.Field.LAT_LNG)
                            val fetchPlaceRequest = FetchPlaceRequest.builder(placeID, placeFields).build()

                            placesClient.fetchPlace(fetchPlaceRequest)
                                .addOnSuccessListener(object : OnSuccessListener<FetchPlaceResponse> {
                                    override fun onSuccess(response: FetchPlaceResponse?) {
                                        val place = response?.place
                                        if (place != null) {
                                            val latLng = place.latLng
                                            if (latLng != null) {
                                                //get address
                                                getAddress(
                                                    LatLng(
                                                        latLng.latitude,
                                                        latLng.longitude
                                                    )
                                                )


                                                mMap.moveCamera(
                                                    CameraUpdateFactory.newLatLngZoom(
                                                        LatLng(
                                                            latLng.latitude,
                                                            latLng.longitude
                                                        ), ZOOM_LEVEL
                                                    )
                                                )

                                            }
                                        }

                                    }

                                }).addOnFailureListener(object : OnFailureListener {
                                    override fun onFailure(p0: Exception) {
                                        if (p0 is ApiException) {

                                        }
                                    }

                                })


                        }

                    })


                }

            }

        })


    }


    private fun closeKeyBoard() {

        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)?.hideSoftInputFromWindow(
            searchBar.windowToken,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )

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
            getDeviceLocation()
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

                        //get address
                        getAddress(
                            LatLng(
                                lastKnownLocation.latitude,
                                lastKnownLocation.longitude
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

                //get address
                getAddress(
                    LatLng(
                        lastKnownLocation.latitude,
                        lastKnownLocation.longitude
                    )
                )

                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)


            }

        }
    }


    private fun getAddress(latLng: LatLng): String {

        var fullAddress = ""

        val geoCoder = Geocoder(this@MainActivity, Locale.getDefault())
        val addressList = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        if (addressList != null) {
            val googleAddress = addressList[0] as Address
            fullAddress = googleAddress.getAddressLine(0)
        }

        val location = addressList[0].locality
        val zipCode = addressList[0].postalCode
        val country = addressList[0].countryName

        d("mmm ", "location =$location <====> zipCode = $zipCode <===> country$country")

        d("mmm ", "fullAddress =$fullAddress")


        val splittedAddress = fullAddress.split(",")

        when {
            splittedAddress.size > 4 -> {
                tvAddressOne.text = splittedAddress[0]
                tvAddressTwo.text = splittedAddress[1]
                tvAddressThree.text = splittedAddress[2]
                tvAddressFour.text = splittedAddress[3]
            }
            splittedAddress.size > 5 -> {
                tvAddressOne.text = splittedAddress[0] + " , " + splittedAddress[1]
                tvAddressTwo.text = splittedAddress[2]
                tvAddressThree.text = splittedAddress[3]
                tvAddressFour.text = splittedAddress[4]
            }
            splittedAddress.size > 6 -> {
                tvAddressOne.text = splittedAddress[0] + " , " + splittedAddress[1]
                tvAddressTwo.text = splittedAddress[2] + "," + splittedAddress[3]
                tvAddressThree.text = splittedAddress[4]
                tvAddressFour.text = splittedAddress[5]
            }
            splittedAddress.size > 7 -> {
                tvAddressOne.text = splittedAddress[0] + " , " + splittedAddress[1]+" , " + splittedAddress[2]
                tvAddressTwo.text = splittedAddress[3]+" , "+ splittedAddress[4]
                tvAddressThree.text = splittedAddress[5]
                tvAddressFour.text = splittedAddress[6]
            }
        }





        return fullAddress
    }

}
