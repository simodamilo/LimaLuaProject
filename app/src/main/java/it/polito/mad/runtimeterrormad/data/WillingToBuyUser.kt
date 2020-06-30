package it.polito.mad.runtimeterrormad.data

data class WillingToBuyUser(
    var userID: String = "",
    var nickname: String = "",
    var imageURL: String = "",
    var willingToBuy: Boolean = true
)