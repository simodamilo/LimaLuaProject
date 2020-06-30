package it.polito.mad.runtimeterrormad.data

data class InterestedUser(
    var userID: String = "",
    var nickname: String = "",
    var imageURL: String = "",
    var subscribed: Boolean = false,
    var willingToBuy: Boolean = false
)