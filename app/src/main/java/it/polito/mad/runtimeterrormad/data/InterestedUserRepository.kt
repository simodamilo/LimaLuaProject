package it.polito.mad.runtimeterrormad.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

object InterestedUserRepository {

    private val interestedUsersRef = FirebaseFirestore.getInstance().collection("itemsRelations")

    fun getSubscriberUsers(itemID: String): Query =
        interestedUsersRef.document(itemID).collection("interestedUsersCollection")
            .whereEqualTo("subscribed", true)

    fun getWillingToBuyUsers(itemID: String): Query =
        interestedUsersRef.document(itemID).collection("interestedUsersCollection")
            .whereEqualTo("willingToBuy", true)

    fun addSubscription(itemID: String, subscribedUser: SubscribedUser): Task<Void> =
        interestedUsersRef.document(itemID).collection("interestedUsersCollection")
            .document(subscribedUser.userID).set(subscribedUser, SetOptions.merge())

    fun removeSubscription(itemID: String, userID: String): Task<Void> =
        interestedUsersRef.document(itemID).collection("interestedUsersCollection").document(userID)
            .update("subscribed", false)

    fun addWillToBuy(itemID: String, willingToBuyUser: WillingToBuyUser): Task<Void> =
        interestedUsersRef.document(itemID).collection("interestedUsersCollection")
            .document(willingToBuyUser.userID).set(willingToBuyUser, SetOptions.merge())

    fun removeWillToBuy(itemID: String, userID: String): Task<Void> =
        interestedUsersRef.document(itemID).collection("interestedUsersCollection").document(userID)
            .update("willingToBuy", false)


    fun getInterestedUser(itemID: String, userID: String): DocumentReference =
        interestedUsersRef.document(itemID).collection("interestedUsersCollection").document(userID)


}