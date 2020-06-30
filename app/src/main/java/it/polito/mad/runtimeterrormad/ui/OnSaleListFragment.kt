package it.polito.mad.runtimeterrormad.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import it.polito.mad.runtimeterrormad.MainActivity
import it.polito.mad.runtimeterrormad.R
import it.polito.mad.runtimeterrormad.viewmodels.OnSaleListViewModel
import kotlinx.android.synthetic.main.fragment_on_sale_list.*


class OnSaleListFragment : Fragment() {

    private val globalItemListVM: OnSaleListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_on_sale_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listOnSaleListFragmentRV.layoutManager = LinearLayoutManager(context)
        setHasOptionsMenu(true)
        globalItemListVM.getItems().observe(viewLifecycleOwner, Observer {
            if (it.isEmpty())
                noAdvTV.visibility = View.VISIBLE
            else
                noAdvTV.visibility = View.GONE
            listOnSaleListFragmentRV.adapter = ItemAdapter(it, Constants.ON_SALE_ITEM_LIST)


            if (globalItemListVM.text != "") {
                searchTextOnSaleListFragmentTV.visibility = View.VISIBLE
                searchTextOnSaleListFragmentTV.text =
                    resources.getString(R.string.searchLabel, globalItemListVM.text)
            } else
                searchTextOnSaleListFragmentTV.visibility = View.GONE
        })
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.on_sale_menu, menu)

        if (globalItemListVM.categoryID != -1 || globalItemListVM.date != ""
            || globalItemListVM.minPrice != "" || globalItemListVM.maxPrice != ""
        )
            menu.findItem(R.id.filterOnSaleMI).setIcon(R.drawable.ic_filter)
        else
            menu.findItem(R.id.filterOnSaleMI).setIcon(R.drawable.ic_no_filter)

        val searchView =
            SearchView((context as MainActivity).supportActionBar?.themedContext ?: context)
        menu.findItem(R.id.searchView).apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            actionView = searchView
        }

        searchView.maxWidth = Integer.MAX_VALUE


        searchView.setOnCloseListener {
            globalItemListVM.text = ""
            globalItemListVM.setFilters()
            false
        }

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    globalItemListVM.text = query
                    globalItemListVM.setFilters()
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    if (newText.isEmpty()) {
                        onQueryTextSubmit("")
                    }
                    return false
                }
            })

        if (globalItemListVM.text != "") {
            searchView.setQuery(globalItemListVM.text, true)
            searchView.isQueryRefinementEnabled = true
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filterOnSaleMI -> {
                val newFragment = FilterDialog()
                newFragment.show(parentFragmentManager, "filterDialog")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun hideKeyboard() {
        activity?.currentFocus?.let { view ->
            context?.getSystemService(Context.INPUT_METHOD_SERVICE)?.let {
                it as InputMethodManager
                it.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }
}