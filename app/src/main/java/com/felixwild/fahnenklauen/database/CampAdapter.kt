package com.felixwild.fahnenklauen.database

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.felixwild.fahnenklauen.R
import com.google.firebase.firestore.GeoPoint
import java.util.*
import kotlin.collections.ArrayList

class CampAdapter() : RecyclerView.Adapter<CampViewHolder>(), Filterable  {

    private var allCamps: List<Camp> = ArrayList()
    private var filteredCamps: List<Camp> = ArrayList()
    private var currentLocation: Location = Location("")
    private var filteredCampsSet: Boolean = false

    fun setCamps(camps: List<Camp>, currentLocation: Location) {
        this.allCamps = camps
        this.currentLocation = currentLocation
        if (!filteredCampsSet && allCamps.isNotEmpty()) {
            filteredCamps = allCamps
            filteredCampsSet = true
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = filteredCamps.size

    fun getCampAt(position: Int): Camp? {
        return filteredCamps[position]
    }

    override fun onCreateViewHolder(group: ViewGroup, i: Int): CampViewHolder {
        val campView: View = LayoutInflater.from(group.context)
            .inflate(R.layout.content_camp, group, false)
        return CampViewHolder(campView)
    }

    override fun onBindViewHolder(holder: CampViewHolder, position: Int) {
        val currentCamp = filteredCamps[position]

        holder.textViewStammName.text = currentCamp.campName
        if(currentCamp.kidsActive)
            holder.checkTVKidsActive.visibility = View.VISIBLE
        else
            holder.checkTVKidsActive.visibility = View.INVISIBLE
        if(currentCamp.tagOnly)
            holder.checkTVTagOnly.visibility = View.VISIBLE
        else
            holder.checkTVTagOnly.visibility = View.INVISIBLE
        holder.ratingBar.rating = currentCamp.currentRating.toFloat()
        holder.textViewNumberParticipants.text = currentCamp.numberParticipants.toString()


        var distanceInM: Float

        val currentCampLocation = Location("")
        currentCampLocation.latitude = currentCamp.location.latitude
        currentCampLocation.longitude = currentCamp.location.longitude
        distanceInM= currentLocation.distanceTo(currentCampLocation)
        currentCamp.distanceInM = distanceInM


        val distanceInKM = currentCamp.distanceInM/1000
        val distanceString = String.format("%.1f", distanceInKM)
        holder.textViewDistance.text = distanceString

    }

    fun sortCamps() {
        allCamps = allCamps.sortedBy { it.distanceInM }
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    filteredCamps = allCamps
                } else {
                    val results: MutableList<Camp> = ArrayList()

                    for (row in allCamps) {
                        if (row.campName.toLowerCase(Locale.ROOT)
                                .contains(charSearch.toLowerCase(Locale.ROOT))
                        ) {
                            results.add(row)
                        }
                    }
                    filteredCamps = results
                }
                val filterResult = FilterResults()
                filterResult.values = filteredCamps
                return filterResult
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredCamps = results?.values as List<Camp>
                notifyDataSetChanged()
            }
        }
    }

}