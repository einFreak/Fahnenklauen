package com.felixwild.fahnenklauen.viewModels

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixwild.fahnenklauen.database.Camp
import com.felixwild.fahnenklauen.database.FirebaseCampService
import kotlinx.coroutines.launch

class CampViewModel() : ViewModel() {
    private val mutableAllCamps = MutableLiveData<List<Camp>>()
    val allCamps: LiveData<List<Camp>> get() = mutableAllCamps

    private val TAG = "LocationViewModel"

    init {
        Log.d(TAG, "init started")
        viewModelScope.launch {
            mutableAllCamps.value =
                FirebaseCampService.getAllCampData()
        }
    }

    fun sortCampData(allCamps: List<Camp>, currentLocation: Location): List<Camp> {
        for (row in allCamps) {
            val currentCampLocation = Location("")
            currentCampLocation.latitude = row.location.latitude
            currentCampLocation.longitude = row.location.longitude
            row.distanceInM = currentLocation.distanceTo(currentCampLocation)
        }
        return allCamps.sortedBy { it.distanceInM }
    }

}