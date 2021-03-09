package com.felixwild.fahnenklauen.database

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.location.Location
import android.net.Uri
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.felixwild.fahnenklauen.R
import com.felixwild.fahnenklauen.ui.EditCampFragment
import com.felixwild.fahnenklauen.ui.EditCampFragmentArgs
import com.felixwild.fahnenklauen.ui.MyCampsFragmentDirections
import com.felixwild.fahnenklauen.viewModels.CampViewModel
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.collections.ArrayList


class CampAdapter(myCamps: Boolean = false) : RecyclerView.Adapter<CampAdapter.RVCampViewHolder>(), Filterable  {

    private var allCamps: List<Camp> = ArrayList()
    private var filteredCamps: List<Camp> = ArrayList()
    private var currentLocation: Location = Location("")
    private var filterActive: Boolean = false
    private var isMyCamps: Boolean = false

    init {
        isMyCamps = myCamps
    }

    fun setCamps(camps: List<Camp>, currentLocation: Location) {
        this.allCamps = camps
        this.currentLocation = currentLocation

        //if (!filterActive)
            filteredCamps = allCamps
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = filteredCamps.size

    fun getCampAt(position: Int): Camp? {
        return filteredCamps[position]
    }

    override fun onCreateViewHolder(group: ViewGroup, i: Int): RVCampViewHolder {
        val campView: View = LayoutInflater.from(group.context)
            .inflate(R.layout.content_camp, group, false)
        return RVCampViewHolder(campView)
    }

    override fun onBindViewHolder(holderRV: RVCampViewHolder, position: Int) {
        val currentCamp = filteredCamps[position]

        holderRV.tVCampName.text = currentCamp.campName
        if(currentCamp.kidsActive)
            holderRV.imgKidsActive.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(holderRV.itemView.context, R.color.red_state))
        else
            holderRV.imgKidsActive.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(holderRV.itemView.context, R.color.green_state))

        if(currentCamp.tagOnly)
            holderRV.imgTagOnly.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(holderRV.itemView.context, R.color.green_state))
        else
            holderRV.imgTagOnly.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(holderRV.itemView.context, R.color.red_state))

        if(currentCamp.flag)
            holderRV.imgFlag.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(holderRV.itemView.context, R.color.green_state))
        else
            holderRV.imgFlag.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(holderRV.itemView.context, R.color.red_state))

        if(currentCamp.hasAdditionalRules)
            holderRV.imgRules.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(holderRV.itemView.context, R.color.green_state))
        else
            holderRV.imgRules.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(holderRV.itemView.context, R.color.red_state))

        holderRV.ratingBar.rating = currentCamp.currentRating.toFloat()
        holderRV.tVNumberParticipants.text = currentCamp.numberParticipants.toString()


        val uri = "http://maps.google.com/maps?daddr=" + currentCamp.location.latitude.toString() + "," + currentCamp.location.longitude.toString() + "(" + currentCamp.campName + ")"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

        holderRV.btnStartNav.setOnClickListener {
            startNavigation(context = holderRV.itemView.context, intent = intent, destinationName = currentCamp.campName)
        }

        if(isMyCamps) {
            holderRV.btnOptions.visibility = View.VISIBLE

            holderRV.btnOptions.setOnClickListener{
                val popup = PopupMenu(holderRV.itemView.context, holderRV.btnOptions)
                popup.inflate(R.menu.rv_options_menu)

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.rv_edit -> {
                            val action = MyCampsFragmentDirections.actionNavigationMyCampsToNavigationEditCamp(currentCamp.campID)
                            holderRV.itemView.findNavController().navigate(action)
                            true
                        }
                        else -> false
                    }
                }

                popup.show()
            }
        }

        holderRV.btnMore.setOnClickListener {

        }

        val distanceInKM = currentCamp.distanceInM/1000
        val distanceString = String.format("%.1f", distanceInKM)
        holderRV.tVDistance.text = distanceString

    }

    private fun startNavigation(context: Context, intent: Intent, destinationName: String) {
        val builder: AlertDialog.Builder = context.let {
            AlertDialog.Builder(it)
        }

        builder.setTitle("Navigation nach $destinationName")
        builder.apply {
            setPositiveButton("Starten"
            ) { dialog, it ->
                context.startActivity(intent)
            }
            setNegativeButton("Abbruch"
            ) { dialog, it ->
                Toast.makeText(context, "Navigation abgebrochen", Toast.LENGTH_LONG).show()
            }
        }

        builder.create()?.show()
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
                    filterActive = false
                } else {
                    filterActive = true
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

    class RVCampViewHolder(campView: View) : RecyclerView.ViewHolder(campView) {
        val tVCampName: TextView = campView.findViewById(R.id.ct_campName)
        val tVDistance: TextView = campView.findViewById(R.id.ct_dist)
        val imgKidsActive: ImageView = campView.findViewById(R.id.ct_img_kids)
        val imgFlag: ImageView = campView.findViewById(R.id.ct_img_flag)
        val imgTagOnly: ImageView = campView.findViewById(R.id.ct_img_tag_only)
        val imgRules: ImageView = campView.findViewById(R.id.ct_img_rules)
        val tVNumberParticipants: TextView = campView.findViewById(R.id.ct_number_participants)
        val btnMore: Button = campView.findViewById(R.id.ct_btn_more)
        val btnStartNav: Button = campView.findViewById(R.id.ct_nav_btn)
        val ratingBar: RatingBar = campView.findViewById(R.id.ct_ratingBar)
        val btnOptions: TextView = campView.findViewById(R.id.ct_options)
    }

}

