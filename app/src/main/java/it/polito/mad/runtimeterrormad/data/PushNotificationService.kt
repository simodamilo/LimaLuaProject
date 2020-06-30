package it.polito.mad.runtimeterrormad.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.polito.mad.runtimeterrormad.MainActivity
import it.polito.mad.runtimeterrormad.R
import it.polito.mad.runtimeterrormad.ui.Constants


class PushNotificationService : FirebaseMessagingService() {


    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d(Constants.FIREBASE_MESSAGE_SERVICE, "New token generated = $p0")
        sendRegistrationToServer(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.d("MESSAGE FIREBASE", "message: ${p0.data}, ID = ${p0.sentTime}")
        if (p0.data.isNullOrEmpty())
            return
        val title: String
        val body: String
        val userNickname = p0.data["userNick"].orEmpty()
        val itemTitle = p0.data["itemTitle"].orEmpty()
        val itemID = p0.data["itemID"].orEmpty()
        val sellerID = p0.data["sellerID"].orEmpty()

        when (p0.data["msgType"]?.toIntOrNull() ?: -1) {
            Constants.SUBSCRIPTION_ADDED -> {
                title = getString(R.string.subscriptionAddedTitle)
                body = getString(R.string.subscriptionAddedBody, userNickname, itemTitle)
            }

            Constants.WILL_TO_BUY_ADDED -> {
                title = getString(R.string.willingToBuyAddedTitle)
                body = getString(R.string.willingToBuyAddedBody, userNickname, itemTitle)
            }

            Constants.STATUS_CHANGED -> {
                when (p0.data["itemStatus"]?.toIntOrNull() ?: -1) {
                    Constants.ITEM_AVAILABLE -> {
                        title = getString(R.string.itemAvailableTitle)
                        body = getString(R.string.itemAvailableBody, userNickname, itemTitle)
                    }
                    Constants.ITEM_SUSPENDED -> {
                        title = getString(R.string.itemSuspendedTitle)
                        body = getString(R.string.itemSuspendedBody, userNickname, itemTitle)
                    }
                    Constants.ITEM_SOLD -> {
                        title = getString(R.string.itemSoldTitle)
                        body = getString(R.string.itemSoldBody, userNickname, itemTitle)
                    }
                    else -> return
                }
            }

            Constants.ITEM_BOUGHT -> {
                title = getString(R.string.itemBoughtTitle)
                body = getString(R.string.itemBoughtBody, userNickname, itemTitle)
            }


            else -> return

        }
        sendNotification(p0.sentTime, title, body, sellerID, itemID)
    }


    private fun sendNotification(
        msgTime: Long,
        msgTitle: String,
        msgBody: String,
        sellerID: String,
        itemID: String
    ) {

        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.itemDetailsFragment)
            .setArguments(
                bundleOf(
                    Constants.BUNDLE_ITEM_USER_ID to sellerID,
                    Constants.BUNDLE_ITEM_ID to itemID

                )
            )
            .createPendingIntent()

        val channelId = getString(R.string.default_notification_channel_id)
        val color = ContextCompat.getColor(this, R.color.colorPrimary)
        val msgTitleSpan = SpannableString(msgTitle)
        msgTitleSpan.setSpan(StyleSpan(Typeface.BOLD), 0, msgTitleSpan.length, 0)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(msgTitleSpan)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(msgBody)
            )
            .setColor(color)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(msgTime.toInt(), notificationBuilder.build())
    }

    private fun sendRegistrationToServer(tokenID: String) {
        val userID = FirebaseAuth.getInstance().uid ?: Constants.EMPTY_UID
        val newToken = NotificationToken(userID, tokenID)
        NotificationTokenRepository.pushToken(newToken).addOnCompleteListener {
            if (it.isSuccessful)
                Log.d(Constants.FIREBASE_MESSAGE_SERVICE, "New Token successfully sent to server")
            else
                Log.d(
                    Constants.FIREBASE_MESSAGE_SERVICE,
                    "There was an error in sending token to server"
                )
        }
    }
}