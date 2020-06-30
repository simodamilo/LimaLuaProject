package it.polito.mad.runtimeterrormad.data

import com.google.firebase.Timestamp
import it.polito.mad.runtimeterrormad.ui.Constants

data class Item(
    val itemID: String = "",
    val userID: String = "",
    var buyerID: String = "",
    var status: Int = 0,
    var hidden: Boolean = false,
    var title: String = "",
    var subtitle: String = "",
    var description: String = "",
    var expiryDate: String = "",
    var price: Double = 0.0,
    var categoryID: Int = -1,
    var location: String = "",
    var image: String = "",
    var imageRotation: Int = 0,
    var lat: Double = Constants.NOT_A_POSITION,
    var lng: Double = Constants.NOT_A_POSITION
)
 {
    val itemCreation: Timestamp = Timestamp.now()
}