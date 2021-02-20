package com.felixwild.fahnenklauen.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.felixwild.fahnenklauen.R
import com.felixwild.fahnenklauen.viewModels.CampViewModel
import com.felixwild.fahnenklauen.viewModels.LocationViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapViewModel: MapViewModel
    private lateinit var mMap: GoogleMap
    private val TAG = "MapFragment"
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val campViewModel: CampViewModel by activityViewModels()
    private var current = LatLng(0.0, 0.0)
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

        return root
    }

    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            mMap = p0
        }

        mMap.clear()

        setCampsOnMap()

        locationViewModel.currentLocation.observe(viewLifecycleOwner, Observer { location ->
            currentLocation = location
            current = LatLng(currentLocation.latitude, currentLocation.longitude)
            setCurrentLocation(current)
            setCircleToLocationWithRadInM(current, 2000.0)
        })
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 10F))

    }

    private fun setCampsOnMap() {
        campViewModel.allCamps.observe(viewLifecycleOwner, Observer { camps ->
            for (row in camps) {
                val newMark = MarkerOptions().position(LatLng(row.location.latitude, row.location.longitude)).title(row.campName)
                mMap.addMarker(newMark)
            }
        })
    }

    fun getBitmapFromVector(
        context: Context,
        @DrawableRes vectorResourceId: Int,
        @ColorInt tintColor: Int
    ): BitmapDescriptor? {
        val vectorDrawable = ResourcesCompat.getDrawable(
            context.getResources(), vectorResourceId, null
        )
        if (vectorDrawable == null) {
            Log.e(TAG, "Requested vector resource was not found")
            return BitmapDescriptorFactory.defaultMarker()
        }
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, tintColor)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun setCurrentLocation(location: LatLng) {
        val locationMarkerIcon = getBitmapFromVector(
            requireContext(),
            R.drawable.ic_baseline_trip_origin_24,
            resources.getColor(R.color.purple_200));

        var newMarker = MarkerOptions()
            .anchor(0.5F, 0.5F)
            .position(location)
            .title("current Location")
            .icon(locationMarkerIcon)
        mMap.addMarker(newMarker)
    }

    private fun setCircleToLocationWithRadInM(location: LatLng, radius: Double) {
        val circle = CircleOptions()
        circle.center(location)
        circle.radius(radius)
        circle.visible(true)
        circle.strokeColor(R.color.purple_500)

        mMap.addCircle(circle)
    }

}

