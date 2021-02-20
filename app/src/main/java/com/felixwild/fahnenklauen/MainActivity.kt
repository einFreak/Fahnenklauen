package com.felixwild.fahnenklauen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.felixwild.fahnenklauen.viewModels.LocationViewModel
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var requestingLocationUpdates: Boolean = false
    private lateinit var currentLocation: Location
    private val locationViewModel: LocationViewModel by viewModels()
    private val locationRequest = LocationRequest.create().apply {
        interval = 5000
        fastestInterval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updateValuesFromBundle(savedInstanceState)

        init()
    }

    private fun init() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    getLocation()
                }
            }
        }

        setupPermissions()
        startLocationUpdates()
        setupBottomNav()
        getLocation()
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("REQUESTING_LOCATION_UPDATES_KEY", requestingLocationUpdates)
        super.onSaveInstanceState(outState)
    }

    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        savedInstanceState ?: return

        // Update the value of requestingLocationUpdates from the Bundle.
        if (savedInstanceState.keySet().contains("REQUESTING_LOCATION_UPDATES_KEY")) {
            requestingLocationUpdates = savedInstanceState.getBoolean(
                    "REQUESTING_LOCATION_UPDATES_KEY")
        }

        // Update UI to match restored state
        //updateUI()
    }

    private fun setupBottomNav() {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_search, R.id.navigation_map))

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        //Creating task with location request
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun setupPermissions() {
        val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted: Boolean ->
            Log.d(TAG, "isGranted: $isGranted")
            if(isGranted) {
                Toast.makeText(this, "Location Permission given", Toast.LENGTH_SHORT).show()
                requestingLocationUpdates = true
            } else {
                Toast.makeText(this, "No Permission", Toast.LENGTH_SHORT).show()
                requestingLocationUpdates = false
            }
        }

        Log.d(TAG, "Check for Perm")
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                Log.d(TAG, "Permission granted")
                requestingLocationUpdates = true
                // You can use the API that requires the permission.
            }
            //shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            //showInContextUI(...)
            //Toast.makeText(this.context, "No Permission - setting to default2", Toast.LENGTH_SHORT).show()
            //}
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

        }

        //saving PermissionState to SharedPreferences
        Log.d(TAG, "reqLoq value: $requestingLocationUpdates")
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putBoolean(getString(R.string.LocationPermission), requestingLocationUpdates)
            apply()
        }

    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val tempLocation: Location = Location("")
        tempLocation.longitude = resources.getString(R.string.Westernhohe_long).toDouble()
        tempLocation.latitude = resources.getString(R.string.Westernhohe_lat).toDouble()

        if(requestingLocationUpdates) {
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            currentLocation = location
                            locationViewModel.changeLocation(location)
                            //Log.d(TAG, "CurrentLocation is $location")
                            //saving currentLocation to SharedPreferences
                            val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return@addOnSuccessListener
                            with (sharedPref.edit()) {
                                putFloat(getString(R.string.CurrentLocationLat), location.latitude.toFloat())
                                putFloat(getString(R.string.CurrentLocationLong), location.longitude.toFloat())
                                apply()
                            }

                        } else {
                            Log.d(TAG, "Location is null - setting default")
                            locationViewModel.changeLocation(tempLocation)
                            //getDataToRV()
                        }
                    }
        }
    }

}