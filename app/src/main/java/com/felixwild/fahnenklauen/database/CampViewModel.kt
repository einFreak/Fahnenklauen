package com.felixwild.fahnenklauen.database

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CampViewModel() : ViewModel() {
    private val mutableAllCamps = MutableLiveData<List<Camp>>()
    val allCamps: LiveData<List<Camp>> get() = mutableAllCamps

    private val TAG = "LocationViewModel"

    init {
        Log.d(TAG, "init started")
        viewModelScope.launch {
            mutableAllCamps.value = FirebaseCampService.getAllCampData()
        }
    }

}