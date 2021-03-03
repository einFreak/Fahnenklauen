package com.felixwild.fahnenklauen.viewModels

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.felixwild.fahnenklauen.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch

class LocationViewModel() : ViewModel() {
    private val mutableLocation = MutableLiveData<Location>()
    val currentLocation: LiveData<Location> get() = mutableLocation

    private val mutableCurrentGeoPoint = MutableLiveData<GeoPoint>()
    val currentGeoPoint: LiveData<GeoPoint> get() = mutableCurrentGeoPoint

    private val mutableRequestingLocationUpdates = MutableLiveData<Boolean>()
    val requestingLocationUpdates: LiveData<Boolean> get() = mutableRequestingLocationUpdates

    companion object {
        const val TAG = "LocationViewModel"
    }


    fun changeLocation(location: Location) {
        mutableLocation.value = location
        mutableCurrentGeoPoint.value = GeoPoint(location.latitude, location.longitude)
    }

    fun setupLocationPermissions(activity: AppCompatActivity) {

        val tempLocation = Location("")
        tempLocation.longitude = activity.resources.getString(R.string.Westernhohe_long).toDouble()
        tempLocation.latitude = activity.resources.getString(R.string.Westernhohe_lat).toDouble()
        
        val requestPermission = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            Log.d(TAG, "isGranted: $isGranted")
            mutableRequestingLocationUpdates.value = if (isGranted) {
                Toast.makeText(activity, "Location Permission given", Toast.LENGTH_SHORT).show()
                true
            } else {
                Toast.makeText(activity, "No Location Permission", Toast.LENGTH_SHORT).show()
                changeLocation(tempLocation)
                false
            }
        }

        Log.d(TAG, "Check for Perm")
        when {
            ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
                    PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "Permission granted")
                mutableRequestingLocationUpdates.value = true
                // You can use the API that requires the permission.
            }
            activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Log.d(TAG, "creating Permission Explanation")
                val builder: AlertDialog.Builder = activity.let {
                    AlertDialog.Builder(it)
                }

                builder.setTitle("Standort Berechtigung")
                builder.setMessage("Fahnenklauen berechnet aus eurem Standort die Entfernung zu gelisteten Camps und richtet die Karte entsprechend aus.\nOhne Berechtigung wird Westernhohe als Standort gewählt")
                builder.apply {
                    setPositiveButton("OK"
                    ) { dialog, it ->
                        requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    setNegativeButton("Nein Danke"
                    ) { dialog, it ->
                        mutableRequestingLocationUpdates.value = false
                        changeLocation(tempLocation)
                    }
                }

                builder.create().show()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                Log.d(TAG, "Permission else")
                requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

        }

    }
}