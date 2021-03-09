package com.felixwild.fahnenklauen.ui

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.felixwild.fahnenklauen.R
import com.felixwild.fahnenklauen.database.Camp
import com.felixwild.fahnenklauen.database.CampAdapter
import com.felixwild.fahnenklauen.ui.search.SearchFragmentDirections
import com.felixwild.fahnenklauen.ui.search.SwipeToDeleteHandler
import com.felixwild.fahnenklauen.viewModels.CampViewModel
import com.felixwild.fahnenklauen.viewModels.LocationViewModel
import com.felixwild.fahnenklauen.viewModels.LoginViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class MyCampsFragment : Fragment() {

    private lateinit var floatingActionButton: FloatingActionButton
    private val TAG = "MyCampFragment"
    private var rvAdapter: CampAdapter = CampAdapter(myCamps = true)
    private var rv: RecyclerView? = null
    private var currentLocation = Location("")
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()
    private val campViewModel: CampViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_camps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFabOnClickListener()
        setupRV()

        locationViewModel.currentLocation.observe(viewLifecycleOwner, { location ->
            currentLocation = location
        })

        loginViewModel.authenticationState.observe(viewLifecycleOwner, { state ->
            campViewModel.setAuthState(state)
            getDataToRV(state)
        })
    }

    fun getMyCamps() {
        val user = FirebaseAuth.getInstance().currentUser
        campViewModel.getMyCamps(user?.uid)
    }

    override fun onResume() {
        super.onResume()
        getDataToRV(loginViewModel.authenticationState.value)
    }

    private fun setupRV(){
        rv = view?.findViewById(R.id.recycler_view_my_camps)
        rv?.layoutManager = LinearLayoutManager(activity)

        // Draws horizontal lines between the list items
        rv?.addItemDecoration( DividerItemDecoration(rv?.context, DividerItemDecoration.VERTICAL) )
        rv?.adapter = rvAdapter
        rvAdapter.setCamps(emptyList(), currentLocation)
    }

    private fun getDataToRV(authState: LoginViewModel.AuthenticationState?) {

        val textViewLogin = view?.findViewById<TextView>(R.id.rv_text_overlay)

        if (authState == LoginViewModel.AuthenticationState.AUTHENTICATED) {

            textViewLogin?.visibility = View.GONE
            getMyCamps()

            campViewModel.myCamps.observe(viewLifecycleOwner, { camps ->
                val myCamps = campViewModel.sortCampData(camps, currentLocation)
                rvAdapter.setCamps(myCamps, currentLocation)
            })

            val swipeHandler = createRvSwipeHandler()

            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(rv)
        }
        else {
            if (campViewModel.allCamps.hasObservers())
                campViewModel.allCamps.removeObservers(viewLifecycleOwner)
            textViewLogin?.visibility = View.VISIBLE
            rvAdapter.setCamps(emptyList(), currentLocation)
            rvAdapter.notifyDataSetChanged()
        }
    }

    private fun createRvSwipeHandler(): SwipeToDeleteHandler {
        return object : SwipeToDeleteHandler(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val currentCamp: Camp? = rvAdapter.getCampAt(viewHolder.adapterPosition)

                if (currentCamp != null) {
                    campViewModel.removeCamp(currentCamp.campID)
                    getMyCamps()
                    campViewModel.refreshAllCamps()

                    Snackbar.make(viewHolder.itemView, currentCamp.campName+" wurde entfernt", Snackbar.LENGTH_SHORT).setAction("Wiederherstellen") {
                        campViewModel.addCamp(currentCamp)
                        getMyCamps()
                        campViewModel.refreshAllCamps()
                    }.show()
                }
            }
        }
    }

    private fun setupFabOnClickListener() {

        loginViewModel.authenticationState.observe(viewLifecycleOwner, { authenticationState ->
            when(authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> floatingActionButton.visibility = View.VISIBLE
                else -> floatingActionButton.visibility = View.GONE
            }
        })

        val action = MyCampsFragmentDirections.actionNavigationMyCampsToNavigationAddCamp()
        floatingActionButton = requireView().findViewById<View>(R.id.fab_addCamp) as FloatingActionButton
        floatingActionButton.setOnClickListener {
            findNavController().navigate(action)
        }
    }

}