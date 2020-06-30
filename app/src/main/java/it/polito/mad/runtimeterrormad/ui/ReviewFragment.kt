package it.polito.mad.runtimeterrormad.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import it.polito.mad.runtimeterrormad.R
import it.polito.mad.runtimeterrormad.viewmodels.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_review_list.*

class ReviewFragment : Fragment() {

    private val profileVM: ProfileViewModel by activityViewModels()
    private val itemUserProfileVM: ProfileViewModel by navGraphViewModels(R.id.item_details_graph)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_review_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mode = arguments?.getInt(Constants.BUNDLE_MODE, -1) ?: ""
        listReviewFragmentRV.layoutManager = LinearLayoutManager(context)

        val viewModel = if (mode == Constants.PROFILE_OWNER)
            profileVM
        else
            itemUserProfileVM

        viewModel.getReviews().observe(viewLifecycleOwner, Observer {
            if (it.isEmpty())
                noReviewTV.visibility = View.VISIBLE
            else
                noReviewTV.visibility = View.GONE

            listReviewFragmentRV.adapter = ReviewAdapter(it)
        })
    }
}