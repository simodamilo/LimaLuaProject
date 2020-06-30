package it.polito.mad.runtimeterrormad.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.runtimeterrormad.R

class ReviewAdapter(private val reviews: List<String>) :
    RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val card =
            LayoutInflater.from(parent.context).inflate(R.layout.cardview_review, parent, false)
        return ViewHolder(card)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(reviews[position])

    override fun getItemCount() = reviews.size

    override fun onViewRecycled(holder: ViewHolder) = holder.unbind()

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val reviewTV: TextView = v.findViewById(R.id.reviewTV)

        fun bind(i: String) {
            reviewTV.text = i
        }

        fun unbind() {

        }
    }
}