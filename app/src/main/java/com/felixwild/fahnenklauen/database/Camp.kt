package com.felixwild.fahnenklauen.database

import android.location.Location
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint

data class Camp (var userId: String,
                 var campName: String,
                 var kidsActive: Boolean,
                 var tagOnly: Boolean,
                 var numberParticipants: Int,
                 var additionalRules: String,
                 var currentRating: Int,
                 var location: GeoPoint,
                 var distanceInM: Float) {

    companion object {
        fun DocumentSnapshot.toCamp(): Camp? {
            try {
                val campName = getString("campName")!!
                val kidsActive = getBoolean("kidsActive")!!
                val tagOnly= getBoolean("tagOnly")!!
                val numberParticipants = getDouble("numberParticipants")!!.toInt()
                val additionalRules = getString("additionalRules")!!
                val currentRating: Int = getDouble("currentRating")!!.toInt()
                val location = getGeoPoint("location")!!

                return Camp(id, campName, kidsActive, tagOnly, numberParticipants, additionalRules, currentRating, location, 0.0F)
            } catch (e: Exception) {
                Log.e(TAG, "Error converting user profile", e)
                FirebaseCrashlytics.getInstance().log("Error converting user profile")
                FirebaseCrashlytics.getInstance().setCustomKey("userId", id)
                FirebaseCrashlytics.getInstance().recordException(e)
                return null
            }

        }
        private const val TAG = "User"
    }




}