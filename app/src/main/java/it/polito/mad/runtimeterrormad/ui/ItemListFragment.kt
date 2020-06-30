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
import it.polito.mad.runtimeterrormad.viewmodels.ItemListViewModel
import kotlinx.android.synthetic.main.fragment_item_list.*

class ItemListFragment : Fragment() {

    private val itemListVM: ItemListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listItemListFragmentRV.layoutManager = LinearLayoutManager(context)

        itemListVM.getItems().observe(viewLifecycleOwner, Observer {
            if (it.isEmpty())
                noAdvTV.visibility = View.VISIBLE
            else
                noAdvTV.visibility = View.GONE
            listItemListFragmentRV.adapter = ItemAdapter(it, Constants.ITEM_LIST)
        })
    }
}
