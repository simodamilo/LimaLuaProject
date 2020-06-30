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
import it.polito.mad.runtimeterrormad.viewmodels.BoughtItemsListViewModel
import kotlinx.android.synthetic.main.fragment_bought_items_list.*

class BoughtItemsListFragment : Fragment() {

    private val boughtItemListVM: BoughtItemsListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bought_items_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listBoughtItemsListFragmentRV.layoutManager = LinearLayoutManager(context)

        boughtItemListVM.getItems().observe(viewLifecycleOwner, Observer {
            if (it.isEmpty())
                noBoughtAdvTV.visibility = View.VISIBLE
            else
                noBoughtAdvTV.visibility = View.GONE
            listBoughtItemsListFragmentRV.adapter = ItemAdapter(it, Constants.BOUGHT_ITEM_LIST)
        })
    }
}
