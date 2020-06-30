package it.polito.mad.runtimeterrormad.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import it.polito.mad.runtimeterrormad.data.Item
import it.polito.mad.runtimeterrormad.data.ItemRepository
import it.polito.mad.runtimeterrormad.ui.Constants
import java.io.File
import java.util.*

class ItemEditViewModel(private val userID: String, private var itemID: String) : ViewModel() {

    private lateinit var _itemListener: ListenerRegistration
    private val _item: MutableLiveData<Item> by lazy { loadItem() }

    private fun loadItem(): MutableLiveData<Item> {
        val mutable = MutableLiveData<Item>()
        if (itemID.isEmpty()) {
            itemID = UUID.randomUUID().toString()
            mutable.value = Item(itemID = itemID, userID = userID)
            Log.d(Constants.ITEM_EDIT_VIEWMODEL, "New Item=${mutable.value.toString()}")
        } else {
            _itemListener = ItemRepository.getItem(itemID).addSnapshotListener { snapshot, e ->
                when {
                    e != null -> Log.e(Constants.ITEM_EDIT_VIEWMODEL, "Error with Item=$itemID")
                    snapshot != null && snapshot.exists() -> {
                        val itemTmp = snapshot.toObject(Item::class.java)
                        Log.d(Constants.ITEM_EDIT_VIEWMODEL, "Item Update=$itemTmp")
                        mutable.value = itemTmp
                    }
                    else -> Log.d(Constants.ITEM_EDIT_VIEWMODEL, "Null snapshot with Item=$itemID")
                }
            }
        }
        return mutable
    }

    fun getItem(): LiveData<Item> = _item

    fun setItem(item: Item) {
        _item.value = item
    }

    fun storeItem(item: Item, imageModified: Boolean) {
        ItemRepository.saveItem(item)
        if (imageModified) {
            val file =
                Uri.fromFile(File(item.image))

            val riversRef = ItemRepository.saveItemPhoto(item)
            val uploadTask = ItemRepository.saveItemPhoto(item).putFile(file)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                riversRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ItemRepository.updateItemPhoto(item.itemID)
                        .update("image", task.result.toString())
                } else {
                    Log.e("Item Repository", "Error")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (this::_itemListener.isInitialized)
            _itemListener.remove()
    }
}