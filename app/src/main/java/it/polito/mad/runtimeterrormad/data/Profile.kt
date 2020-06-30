package it.polito.mad.runtimeterrormad.data

import it.polito.mad.runtimeterrormad.ui.Constants.NOT_A_POSITION

data class Profile(
    var fullname: String = "",
    var nickname: String = "",
    var email: String = "",
    var location: String = "",
    var rating: Double = 0.0,
    var totalRating: Int = 0,
    var image: String = "",
    var lat: Double = NOT_A_POSITION,
    var lng: Double = NOT_A_POSITION,
    var imageRotation: Int = 0
)