package it.polito.mad.runtimeterrormad.ui

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.util.Linkify
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.runtimeterrormad.R
import it.polito.mad.runtimeterrormad.data.SubscribedUser
import it.polito.mad.runtimeterrormad.data.WillingToBuyUser
import it.polito.mad.runtimeterrormad.viewmodels.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_item_details.*

class ItemDetailsFragment : Fragment(), OnMapReadyCallback {
    private lateinit var latLng: LatLng
    private lateinit var itemID: String
    private lateinit var itemUserID: String
    private lateinit var authUserID: String
    private lateinit var itemDetailsVM: ItemDetailsViewModel
    private lateinit var itemUserProfileVM: ProfileViewModel
    private lateinit var interestedUsersListVM: InterestedUsersListViewModel
    private lateinit var interestedUserVM: InterestedUserViewModel
    private val authVM: AuthViewModel by activityViewModels()
    private val profileVM: ProfileViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_item_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        itemID = arguments?.getString(Constants.BUNDLE_ITEM_ID, "") ?: ""
        itemUserID = arguments?.getString(Constants.BUNDLE_ITEM_USER_ID, "") ?: ""
        authUserID = authVM.getUserID().value!!

        itemDetailsVM = ViewModelProvider(
            findNavController().getViewModelStoreOwner(R.id.item_details_graph),
            ItemDetailsViewModelFactory(itemID)
        ).get(ItemDetailsViewModel::class.java)

        locationMapItemDetailsMV.isClickable = false
        locationMapItemDetailsMV.onCreate(savedInstanceState)

        setItemDetailObserver()

