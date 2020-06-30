package it.polito.mad.runtimeterrormad.ui

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.navGraphViewModels
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.GoogleAuthProvider
import it.polito.mad.runtimeterrormad.R
import it.polito.mad.runtimeterrormad.data.Profile
import it.polito.mad.runtimeterrormad.viewmodels.AuthViewModel
import it.polito.mad.runtimeterrormad.viewmodels.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_profile_show.*


class ProfileShowFragment : Fragment(), OnMapReadyCallback {

    private val profileVM: ProfileViewModel by activityViewModels()
    private val authVM: AuthViewModel by activityViewModels()
    private val itemUserProfileVM: ProfileViewModel by navGraphViewModels(R.id.item_details_graph)
    private lateinit var profileObserver: Observer<Profile>
    private lateinit var userID: String
    private lateinit var authUserID: String
    private lateinit var latLng: LatLng

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_show, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userID = arguments?.getString(Constants.BUNDLE_USER_ID, "") ?: ""
        authUserID = authVM.getUserID().value!!

        locationMapShowProfileMV.isClickable = false
        locationMapShowProfileMV.onCreate(savedInstanceState)

        if (userID.isEmpty() || userID == authUserID) {
            setProfileOwnerUI()
        } else if (userID.isNotEmpty() && authUserID == Constants.EMPTY_UID)
            setAnonymousUI()
        else if (userID.isNotEmpty() && authUserID != Constants.EMPTY_UID && userID != authUserID)
            setConfidentialUI()
    }

    override fun onStart() {
        super.onStart()
        locationMapShowProfileMV.onStart()
    }

    override fun onResume() {
        super.onResume()
        locationMapShowProfileMV.onResume()
    }

    override fun onPause() {
        super.onPause()
        locationMapShowProfileMV.onPause()
    }

    override fun onStop() {
        super.onStop()
        locationMapShowProfileMV.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationMapShowProfileMV.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        locationMapShowProfileMV.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (view != null) {
            locationMapShowProfileMV.onSaveInstanceState(outState)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_bar_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.pencilShowActivityMI -> {
                view?.findNavController()
                    ?.navigate(R.id.action_showProfileFragment_to_editProfileFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(ContentValues.TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(ContentValues.TAG, "Google sign in failed", e)
                Snackbar.make(requireView(), R.string.errorSignInButtonText, Snackbar.LENGTH_LONG)
                    .show()
            }
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

    private fun setAnonymousUI() {
        fullnameShowProfileTV.visibility = View.GONE
        nicknameShowProfileTV.visibility = View.GONE
        emailShowProfileTV.visibility = View.GONE
        locationMapShowProfileMV.visibility = View.GONE

        ratingLabelShowProfileTV.visibility = View.GONE
        reviewShowProfileB.visibility = View.GONE
        totalReviewShowProfileTV.visibility = View.GONE
        ratingShowProfileRB.visibility = View.GONE

        signShowProfileBTN.visibility = View.GONE
    }


    private fun setConfidentialUI() {
        hidePanelShowProfileVW.visibility = View.GONE
        fullnameShowProfileTV.visibility = View.GONE
        emailShowProfileTV.visibility = View.GONE
        locationMapShowProfileMV.visibility = View.VISIBLE //TODO

        ratingLabelShowProfileTV.visibility = View.VISIBLE
        reviewShowProfileB.visibility = View.VISIBLE
        totalReviewShowProfileTV.visibility = View.VISIBLE
        ratingShowProfileRB.visibility = View.VISIBLE

        signShowProfileBTN.visibility = View.GONE

        emailShowProfileTV.linksClickable = true
        setUserProfile(Constants.CONFIDENTIAL)
    }

    private fun setProfileOwnerUI() {
        signShowProfileBTN.visibility = View.VISIBLE
        authVM.getUserID().observe(viewLifecycleOwner, Observer {

            if (it == Constants.EMPTY_UID)
                setSignedOutUI()
            else
                setSignedInUI()
        })
    }

    private fun setSignedInUI() {
        setHasOptionsMenu(true)
        setUserProfile(Constants.PROFILE_OWNER)

        hidePanelShowProfileVW.visibility = View.GONE
        locationMapShowProfileMV.visibility = View.VISIBLE

        ratingLabelShowProfileTV.visibility = View.VISIBLE
        reviewShowProfileB.visibility = View.VISIBLE
        totalReviewShowProfileTV.visibility = View.VISIBLE
        ratingShowProfileRB.visibility = View.VISIBLE

        signShowProfileBTN.text = getString(R.string.signoutButtonText)
        signShowProfileBTN.setOnClickListener { signOut() }
    }

    private fun setSignedOutUI() {
        if (this::profileObserver.isInitialized) {
            profileVM.getProfile().removeObserver(profileObserver)
            fullnameShowProfileTV.text = getString(R.string.fullname)
            nicknameShowProfileTV.text = getString(R.string.nickname)
            emailShowProfileTV.text = getString(R.string.email)
            userImageShowProfileIV.setImageResource(R.drawable.ic_profile_image)
        }
        setHasOptionsMenu(false)

        hidePanelShowProfileVW.visibility = View.VISIBLE
        locationMapShowProfileMV.visibility = View.GONE

        ratingLabelShowProfileTV.visibility = View.GONE
        reviewShowProfileB.visibility = View.GONE
        totalReviewShowProfileTV.visibility = View.GONE
        ratingShowProfileRB.visibility = View.GONE

        signShowProfileBTN.text = getString(R.string.signinButtonText)
        signShowProfileBTN.setOnClickListener { signIn() }
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, Constants.RC_SIGN_IN)

    }

    private fun signOut() {
        authVM.getAuth().signOut()
        authVM.updateUserID()
        Snackbar.make(requireView(), R.string.signedOutSnackbar, Snackbar.LENGTH_LONG).show()
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        authVM.getAuth().signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(ContentValues.TAG, "signInWithCredential:success")
                authVM.checkAndCreateUser()
                authVM.updateUserID()
                Snackbar.make(requireView(), R.string.signedInSnackbar, Snackbar.LENGTH_LONG).show()
            } else {
                Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                Snackbar.make(
                    requireView(),
                    R.string.errorSignInFirebaseSnackbar,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setUserProfile(mode: Int) {
        profileObserver = Observer<Profile> {
            if (it.fullname.isNotEmpty())
                fullnameShowProfileTV.text = it.fullname
            if (it.nickname.isNotEmpty())
                nicknameShowProfileTV.text = it.nickname
            if (it.email.isNotEmpty())
                emailShowProfileTV.text = it.email
            if (it.totalRating != 0)
                ratingShowProfileRB.rating = (it.rating / it.totalRating).toFloat()
            totalReviewShowProfileTV.text =
                getString(R.string.totalRating, it.totalRating.toString())
            if (it.image.isNotEmpty()) {
                Glide.with(this)
                    .load(it.image)
                    .placeholder(R.drawable.ic_profile_image)
                    .error(R.drawable.ic_broken_image)
                    .into(userImageShowProfileIV)

            }
            if (it.lat != Constants.NOT_A_POSITION && it.lng != Constants.NOT_A_POSITION) {
                LatLng(it.lat, it.lng).let { latLngTmp ->
                    latLng = latLngTmp
                    locationMapShowProfileMV.getMapAsync(this)
                }
            }
        }
        if (mode == Constants.PROFILE_OWNER) {
            profileVM.getProfile().observe(viewLifecycleOwner, profileObserver)

            reviewShowProfileB.setOnClickListener {
                requireView().findNavController().navigate(
                    R.id.action_showProfileFragment_to_reviewFragment,
                    bundleOf(Constants.BUNDLE_MODE to mode)
                )
            }
        } else if (mode == Constants.CONFIDENTIAL) {
            itemUserProfileVM.getProfile().observe(viewLifecycleOwner, profileObserver)

            reviewShowProfileB.setOnClickListener {
                requireView().findNavController().navigate(
                    R.id.action_showOtherProfileFragment_to_otherReviewFragment,
                    bundleOf(Constants.BUNDLE_MODE to mode)
                )
            }
        }
    }
}
