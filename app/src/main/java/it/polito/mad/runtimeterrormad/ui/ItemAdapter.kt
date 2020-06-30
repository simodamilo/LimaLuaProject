package it.polito.mad.runtimeterrormad.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.runtimeterrormad.R
import it.polito.mad.runtimeterrormad.data.Item

class ItemAdapter(private val items: List<Item>, private val mode: Int) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val card =
            LayoutInflater.from(parent.context).inflate(R.layout.cardview_item, parent, false)
        return ViewHolder(card)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    override fun onViewRecycled(holder: ViewHolder) = holder.unbind()

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val cardItemListCW: CardView = v.findViewById(R.id.cardItemListCW)
        private val imageItemListIW: ImageView = v.findViewById(R.id.imageItemListIW)
        private val titleItemListTV: TextView = v.findViewById(R.id.titleItemListTV)
        private val priceItemListTV: TextView = v.findViewById(R.id.priceItemListTV)
        private val editItemListMB: MaterialButton = v.findViewById(R.id.editItemListMB)

        fun bind(i: Item) {
            if (i.image.isEmpty())
                imageItemListIW.setImageResource(R.drawable.ic_item_image)
            else {
                Glide.with(itemView)
                    .load(i.image)
                    /*.transform(Rotate(i.imageRotation))*/
                    .error(R.drawable.ic_broken_image)
                    .into(imageItemListIW)
            }
            titleItemListTV.text = i.title
            priceItemListTV.text =
                String.format("%s%.02f", itemView.context.getString(R.string.currency), i.price)
            when (mode) {
                Constants.ITEM_LIST -> {
                    if (i.status == Constants.ITEM_SOLD || i.status == Constants.ITEM_SOLD_EVALUATED) {
                        editItemListMB.iconTint =
                            ContextCompat.getColorStateList(itemView.context, R.color.Grey500)
                        editItemListMB.setOnClickListener {
                            Snackbar.make(
                                itemView,
                                itemView.resources.getString(R.string.itemNotEditableSnackbar),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        editItemListMB.setOnClickListener {
                            itemView.findNavController()
                                .navigate(
                                    R.id.action_itemListFragment_to_itemEditFragment,
                                    bundleOf(Constants.BUNDLE_ITEM_ID to items[absoluteAdapterPosition].itemID)
                                )
                        }
                    }
                    cardItemListCW.setOnClickListener {
                        itemView.findNavController()
                            .navigate(
                                R.id.action_itemListFragment_to_itemDetailsFragment,
                                bundleOf(
                                    Constants.BUNDLE_ITEM_ID to items[absoluteAdapterPosition].itemID,
                                    Constants.BUNDLE_ITEM_USER_ID to items[absoluteAdapterPosition].userID
                                )
                            )
                    }
                }
                Constants.ON_SALE_ITEM_LIST -> {
                    editItemListMB.visibility = View.GONE
                    cardItemListCW.setOnClickListener {
                        itemView.findNavController()
                            .navigate(
                                R.id.action_onSaleListFragment_to_itemDetailsFragment,
                                bundleOf(
                                    Constants.BUNDLE_ITEM_ID to items[absoluteAdapterPosition].itemID,
                                    Constants.BUNDLE_ITEM_USER_ID to items[absoluteAdapterPosition].userID
                                )
                            )
                    }
                }
                Constants.INTEREST_ITEM_LIST -> {
                    editItemListMB.visibility = View.GONE
                    cardItemListCW.setOnClickListener {
                        itemView.findNavController()
                            .navigate(
                                R.id.action_itemsOfInterestListFragment_to_item_details_graph,
                                bundleOf(
                                    Constants.BUNDLE_ITEM_ID to items[absoluteAdapterPosition].itemID,
                                    Constants.BUNDLE_ITEM_USER_ID to items[absoluteAdapterPosition].userID
                                )
                            )
                    }
                }
                Constants.BOUGHT_ITEM_LIST -> {
                    editItemListMB.visibility = View.GONE
                    cardItemListCW.setOnClickListener {
                        itemView.findNavController()
                            .navigate(
                                R.id.action_boughtItemsListFragment_to_item_details_graph,
                                bundleOf(
                                    Constants.BUNDLE_ITEM_ID to items[absoluteAdapterPosition].itemID,
                                    Constants.BUNDLE_ITEM_USER_ID to items[absoluteAdapterPosition].userID
                                )
                            )
                    }
                }
            }
        }

        fun unbind() {
            cardItemListCW.setOnClickListener(null)
            editItemListMB.setOnClickListener(null)
        }
    }
}