package com.felixwild.fahnenklauen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.felixwild.fahnenklauen.viewModels.LocationViewModel
import com.felixwild.fahnenklauen.viewModels.LoginViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var requestingLocationUpdates: Boolean = false
    private val locationViewModel: LocationViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val locationRequest = LocationRequest.create().apply {
        interval = 5000
        fastestInterval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private var isLoggedIn = false

    companion object {
        const val TAG = "MainFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updateValuesFromBundle(savedInstanceState)
        setSupportActionBar(findViewById(R.id.toolbar))

        init()
    }

    private fun init() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    locationViewModel.changeLocation(location)
                }
            }
        }

        locationViewModel.setupLocationPermissions(this)
        locationViewModel.requestingLocationUpdates.observe(this, {
            requestingLocationUpdates = it
            if (it)
                startLocationUpdates()
            else
                stopLocationUpdates()
        })

        setupBottomNav()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)

        val loginBtn = menu?.findItem(R.id.appbar_logout)

        loginViewModel.authenticationState.observe(this, { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    loginBtn?.title = getString(R.string.logout)
                    isLoggedIn = true
                }
                else -> {
                    loginBtn?.title = getString(R.string.login)
                    isLoggedIn = false
                }
            }

        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.appbar_logout -> {
            if(isLoggedIn) {
                val user = FirebaseAuth.getInstance().currentUser
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener {
                            Snackbar.make(findViewById(android.R.id.content), user?.displayName.toString() + getString(R.string.user_logged_out), Snackbar.LENGTH_SHORT).show()
                        }
            }
            else {
                loginViewModel.createSignInIntent(this)
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
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

    //allows back button through NavGraph
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LoginViewModel.RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                Log.i(TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
                if (user != null) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.hello_user) + user.displayName, Snackbar.LENGTH_SHORT).show()
                }

            } else {
                if (response == null) {
                    Log.i(TAG, "User canceled Login")
                    FirebaseCrashlytics.getInstance().log("User canceled Login")
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.canceled_login_message), Snackbar.LENGTH_LONG).show()

                }
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...

                val e = response?.error
                FirebaseCrashlytics.getInstance().log("Error getting all camp data")
                if (e != null) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Log.i(TAG, "Sign in unsuccessful ${e.errorCode}")
                }
            }
        }
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

    }

}