package it.polito.mad.runtimeterrormad.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class NotificationToken(
    val userID: String,
    @Exclude @get:Exclude val tokenID: String

) {
    val lastRenew: Timestamp = Timestamp.now()
}
