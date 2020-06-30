package it.polito.mad.runtimeterrormad.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import it.polito.mad.runtimeterrormad.data.Item
import it.polito.mad.runtimeterrormad.data.ItemRepository
import it.polito.mad.runtimeterrormad.ui.Constants

class ItemDetailsViewModel(private var itemID: String) : ViewModel() {

    private lateinit var _itemListener: ListenerRegistration
    private val _item: MutableLiveData<Item> by lazy { loadItem() }


    private fun loadItem(): MutableLiveData<Item> {
        val mutable = MutableLiveData<Item>()
        if (itemID.isEmpty()) {
            Log.d(Constants.ITEM_VIEWMODEL, "Empty itemID")
            return mutable
        }
        _itemListener = ItemRepository.getItem(itemID).addSnapshotListener { snapshot, e ->
            when {
                e != null -> Log.e(Constants.ITEM_VIEWMODEL, "Error with Item=$itemID")
                snapshot != null && snapshot.exists() -> {
                    val itemTmp = snapshot.toObject(Item::class.java)
                    mutable.value = itemTmp
                    Log.d(Constants.ITEM_VIEWMODEL, "Item Update=$itemTmp")
                }
                else -> Log.d(Constants.ITEM_VIEWMODEL, "Null snapshot with Item=$itemID")
            }
        }
        return mutable
    }

    fun getItem(): LiveData<Item> = _item

    fun storeItem(item: Item) {
        ItemRepository.saveItem(item)
    }

    fun updateStatus(status: Int) {
        ItemRepository.updateStatusItem(itemID, status)
    }

    fun deleteItem() {
        ItemRepository.deleteItem(itemID)
    }

    override fun onCleared() {
        super.onCleared()
        if (this::_itemListener.isInitialized) {
            _itemListener.remove()
        }
    }
}