package it.polito.mad.runtimeterrormad.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import it.polito.mad.runtimeterrormad.R
import it.polito.mad.runtimeterrormad.viewmodels.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_items_of_interest_list.*

class ItemsOfInterestListFragment : Fragment() {

    private val profileVM: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_items_of_interest_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listInterestListFragmentRV.layoutManager = LinearLayoutManager(context)

        profileVM.getItemsOfInterest().observe(viewLifecycleOwner, Observer {
            if (it.isEmpty())
                noInterestAdvTV.visibility = View.VISIBLE
            else
                noInterestAdvTV.visibility = View.GONE
            listInterestListFragmentRV.adapter = ItemAdapter(it, Constants.INTEREST_ITEM_LIST)
        })
    }
}
