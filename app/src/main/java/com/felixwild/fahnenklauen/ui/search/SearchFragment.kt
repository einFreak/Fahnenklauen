package com.felixwild.fahnenklauen.ui.search

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.felixwild.fahnenklauen.R
import com.felixwild.fahnenklauen.database.Camp
import com.felixwild.fahnenklauen.database.CampAdapter
import com.felixwild.fahnenklauen.viewModels.CampViewModel
import com.felixwild.fahnenklauen.viewModels.LocationViewModel
import com.felixwild.fahnenklauen.viewModels.LoginViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.GeoPoint


class SearchFragment : Fragment() {

    private lateinit var floatingActionButton: FloatingActionButton
    private val TAG = "SearchFragment"
    private var rvAdapter: CampAdapter = CampAdapter()
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
        getDataToRV()
    }

    private fun getDataToRV() {


        val textViewLogin = view?.findViewById<TextView>(R.id.rv_text_overlay)
        val sampleCamp = view?.findViewById<ConstraintLayout>(R.id.rv_sample_camp)

        loginViewModel.authenticationState.observe(viewLifecycleOwner, Observer { state ->
            campViewModel.setAuthState(state)
            campViewModel.refreshAllCamps()
            val rv = view?.findViewById<RecyclerView>(R.id.recycler_view_camps)
            rv?.layoutManager = LinearLayoutManager(activity)

            // Draws horizontal lines between the list items
            rv?.addItemDecoration( DividerItemDecoration(rv.context, DividerItemDecoration.VERTICAL) )
            rv?.adapter = rvAdapter
            var allCamps: List<Camp> = emptyList()
            rvAdapter.setCamps(allCamps, currentLocation)

            if (state == LoginViewModel.AuthenticationState.AUTHENTICATED) {

                textViewLogin?.visibility = View.GONE
                sampleCamp?.visibility = View.GONE

                campViewModel.allCamps.observe(viewLifecycleOwner, Observer { camps ->
                    if (loginViewModel.authenticationState.value == LoginViewModel.AuthenticationState.AUTHENTICATED)
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
            else {
                if (campViewModel.allCamps.hasObservers())
                    campViewModel.allCamps.removeObservers(viewLifecycleOwner)
                textViewLogin?.visibility = View.VISIBLE
                sampleCamp?.visibility = View.VISIBLE
                rvAdapter.setCamps(emptyList(), currentLocation)
                rvAdapter.notifyDataSetChanged()
            }
        })


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