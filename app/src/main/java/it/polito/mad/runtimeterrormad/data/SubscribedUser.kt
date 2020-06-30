package it.polito.mad.runtimeterrormad.data

data class SubscribedUser(
    var userID: String = "",
    var nickname: String = "",
    var imageURL: String = "",
    var subscribed: Boolean = true
)