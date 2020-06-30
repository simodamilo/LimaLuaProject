package it.polito.mad.runtimeterrormad.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.runtimeterrormad.R
import it.polito.mad.runtimeterrormad.data.Profile
import it.polito.mad.runtimeterrormad.ui.Constants.NOT_A_POSITION
import it.polito.mad.runtimeterrormad.viewmodels.AuthViewModel
import it.polito.mad.runtimeterrormad.viewmodels.ProfileEditViewModel
import it.polito.mad.runtimeterrormad.viewmodels.ProfileViewModelFactory
import kotlinx.android.synthetic.main.fragment_profile_edit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.*


class ProfileEditFragment : Fragment(), OnMapReadyCallback {

    private val authVM: AuthViewModel by activityViewModels()
    private lateinit var profileEditVM: ProfileEditViewModel
    private var takePhotoUri: Uri = Uri.EMPTY
    private var imageModified = false
    private lateinit var latLng: LatLng
    private lateinit var geocoder: Geocoder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)


        val userID = authVM.getUserID().value!!
        profileEditVM = ViewModelProvider(
            this,
            ProfileViewModelFactory(userID)
        ).get(ProfileEditViewModel::class.java)

        locationMapProfileEditMV.onCreate(savedInstanceState)


        profileEditVM.getProfile().observe(viewLifecycleOwner, Observer {
            if (it.fullname.isNotEmpty())
                fullnameEditProfileET.setText(it.fullname)
            if (it.nickname.isNotEmpty())
                nicknameEditProfileET.setText(it.nickname)
            if (it.email.isNotEmpty())
                emailEditProfileET.setText(it.email)
            if (it.image.isNotEmpty()) {
                Glide.with(this)
                    .load(it.image)
                    /*.transform(Rotate(it.imageRotation))*/
                    .error(R.drawable.ic_broken_image)
                    .into(userImageEditProfileIV)
            }
            if (it.lat != NOT_A_POSITION && it.lng != NOT_A_POSITION) {
                LatLng(it.lat, it.lng).let { latLngTmp ->
                    latLng = latLngTmp
                    locationMapProfileEditMV.getMapAsync(this)
                }
            }
        })

        if (savedInstanceState != null) {
            takePhotoUri = savedInstanceState.getParcelable(Constants.BUNDLE_PHOTO_URI) ?: Uri.EMPTY
            imageModified = savedInstanceState.getBoolean(Constants.BUNDLE_IMAGE_MOD)
        }


        setAfterTextChangedListener()
        setAddImageFab()

        geocoder = Geocoder(context, Locale.getDefault())

        searchLocationEditProfileBTN.setOnClickListener {
            if (!searchLocationEditProfileET.text.isNullOrBlank()) {
                MainScope().launch {
                    try {
                        val result = withContext(Dispatchers.Default) { getCoordinates() }
                        if (result != null) {
                            latLng = LatLng(result.latitude, result.longitude)
                            locationMapProfileEditMV.getMapAsync(this@ProfileEditFragment)
                        } else {
                            Snackbar.make(
                                requireView(),
                                getString(
                                    R.string.no_location,
                                    searchLocationEditProfileET.text.toString()
                                ),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                    } catch (e: IOException) {
                        Snackbar.make(
                            requireView(),
                            R.string.no_connection_location,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
                hideKeyboard()
            }
        }

        /*searchLocationEditProfileBTN.setOnClickListener {
            if (!searchLocationEditProfileET.text.isNullOrEmpty()) {
                locationMapProfileEditMV.getMapAsync { googleMap ->
                    MainScope().launch {
                        try {
                            withContext(Dispatchers.Default) {
                                getCoordinates()
                            }.let {
                                if (it.isEmpty()) {
                                    Snackbar.make(
                                        requireView(),
                                        getString(
                                            R.string.no_location,
                                            searchLocationEditProfileET.text.toString()
                                        ),
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                } else {
                                    latLng = LatLng(it.first().latitude, it.first().longitude)
                                    googleMap.moveCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            latLng,
                                            12F
                                        )
                                    )
                                    googleMap.addMarker(MarkerOptions().position(latLng))
                                }
                            }
                        } catch (e: IOException) {
                            Snackbar.make(
                                requireView(),
                                R.string.no_connection_location,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        } */

    }

    /* private suspend fun getCoordinates(): MutableList<Address> {
         return withContext(Dispatchers.IO) {
             geocoder.getFromLocationName(searchLocationEditProfileET.text.toString(), 1)
         }
     } */
    private suspend fun getCoordinates(): Address? {
        return withContext(Dispatchers.IO) {
            geocoder.getFromLocationName(searchLocationEditProfileET.text.toString(), 1)
                .firstOrNull()
        }
    }

    override fun onStart() {
        super.onStart()
        locationMapProfileEditMV.onStart()
    }

    override fun onResume() {
        super.onResume()
        locationMapProfileEditMV.onResume()
    }

    override fun onPause() {
        super.onPause()
        locationMapProfileEditMV.onPause()
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
        locationMapProfileEditMV.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationMapProfileEditMV.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        locationMapProfileEditMV.onLowMemory()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        locationMapProfileEditMV.onSaveInstanceState(outState)
        val profile = Profile(
            fullname = fullnameEditProfileET.text.toString(),
            nickname = nicknameEditProfileET.text.toString(),
            email = emailEditProfileET.text.toString(),
            image = profileEditVM.getProfile().value?.image ?: "",
            imageRotation = profileEditVM.getProfile().value?.imageRotation ?: 0
        )
        if (this::latLng.isInitialized) {
            profile.lat = latLng.latitude
            profile.lng = latLng.longitude
        }
        profileEditVM.setProfile(profile)
        outState.putParcelable(Constants.BUNDLE_PHOTO_URI, takePhotoUri)
        outState.putBoolean(Constants.BUNDLE_IMAGE_MOD, imageModified)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.REQUEST_CODE_IMAGE_CAPTURE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val tmpProfile = profileEditVM.getProfile().value ?: Profile()
                        tmpProfile.apply {
                            imageRotation = getSensorOrientation()
                            image = takePhotoUri.toString()
                        }
                        profileEditVM.setProfile(tmpProfile)
                        imageModified = true
                    }
                }
            }
            Constants.REQUEST_CODE_OPEN_GALLERY -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val tmpProfile = profileEditVM.getProfile().value ?: Profile()
                        tmpProfile.apply {
                            imageRotation = 0
                            image = data?.data?.toString() ?: ""
                        }
                        profileEditVM.setProfile(tmpProfile)
                        imageModified = true
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.uiSettings.apply {
            isCompassEnabled = false
            isMapToolbarEnabled = false
            isRotateGesturesEnabled = false
            isScrollGesturesEnabled = false
            isTiltGesturesEnabled = false
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = true
        }
        if (this::latLng.isInitialized) {
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
        }
        googleMap.setOnMapClickListener {
            latLng = it
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng))
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_bar_save, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveEditProfileMI -> {
                saveUserInfo()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun dispatchPickFromGalleryIntent() {
        Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ).also { pickFromGalleryIntent ->
            pickFromGalleryIntent.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            pickFromGalleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(pickFromGalleryIntent, Constants.REQUEST_CODE_OPEN_GALLERY)
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity?.packageManager!!)?.also {
                createImageFile()?.let {
                    takePhotoUri = it.toUri()
                    val providerUri = context?.let { ctx ->
                        FileProvider.getUriForFile(
                            ctx,
                            "it.polito.mad.runtimeterrormad.fileprovider",
                            it
                        )
                    }
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerUri)
                    startActivityForResult(
                        takePictureIntent,
                        Constants.REQUEST_CODE_IMAGE_CAPTURE
                    )
                }
            }
        }
    }

    private fun setAddImageFab() {
        addImageEditProfileFAB.setOnClickListener {
            val popup = PopupMenu(activity, it)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.context_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.galleryEditProfileMI -> {
                        if (checkTakePictureReqs()) {
                            dispatchPickFromGalleryIntent()
                        }
                        true
                    }
                    R.id.cameraEditProfileMI -> {
                        dispatchTakePictureIntent()
                        true
                    }
                    else -> super.onContextItemSelected(item)
                }
            }
            popup.show()
        }
    }

    private fun setAfterTextChangedListener() {
        fullnameEditProfileET.doOnTextChanged { text, _, _, _ ->
            when {
                text.isNullOrBlank() -> fullnameEditProfileTIL.error =
                    getString(R.string.emptyFullnameError)
                else -> fullnameEditProfileTIL.error = null
            }
        }
        nicknameEditProfileET.doOnTextChanged { text, _, _, _ ->
            when {
                text.isNullOrBlank() -> nicknameEditProfileTIL.error =
                    getString(R.string.emptyNicknameError)
                else -> nicknameEditProfileTIL.error = null
            }
        }
        emailEditProfileET.doOnTextChanged { text, _, _, _ ->
            when {
                text.isNullOrBlank() -> emailEditProfileTIL.error =
                    getString(R.string.emptyEmailError)
                !Patterns.EMAIL_ADDRESS.matcher(text).matches() -> emailEditProfileTIL.error =
                    getString(R.string.wrongEmailError)
                else -> emailEditProfileTIL.error = null
            }
        }
    }

    private fun checkFields(): Boolean {
        if (fullnameEditProfileET.text.isNullOrBlank())
            fullnameEditProfileTIL.error = getString(R.string.emptyFullnameError)
        if (nicknameEditProfileET.text.isNullOrBlank())
            nicknameEditProfileTIL.error = getString(R.string.emptyNicknameError)
        if (emailEditProfileET.text.isNullOrBlank())
            emailEditProfileTIL.error = getString(R.string.emptyEmailError)

        return (fullnameEditProfileTIL.error.isNullOrEmpty() && nicknameEditProfileTIL.error.isNullOrEmpty()
                && emailEditProfileTIL.error.isNullOrEmpty() && this::latLng.isInitialized)
    }

    private fun checkTakePictureReqs(): Boolean {
        if (activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)!!) {
            return true
            /* Unused because permissions don't work as intended
            return when {
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.P -> true
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> true
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        PERMISSIONS_REQUEST_CAMERA
                    )
                    Toast.makeText(applicationContext, "No permission", Toast.LENGTH_SHORT)
                        .show()
                    false
                }
            } */
        } else {
            Snackbar.make(requireView(), R.string.cameraErrorToast, Snackbar.LENGTH_LONG).show()
            return false
        }
    }

    private fun saveUserInfo() {
        if (checkFields()) {
            val tmpProfile = profileEditVM.getProfile().value?.copy() ?: Profile()
            tmpProfile.apply {
                fullname = fullnameEditProfileET.text.toString()
                nickname = nicknameEditProfileET.text.toString()
                email = emailEditProfileET.text.toString()
                lat = latLng.latitude
                lng = latLng.longitude
            }
            if (imageModified) {
                tmpProfile.let {
                    activity?.contentResolver?.let { cntRes ->
                        val inputStream = cntRes.openInputStream(Uri.parse(it.image))
                        val outputFile = File(
                            activity?.externalCacheDir,
                            "profilePhoto.jpg"
                        )
                        val outStream = outputFile.outputStream()

                        val btm: Bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        /*val matrix = Matrix()
                        matrix.postRotate(item.imageRotation.toFloat())
                        val finalBitmap = Bitmap.createBitmap(
                            btm, 0, 0,
                            btm.width, btm.height,
                            matrix, true
                        )*/
                        btm.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                        it.image = outputFile.toString()
                    }
                }
            }
            profileEditVM.storeProfile(tmpProfile, imageModified)

            view?.findNavController()?.navigate(
                R.id.action_editProfileFragment_to_showProfileFragment
            )
            Snackbar.make(requireView(), R.string.updatedShowProfileToast, Snackbar.LENGTH_LONG)
                .show()
        } else {
            Snackbar.make(requireView(), R.string.emptyFieldsToast, Snackbar.LENGTH_LONG)
                .show()
        }
    }

    private fun hideKeyboard() {
        activity?.currentFocus?.let { view ->
            context?.getSystemService(Context.INPUT_METHOD_SERVICE)?.let {
                it as InputMethodManager
                it.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    private fun createImageFile(): File? {
        /* create an image temp file in app's files directory */
        return try {
            File.createTempFile(
                "profile_",
                "",
                activity?.externalCacheDir
            )
        } catch (ex: IOException) {
            /* show output if error occurred while creating the File */
            Snackbar.make(requireView(), R.string.memoryErrorToast, Snackbar.LENGTH_LONG)
                .show()
            null
        }
    }

    /* this function get the back camera sensor module orientation */
    private fun getSensorOrientation(): Int {
        val manager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        return try {
            manager.let {
                val rearCamera = it.cameraIdList[0]
                it.getCameraCharacteristics(rearCamera)
                    .get(CameraCharacteristics.SENSOR_ORIENTATION)
            } ?: 0
        } catch (e: CameraAccessException) {
            0
        }
    }

    /* private fun setMarkerPosition(lat: Double, lng: Double) {
         mapFragment.getMapAsync {
             val latLng = LatLng(lat, lng)
             locationCoord = latLng
             it.addMarker(MarkerOptions().position(locationCoord))
             it.moveCamera(CameraUpdateFactory.newLatLng(locationCoord))
         }
     }

     private fun setGoogleMap() {
         mapFragment.getMapAsync { googleMap ->
             googleMap.setOnMapClickListener {
                 locationCoord = it
                 googleMap.clear()
                 googleMap.addMarker(MarkerOptions().position(locationCoord))
                 googleMap.moveCamera(CameraUpdateFactory.newLatLng(locationCoord))
             }
             googleMap.setOnMarkerClickListener {
                 googleMap.clear()
                 val latLng = LatLng(NOT_A_POSITION, NOT_A_POSITION)
                 locationCoord = latLng
                 true
             }
         }
         childFragmentManager.beginTransaction().replace(R.id.locationMapEditProfileFCV, mapFragment)
             .commit()
     } */


/* this function checks exif data and rotate image, but android rotate it
    according to the screen orientation so it useless */
/* private fun checkAndRotateImage(photoUri: Uri) {
     try {
         val orientation = contentResolver.openInputStream(photoUri)?.let {
             ExifInterface(it).getAttributeInt(
                 ExifInterface.TAG_ORIENTATION,
                 ExifInterface.ORIENTATION_UNDEFINED
             )
         }
         if (orientation == ExifInterface.ORIENTATION_NORMAL) return

         val imageIn = MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
         /* ImageDecoder.createSource(contentResolver, photoUri).let{ImageDecoder.decodeBitmap(it)} */
         contentResolver.openOutputStream(photoUri)?.let { streamOut ->
             when (orientation) {
                 ExifInterface.ORIENTATION_ROTATE_90 -> imageIn.rotateBy(90F)
                 ExifInterface.ORIENTATION_ROTATE_180 -> imageIn.rotateBy(180F)
                 ExifInterface.ORIENTATION_ROTATE_270 -> imageIn.rotateBy(270F)
                 else -> imageIn
             }.compress(Bitmap.CompressFormat.PNG, 100, streamOut)
             imageIn.recycle()
             streamOut.close()
         }
     } catch (ex: IOException) {
     }
 } */

/* this function check orientation and rotate the image, overwriting the original file*/
/* private fun checkOrientAndRotateImage(photoUri: Uri) {
    val orientation = getSensorOrientation()
    if (orientation != 0) {
        val inStream = contentResolver.openInputStream(photoUri)
        val bitmap = BitmapFactory.decodeStream(inStream)
        inStream?.close()
        val outStream = contentResolver.openOutputStream(photoUri)
        bitmap.rotateBy(orientation.toFloat()).apply {
            compress(Bitmap.CompressFormat.PNG, 100, outStream)
            recycle()
        }
        outStream?.close()
    }
}*/

}