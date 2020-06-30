package it.polito.mad.runtimeterrormad.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import it.polito.mad.runtimeterrormad.data.NotificationToken
import it.polito.mad.runtimeterrormad.data.NotificationTokenRepository
import it.polito.mad.runtimeterrormad.data.Profile
import it.polito.mad.runtimeterrormad.data.ProfileRepository
import it.polito.mad.runtimeterrormad.ui.Constants

class AuthViewModel(private val firebaseAuth: FirebaseAuth) : ViewModel() {

    private val _userID: MutableLiveData<String> by lazy { loadUserID() }

    private fun loadUserID(): MutableLiveData<String> {
        val mutable = MutableLiveData<String>()
        mutable.value = firebaseAuth.uid ?: Constants.EMPTY_UID
        return mutable
    }

    fun getAuth(): FirebaseAuth {
        return firebaseAuth
    }

    fun getUserID(): LiveData<String> = _userID

    fun updateUserID() {
        _userID.value = firebaseAuth.uid ?: Constants.EMPTY_UID
    }

    fun checkAndCreateUser() {
        firebaseAuth.uid?.let { userID ->
            ProfileRepository.getProfile(userID).get().addOnCompleteListener {
                val user = firebaseAuth.currentUser
                if (user != null && !it.result!!.exists()) {
                    Log.d(Constants.AUTH_VIEWMODEL, "Default profile will be created ")
                    val p = Profile(
                        fullname = user.displayName!!,
                        nickname = user.displayName!!,
                        email = user.email!!
                    )
                    ProfileRepository.saveProfile(userID, p)
                }
            }
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { tokenTask ->
                Log.d(Constants.AUTH_VIEWMODEL, "token = ${tokenTask.token}")
                val newToken = NotificationToken(userID, tokenTask.token)
                NotificationTokenRepository.pushToken(newToken)
            }
        }
    }
}