package it.polito.mad.runtimeterrormad.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.polito.mad.runtimeterrormad.R
import it.polito.mad.runtimeterrormad.viewmodels.InterestedUsersListViewModel
import kotlinx.android.synthetic.main.dialog_subscriber_users.*

class SubscribedUsersListDialog : DialogFragment() {

    private lateinit var dialog: AlertDialog
    private val subscriberUsersVM: InterestedUsersListViewModel by navGraphViewModels(R.id.item_details_graph)

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.followerListDialogTitle))
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ -> }
            .create()
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_subscriber_users, container, false)
        dialog.setView(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listSubscriberUsersDialogRV.layoutManager = LinearLayoutManager(context)
        subscriberUsersVM.getSubscribedUsers().observe(this, Observer {
            listSubscriberUsersDialogRV.adapter = SubscribedUserAdapter(it)
        })
    }
}