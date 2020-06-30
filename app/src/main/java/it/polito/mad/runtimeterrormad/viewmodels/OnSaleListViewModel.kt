package it.polito.mad.runtimeterrormad.viewmodels


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import it.polito.mad.runtimeterrormad.data.Item
import it.polito.mad.runtimeterrormad.data.ItemRepository
import it.polito.mad.runtimeterrormad.ui.Constants

class OnSaleListViewModel : ViewModel() {
    var categoryID: Int = -1
    var date: String = ""
    var minPrice: String = ""
    var maxPrice: String = ""
    var text: String = ""
    private lateinit var _itemsListener: ListenerRegistration
    private val _items: MutableLiveData<List<Item>> by lazy { loadItems() }

    private fun loadItems(): MutableLiveData<List<Item>> {
        val mutable = MutableLiveData<List<Item>>()
        _itemsListener = ItemRepository.getAllItems().addSnapshotListener { snapshot, e ->
            when {
                e != null -> Log.e(Constants.ONSALE_LIST_VIEWMODEL, "Error with onSale Items")
                snapshot != null -> {
                    val listTmp = mutableListOf<Item>()
                    for (doc in snapshot) {
                        val itemTmp = doc.toObject(Item::class.java)
                        listTmp.add(itemTmp)
                        Log.d(Constants.ONSALE_LIST_VIEWMODEL, "Item Update=$itemTmp")
                    }
                    mutable.value = listTmp
                }
                else -> Log.d(Constants.ONSALE_LIST_VIEWMODEL, "Null snapshot with onSale Items")
            }
        }
        return mutable
    }

    fun getItems(): LiveData<List<Item>> = _items

    fun setFilters() {
        var query: Query = ItemRepository.getAllItemsFilter()
        if (categoryID != -1)
            query = ItemRepository.itemsRef.whereEqualTo("categoryID", categoryID)
        if (date != "")
            query = query.whereEqualTo("expiryDate", date)
        if (minPrice != "")
            query = query.whereGreaterThanOrEqualTo("price", Integer.parseInt(minPrice))
        if (maxPrice != "")
            query = query.whereLessThanOrEqualTo("price", Integer.parseInt(maxPrice))

        if (this::_itemsListener.isInitialized) {
            _itemsListener.remove()
        }
        _itemsListener = query.addSnapshotListener { snapshot, e ->
            if (e != null)
                Log.e("Query Error", e.message!!)
            else if (snapshot != null) {
                val itemList = mutableListOf<Item>()

                for (i in snapshot) {
                    val tmpItem: Item = i.toObject(Item::class.java)
                    if (tmpItem.title.contains(text, ignoreCase = true)
                        || tmpItem.description.contains(text, ignoreCase = true)
                    )
                        itemList.add(tmpItem)
                }
                itemList.sortByDescending { it.itemCreation }
                _items.value = itemList
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        if (this::_itemsListener.isInitialized) {
            _itemsListener.remove()
        }
    }
}