/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uw.eep523.mapslocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_maps_layers.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.text.DateFormat
import java.util.*


private const val LOCATION_PERMISSION_REQUEST_CODE = 1

/**
 * Demonstrates the different base layers of a map.
 */
class MapsLayersActivity :
    AppCompatActivity(),
    OnMapReadyCallback,
    AdapterView.OnItemSelectedListener,
    EasyPermissions.PermissionCallbacks {

    private val TAG = "tag"

    private lateinit var map: GoogleMap

    private lateinit var myLocationCheckbox: CheckBox
    private lateinit var spinner: Spinner
    private var mFusedLocationClient: FusedLocationProviderClient? = null // Provides access to the Fused Location Provider API.
    private var mSettingsClient: SettingsClient? = null //  access to the Location Settings API.
    private var mLocationRequest: LocationRequest? = null //parameters for requests to the FusedLocationProviderApi.
    private var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var mLocationCallback: LocationCallback? = null //Callback for Location events.
    private var mCurrentLocation: Location? = null // geographical location.
    private var mRequestingLocationUpdates: Boolean? = null //Tracks the status of the location updates request
    private var mLastUpdateTime: String? = null //Time when the location was updated
    private var nupdates = 0

    private var currentlattext: String? = null
    private var currentlongtext: String? = null


    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * [.onRequestPermissionsResult].
     */
    private var showPermissionDeniedDialog = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_layers)

        mRequestingLocationUpdates = false
        mLastUpdateTime = ""

        // Update values using data stored in the Bundle.
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                    KEY_REQUESTING_LOCATION_UPDATES)
            }
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            }
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME)
            }
            updateUI()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)

        // Start the process of building the LocationCallback, LocationRequest
        createLocationCallback()
        createLocationRequest()

        /**
         *Check  if a device has the needed location settings.
         */
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        mLocationSettingsRequest = builder.build()


        spinner = findViewById<Spinner>(R.id.layers_spinner).apply {
            adapter = ArrayAdapter.createFromResource(this@MapsLayersActivity,
                R.array.layers_array, android.R.layout.simple_spinner_item).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            // set a listener for when the spinner to select map type is changed.
            onItemSelectedListener = this@MapsLayersActivity
        }

        myLocationCheckbox = findViewById(R.id.my_location)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        /*
        startRecordingButton.setOnClickListener(){
            // do something when startRecordingButton is pressed
            Toast.makeText(applicationContext,"this is toast message",Toast.LENGTH_SHORT).show()
            val toast = Toast.makeText(applicationContext, "Hello Javatpoint", Toast.LENGTH_SHORT)
            toast.show()
            val myToast = Toast.makeText(applicationContext,"toast message with gravity",Toast.LENGTH_SHORT)
            myToast.setGravity(Gravity.LEFT,200,200)
            myToast.show()
        }
        */

    }

    /**
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     */
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest!!.interval = UPDATE_INTERVAL_IN_MILLISECONDS

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Creates a callback for receiving location events.
     */
    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                mCurrentLocation = locationResult.lastLocation
                mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
                updateUI()
                nupdates++}
        }
    }

    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    fun startUpdatesButton(view: View?) {
        if (!mRequestingLocationUpdates!!) {
            mRequestingLocationUpdates = true
            updateUI()
            startLocationUpdates()
        }
    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates.
     */
    fun stopUpdatesButton(view: View?) {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        stopLocationUpdates()
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private fun startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient!!.checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener(this) {
                Log.i(TAG, "All location settings are ok.")
                mFusedLocationClient!!.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper())
                updateUI()
            }
            .addOnFailureListener(this) {
                mRequestingLocationUpdates = false
                updateUI()
            }
    }

    /**
     * Updates all UI fields.
     */
    private fun updateUI() {
        if (mRequestingLocationUpdates!!) {
            start_updates_button!!.isEnabled = false
            stop_updates_button!!.isEnabled = true
        } else {
            start_updates_button!!.isEnabled = true
            stop_updates_button!!.isEnabled = false
        }
        if (mCurrentLocation != null) {
            //latitude_text!!.text =getString(R.string.latitude_label) + ": " +  mCurrentLocation!!.latitude
            //longitude_text!!.text = getString(R.string.longitude_label) + ": " +  mCurrentLocation!!.longitude
            //last_update_time_text!!.text = getString( R.string.last_update_time_label) + ": " + mLastUpdateTime

            currentlattext = getString(R.string.latitude_label) + ": " +  mCurrentLocation!!.latitude
            currentlongtext = getString(R.string.longitude_label) + ": " +  mCurrentLocation!!.longitude


            //String latlongtext = (latitude_text!!.text as String) + " " +(longitude_text!!.text as String)
            Log.i("LATLONG", (currentlattext as String) + " " +(currentlongtext as String))

                val polyline1 = map.addPolyline(
                PolylineOptions()
                    .clickable(true)
                    .add(
                        LatLng(47.605975, -122.303098),
                        LatLng(47.633620, -122.304497)
                    )
            )


// latitude_text!!.text as Double, latitude_text!!.text as Double

        }
    }


    /**
     * Removes location updates from the FusedLocationApi.
     */
    private fun stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
            .addOnCompleteListener(this) {
                mRequestingLocationUpdates = false
                updateUI()
                map.clear()
            }
    }


    /**
     * Stores activity data in the Bundle.
     */
    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates!!)
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation)
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME, mLastUpdateTime)
        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onResume() {
        super.onResume()
        if (mRequestingLocationUpdates!! && checkPermissions()) {
            startLocationUpdates()
        } else if (!checkPermissions()) {
            requestPermissions()
        }
    }

    override fun onPause() {
        super.onPause()
        // stopLocationUpdates()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> {}
                Activity.RESULT_CANCELED -> {
                    mRequestingLocationUpdates = false
                    updateUI()
                }
            }
        }
    }







    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.ACCESS_FINE_LOCATION)

    }


    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,
            permissions, grantResults, this)

        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates!!) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates")
                    startLocationUpdates()
                }
            } else {
                // Permission denied.
                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.
                //TODO
            }
        }
    }



    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 123
        private const val REQUEST_CHECK_SETTINGS = 0x1
        /**
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

        private const val KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates"
        private const val KEY_LOCATION = "location"
        private const val KEY_LAST_UPDATED_TIME = "last-updated-time"
    }

    /**
     * Display a dialog box asking the user to grant permissions if they were denied
     */
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (showPermissionDeniedDialog) {
            AlertDialog.Builder(this).apply {
                setPositiveButton(R.string.ok, null)
                setMessage(R.string.location_permission_denied)
                create()
            }.show()
            showPermissionDeniedDialog = false
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {

        // exit early if the map was not initialised properly
        map = googleMap ?: return

        updateMapType()



        //val polyline1 = googleMap.addPolyline(
         //   PolylineOptions()
        //        .clickable(true)
        //        .add(
         //           LatLng(47.613984, -122.317908),
         //           LatLng(47.605975, -122.303098),
         //           LatLng(47.633620, -122.304497)
        //        )
        //)



        // check the state of all checkboxes and update the map accordingly
        with(map) {
            // deleted
        }

        // Must deal with the location checkbox separately as must check that
        // location permission have been granted before enabling the 'My Location' layer.
        if (myLocationCheckbox.isChecked) enableMyLocation()


        // if this box is checked, must check for permission before enabling the My Location layer
        myLocationCheckbox.setOnClickListener {
            if (!myLocationCheckbox.isChecked) {
                map.isMyLocationEnabled = false
            } else {
                enableMyLocation()
            }
        }
    }

    // startRecordingButton

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(LOCATION_PERMISSION_REQUEST_CODE)
    private fun enableMyLocation() {
        // Enable the location layer. Request the location permission if needed.
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        if (EasyPermissions.hasPermissions(this, *permissions)) {
            map.isMyLocationEnabled = true

        } else {
            // if permissions are not currently granted, request permissions
            EasyPermissions.requestPermissions(this,
                getString(R.string.permission_rationale_location),
                LOCATION_PERMISSION_REQUEST_CODE, *permissions)
        }
    }

    /**
     * Change the type of the map depending on the currently selected item in the spinner
     */
    private fun updateMapType() {
        // This can also be called by the Android framework in onCreate() at which
        // point map may not be ready yet.
        if (!::map.isInitialized) return

        map.mapType = when (spinner.selectedItem) {
            getString(R.string.normal) -> MAP_TYPE_NORMAL
            getString(R.string.hybrid) -> MAP_TYPE_HYBRID
            getString(R.string.satellite) -> MAP_TYPE_SATELLITE
            getString(R.string.terrain) -> MAP_TYPE_TERRAIN
            getString(R.string.none_map) -> MAP_TYPE_NONE
            else -> {
                map.mapType // do not change map type
                Log.e(TAG, "Error setting layer with name ${spinner.selectedItem}")
            }
        }
    }



    override fun onPermissionsDenied(requestCode: Int, list: List<String>) {
        // Un-check the box until the layer has been enabled
        // and show dialog box with permission rationale.
        myLocationCheckbox.isChecked = false
        showPermissionDeniedDialog = true
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // do nothing, handled in updateMyLocation
    }

    /**
     * Called as part of the AdapterView.OnItemSelectedListener
     */
    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        updateMapType()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Do nothing.
    }

    fun startPathRecording() {
        // Do nothing.
    }

    fun stopPathRecording() {
        // Do nothing.
    }

}