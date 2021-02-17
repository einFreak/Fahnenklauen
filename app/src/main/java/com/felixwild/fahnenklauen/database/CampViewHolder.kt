package com.felixwild.fahnenklauen.database

import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.felixwild.fahnenklauen.R

class CampViewHolder(campView: View) : RecyclerView.ViewHolder(campView) {
    val textViewStammName: TextView = campView.findViewById(R.id.ct_StammName)
    val checkBoxCampSelected: CheckBox = campView.findViewById(R.id.ct_campSelected)
    val checkTVKidsActive: CheckedTextView = campView.findViewById(R.id.ct_check_kids_active)
    val checkTVTagOnly: CheckedTextView = campView.findViewById(R.id.ct_check_tag_only)
    val textViewNumberParticipants: TextView = campView.findViewById(R.id.ct_number_participants)
    val buttonAdditionalRules: Button = campView.findViewById(R.id.ct_btn_addit_rules)
    val ratingBar: RatingBar = campView.findViewById(R.id.ct_ratingBar)
    val buttonStartNav: Button = campView.findViewById(R.id.ct_nav_btn)
}