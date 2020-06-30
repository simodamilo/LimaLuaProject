package it.polito.mad.runtimeterrormad.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage


object ItemRepository {

    val itemsRef = FirebaseFirestore.getInstance().collection("items")
    private var storage = Firebase.storage
    private val storageRef = storage.reference

    fun getItem(itemID: String): DocumentReference = itemsRef.document(itemID)

    fun getAllItems(): Query =
        itemsRef.whereEqualTo("hidden", false).orderBy("itemCreation", Query.Direction.DESCENDING)

    fun getAllItemsFilter(): Query = itemsRef.whereEqualTo("hidden", false)

    fun getUserItems(userID: String): Query =
        itemsRef.whereEqualTo("userID", userID).whereEqualTo("hidden", false)
            .orderBy("itemCreation", Query.Direction.DESCENDING)

    fun getUserBoughtItems(userID: String): Query = itemsRef.whereEqualTo("buyerID", userID)


    fun saveItem(item: Item): Task<Void> {
        return itemsRef.document(item.itemID).set(item)
    }

    fun deleteItem(itemID: String) {
        itemsRef.document(itemID).update("hidden", true)
    }

    fun saveItemPhoto(item: Item): StorageReference {
        return storageRef.child("itemsImages/${item.userID}/item_${item.itemID}.jpg")
    }

    fun updateItemPhoto(itemID: String): DocumentReference {
        return itemsRef.document(itemID)
    }

    fun updateStatusItem(itemID: String, status: Int) {
        itemsRef.document(itemID).update("status", status)
    }
}