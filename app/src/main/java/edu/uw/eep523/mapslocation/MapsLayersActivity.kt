package edu.uw.eep523.mapslocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
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
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_maps_layers.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.text.DateFormat
import java.util.*


private const val LOCATION_PERMISSION_REQUEST_CODE = 1

class MapsLayersActivity :
    AppCompatActivity(),
    OnMapReadyCallback,
    AdapterView.OnItemSelectedListener,
    EasyPermissions.PermissionCallbacks {

    private lateinit var saveRouteDialog: Dialog

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
    private lateinit var routePolyline: Polyline
    private  var routePolylineOptions: PolylineOptions = PolylineOptions()
    private var startLatLng = LatLng(0.0, 0.0)
    private var cumulativeLength : Double = 0.0
    private var prevLat: Double = 0.0
    private var prevLong: Double = 0.0
    var spinnerArray: MutableList<String> =  ArrayList()
    lateinit var adapter: ArrayAdapter<String>

    lateinit var dialog :AlertDialog.Builder
    lateinit var dialogView: View

    private var userFilename: String = ""
    private var userCategory: String = ""
    private var spinnerSelection: String =""


    var SPINNER: Spinner? = null
    var ADD: Button? = null
    var EDITTEXT: EditText? = null
    var spinnerItems = arrayOf(
        "Cycle",
        "Run"
    )
    var GETTEXT: String? = null
    var stringlist: MutableList<String>? = null
    var arrayadapter: ArrayAdapter<String>? = null


    private lateinit var databaseHelper: RouteDatabaseAdapter

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * [.onRequestPermissionsResult].
     */
    private var showPermissionDeniedDialog = false

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_layers)


        databaseHelper = RouteDatabaseAdapter(this)

        saveRouteDialog = Dialog(this)

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


        fun posButtonAction(text: String){

            // get user inputs!
            userFilename = text
            userCategory = SPINNER?.selectedItem as String

            saveDataInDb()
        }

        fun negButtonAction(){
            cancelRoute()
            val cancel = dialog.create()
            cancel.dismiss()

        }

        fun addToSpinnerAction(){
            Toast.makeText(this, "Item Added", Toast.LENGTH_LONG).show();
        }

        dialog = AlertDialog.Builder(this)
        val cancel = dialog.create()

         dialogView = layoutInflater.inflate(R.layout.save_popup, null) // try not null?
        val filenameField = dialogView.findViewById<EditText>(R.id.filename_field)

        dialog.setView(dialogView)
        dialog.setCancelable(false)
        dialog.setTitle("Save Route")

        dialog.setPositiveButton("Save", {dialogInterface: DialogInterface, i: Int -> posButtonAction(filenameField.text.toString()) })
        dialog.setNegativeButton("Cancel", {dialogInterface: DialogInterface, i: Int -> negButtonAction()})


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
            //pause_updates_button!!.isEnabled = true
        } else {
            start_updates_button!!.isEnabled = true
            stop_updates_button!!.isEnabled = false
            //pause_updates_button!!.isEnabled = false
        }
        if (mCurrentLocation != null) {

            updateRoute()

        }
    }

    private fun updateRoute(){

        // log location updates
        currentlattext = getString(R.string.latitude_label) + ": " +  mCurrentLocation!!.latitude
        currentlongtext = getString(R.string.longitude_label) + ": " +  mCurrentLocation!!.longitude
        //String latlongtext = (latitude_text!!.text as String) + " " +(longitude_text!!.text as String)
        Log.i("LATLONG", (currentlattext as String) + " " +(currentlongtext as String))

        Log.i("NUPDATES", (nupdates.toString()))




        if(nupdates == 0 ){
            startLatLng = LatLng(mCurrentLocation!!.latitude,mCurrentLocation!!.longitude)
            prevLat = mCurrentLocation!!.latitude
            prevLong = mCurrentLocation!!.longitude
            initPolyline()
        }
        else{
            routePolylineOptions.points.add(LatLng(mCurrentLocation!!.latitude,mCurrentLocation!!.longitude))

            routePolyline.remove()
            routePolyline = map.addPolyline(routePolylineOptions)

            cumulativeLength = cumulativeLength + haversine(prevLat, prevLong,mCurrentLocation!!.latitude, mCurrentLocation!!.longitude )
            Log.i("cum_length", cumulativeLength.toString())

            km_test.text = "Distance: " + "%.3f".format(cumulativeLength).toDouble().toString() + " km"

            prevLat = mCurrentLocation!!.latitude
            prevLong = mCurrentLocation!!.longitude


        }

    }


    private fun initPolyline(){

        routePolylineOptions.width(4F);
        routePolylineOptions.color(Color.BLUE);
        routePolylineOptions.geodesic(true);
        routePolylineOptions.add(startLatLng)

        routePolyline = map.addPolyline(routePolylineOptions)

    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private fun stopLocationUpdates() {
        nupdates = 0
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
            .addOnCompleteListener(this) {
                mRequestingLocationUpdates = false
                updateUI()
                //map.clear()
                routePolyline.remove();
            }

        launchSavePopup()
    }


    private fun launchSavePopup(){

        val btn = findViewById<Button>(R.id.stop_updates_button) as Button

        /*Create Object Of PopupMenu
            we need to pas  Context and View (Our View is Button so we pass btn to it)
            */

        val popUp = PopupMenu(this, btn)

        //Inflate our menu Layout.
        popUp.menuInflater.inflate(R.menu.popup_menu, popUp.menu)


        //Set Click Listener on Popup Menu Item
        popUp.setOnMenuItemClickListener { myItem ->

            //Getting Id of selected Item
            val item = myItem!!.itemId

            when (item) {
                R.id.saveRoute -> {
                    //showSaveRoutePopup()
                    //Toast.makeText(this, "Save the route", Toast.LENGTH_LONG).show()
                    // do new thing
                    //newthing()
                    dialog.show()


                    SPINNER = dialogView.findViewById(R.id.spinner1) as Spinner
                    ADD = dialogView.findViewById(R.id.button1) as Button
                    EDITTEXT = dialogView.findViewById(R.id.editText1) as EditText
                    stringlist =
                        ArrayList(Arrays.asList(*spinnerItems))
                    stringlist =
                        ArrayList(Arrays.asList(*spinnerItems))
                    arrayadapter = ArrayAdapter(this, R.layout.textview,
                        stringlist as ArrayList<String>
                    )
                    arrayadapter!!.setDropDownViewResource(R.layout.textview)
                    SPINNER!!.adapter = arrayadapter
                    ADD!!.setOnClickListener {
                        // TODO Auto-generated method stub
                        GETTEXT = EDITTEXT!!.text.toString()
                        //userCategory = GETTEXT as String


                        (stringlist as ArrayList<String>).add(GETTEXT!!)
                        arrayadapter!!.notifyDataSetChanged()
                        //Toast.makeText(this, "Item Added", Toast.LENGTH_LONG).show()
                    }

                    // TODO: get the category from user
                    //userCategory = (dialogView.findViewById(R.id.spinner1) as Spinner).getSelectedItem().toString();



                    Log.e("SpinnerSelect", userCategory)
                    //Toast.makeText(this, userCategory + "added", Toast.LENGTH_SHORT).show()



                }

                R.id.cancelRoute -> {
                    cancelRoute()

                }
            }

            true
        }
        popUp.show()
    }

    private fun cancelRoute(){
        // reset all variables
        nupdates = 0
        currentlattext= null
        currentlongtext = null
        routePolyline.remove()
        routePolyline.points.clear()
        routePolylineOptions = PolylineOptions()
        startLatLng = LatLng(0.0, 0.0)
        cumulativeLength = 0.0
        prevLat = 0.0
        prevLong = 0.0
        userFilename = ""
        userCategory = ""
        spinnerSelection = ""

        // clear UI
        map.clear()
        km_test.text = "Distance: " + "%.3f".format(cumulativeLength).toDouble().toString() + " km"
        Toast.makeText(this, "Cancelled route mapping.", Toast.LENGTH_LONG).show()
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

    private fun computeLength(){
        //computeLength(routePolyline)
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double{
            var km: Double = 0.0
            var p: Double = 0.017453292519943295;    // Math.PI / 180
            var a : Double = 0.5 - kotlin.math.cos((lat2 - lat1) * p)/2 +
                    kotlin.math.cos(lat1 * p) * kotlin.math.cos(lat2 * p) *
                    (1 - kotlin.math.cos((lon2 - lon1) * p))/2;
            km = 12742 * Math.asin(Math.sqrt(a)); // 2 * R; R = 6371 km
            return km
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

    private fun saveRouteToFile(){
        //TODO - figure out how to save to a file
        // text_to_save = polylineToGeoJSON(routePolyline)

        /*
                val file:String = "testfilename.txt".toString()
        val data:String = polylineToGeoJSON(routePolyline).toString()
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = openFileOutput(file, Context.MODE_PRIVATE)
            fileOutputStream.write(data.toByteArray())
        }catch (e: Exception){
            e.printStackTrace()
            Log.e("filewrite", "file write exception")
        }
         */
    }


    private fun saveDataInDb() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datestring = month.toString() + "-"+ day.toString() + "-" + year.toString()


        val name  =  userFilename
        val routeDate  = datestring
        val routeCategory  = "Route Type: " + userCategory
        val routeDistance  ="Distance (km): " + "%.3f".format(cumulativeLength).toDouble().toString()


       
            val id = databaseHelper.insertData(name,routeDate,routeCategory, routeDistance)
            if(id>0){
                Message.message(this,"Successfully inserted a row")
                finish()
            }else{
                Message.message(this,"Unsuccessful")
            
        }

    }

    private fun polylineToGeoJSON(polyline: Polyline): String {

        var geojsonstring: String = ""
        var gjarray: MutableList<String> = ArrayList()

        /*
        example geojson:

        {
           "type": "Feature",
           "geometry": {
               "type": "LineString",
               "coordinates": [
                   [-50.0,-10.0],[50.0,-10.0],[50.0,10.0],[-50.0,10.0]
               ]
           },
           "properties": {
               "prop0": "value0",
               "prop1": "value1"
         }
         */

        var geojson_string_beginning: String =
            "{\"type\": \"Feature\",\"geometry\": {\"type\": \"LineString\",\"coordinates\": "

        var geojson_string_ending: String =
            "},\"properties\": {\"prop0\": \"value0\",\"prop1\": \"value1\"}"

        routePolyline.points.toList().forEach {
            e ->
            var gjstring: String = e.toString().replace("lat/lng: (", "[", true).replace(")", "]", true)
            gjarray.add(gjstring)

        }

        geojsonstring = geojson_string_beginning+ gjarray.toString() + geojson_string_ending

        //Log.e("RTLIST", geojsonstring)

        return geojsonstring

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

}