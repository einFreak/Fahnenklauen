package com.felixwild.fahnenklauen.ui.home

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.felixwild.fahnenklauen.viewModels.LocationViewModel
import com.felixwild.fahnenklauen.R

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private val locationViewModel: LocationViewModel by activityViewModels()
    private var currentLocation: Location = Location("")

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val textView: TextView = root.findViewById(R.id.text_home)
//        homeViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })

        locationViewModel.currentLocation.observe(viewLifecycleOwner, Observer { location ->
            currentLocation = location
            textView.text = currentLocation.toString()
        })


        return root
    }
}