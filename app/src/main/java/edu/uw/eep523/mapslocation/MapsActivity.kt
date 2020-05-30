package edu.uw.eep523.mapslocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import java.util.*
import kotlin.collections.ArrayList
import android.location.LocationManager
import android.content.pm.PackageManager
import android.location.Location

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices



class MapsActivity : AppCompatActivity(), OnMapReadyCallback, AdapterView.OnItemSelectedListener
{
    private lateinit var mMap: GoogleMap //Create an instace of my map so that I can refer to it in my app


    private val TAG = "MainActivity"
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private lateinit var fusedLocationClient: FusedLocationProviderClient



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Add listener to the spinner
        cities_spinner.setOnItemSelectedListener(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }
    ////////////////////////////////////////////////////////////////


    // An city form the spinner was selected
    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        //

    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID) //set the map to hybrid mode
    }


    /*
       * Returns the user's current location as a LatLng object.
       * Returns null if location could not be found (such as in an AVD emulated virtual device).
       */
    fun getMyLocation(v:View) {


        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationProvider: String = LocationManager.NETWORK_PROVIDER
        val myLocation: Location = locationManager.getLastKnownLocation(locationProvider)/////////////


        val permission: Int = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION   )
        if (permission == PackageManager.PERMISSION_GRANTED) {
            Log.d("tag","Permision granted")

        }
        else{ ActivityCompat.requestPermissions( this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 0 )
            Log.d("tag","Request permission")}

        val myLat = myLocation!!.getLatitude()
        val myLng = myLocation!!.getLongitude()

        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(myLat,myLng)))
        mMap.addMarker(
            MarkerOptions()
                .position(LatLng(myLat,myLng))
                .title("Marker")
        )
    }

    fun getMyLocationWithLocationAPI(v:View){
        //OPTION 2: USE GOOGLE PLAY SERVICES LOCATION API
     getLastLocation()

    }



    //  Provides a simple way of getting a device's location

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {

                    val myLat = task.getResult()!!.latitude
                    val myLng = task.getResult()!!.longitude

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(myLat,myLng)))
                    mMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(myLat,myLng))
                            .title("Marker")
                    )

                } else {
                    Log.w(TAG, "getLastLocation:exception", task.exception)

                }
            }
    }



    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE)
    }


}
