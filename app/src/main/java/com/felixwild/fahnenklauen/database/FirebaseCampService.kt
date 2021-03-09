package com.felixwild.fahnenklauen.database

import android.location.Location
import android.util.Log
import androidx.activity.viewModels
import androidx.constraintlayout.helper.widget.Flow
import com.felixwild.fahnenklauen.database.Camp.Companion.toCamp
import com.felixwild.fahnenklauen.viewModels.LocationViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*

object FirebaseCampService {
    private const val TAG = "FirebaseCampService"
    private const val campCollection = "Camps"

    suspend fun getCampData(campId: String): Camp? {
        val db = FirebaseFirestore.getInstance()
        return try {
            db.collection(campCollection)
                .document(campId).get().await().toCamp()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user details", e)
            FirebaseCrashlytics.getInstance().log("Error getting user details")
            FirebaseCrashlytics.getInstance().setCustomKey("user id", "einFreak")
            FirebaseCrashlytics.getInstance().recordException(e)
            null
        }
    }

    suspend fun getAllCampData(): List<Camp> {
        val db = FirebaseFirestore.getInstance()
        return try {
            db.collection(campCollection).get().await().documents.mapNotNull { it.toCamp() }
        } catch (e: Exception) {
            if(e is FirebaseFirestoreException)
                Log.e(TAG, "Error getting all camp data", e)
            FirebaseCrashlytics.getInstance().log("Error getting all camp data")
            FirebaseCrashlytics.getInstance().recordException(e)
            emptyList()
        }
    }

    suspend fun getMyCampsData(userId: String): List<Camp> {
        val db = FirebaseFirestore.getInstance()
        return try {
            db.collection(campCollection)
                .whereEqualTo("owner", userId)
                .get().await().documents.mapNotNull { it.toCamp() }
        } catch (e: Exception) {
            if(e is FirebaseFirestoreException)
                Log.e(TAG, "Error getting owner camp data", e)
            FirebaseCrashlytics.getInstance().log("Error getting owner camp data")
            FirebaseCrashlytics.getInstance().recordException(e)
            emptyList()
        }
    }

    fun addCamp(campName: String, kidsActive: Boolean, tagOnly: Boolean, flag: Boolean, hasAdditionalRules: Boolean, numberParticipants: Double, additRules: String, currentRating: Double, location: GeoPoint) {
        val db = FirebaseFirestore.getInstance()

        val user = hashMapOf(
                "campName" to campName,
                "kidsActive" to kidsActive,
                "tagOnly" to tagOnly,
                "flag" to flag,
                "hasAdditionalRules" to hasAdditionalRules,
                "numberParticipants" to numberParticipants,
                "additionalRules" to additRules,
                "currentRating" to currentRating,
                "location" to location,
                "owner" to (FirebaseAuth.getInstance().currentUser?.uid ?: "")
        )

        try {
            db.collection("Camps")
                .add(user)
                .addOnSuccessListener { Log.d(TAG, "Camp added")}
                .addOnFailureListener { Log.d(TAG, "adding Camp failed")}
        } catch (e: Exception) {
            Log.e(TAG, "Error adding Camp", e)
            FirebaseCrashlytics.getInstance().log("Error adding Camp")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    fun addCamp(currentCamp: Camp) {
        val db = FirebaseFirestore.getInstance()

        val user = hashMapOf(
                "campName" to currentCamp.campName,
                "kidsActive" to currentCamp.kidsActive,
                "tagOnly" to currentCamp.tagOnly,
                "flag" to currentCamp.flag,
                "hasAdditionalRules" to currentCamp.hasAdditionalRules,
                "numberParticipants" to currentCamp.numberParticipants,
                "additionalRules" to currentCamp.additionalRules,
                "currentRating" to currentCamp.currentRating,
                "location" to currentCamp.location,
                "owner" to (FirebaseAuth.getInstance().currentUser?.uid ?: "")
        )

        try {
            db.collection("Camps")
                    .add(user)
                    .addOnSuccessListener { Log.d(TAG, "currentCamp added")}
                    .addOnFailureListener { Log.d(TAG, "adding currentCamp failed")}
        } catch (e: Exception) {
            Log.e(TAG, "Error adding Camp", e)
            FirebaseCrashlytics.getInstance().log("Error adding currentCamp")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    fun removeCamp(CampId: String) {
        val db = FirebaseFirestore.getInstance()
        try {
            db.collection("Camps").document(CampId)
                .delete()
                .addOnSuccessListener { Log.d(TAG, "Camp deleted") }
                .addOnFailureListener { Log.d(TAG, "deleting Camp failed")}
        } catch (e: Exception) {
            Log.e(TAG, "Error adding Camp", e)
            FirebaseCrashlytics.getInstance().log("Error adding Camp")
            FirebaseCrashlytics.getInstance().setCustomKey("user id", "einFreak")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

}