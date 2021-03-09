package com.felixwild.fahnenklauen.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.felixwild.fahnenklauen.R
import com.felixwild.fahnenklauen.database.Camp
import com.felixwild.fahnenklauen.viewModels.CampViewModel
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


class EditCampFragment: Fragment(), OnMapReadyCallback {

    private val campViewModel: CampViewModel by activityViewModels()
    private lateinit var thisCamp: Camp
    private lateinit var mMap: GoogleMap
    private lateinit var buttonUpdateCamp: Button
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
    private lateinit var ratingBar: RatingBar
    private val TAG = "AddCampFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_camp_edit, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.addCamp_map) as SupportMapFragment?

        mapFragment?.getMapAsync(this)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inputCampName = requireView().findViewById(R.id.inputCampName)
        inputNumberParticipants = requireView().findViewById(R.id.inputNumberParticipants)
        inputAdditionalRules = requireView().findViewById(R.id.inputAdditionalRules)
        inputLatitude = requireView().findViewById(R.id.inputLatitude)
        inputLongitude = requireView().findViewById(R.id.inputLongitude)
        inputAdditionalRulesLayout = requireView().findViewById(R.id.inputAdditionalRulesLayout)
        buttonCloseMap = requireView().findViewById(R.id.btn_closeSelectionMap)
        buttonShowMap = requireView().findViewById(R.id.btn_showSelectionMap)
        buttonUpdateCamp = requireView().findViewById(R.id.btn_update_camp_commit)
        frameMap = requireView().findViewById(R.id.frame_SelectionMap)
        checkAdditionalRules = requireView().findViewById(R.id.checkAddtitionalRules)
        checkKidsActive = requireView().findViewById(R.id.checkKidsActive)
        checkTagOnly = requireView().findViewById(R.id.checkTagOnly)
        ratingBar = requireView().findViewById(R.id.ratingBar_selected_camp)

        val args: EditCampFragmentArgs by navArgs()
//
//        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//            val tv: TextView = view.findViewById(R.id.textViewAmount)
//            val amount = args.amount
//            tv.text = amount.toString()
//        }
        campViewModel.getCamp(args.selectedCampId)
        campViewModel.selectedCamp.observe(viewLifecycleOwner, {
            thisCamp = it
            inputCampName.setText(it.campName)
            inputNumberParticipants.setText(it.numberParticipants.toString())
            inputAdditionalRules.setText(it.additionalRules.toString())
            inputLatitude.setText(it.location.latitude.toString())
            inputLongitude.setText(it.location.longitude.toString())
            if (it.hasAdditionalRules) {
                inputAdditionalRulesLayout.visibility = View.VISIBLE
            } else {
                inputAdditionalRulesLayout.visibility = View.GONE
            }
            checkKidsActive.isChecked = it.kidsActive
            checkTagOnly.isChecked = it.tagOnly
            checkAdditionalRules.isChecked = it.hasAdditionalRules
            ratingBar.rating = it.currentRating

        })

    }

    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            mMap = p0
        }
//        mMap.moveCamera(
//            CameraUpdateFactory.newLatLngZoom(
//                LatLng(
//                    currentGeoPoint.latitude,
//                    currentGeoPoint.longitude
//                ), 10F
//            )
//        )
        setupOnClickListener()

        mMap.setOnMapClickListener {
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(it))
            inputLatitude.setText(it.latitude.toString())
            inputLongitude.setText(it.longitude.toString())

        }
    }

    private fun updateDataAtFirestore() {
        val additRules: String = if (!checkAdditionalRules.isChecked) ""
        else
            inputAdditionalRules.text.toString()

//        campName = inputCampName.text.toString(),
//        kidsActive = checkKidsActive.isChecked,
//        tagOnly = checkTagOnly.isChecked,
//        flag = true,
//        hasAdditionalRules = checkAdditionalRules.isChecked,
//        numberParticipants = inputNumberParticipants.text.toString().toDouble(),
//        additionalRules = additRules,
//        currentRating = 0.0,
//        location = GeoPoint(
//                inputLatitude.text.toString().toDouble(),
//                inputLongitude.text.toString().toDouble()
//        )
//
//
//        campViewModel.updateCamp(
//            campName = inputCampName.text.toString(),
//            kidsActive = checkKidsActive.isChecked,
//            tagOnly = checkTagOnly.isChecked,
//            flag = true,
//            hasAdditionalRules = checkAdditionalRules.isChecked,
//            numberParticipants = inputNumberParticipants.text.toString().toDouble(),
//            additionalRules = additRules,
//            currentRating = 0.0,
//            location = GeoPoint(
//                inputLatitude.text.toString().toDouble(),
//                inputLongitude.text.toString().toDouble()
//            )
//        )
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

        buttonUpdateCamp.setOnClickListener {
            if (checkInputOk()) {
//                TODO updateDataAtFirestore()
                Snackbar.make(
                    requireView(),
                    "Lager aktualisiert",
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
