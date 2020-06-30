package it.polito.mad.runtimeterrormad.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import it.polito.mad.runtimeterrormad.data.InterestedUserRepository
import it.polito.mad.runtimeterrormad.data.SubscribedUser
import it.polito.mad.runtimeterrormad.data.WillingToBuyUser
import it.polito.mad.runtimeterrormad.ui.Constants

class InterestedUsersListViewModel(private val itemID: String) : ViewModel() {
    private lateinit var _subscribedUsersListener: ListenerRegistration
    private lateinit var _willingToBuyUsersListener: ListenerRegistration
    private val _subscribedUsers: MutableLiveData<List<SubscribedUser>> by lazy { loadSubscriberUsers() }
    private val _willingToBuyUsers: MutableLiveData<List<WillingToBuyUser>> by lazy { loadWillingToBuyUsers() }

    private fun loadSubscriberUsers(): MutableLiveData<List<SubscribedUser>> {
        val mutable = MutableLiveData<List<SubscribedUser>>()
        if (itemID.isEmpty()) {
            Log.d(Constants.INTERESTED_USERS_LIST_VM, "Empty itemID")
            return mutable
        }
        _subscribedUsersListener =
            InterestedUserRepository.getSubscriberUsers(itemID).addSnapshotListener { snapshot, e ->
                when {
                    e != null -> Log.e(
                        Constants.INTERESTED_USERS_LIST_VM,
                        "Error with subscriberUsers of Item=$itemID"
                    )
                    snapshot != null -> {
                        val listTmp = mutableListOf<SubscribedUser>()
                        for (doc in snapshot) {
                            val subscriberUserTmp = doc.toObject(SubscribedUser::class.java)
                            listTmp.add(subscriberUserTmp)
                        }
                        Log.d(Constants.INTERESTED_USERS_LIST_VM, "subscriberUsers Update=$listTmp")
                        mutable.value = listTmp
                    }
                    else -> Log.d(
                        Constants.INTERESTED_USERS_LIST_VM,
                        "Null snapshot with subscriberUsers of Item=$itemID"
                    )
                }
            }
        return mutable
    }

    private fun loadWillingToBuyUsers(): MutableLiveData<List<WillingToBuyUser>> {
        val mutable = MutableLiveData<List<WillingToBuyUser>>()
        if (itemID.isEmpty()) {
            Log.d(Constants.INTERESTED_USERS_LIST_VM, "Empty itemID")
            return mutable
        }
        _willingToBuyUsersListener =
            InterestedUserRepository.getWillingToBuyUsers(itemID)
                .addSnapshotListener { snapshot, e ->
                    when {
                        e != null -> Log.e(
                            Constants.INTERESTED_USERS_LIST_VM,
                            "Error with Followers of Item=$itemID"
                        )
                        snapshot != null -> {
                            val listTmp = mutableListOf<WillingToBuyUser>()
                            for (doc in snapshot) {
                                val willingToUserTmp = doc.toObject(WillingToBuyUser::class.java)
                                listTmp.add(willingToUserTmp)
                            }
                            Log.d(
                                Constants.INTERESTED_USERS_LIST_VM,
                                "willingToBuyUsers Update=$listTmp"
                            )
                            mutable.value = listTmp
                        }
                        else -> Log.d(
                            Constants.FOLLOWER_LIST_VIEWMODEL,
                            "Null snapshot with willingToUser of Item=$itemID"
                        )
                    }
                }
        return mutable
    }


    fun getSubscribedUsers(): LiveData<List<SubscribedUser>> = _subscribedUsers

    fun getWillingToBuyUsers(): LiveData<List<WillingToBuyUser>> = _willingToBuyUsers


    override fun onCleared() {
        super.onCleared()
        if (this::_subscribedUsersListener.isInitialized) {
            _subscribedUsersListener.remove()
        }
        if (this::_willingToBuyUsersListener.isInitialized) {
            _willingToBuyUsersListener.remove()
        }
    }
}