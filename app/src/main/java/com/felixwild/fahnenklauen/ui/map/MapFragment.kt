package com.felixwild.fahnenklauen.ui.map

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
import com.felixwild.fahnenklauen.database.LocationViewModel
import com.felixwild.fahnenklauen.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapViewModel: MapViewModel
    private lateinit var mMap: GoogleMap
    private val TAG = "MapFragment"
    private var current = LatLng(0.0, 0.0)
    private val locationViewModel: LocationViewModel by activityViewModels()
    private var currentLocation: Location = Location("")

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mapViewModel =
                ViewModelProvider(this).get(MapViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_map, container, false)

        val textView: TextView = root.findViewById(R.id.text_map)
        mapViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        mapFragment?.getMapAsync(this)

        locationViewModel.currentLocation.observe(viewLifecycleOwner, Observer { location ->
            currentLocation = location
            current = LatLng(currentLocation.latitude, currentLocation.longitude)
        })

        return root
    }

    private fun setAndMoveToLocation(location: LatLng) {
        mMap.addMarker(MarkerOptions().position(location).title("current Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 10F))
    }

    private fun setCircleToLocationWithRadInM(location: LatLng, radius: Double) {
        val circle = CircleOptions()
        circle.center(location)
        circle.radius(radius)
        circle.visible(true)
        circle.strokeColor(R.color.purple_500)

        mMap.addCircle(circle)
    }

    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            mMap = p0
        }

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        setAndMoveToLocation(current)
        setCircleToLocationWithRadInM(current, 2000.0)

    }



}

