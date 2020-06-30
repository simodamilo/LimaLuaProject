package it.polito.mad.runtimeterrormad.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ItemDetailsViewModelFactory(private val itemID: String) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(String::class.java)
            .newInstance(itemID)
    }
}