package com.felixwild.fahnenklauen.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.felixwild.fahnenklauen.R
import com.felixwild.fahnenklauen.viewModels.CampViewModel
import com.felixwild.fahnenklauen.viewModels.LocationViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.GeoPoint


class AddCampFragment: Fragment(), OnMapReadyCallback {

    private val locationViewModel: LocationViewModel by activityViewModels()
    private val campViewModel: CampViewModel by activityViewModels()
    private var currentGeoPoint: GeoPoint = GeoPoint(0.0, 0.0)
    private lateinit var mMap: GoogleMap
    private lateinit var buttonAddCamp: Button
    private lateinit var buttonShowMap: Button
    private lateinit var buttonCloseMap: ImageButton
    private lateinit var frameMap: FrameLayout
    private lateinit var inputCampName : TextInputEditText
    private lateinit var inputNumberParticipants : TextInputEditText
    private lateinit var inputAdditionalRules : TextInputEditText
    private lateinit var inputLatitude : TextInputEditText
    private lateinit var inputLongitude : TextInputEditText
    private lateinit var checkAdditionalRules : CheckBox
    private lateinit var checkTagOnly : CheckBox
    private lateinit var checkKidsActive : CheckBox
    private lateinit var inputAdditionalRulesLayout : TextInputLayout
    private val TAG = "AddCampFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_camp_add, container, false)

        locationViewModel.currentGeoPoint.observe(viewLifecycleOwner, { location ->
            currentGeoPoint = location
        })

        val mapFragment = childFragmentManager.findFragmentById(R.id.addCamp_map) as SupportMapFragment?

        mapFragment?.getMapAsync(this)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inputCampName = requireActivity().findViewById(R.id.inputCampName)
        inputNumberParticipants = requireActivity().findViewById(R.id.inputNumberParticipants)
        inputAdditionalRules = requireActivity().findViewById(R.id.inputAdditionalRules)
        inputLatitude = requireActivity().findViewById(R.id.inputLatitude)
        inputLongitude = requireActivity().findViewById(R.id.inputLongitude)
        inputAdditionalRulesLayout = requireView().findViewById(R.id.inputAdditionalRulesLayout)
        buttonCloseMap = requireView().findViewById(R.id.btn_closeSelectionMap)
        buttonShowMap = requireView().findViewById(R.id.btn_showSelectionMap)
        buttonAddCamp = requireView().findViewById(R.id.btn_update_camp_commit)
        frameMap = requireView().findViewById(R.id.frame_SelectionMap)
        checkAdditionalRules = requireView().findViewById(R.id.checkAddtitionalRules)
        checkKidsActive = requireView().findViewById(R.id.checkKidsActive)
        checkTagOnly = requireView().findViewById(R.id.checkTagOnly)
    }

    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            mMap = p0
        }
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    currentGeoPoint.latitude,
                    currentGeoPoint.longitude
                ), 10F
            )
        )
        setupOnClickListener()

        mMap.setOnMapClickListener {
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(it))
            inputLatitude.setText(it.latitude.toString())
            inputLongitude.setText(it.longitude.toString())

        }
    }

    private fun addDataToFirestore() {
        val additRules: String = if (!checkAdditionalRules.isChecked) ""
        else
            inputAdditionalRules.text.toString()

        campViewModel.addCamp(
            campName = inputCampName.text.toString(),
            kidsActive = checkKidsActive.isChecked,
            tagOnly = checkTagOnly.isChecked,
            flag = true,
            hasAdditionalRules = checkAdditionalRules.isChecked,
            numberParticipants = inputNumberParticipants.text.toString().toDouble(),
            additionalRules = additRules,
            currentRating = 0.0,
            location = GeoPoint(
                inputLatitude.text.toString().toDouble(),
                inputLongitude.text.toString().toDouble()
            )
        )
        Log.i(TAG, "Camp added")

    }

    private fun checkInputOk() : Boolean {
        if (inputCampName.text.isNullOrEmpty())
            inputCampName.error = getString(R.string.required_field)
        if (inputLatitude.text.isNullOrEmpty())
            inputLatitude.error = getString(R.string.required_field)
        if (inputLongitude.text.isNullOrEmpty())
            inputLongitude.error = getString(R.string.required_field)
        if (inputNumberParticipants.text.isNullOrEmpty())
            inputNumberParticipants.error = getString(R.string.required_field)
        if (inputCampName.text.isNullOrEmpty() || inputLatitude.text.isNullOrEmpty() || inputLongitude.text.isNullOrEmpty() || inputNumberParticipants.text.isNullOrEmpty())
            return false
        return true

    }

    private fun setupOnClickListener() {

        buttonAddCamp.setOnClickListener {
            if (checkInputOk()) {
                addDataToFirestore()
                Snackbar.make(
                    requireView(),
                    "Lager hinzugefügt",
                    Snackbar.LENGTH_LONG
                ).show()
                requireActivity().onBackPressed()
            }
            else
                Snackbar.make(
                    requireView(),
                    "Bitte erforderliche Felder ausfüllen",
                    Snackbar.LENGTH_LONG
                ).show()
        }

        buttonShowMap.setOnClickListener {
            frameMap.visibility = View.VISIBLE
        }

        buttonCloseMap.setOnClickListener {
            frameMap.visibility = View.GONE
        }

        checkAdditionalRules.setOnClickListener {
            if(checkAdditionalRules.isChecked) {
                inputAdditionalRulesLayout.visibility = View.VISIBLE
            } else
                inputAdditionalRulesLayout.visibility = View.GONE
        }


    }

}
