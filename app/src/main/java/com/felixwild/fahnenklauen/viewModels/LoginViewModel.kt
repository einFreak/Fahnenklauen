package com.felixwild.fahnenklauen.viewModels

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.felixwild.fahnenklauen.MainActivity
import com.felixwild.fahnenklauen.database.FirebaseUserLiveData
import com.firebase.ui.auth.AuthUI

open class LoginViewModel  : ViewModel() {

    companion object {
        const val TAG = "LoginViewModel"
        const val RC_SIGN_IN = 1002
        // Choose authentication providers
        val loginProviders = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())
    }

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    fun createSignInIntent(activity: AppCompatActivity) {
        // Create and launch sign-in intent
        activity.startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(LoginViewModel.loginProviders)
                .build(),
            RC_SIGN_IN
        )
    }
}

