package it.polito.mad.runtimeterrormad.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import it.polito.mad.runtimeterrormad.data.Item
import it.polito.mad.runtimeterrormad.data.ItemRepository
import it.polito.mad.runtimeterrormad.ui.Constants

class ItemListViewModel(private val userID: String) : ViewModel() {
    private lateinit var _itemsListener: ListenerRegistration
    private val _items: MutableLiveData<List<Item>> by lazy { loadItems() }

    private fun loadItems(): MutableLiveData<List<Item>> {
        val mutable = MutableLiveData<List<Item>>()
        if (userID.isEmpty()) {
            Log.d(Constants.ITEM_LIST_VIEWMODEL, "Empty userID")
            return mutable
        }
        _itemsListener = ItemRepository.getUserItems(userID).addSnapshotListener { snapshot, e ->
            when {
                e != null -> Log.e(Constants.ITEM_LIST_VIEWMODEL, "Error with Item=$userID")
                snapshot != null -> {
                    val listTmp = mutableListOf<Item>()
                    for (doc in snapshot) {
                        val itemTmp = doc.toObject(Item::class.java)
                        listTmp.add(itemTmp)
                        Log.d(Constants.ITEM_LIST_VIEWMODEL, "Item Update=$itemTmp")
                    }
                    mutable.value = listTmp
                }
                else -> Log.d(Constants.ITEM_LIST_VIEWMODEL, "Null snapshot with Item=$userID")
            }
        }
        return mutable
    }

    fun getItems(): LiveData<List<Item>> = _items

    override fun onCleared() {
        super.onCleared()
        if (this::_itemsListener.isInitialized) {
            _itemsListener.remove()
        }
    }
}
