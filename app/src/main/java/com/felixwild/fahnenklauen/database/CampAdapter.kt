package com.felixwild.fahnenklauen.database

import android.content.Context
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.felixwild.fahnenklauen.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class CampAdapter(options: FirestoreRecyclerOptions<Camp?>, startLocation: Location) : FirestoreRecyclerAdapter<Camp?, CampViewHolder?>(options)  {

    val start = startLocation

    override fun onCreateViewHolder(group: ViewGroup, i: Int): CampViewHolder {
        Log.d("SearchFragment", "startLoc: Lat=${start.latitude}, Long=${start.longitude}")
        val campView: View = LayoutInflater.from(group.context)
            .inflate(R.layout.content_camp, group, false)
        return CampViewHolder(campView)
    }

    override fun onBindViewHolder(holder: CampViewHolder, position: Int, model: Camp) {
        holder.textViewStammName.text = model.campName
        if(model.kidsActive)
            holder.checkTVKidsActive.visibility = View.VISIBLE
        else
            holder.checkTVKidsActive.visibility = View.INVISIBLE
        if(model.tagOnly)
            holder.checkTVTagOnly.visibility = View.VISIBLE
        else
            holder.checkTVTagOnly.visibility = View.INVISIBLE
        holder.ratingBar.rating = model.currentRating.toFloat()
        holder.textViewNumberParticipants.text = model.numberParticipants.toString()

        //calculating distance
        val modelLocation = Location("")
        modelLocation.latitude = model.location.latitude
        modelLocation.longitude = model.location.longitude
        val distanceInM = start.distanceTo(modelLocation)
        val distanceInKM = (distanceInM/1000)
        val distanceString = String.format("%.1f", distanceInKM)
        holder.textViewDistance.text = distanceString

    }
}