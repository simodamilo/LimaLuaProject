package it.polito.mad.runtimeterrormad.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import it.polito.mad.runtimeterrormad.data.Profile
import it.polito.mad.runtimeterrormad.data.ProfileRepository
import it.polito.mad.runtimeterrormad.ui.Constants
import java.io.File

class ProfileEditViewModel(private val userID: String) : ViewModel() {

    private lateinit var _profileListener: ListenerRegistration
    private val _profile: MutableLiveData<Profile> by lazy { loadProfile() }

    private fun loadProfile(): MutableLiveData<Profile> {
        val mutable = MutableLiveData<Profile>()
        if (userID.isEmpty()) {
            Log.d(Constants.PROFILE_EDIT_VIEWMODEL, "Empty userID")
            return mutable
        }
        _profileListener = ProfileRepository.getProfile(userID).addSnapshotListener { snapshot, e ->
            when {
                e != null -> Log.e(Constants.PROFILE_EDIT_VIEWMODEL, "Error with Profile=$userID")
                snapshot != null && snapshot.exists() -> {
                    val profileTmp = snapshot.toObject(Profile::class.java)
                    mutable.value = profileTmp
                    Log.d(Constants.PROFILE_EDIT_VIEWMODEL, "Profile Update=$profileTmp")
                }
                else -> Log.d(
                    Constants.PROFILE_EDIT_VIEWMODEL,
                    "Null snapshot with Profile=$userID"
                )
            }
        }
        return mutable
    }

    fun getProfile(): LiveData<Profile> = _profile

    fun setProfile(profile: Profile) {
        _profile.value = profile
    }

    fun storeProfile(profile: Profile, imageModified: Boolean) {
        ProfileRepository.saveProfile(userID, profile)
        if (imageModified) {
            val file =
                Uri.fromFile(File(profile.image))

            val riversRef = ProfileRepository.saveProfilePhoto(userID)
            val uploadTask = ProfileRepository.saveProfilePhoto(userID).putFile(file)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                riversRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ProfileRepository.updateProfilePhoto(userID)
                        .update("image", task.result.toString())
                } else {
                    Log.e("Profile Repository", "Error")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (this::_profileListener.isInitialized) {
            _profileListener.remove()
        }
    }
}