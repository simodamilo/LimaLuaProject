package it.polito.mad.runtimeterrormad.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import it.polito.mad.runtimeterrormad.data.InterestedUser
import it.polito.mad.runtimeterrormad.data.InterestedUserRepository
import it.polito.mad.runtimeterrormad.data.SubscribedUser
import it.polito.mad.runtimeterrormad.data.WillingToBuyUser
import it.polito.mad.runtimeterrormad.ui.Constants

class InterestedUserViewModel(private val itemID: String, private val userID: String) :
    ViewModel() {

    private lateinit var _subscribedUserListener: ListenerRegistration
    private lateinit var _willingToBuyUserListener: ListenerRegistration
    private val _isSubscribed: MutableLiveData<Boolean> by lazy { loadSubscription() }
    private val _isWillingToBuy: MutableLiveData<Boolean> by lazy { loadWillToBuy() }

    private fun loadSubscription(): MutableLiveData<Boolean> {
        val mutable = MutableLiveData<Boolean>()
        if (itemID.isEmpty()) {
            Log.d(Constants.INTERESTED_USER_VM, "Empty itemID.")
            return mutable
        }
        _subscribedUserListener =
            InterestedUserRepository.getInterestedUser(itemID, userID)
                .addSnapshotListener { snapshot, e ->
                    when {
                        e != null -> Log.e(
                            Constants.INTERESTED_USER_VM,
                            "Error with subscribedUser $userID of Item=$itemID."
                        )
                        snapshot != null -> {
                            val subscribedUserTmp = snapshot.toObject(InterestedUser::class.java)
                            _isSubscribed.value = subscribedUserTmp?.subscribed ?: false
                            Log.d(
                                Constants.INTERESTED_USER_VM,
                                "subscribedUser $userID correctly of Item $itemID updated."
                            )
                        }
                        else -> Log.d(
                            Constants.INTERESTED_USER_VM,
                            "Null snapshot with subscribedUser $userID of Item=$itemID."
                        )
                    }
                }
        return mutable
    }

    private fun loadWillToBuy(): MutableLiveData<Boolean> {
        val mutable = MutableLiveData<Boolean>()
        if (itemID.isEmpty()) {
            Log.d(Constants.INTERESTED_USER_VM, "Empty itemID.")
            return mutable
        }
        _willingToBuyUserListener =
            InterestedUserRepository.getInterestedUser(itemID, userID)
                .addSnapshotListener { snapshot, e ->
                    when {
                        e != null -> Log.e(
                            Constants.INTERESTED_USER_VM,
                            "Error with willingToBuyUser $userID of Item=$itemID"
                        )
                        snapshot != null -> {
                            val willingToBuyUserTmp = snapshot.toObject(InterestedUser::class.java)
                            _isWillingToBuy.value = willingToBuyUserTmp?.willingToBuy ?: false
                            Log.d(
                                Constants.INTERESTED_USER_VM,
                                "willingToBuyUser $userID correctly of Item $itemID updated."
                            )
                        }
                        else -> Log.d(
                            Constants.INTERESTED_USER_VM,
                            "Null snapshot with willingToBuyUser $userID of Item=$itemID."
                        )
                    }
                }
        return mutable
    }

    fun isSubscribed(): LiveData<Boolean> = _isSubscribed

    fun isWillingToBuy(): LiveData<Boolean> = _isWillingToBuy

    fun addSubscription(subscribedUser: SubscribedUser) {
        InterestedUserRepository.addSubscription(itemID, subscribedUser).addOnCompleteListener {
            if (it.isSuccessful)
                Log.d(
                    Constants.INTERESTED_USER_VM,
                    "subscribedUser $userID of Item $itemID correctly added."
                )
            else
                Log.e(
                    Constants.INTERESTED_USER_VM,
                    "Error with subscribedUser $userID of Item=$itemID."
                )
        }
    }

    fun addWillToBuy(willingToBuyUser: WillingToBuyUser) {
        InterestedUserRepository.addWillToBuy(itemID, willingToBuyUser).addOnCompleteListener {
            if (it.isSuccessful)
                Log.d(
                    Constants.INTERESTED_USER_VM,
                    "subscribedUser $userID of Item $itemID correctly added."
                )
            else
                Log.e(
                    Constants.INTERESTED_USER_VM,
                    "Error with subscribedUser $userID of Item=$itemID."
                )
        }
    }

    fun removeSubscription() {
        InterestedUserRepository.removeSubscription(itemID, userID).addOnCompleteListener {
            if (it.isSuccessful)
                Log.d(
                    Constants.INTERESTED_USER_VM,
                    "subscribedUser $userID of Item $itemID correctly removed."
                )
            else
                Log.e(
                    Constants.INTERESTED_USER_VM,
                    "Error with subscribedUser $userID of Item=$itemID."
                )
        }
    }

    /* fun removeWillToBuy() {
         InterestedUserRepository.removeWillToBuy(itemID, userID).addOnCompleteListener {
             if (it.isSuccessful)
                 Log.d(
                     Constants.INTERESTED_USER_VM,
                     "willingToBuyUser $userID of Item $itemID correctly removed."
                 )
             else
                 Log.e(
                     Constants.INTERESTED_USER_VM,
                     "Error with willingToBuyUser $userID of Item=$itemID."
                 )
         }
     } */


    override fun onCleared() {
        super.onCleared()
        if (this::_subscribedUserListener.isInitialized) {
            _subscribedUserListener.remove()
        }
        if (this::_willingToBuyUserListener.isInitialized) {
            _willingToBuyUserListener.remove()
        }
    }
}