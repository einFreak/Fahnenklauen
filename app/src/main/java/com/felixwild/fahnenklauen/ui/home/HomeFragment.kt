package com.felixwild.fahnenklauen.ui.home

import android.content.ClipData
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.felixwild.fahnenklauen.MainActivity
import com.felixwild.fahnenklauen.viewModels.LocationViewModel
import com.felixwild.fahnenklauen.R
import com.felixwild.fahnenklauen.viewModels.LoginViewModel
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()
    private var currentLocation: Location = Location("")

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val textLat: TextView = root.findViewById(R.id.text_lat)
        val textLong: TextView = root.findViewById(R.id.text_long)
        val btnLogin: Button = root.findViewById(R.id.btn_login)
        val btnManageCamps: Button = root.findViewById(R.id.btn_manageCamps)

        locationViewModel.currentLocation.observe(viewLifecycleOwner, Observer { location ->
            currentLocation = location
            textLat.text = currentLocation.latitude.toString()
            textLong.text = currentLocation.longitude.toString()
        })

        loginViewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    btnManageCamps.visibility = View.VISIBLE
                    btnLogin.visibility = View.GONE
                }
                else -> {
                    btnLogin.visibility = View.VISIBLE
                    btnManageCamps.visibility = View.GONE
                }
            }
        })

        btnLogin.setOnClickListener {
            // Create and launch sign-in intent
            loginViewModel.createSignInIntent(activity as AppCompatActivity)
        }
        return root
    }

}