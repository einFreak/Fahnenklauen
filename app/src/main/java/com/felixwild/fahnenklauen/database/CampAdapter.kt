package com.felixwild.fahnenklauen.database

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.felixwild.fahnenklauen.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class CampAdapter(options: FirestoreRecyclerOptions<Camp?>) : FirestoreRecyclerAdapter<Camp?, CampViewHolder?>(options)  {

    override fun onCreateViewHolder(group: ViewGroup, i: Int): CampViewHolder {
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
    }
}