package com.felixwild.fahnenklauen.ui.search

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.felixwild.fahnenklauen.R
import com.felixwild.fahnenklauen.database.Camp
import com.felixwild.fahnenklauen.database.CampAdapter
import com.felixwild.fahnenklauen.viewModels.CampViewModel
import com.felixwild.fahnenklauen.viewModels.LocationViewModel


class SearchFragment : Fragment() {

    private lateinit var searchViewModel: SearchViewModel
    private val TAG = "SearchFragment"
    private lateinit var rvAdapter: CampAdapter
    private var currentLocation = Location("")
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val campViewModel: CampViewModel by activityViewModels()

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

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataToRV()
        initSearch()

        locationViewModel.currentLocation.observe(viewLifecycleOwner, Observer { location ->
            currentLocation = location
        })
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

        rvAdapter = CampAdapter()
        rv?.adapter = rvAdapter
        var allCamps: List<Camp> = ArrayList()
        campViewModel.allCamps.observe(viewLifecycleOwner, Observer { camps ->
            rvAdapter.setCamps(camps, currentLocation)
            allCamps = camps
        })
        locationViewModel.currentLocation.observe(viewLifecycleOwner, Observer { location ->
            allCamps = campViewModel.sortCampData(allCamps, location)
            rvAdapter.setCamps(allCamps, location)
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
}