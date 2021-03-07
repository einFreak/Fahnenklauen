package com.felixwild.fahnenklauen.viewModels

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixwild.fahnenklauen.database.Camp
import com.felixwild.fahnenklauen.database.FirebaseCampService
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch

class CampViewModel() : ViewModel(){
    private val mutableAllCamps = MutableLiveData<List<Camp>>()
    val allCamps: LiveData<List<Camp>> get() = mutableAllCamps

    private val mutableCamp = MutableLiveData<Camp>()
    val selectedCamp : LiveData<Camp> get() = mutableCamp

    private var authenticationState: LoginViewModel.AuthenticationState = LoginViewModel.AuthenticationState.UNAUTHENTICATED

    private val TAG = "CampViewModel"

    init {
        if(authenticationState == LoginViewModel.AuthenticationState.AUTHENTICATED) {
            viewModelScope.launch {
                mutableAllCamps.value = FirebaseCampService.getAllCampData()
            }
        } else emptyList<Camp>()
    }

    fun setAuthState(passedState: LoginViewModel.AuthenticationState) {
        authenticationState = passedState
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

    fun refreshAllCamps() {
        if(authenticationState == LoginViewModel.AuthenticationState.AUTHENTICATED) {
            viewModelScope.launch {
                mutableAllCamps.value = FirebaseCampService.getAllCampData()
            }
        } else emptyList<Camp>()
    }

    fun addCamp(
            campName: String,
            kidsActive: Boolean,
            tagOnly: Boolean,
            flag: Boolean,
            hasAdditionalRules: Boolean,
            numberParticipants: Double,
            additionalRules: String,
            currentRating: Double,
            location: GeoPoint) {
        viewModelScope.launch {
            FirebaseCampService.addCamp(campName, kidsActive, tagOnly, flag, hasAdditionalRules, numberParticipants, additionalRules, currentRating, location)
        }
    }

    fun addCamp(currentCamp: Camp) {
        viewModelScope.launch {
            FirebaseCampService.addCamp(currentCamp)
        }
    }

    fun removeCamp(campID: String) {
        viewModelScope.launch {
            FirebaseCampService.removeCamp(campID)
        }
    }

    fun getCamp(campID: String?) {
        if (campID != null) {
            viewModelScope.launch {
                mutableCamp.value = FirebaseCampService.getCampData(campID)
            }
        }
    }

}