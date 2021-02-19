package com.felixwild.fahnenklauen.database

import android.util.Log
import androidx.constraintlayout.helper.widget.Flow
import com.felixwild.fahnenklauen.database.Camp.Companion.toCamp
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

object FirebaseCampService {
    private const val TAG = "FirebaseProfileService"
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
         try {
             return db.collection(campCollection).get().await().documents.mapNotNull { it.toCamp() }
        } catch (e: Exception) {
             Log.e(TAG, "Error getting user friends", e)
             FirebaseCrashlytics.getInstance().log("Error getting user friends")
             FirebaseCrashlytics.getInstance().setCustomKey("user id", "einFreak")
             FirebaseCrashlytics.getInstance().recordException(e)
             return emptyList()
        }
    }

    /*
    @ExperimentalCoroutinesApi
    fun getAllCampData2(): kotlinx.coroutines.flow.Flow<List<Camp>> {
        val db = FirebaseFirestore.getInstance()
        return callbackFlow {
            val listenerRegistration = db.collection(campCollection)
                .addSnapshotListener { querySnapshot: QuerySnapshot?,
                                       firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null) {
                        cancel(
                            message = "Error fetching posts",
                            cause = firebaseFirestoreException
                        )
                        return@addSnapshotListener
                    }
                    val map = querySnapshot.documents
                        .mapNotNull { it.toCamp() }
                    offer(map)
                }
            awaitClose {
                Log.d(TAG, "Cancelling posts listener")
                listenerRegistration.remove()
            }
        }
    }*/

}