        when (authUserID) {
            itemUserID -> setItemOwnerUI()
            Constants.EMPTY_UID -> setAnonymousUI()
            else -> setConfidentialUI()
        }
    }

    override fun onStart() {
        super.onStart()
        locationMapItemDetailsMV.onStart()
    }

    override fun onResume() {
        super.onResume()
        locationMapItemDetailsMV.onResume()
    }

    override fun onPause() {
        super.onPause()
        locationMapItemDetailsMV.onPause()
    }

    override fun onStop() {
        super.onStop()
        locationMapItemDetailsMV.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationMapItemDetailsMV.onDestroy()
    }

    override fun onLowMemory() {
        locationMapItemDetailsMV.onLowMemory()
        super.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (view != null) {
            locationMapItemDetailsMV.onSaveInstanceState(outState)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_bar_item, menu)
        val itemStatus = itemDetailsVM.getItem().value?.status ?: Constants.ITEM_AVAILABLE
        if (itemStatus == Constants.ITEM_SOLD || itemStatus == Constants.ITEM_SOLD_EVALUATED) {
            menu.findItem(R.id.statusItemMenuMI).apply {
                isEnabled = false
                val titleStr = SpannableString(getString(R.string.changeStatus))
                val color = ContextCompat.getColor(requireContext(), R.color.Grey500)
                titleStr.setSpan(ForegroundColorSpan(color), 0, titleStr.length, 0)
                title = titleStr
            }
            menu.findItem(R.id.editItemMenuMI).apply {
                val color =
                    ContextCompat.getColor(requireContext(), R.color.colorPrimaryTransparent)
                icon.setTint(color)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editItemMenuMI -> {
                val itemStatus = itemDetailsVM.getItem().value?.status ?: Constants.ITEM_AVAILABLE
                if (itemStatus == Constants.ITEM_SOLD || itemStatus == Constants.ITEM_SOLD_EVALUATED) {
                    Snackbar.make(
                        requireView(),
                        resources.getString(R.string.itemNotEditableSnackbar),
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    view?.findNavController()?.navigate(
                        R.id.action_itemDetailsFragment_to_itemEditFragment,
                        bundleOf(Constants.BUNDLE_ITEM_ID to itemID)
                    )
                }
                true
            }
            R.id.deleteItemMenuMI -> {
                openDeleteDialog()
                true
            }
            R.id.statusItemMenuMI -> {
                openItemStatusDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (this::latLng.isInitialized) {
            googleMap.clear()
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12F))
            val bounds = LatLngBounds.builder().include(latLng).build()
            googleMap.setLatLngBoundsForCameraTarget(bounds)
            googleMap.addMarker(MarkerOptions().position(latLng))
        }
    }


    private fun setItemDetailObserver() {
        itemDetailsVM.getItem().observe(viewLifecycleOwner, Observer {
            if (it.buyerID.isNotBlank() && it.buyerID == authUserID && it.status == Constants.ITEM_SOLD) {
                ratingItemDetailsRB.visibility = View.VISIBLE
                reviewItemDetailsTIL.visibility = View.VISIBLE
                sendReviewItemDetailsB.visibility = View.VISIBLE
            } else {
                ratingItemDetailsRB.visibility = View.GONE
                reviewItemDetailsTIL.visibility = View.GONE
                sendReviewItemDetailsB.visibility = View.GONE
            }
            titleItemDetailsTV.text = it.title
            priceItemDetailsTV.text =
                String.format("%s%.02f", getString(R.string.currency), it.price)
            expiryDateItemDetailsTV.text = it.expiryDate
            descriptionItemDetailsETV.text = it.description
            if (it.categoryID != -1)
                categoryItemDetailsTV.text =
                    resources.getStringArray(R.array.categories)[it.categoryID]
            if (it.subtitle.isEmpty())
                subtitleItemDetailsTV.visibility = View.GONE
            else {
                subtitleItemDetailsTV.visibility = View.VISIBLE
                subtitleItemDetailsTV.text = it.subtitle
            }
            if (it.image.isNotEmpty()) {
                Glide.with(this)
                    .load(it.image)
                    /*.transform(Rotate(it.imageRotation))*/
                    .error(R.drawable.ic_broken_image)
                    .into(itemImageItemDetailsIV)
            }
            if (it.lat != Constants.NOT_A_POSITION && it.lng != Constants.NOT_A_POSITION) {
                LatLng(it.lat, it.lng).let { latLngTmp ->
                    latLng = latLngTmp
                    locationMapItemDetailsMV.getMapAsync(this)
                }
            }
            when (it.status) {
                Constants.ITEM_AVAILABLE -> statusItemDetailsTV.visibility = View.GONE
                Constants.ITEM_SUSPENDED -> {
                    statusItemDetailsTV.visibility = View.VISIBLE
                    statusItemDetailsTV.text = getString(R.string.itemSuspended)
                }
                Constants.ITEM_SOLD, Constants.ITEM_SOLD_EVALUATED -> { /* is it necessary to split these two cases? */
                    buyItemDetailsB.visibility = View.GONE
                    buyerItemDetailsTV.visibility = View.GONE
                    informativeItemDetailsIB.visibility = View.GONE
                    statusItemDetailsTV.visibility = View.VISIBLE
                    statusItemDetailsTV.text = getString(R.string.itemSold)
                    requireActivity().invalidateOptionsMenu()
                    requireActivity().mainActivityFAB.hide()
                    if (itemUserID == authUserID)
                        requireView().findNavController().navigateUp()
                }
            }
        })
    }


    private fun setAnonymousUI() {
        requireActivity().mainActivityFAB.hide()
        likesItemDetailsBTN.visibility = View.GONE
        profileItemDetailsCW.visibility = View.GONE
        locationMapItemDetailsMV.visibility = View.GONE
    }

    private fun setConfidentialUI() {
        interestedUserVM = ViewModelProvider(
            this,
            InterestedUserViewModelFactory(itemID, authUserID)
        ).get(InterestedUserViewModel::class.java)

        itemUserProfileVM = ViewModelProvider(
            findNavController().getViewModelStoreOwner(R.id.item_details_graph),
            ProfileViewModelFactory(itemUserID)
        ).get(ProfileViewModel::class.java)

        requireActivity().mainActivityFAB.apply {
            setImageResource(R.drawable.ic_heart)
            show()
        }

        profileItemDetailsCW.visibility = View.VISIBLE
        locationMapItemDetailsMV.visibility = View.VISIBLE
        setIsFollowerObserver()
        setItemUserProfileObserver()
        setIsBuyerObserver()

        profileItemDetailsCW.setOnClickListener {
            requireView().findNavController().navigate(
                R.id.action_itemDetailsFragment_to_showProfileFragmentTemp,
                bundleOf(Constants.BUNDLE_USER_ID to itemUserID)
            )
        }

        informativeItemDetailsIB.setOnClickListener {
            openInformativeDialog()
        }

        buyItemDetailsB.setOnClickListener {
            val profileTmp = profileVM.getProfile().value!!
            interestedUserVM.addWillToBuy(
                WillingToBuyUser(
                    userID = authUserID,
                    nickname = profileTmp.nickname,
                    imageURL = profileTmp.image
                )
            )
        }

        sendReviewItemDetailsB.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(
                    getString(
                        R.string.ratingConfirmDialog,
                        ratingItemDetailsRB.rating.toString()
                    )
                )
                .setPositiveButton(R.string.ok) { _, _ ->
                    itemUserProfileVM.updateRating(
                        ratingItemDetailsRB.rating.toDouble(),
                        reviewItemDetailsET.text.toString()
                    ).addOnSuccessListener {
                        Snackbar.make(
                            requireView(),
                            R.string.positiveUdateRatingSnackbar,
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                        itemDetailsVM.updateStatus(Constants.ITEM_SOLD_EVALUATED)
                    }.addOnFailureListener {
                        Snackbar.make(
                            requireView(),
                            R.string.negativeUdateRatingSnackbar,
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .show()
        }
    }

    private fun setItemOwnerUI() {
        setHasOptionsMenu(true)
        requireActivity().mainActivityFAB.hide()
        interestedUsersListVM = ViewModelProvider(
            findNavController().getViewModelStoreOwner(R.id.item_details_graph),
            InterestedUsersListViewModelFactory(itemID)
        ).get(InterestedUsersListViewModel::class.java)

        likesItemDetailsBTN.visibility = View.VISIBLE
        locationMapItemDetailsMV.visibility = View.VISIBLE
        setLikesListObserver()
        likesItemDetailsBTN.setOnClickListener {
            findNavController().navigate(
                R.id.action_itemDetailsFragment_to_followerListDialog
            )
        }
    }

    private fun setLikesListObserver() {
        interestedUsersListVM.getSubscribedUsers().observe(viewLifecycleOwner, Observer {
            when (it.size) {
                0 -> likesItemDetailsBTN.text = "0"
                in 1..99 -> likesItemDetailsBTN.text = it.size.toString()
                else -> likesItemDetailsBTN.text = getString(R.string.maxLikesVisible)
            }
        })
    }

    private fun setIsFollowerObserver() {
        interestedUserVM.isSubscribed().observe(viewLifecycleOwner, Observer {
            if (it)
                requireActivity().mainActivityFAB.apply {
                    setColorFilter(Color.RED)
                    setOnClickListener {
                        interestedUserVM.removeSubscription()
                        //profileVM.deleteItemOfInterest(itemID)
                    }
                }
            else
                requireActivity().mainActivityFAB.apply {
                    setColorFilter(Color.BLACK)
                    setOnClickListener {
                        val profileTmp = profileVM.getProfile().value!!
                        interestedUserVM.addSubscription(
                            SubscribedUser(
                                userID = authUserID,
                                nickname = profileTmp.nickname,
                                imageURL = profileTmp.image,
                                subscribed = true
                            )
                        )
                        /*   profileVM.storeItemOfInterest(
                               itemID,
                               itemDetailsVM.getItem().value!!
                           )*/

                    }
                }
        })
    }

    private fun setItemUserProfileObserver() {
        itemUserProfileVM.getProfile().observe(viewLifecycleOwner, Observer {
            Glide.with(requireContext())
                .load(it.image)
                .placeholder(R.drawable.ic_profile_image)
                .into(profileItemDetailsIW)
            emailItemDetailsTV.text = it.email
        })
    }

    private fun setIsBuyerObserver() {
        interestedUserVM.isWillingToBuy().observe(viewLifecycleOwner, Observer {
            val status = itemDetailsVM.getItem().value?.status
            if (it && (status == 0 || status == 1)) {
                buyItemDetailsB.isEnabled = false
                emailItemDetailsTV.visibility = View.VISIBLE
                Linkify.addLinks(emailItemDetailsTV, Linkify.EMAIL_ADDRESSES)
                buyerItemDetailsTV.visibility = View.GONE
                informativeItemDetailsIB.visibility = View.GONE
                buyItemDetailsB.visibility = View.GONE
            } else if (!it && (status == 0 || status == 1)) {
                buyItemDetailsB.isEnabled = true
                emailItemDetailsTV.visibility = View.GONE
                buyerItemDetailsTV.visibility = View.VISIBLE
                informativeItemDetailsIB.visibility = View.VISIBLE
                buyItemDetailsB.visibility = View.VISIBLE
            }
        })
    }

    private fun openItemStatusDialog() {
        findNavController().navigate(
            R.id.action_itemDetailsFragment_to_statusDialog,
            bundleOf(
                Constants.BUNDLE_ITEM_ID to itemID,
                Constants.BUNDLE_USER_ID to authUserID
            )
        )
    }

    private fun openInformativeDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.informativeMessageTitle)
            .setMessage(R.string.informativeMessage)
            .setNeutralButton(R.string.ok) { _, _ -> }
            .show()
    }

    private fun openDeleteDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(
                R.string.confirmDeleteItem
            )
            .setNeutralButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                itemDetailsVM.deleteItem()
                requireView().findNavController().navigateUp()
            }
            .create()
            .show()
    }
}