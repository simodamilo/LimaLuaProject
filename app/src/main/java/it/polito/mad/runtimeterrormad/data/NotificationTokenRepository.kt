package it.polito.mad.runtimeterrormad.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore

object NotificationTokenRepository {
    private val notificationTokensRef =
        FirebaseFirestore.getInstance().collection("notificationTokens")

    fun pushToken(token: NotificationToken): Task<Void> {
        return notificationTokensRef.document(token.tokenID).set(token)
    }
}