package com.felixwild.fahnenklauen.viewModels

import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {
    private val mutableLocation = MutableLiveData<Location>()
    val currentLocation: LiveData<Location> get() = mutableLocation

    private val mutableCurrentGeoPoint = MutableLiveData<GeoPoint>()
    val currentGeoPoint: LiveData<GeoPoint> get() = mutableCurrentGeoPoint


    fun changeLocation(location: Location) {
        mutableLocation.value = location
        mutableCurrentGeoPoint.value = GeoPoint(location.latitude, location.longitude)
    }

}