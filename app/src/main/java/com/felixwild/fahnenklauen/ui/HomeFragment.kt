package com.felixwild.fahnenklauen.ui

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
import androidx.navigation.fragment.findNavController
import com.felixwild.fahnenklauen.MainActivity
import com.felixwild.fahnenklauen.viewModels.LocationViewModel
import com.felixwild.fahnenklauen.R
import com.felixwild.fahnenklauen.ui.search.SearchFragmentDirections
import com.felixwild.fahnenklauen.viewModels.LoginViewModel
import com.firebase.ui.auth.AuthUI
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private val locationViewModel: LocationViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val textLat: TextView = root.findViewById(R.id.text_lat)
        val textLong: TextView = root.findViewById(R.id.text_long)
        val btnLogin: Button = root.findViewById(R.id.btn_login)
        val btnManageCamps: Button = root.findViewById(R.id.btn_manageCamps)

        locationViewModel.currentLocation.observe(viewLifecycleOwner, { location ->
            textLat.text = location.latitude.toString()
            textLong.text = location.longitude.toString()
        })

        loginViewModel.authenticationState.observe(viewLifecycleOwner, { authenticationState ->
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

        btnManageCamps.setOnClickListener {
            val action = HomeFragmentDirections.actionNavigationHomeToMyCamps()
            findNavController().navigate(action)
        }
        return root
    }

}