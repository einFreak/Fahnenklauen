package com.felixwild.fahnenklauen.ui.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.felixwild.fahnenklauen.R
import com.felixwild.fahnenklauen.database.Camp
import com.felixwild.fahnenklauen.viewModels.CampViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class CampDetailedFragment : Fragment() {

    private var campID: String? = null
    private val campViewModel: CampViewModel by activityViewModels()
    private lateinit var selectedCamp: Camp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            campID = it.getString("selectedCamp")
            campViewModel.getCamp(campID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camp_detailed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        campViewModel.selectedCamp.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            selectedCamp = it
        })

        super.onViewCreated(view, savedInstanceState)
    }

    class DetailedCampViewHolder(detailedCampView: View) : RecyclerView.ViewHolder(detailedCampView) {
        val tVCampName: TextView = detailedCampView.findViewById(R.id.ct_campName)
        val tVDistance: TextView = detailedCampView.findViewById(R.id.ct_dist)
        val imgKidsActive: ImageView = detailedCampView.findViewById(R.id.ct_img_kids)
        val imgFlag: ImageView = detailedCampView.findViewById(R.id.ct_img_flag)
        val imgTagOnly: ImageView = detailedCampView.findViewById(R.id.ct_img_tag_only)
        val imgRules: ImageView = detailedCampView.findViewById(R.id.ct_img_rules)
        val tVNumberParticipants: TextView = detailedCampView.findViewById(R.id.ct_number_participants)
        val btnMore: Button = detailedCampView.findViewById(R.id.ct_btn_more)
        val btnStartNav: Button = detailedCampView.findViewById(R.id.ct_nav_btn)
        val ratingBar: RatingBar = detailedCampView.findViewById(R.id.ct_ratingBar)
    }

}