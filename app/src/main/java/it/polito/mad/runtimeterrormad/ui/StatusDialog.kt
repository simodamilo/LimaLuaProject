package it.polito.mad.runtimeterrormad.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.runtimeterrormad.R
import it.polito.mad.runtimeterrormad.data.WillingToBuyUser
import it.polito.mad.runtimeterrormad.viewmodels.InterestedUsersListViewModel
import it.polito.mad.runtimeterrormad.viewmodels.ItemDetailsViewModel
import kotlinx.android.synthetic.main.dialog_buyer.*


class StatusDialog : DialogFragment() {
    private lateinit var dialog: AlertDialog
    private lateinit var itemID: String
    private lateinit var authUserID: String
    private lateinit var willingToBuyUsers: List<WillingToBuyUser>
    private val itemDetailsVM: ItemDetailsViewModel by navGraphViewModels(R.id.item_details_graph)
    private val willingToBuyUsersVM: InterestedUsersListViewModel by navGraphViewModels(R.id.item_details_graph)
    private var checkedItem: Int = -1

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        itemID = arguments?.getString(Constants.BUNDLE_ITEM_ID, "") ?: ""
        authUserID = arguments?.getString(Constants.BUNDLE_USER_ID, "") ?: ""

        val itemTmp = itemDetailsVM.getItem().value!!
        checkedItem = itemTmp.status

        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.statusDialogTitle))
            .setNeutralButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ -> }
            .create()


        dialog.setOnShowListener {
            val button =
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                when {
                    checkedItem == Constants.ITEM_SOLD && buyerBuyerDialogAT.text.toString()
                        .isBlank() -> {
                        Snackbar.make(
                            requireParentFragment().requireView(),
                            R.string.noBuyerSelectedSnackbar,
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }
                    checkedItem == Constants.ITEM_SOLD -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage(
                                resources.getString(
                                    R.string.confirmSellDialogMessage,
                                    buyerBuyerDialogAT.text.toString()
                                )
                            )
                            .setNeutralButton(resources.getString(R.string.cancel)) { _, _ -> }
                            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                                val buyer =
                                    willingToBuyUsers.find { it.nickname == buyerBuyerDialogAT.text.toString() }
                                buyer?.let { itemTmp.buyerID = it.userID }
                                itemTmp.status = checkedItem
                                itemTmp.hidden = true
                                itemDetailsVM.storeItem(itemTmp)
                                //itemDetailsVM.deleteItem()
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    }
                    else -> {
                        itemTmp.status = checkedItem
                        itemDetailsVM.storeItem(itemTmp)
                        dialog.dismiss()
                    }
                }
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("AAA", "onCreateView")
        val view = inflater.inflate(R.layout.dialog_buyer, container, false)
        dialog.setView(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (checkedItem) {
            0 -> optionsStatusDialogRG.check(R.id.available)
            1 -> optionsStatusDialogRG.check(R.id.suspended)
            2 -> optionsStatusDialogRG.check(R.id.sold)
            3 -> optionsStatusDialogRG.check(R.id.sold)
        }

        willingToBuyUsersVM.getWillingToBuyUsers().observe(this, Observer {
            willingToBuyUsers = it
            val nicknamesList = willingToBuyUsers.map { user -> user.nickname }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                nicknamesList
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            buyerBuyerDialogAT.setAdapter(adapter)

        })

        optionsStatusDialogRG.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                R.id.available -> {
                    checkedItem = Constants.ITEM_AVAILABLE
                    buyerBuyerDialogTIL.visibility = View.GONE
                }
                R.id.suspended -> {
                    checkedItem = Constants.ITEM_SUSPENDED
                    buyerBuyerDialogTIL.visibility = View.GONE

                }
                R.id.sold -> {
                    checkedItem = Constants.ITEM_SOLD
                    buyerBuyerDialogTIL.visibility = View.VISIBLE
                }
            }
        }
    }
}