package com.felixwild.fahnenklauen.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
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
import com.felixwild.fahnenklauen.database.CampViewHolder
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore


class SearchFragment : Fragment() {

    private lateinit var searchViewModel: SearchViewModel
    private var mFirestore = FirebaseFirestore.getInstance()
    private var campCollection = mFirestore.collection("Camps")
    private val TAG = "SearchFragment"
    private lateinit var rvAdapter: CampAdapter


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

        //TODO initSearch()
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

        val rvOptions = FirestoreRecyclerOptions.Builder<Camp>()
                .setQuery(allCampsQuery, Camp::class.java)
                .build()

        rvAdapter = CampAdapter(rvOptions)

        val adapter: FirestoreRecyclerAdapter<*, *> = object : FirestoreRecyclerAdapter<Camp?, CampViewHolder?>(rvOptions) {
            override fun onBindViewHolder(holder: CampViewHolder, position: Int, model: Camp) {}
            override fun onCreateViewHolder(group: ViewGroup, i: Int): CampViewHolder {
                // Using a custom layout called R.layout.message for each item, we create a new instance of the viewholder
                val view: View = LayoutInflater.from(group.context)
                        .inflate(R.layout.content_camp, group, false)
                return CampViewHolder(view)
            }
        }

        rv?.adapter = rvAdapter


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