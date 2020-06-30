package it.polito.mad.runtimeterrormad.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class InterestedUserViewModelFactory(private val itemID: String, private val userID: String) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(String::class.java, String::class.java)
            .newInstance(itemID, userID)
    }
}