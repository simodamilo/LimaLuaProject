package it.polito.mad.runtimeterrormad.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ListenerRegistration
import it.polito.mad.runtimeterrormad.data.Item
import it.polito.mad.runtimeterrormad.data.Profile
import it.polito.mad.runtimeterrormad.data.ProfileRepository
import it.polito.mad.runtimeterrormad.ui.Constants

class ProfileViewModel(private val userID: String) : ViewModel() {

    private lateinit var _profileListener: ListenerRegistration
    private lateinit var _interestItemsListener: ListenerRegistration
    private val _profile: MutableLiveData<Profile> by lazy { loadProfile() }
    private val _itemsOfInterest: MutableLiveData<List<Item>> by lazy { loadItemsOfInterest() }
    private val _reviews: MutableLiveData<List<String>> by lazy { loadReviews() }

    private fun loadProfile(): MutableLiveData<Profile> {
        val mutable = MutableLiveData<Profile>()
        if (userID.isEmpty()) {
            Log.d(Constants.ITEM_VIEWMODEL, "Empty UserID")
            mutable.value = Profile()
            return mutable
        }
        _profileListener = ProfileRepository.getProfile(userID).addSnapshotListener { snapshot, e ->
            when {
                e != null -> Log.e(Constants.PROFILE_VIEWMODEL, "Error with Profile=$userID")
                snapshot != null && snapshot.exists() -> {
                    val profileTmp = snapshot.toObject(Profile::class.java)
                    mutable.value = profileTmp
                    Log.d(Constants.PROFILE_VIEWMODEL, "UserID= $userID, Profile=$profileTmp")
                }
                else -> Log.d(Constants.PROFILE_VIEWMODEL, "Null snapshot with Profile=$userID")
            }
        }
        return mutable
    }

    private fun loadItemsOfInterest(): MutableLiveData<List<Item>> {
        val mutable = MutableLiveData<List<Item>>()
        if (userID.isEmpty()) {
            Log.d(Constants.ITEM_LIST_VIEWMODEL, "Empty userID")
            return mutable
        }
        _interestItemsListener =
            ProfileRepository.getItemsOfInterest(userID).addSnapshotListener { snapshot, e ->
                when {
                    e != null -> Log.e(
                        Constants.PROFILE_VIEWMODEL,
                        "Error with InterestItem=$userID"
                    )
                    snapshot != null -> {
                        val listTmp = mutableListOf<Item>()
                        for (doc in snapshot) {
                            val itemTmp = doc.toObject(Item::class.java)
                            listTmp.add(itemTmp)
                            Log.d(Constants.PROFILE_VIEWMODEL, "InterestItem Update=$itemTmp")
                        }
                        mutable.value = listTmp
                    }
                    else -> Log.d(
                        Constants.PROFILE_VIEWMODEL,
                        "Null snapshot with InterestItem=$userID"
                    )
                }
            }
        return mutable
    }

    fun getProfile(): LiveData<Profile> = _profile

    fun getItemsOfInterest(): LiveData<List<Item>> = _itemsOfInterest

/*
    fun storeItemOfInterest(itemID: String, item: Item) {
        ProfileRepository.storeItemOfInterest(userID, itemID, item)
    }

    fun deleteItemOfInterest(itemID: String) {
        ProfileRepository.deleteItemOfInterest(userID, itemID)
    }
*/

    private fun loadReviews(): MutableLiveData<List<String>> {
        val mutable = MutableLiveData<List<String>>()
        if (userID.isEmpty()) {
            Log.d(Constants.ITEM_VIEWMODEL, "Empty UserID")
            mutable.value = listOf()
            return mutable
        }
        _profileListener = ProfileRepository.getReviews(userID).addSnapshotListener { snapshot, e ->
            when {
                e != null -> Log.e(Constants.PROFILE_VIEWMODEL, "Error with Profile=$userID")
                snapshot != null -> {
                    val listTmp = mutableListOf<String>()
                    for (doc in snapshot) {
                        val tmp = doc.get("review")
                        listTmp.add(tmp as String)
                    }
                    mutable.value = listTmp
                }
                else -> Log.d(Constants.PROFILE_VIEWMODEL, "Null snapshot with Profile=$userID")
            }
        }
        return mutable
    }

    fun getReviews(): LiveData<List<String>> = _reviews

    fun updateRating(rating: Double, review: String): Task<Nothing> {
        Log.d("AAA", "UpdateRating called")
        return ProfileRepository.updateRating(rating, review, userID)
    }

    override fun onCleared() {
        super.onCleared()
        if (this::_profileListener.isInitialized) {
            _profileListener.remove()
        }
        if (this::_interestItemsListener.isInitialized) {
            _interestItemsListener.remove()
        }
    }
}