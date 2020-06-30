package it.polito.mad.runtimeterrormad.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import it.polito.mad.runtimeterrormad.R
import it.polito.mad.runtimeterrormad.data.SubscribedUser

class SubscribedUserAdapter(private val subscribedUsers: List<SubscribedUser>) :
    RecyclerView.Adapter<SubscribedUserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val card =
            LayoutInflater.from(parent.context).inflate(R.layout.cardview_follower, parent, false)
        return ViewHolder(card)
    }

    override fun onBindViewHolder(holder: SubscribedUserAdapter.ViewHolder, position: Int) =
        holder.bind(subscribedUsers[position])

    override fun getItemCount() = subscribedUsers.size

    override fun onViewRecycled(holder: ViewHolder) = holder.unbind()


    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val imageFollowerListIW: ImageView = v.findViewById(R.id.imageFollowerListIW)
        private val nicknameFollowerListTV: TextView = v.findViewById(R.id.nicknameFollowerListTV)

        fun bind(i: SubscribedUser) {
            Glide.with(itemView)
                .load(i.imageURL)
                .placeholder(R.drawable.ic_profile_image)
                .into(imageFollowerListIW)
            nicknameFollowerListTV.text = i.nickname
        }

        fun unbind() {}
    }
}