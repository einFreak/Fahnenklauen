package com.felixwild.fahnenklauen.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.felixwild.fahnenklauen.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapViewModel: MapViewModel
    private lateinit var mMap: GoogleMap
    private val TAG = "MapFragment"
    private var currentLocation: Location = Location("")
    private var current = LatLng(0.0, 0.0)
    private var requestingLocationUpdates = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        getSharedPreferences()

        return root
    }

   private fun getSharedPreferences () {
       val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
       requestingLocationUpdates = sharedPreferences.getBoolean(getString(R.string.LocationPermission), false)
       Log.d(TAG, "reqLoq value: $requestingLocationUpdates")
   }



    @SuppressLint("MissingPermission")
    private fun getLocation() {
        Log.d(TAG, "value fusedLocCLient: $fusedLocationClient")
        fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        setAndMoveToLocation(location)
                    } else {
                        Log.d(TAG, "Location is null")
                    }
                }
    }

    private fun setAndMoveToLocation(location: Location) {
        currentLocation = location
        current = LatLng(currentLocation.latitude, currentLocation.longitude)
        mMap.addMarker(MarkerOptions().position(current).title("current Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 10F))
    }

    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            mMap = p0
        }

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))

        setupLocation(permission = requestingLocationUpdates)
    }

    private fun setupLocation(permission: Boolean) {
        if (permission) {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION))
                getLocation()
            else
                Log.wtf(TAG, "No Location Permission but Bundle Information says otherwise")
        }
        else {
            Toast.makeText(this.context, "No Permission - setting to default location", Toast.LENGTH_SHORT).show()
            var location = Location("")
            location.longitude = resources.getString(R.string.Westernhohe_long).toDouble()
            location.latitude = resources.getString(R.string.Westernhohe_lat).toDouble()
            Log.d(TAG, "WesternhoheLong: ${location.longitude}, WesternhoheLat: ${location.latitude}")
            setAndMoveToLocation(location)
        }

    }

}

