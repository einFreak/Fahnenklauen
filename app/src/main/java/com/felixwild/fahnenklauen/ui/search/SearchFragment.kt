package com.felixwild.fahnenklauen.ui.search

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.felixwild.fahnenklauen.R
import com.felixwild.fahnenklauen.database.Camp
import com.felixwild.fahnenklauen.database.CampAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query


class SearchFragment : Fragment() {

    private lateinit var searchViewModel: SearchViewModel
    private var mFirestore = FirebaseFirestore.getInstance()
    private var campCollection = mFirestore.collection("Camps")
    private val TAG = "SearchFragment"
    private lateinit var rvAdapter: CampAdapter
    private var requestingLocationUpdates = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location = Location("")


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        searchViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_search, container, false)

        //calls string for 1 Text View
        val textView: TextView = root.findViewById(R.id.text_dashboard)
        searchViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        getSharedPreferences()
        getCurrentLocation()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataToRV()

        //TODO initSearch()
    }

    private fun getSharedPreferences () {
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        requestingLocationUpdates = sharedPreferences.getBoolean(getString(R.string.LocationPermission), false)
        currentLocation.longitude = sharedPreferences.getFloat(getString(R.string.CurrentLocationLong), resources.getString(R.string.Westernhohe_long).toFloat()).toDouble()
        currentLocation.latitude = sharedPreferences.getFloat(getString(R.string.CurrentLocationLat), resources.getString(R.string.Westernhohe_lat).toFloat()).toDouble()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        var tempLocation: Location = Location("")
        tempLocation.longitude = resources.getString(R.string.Westernhohe_long).toDouble()
        tempLocation.latitude = resources.getString(R.string.Westernhohe_lat).toDouble()

        if(requestingLocationUpdates) {
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            currentLocation = location
                            Log.d(TAG, "CurrentLocation is $location")
                            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return@addOnSuccessListener
                            with (sharedPref.edit()) {
                                putFloat(getString(R.string.CurrentLocationLat), location.latitude.toFloat())
                                putFloat(getString(R.string.CurrentLocationLong), location.longitude.toFloat())
                                apply()
                            }

                        } else {
                            Log.d(TAG, "Location is null - setting default")
                            //getDataToRV()
                        }
                    }
        }
    }

    private fun getDataToRV() {
        val rv = view?.findViewById<RecyclerView>(R.id.recycler_view_camps)
        rv?.layoutManager = LinearLayoutManager(activity)

        // Draws horizontal lines between the list items
        rv?.addItemDecoration(
                DividerItemDecoration(
                        rv.context, DividerItemDecoration.VERTICAL
                )
        )

        val allCampsQuery = campCollection.orderBy("campName")
        val distCampsQuery = getDistQuery(currentLocation, 15F)

        val rvOptions = FirestoreRecyclerOptions.Builder<Camp>()
                .setQuery(allCampsQuery, Camp::class.java)
                .build()

        Log.d(TAG, "Location for RV is $currentLocation")
        rvAdapter = CampAdapter(rvOptions, currentLocation)
        rv?.adapter = rvAdapter

    }

    private fun getDistQuery(currentLocation: Location, distanceInKM: Float): Query {
        val lat1km = 0.01
        val long1km = 0.01

        val latMax = currentLocation.latitude + (lat1km*distanceInKM)
        val latMin = currentLocation.latitude - (lat1km*distanceInKM)
        val longMax = currentLocation.longitude + (long1km*distanceInKM)
        val longMin = currentLocation.longitude - (long1km*distanceInKM)

        val lesserGeoPoint = GeoPoint(latMin, longMin)
        val greaterGeoPoint = GeoPoint(latMax, longMax)
        return campCollection.whereGreaterThan("location", lesserGeoPoint).whereLessThan("location", greaterGeoPoint)
    }

    override fun onStart() {
        super.onStart()
        rvAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        rvAdapter.stopListening()
    }

    //TODO: search
    /* private fun initSearch() {
        val search = view?.findViewById<SearchView>(R.id.search_camp)
        search?.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                rvAdapter.filter.filter(newText)
                return false
            }
        })
    } */
}