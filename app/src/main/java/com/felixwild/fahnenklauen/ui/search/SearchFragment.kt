package com.felixwild.fahnenklauen.ui.search

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.felixwild.fahnenklauen.MainActivity
import com.felixwild.fahnenklauen.R
import com.felixwild.fahnenklauen.database.Camp
import com.felixwild.fahnenklauen.database.CampAdapter
import com.felixwild.fahnenklauen.database.FirebaseCampService
import com.felixwild.fahnenklauen.ui.addCamp.AddCampFragment
import com.felixwild.fahnenklauen.viewModels.CampViewModel
import com.felixwild.fahnenklauen.viewModels.LocationViewModel
import com.felixwild.fahnenklauen.viewModels.LoginViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport


class SearchFragment : Fragment() {

    private lateinit var floatingActionButton: FloatingActionButton
    private val TAG = "SearchFragment"
    private lateinit var rvAdapter: CampAdapter
    private var currentLocation = Location("")
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()
    private val campViewModel: CampViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_search, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataToRV()
        initSearch()
        setupFabOnClickListener()

        locationViewModel.currentLocation.observe(viewLifecycleOwner, Observer { location ->
            currentLocation = location
        })
    }

    override fun onResume() {
        super.onResume()
        campViewModel.refreshAllCamps()
        getDataToRV()
    }

    private fun getDataToRV() {

        val rv = view?.findViewById<RecyclerView>(R.id.recycler_view_camps)
        rv?.layoutManager = LinearLayoutManager(activity)

        // Draws horizontal lines between the list items
        rv?.addItemDecoration( DividerItemDecoration(rv.context, DividerItemDecoration.VERTICAL) )

        rvAdapter = CampAdapter()
        rv?.adapter = rvAdapter
        var allCamps: List<Camp> = ArrayList()
        campViewModel.allCamps.observe(viewLifecycleOwner, Observer { camps ->
            allCamps = campViewModel.sortCampData(camps, currentLocation)
            rvAdapter.setCamps(allCamps, currentLocation)
        })

        val swipeHandler = object : SwipeToDelete(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val currentCamp: Camp? = rvAdapter.getCampAt(viewHolder.adapterPosition)

                if (currentCamp != null) {
                    campViewModel.removeCamp(currentCamp.campID)
                    campViewModel.refreshAllCamps()

                    Snackbar.make(viewHolder.itemView, currentCamp.campName+" wurde entfernt", Snackbar.LENGTH_SHORT).setAction("Wiederherstellen") {
                        campViewModel.addCamp(
                                currentCamp.campName,
                                currentCamp.kidsActive,
                                currentCamp.tagOnly,
                                currentCamp.flag,
                                currentCamp.hasAdditionalRules,
                                currentCamp.numberParticipants.toDouble(),
                                currentCamp.additionalRules,
                                currentCamp.currentRating.toDouble(),
                                currentCamp.location
                        )
                        campViewModel.refreshAllCamps()
                    }.show()
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(rv)

    }

    private fun initSearch() {
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
    }

    private fun setupFabOnClickListener() {

        loginViewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when(authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> floatingActionButton.visibility = View.VISIBLE
                else -> floatingActionButton.visibility = View.GONE
            }
        })

        val action = SearchFragmentDirections.actionNavigationSearchToNavigationAddCamp()
        floatingActionButton = requireView().findViewById<View>(R.id.fab_addCamp) as FloatingActionButton
        floatingActionButton.setOnClickListener {
            findNavController().navigate(action)
        }
    }

}