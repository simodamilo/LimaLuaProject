package it.polito.mad.runtimeterrormad.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.exifinterface.media.ExifInterface
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
import it.polito.mad.runtimeterrormad.data.Item
import it.polito.mad.runtimeterrormad.viewmodels.AuthViewModel
import it.polito.mad.runtimeterrormad.viewmodels.ItemEditViewModel
import it.polito.mad.runtimeterrormad.viewmodels.ItemEditViewModelFactory
import kotlinx.android.synthetic.main.fragment_item_edit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.util.*


class ItemEditFragment : Fragment(), OnMapReadyCallback {

    private val authVM: AuthViewModel by activityViewModels()
    private lateinit var itemEditVM: ItemEditViewModel
    private lateinit var userID: String
    private lateinit var itemID: String
    private var takePhotoUri: Uri = Uri.EMPTY
    private var imageModified = false
    private lateinit var latLng: LatLng
    private lateinit var geocoder: Geocoder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_item_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        itemID = arguments?.getString(Constants.BUNDLE_ITEM_ID, "") ?: ""
        userID = authVM.getUserID().value!!

        itemEditVM = ViewModelProvider(
            this,
            ItemEditViewModelFactory(userID, itemID)
        ).get(ItemEditViewModel::class.java)

        locationMapItemEditMV.onCreate(savedInstanceState)

        itemEditVM.getItem().observe(viewLifecycleOwner, Observer {
            if (it.title.isNotEmpty())
                titleItemEditET.setText(it.title)
            if (it.subtitle.isNotEmpty())
                subtitleItemEditET.setText(it.subtitle)
            if (it.price != 0.0)
                priceItemEditET.setText(String.format("%.02f", it.price))
            if (it.expiryDate.isNotEmpty())
                expiryDateItemEditET.setText(it.expiryDate)
            if (it.description.isNotEmpty())
                descriptionItemEditET.setText(it.description)
            if (it.categoryID != -1)
                categoryItemEditAT.setText(
                    resources.getStringArray(R.array.categories)[it.categoryID],
                    false
                )
            if (it.image.isNotEmpty()) {
                Glide.with(this)
                    .load(it.image)
                    /*.transform(Rotate(it.imageRotation))*/
                    .error(R.drawable.ic_broken_image)
                    .into(imageItemEditIV)
            }
            if (it.lat != Constants.NOT_A_POSITION && it.lng != Constants.NOT_A_POSITION) {
                LatLng(it.lat, it.lng).let { latLngTmp ->
                    latLng = latLngTmp
                    locationMapItemEditMV.getMapAsync(this)
                }
            }
        })

        if (savedInstanceState != null) {
            takePhotoUri = savedInstanceState.getParcelable(Constants.BUNDLE_PHOTO_URI) ?: Uri.EMPTY
            imageModified = savedInstanceState.getBoolean(Constants.BUNDLE_IMAGE_MOD)
        }

        this.context?.let {
            val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(
                it,
                android.R.layout.simple_spinner_dropdown_item,
                resources.getStringArray(R.array.categories)
            ) {

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val itemView: TextView = super.getDropDownView(
                        position,
                        convertView,
                        parent
                    ) as TextView

                    if (position == 0 || position == 5 || position == 10
                        || position == 16 || position == 22 || position == 27
                        || position == 40 || position == 49
                    ) {
                        itemView.setPaddingRelative(32, 0, 32, 0)
                        itemView.background = ColorDrawable(Color.parseColor("#F5F5F5"))
                        itemView.typeface = Typeface.DEFAULT_BOLD
                    } else {
                        itemView.setPaddingRelative(128, 0, 32, 0)
                        itemView.background = ColorDrawable(Color.parseColor("#FFFFFF"))
                        itemView.typeface = Typeface.DEFAULT
                    }

                    return itemView
                }

                override fun isEnabled(position: Int): Boolean {
                    return position != 0 && position != 5 && position != 10
                            && position != 16 && position != 22 && position != 27
                            && position != 40 && position != 49
                }
            }
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categoryItemEditAT.setAdapter(adapter)
        }

        expiryDateItemEditET.setOnClickListener {
            var year: Int
            var month: Int
            var day: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                LocalDate.now().let { date ->
                    day = date.dayOfMonth
                    month = date.monthValue
                    year = date.year
                }
            else {
                Calendar.getInstance().let { date ->
                    day = date.get(Calendar.DAY_OF_MONTH)
                    month = date.get(Calendar.MONTH)
                    year = date.get(Calendar.YEAR)
                }
            }
            DatePickerDialog(
                it.context,
                DatePickerDialog.OnDateSetListener { _, mYear, mMonth, mDay ->
                    expiryDateItemEditET.setText(
                        String.format(
                            "%02d-%02d-%04d",
                            mDay,
                            mMonth + 1,
                            mYear
                        )
                    )
                }, year, month - 1, day
            ).apply {
                datePicker.minDate = System.currentTimeMillis() - 1
            }.show()
        }

        setAfterTextChangedListener()
        setAddImageFab()
        locationMapItemEditMV.getMapAsync(this)

        geocoder = Geocoder(context, Locale.getDefault())

        /*searchLocationItemEditBTN.setOnClickListener {
            if (!searchLocationItemEditET.text.isNullOrEmpty()) {
                locationMapItemEditMV.getMapAsync { googleMap ->
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
                                            searchLocationItemEditET.text.toString()
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

        searchLocationItemEditBTN.setOnClickListener {
            if (!searchLocationItemEditET.text.isNullOrBlank()) {
                MainScope().launch {
                    try {
                        val result = withContext(Dispatchers.Default) { getCoordinates() }
                        if (result != null) {
                            latLng = LatLng(result.latitude, result.longitude)
                            locationMapItemEditMV.getMapAsync(this@ItemEditFragment)
                        } else {
                            Snackbar.make(
                                requireView(),
                                getString(
                                    R.string.no_location,
                                    searchLocationItemEditET.text.toString()
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
    }

    /*  private suspend fun getCoordinates(): MutableList<Address> {
          return withContext(Dispatchers.IO) {
              geocoder.getFromLocationName(searchLocationItemEditET.text.toString(), 1)
          }
      } */

    private suspend fun getCoordinates(): Address? {
        return withContext(Dispatchers.IO) {
            geocoder.getFromLocationName(searchLocationItemEditET.text.toString(), 1).firstOrNull()
        }
    }

    override fun onStart() {
        super.onStart()
        locationMapItemEditMV.onStart()
    }

    override fun onResume() {
        super.onResume()
        locationMapItemEditMV.onResume()
    }

    override fun onPause() {
        super.onPause()
        locationMapItemEditMV.onPause()
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
        locationMapItemEditMV.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationMapItemEditMV.onDestroy()
    }

    override fun onLowMemory() {
        locationMapItemEditMV.onLowMemory()
        super.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (view != null) {
            locationMapItemEditMV.onSaveInstanceState(outState)
        }
        outState.putParcelable(Constants.BUNDLE_PHOTO_URI, takePhotoUri)
        outState.putBoolean(Constants.BUNDLE_IMAGE_MOD, imageModified)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.REQUEST_CODE_IMAGE_CAPTURE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val tmpItem = itemEditVM.getItem().value ?: Item()
                        tmpItem.apply {
                            imageRotation = getSensorOrientation()
                            image = takePhotoUri.toString()
                            activity?.contentResolver?.openInputStream(Uri.parse(image))?.let {
                                ExifInterface(it).setAttribute(
                                    ExifInterface.TAG_ORIENTATION,
                                    0.toString()
                                )
                            }
                        }
                        itemEditVM.setItem(tmpItem)
                        imageModified = true

                    }
                }
            }
            Constants.REQUEST_CODE_OPEN_GALLERY -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val tmpItem = itemEditVM.getItem().value ?: Item()
                        tmpItem.apply {
                            imageRotation = 0
                            image = data?.data?.toString() ?: ""
                        }
                        itemEditVM.setItem(tmpItem)
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
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_bar_save, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveEditProfileMI -> {
                saveItem()
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
        insertItemEditFab.setOnClickListener {
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
        titleItemEditET.doOnTextChanged { text, _, _, _ ->
            when {
                text.isNullOrBlank() -> titleItemEditTIL.error =
                    getString(R.string.emptyTitleError)
                else -> titleItemEditTIL.error = null
            }
        }
        descriptionItemEditET.doOnTextChanged { text, _, _, _ ->
            when {
                text.isNullOrBlank() -> descriptionItemEditTIL.error =
                    getString(R.string.emptyDescriptionError)
                else -> descriptionItemEditTIL.error = null
            }
        }
        expiryDateItemEditET.doOnTextChanged { text, _, _, _ ->
            when {
                text.isNullOrBlank() -> expiryDateItemEditTIL.error =
                    getString(R.string.emptyExpiryDateError)
                else -> expiryDateItemEditTIL.error = null
            }
        }
        priceItemEditET.doOnTextChanged { text, _, _, _ ->
            when {
                text.isNullOrBlank() -> priceItemEditTIL.error =
                    getString(R.string.emptyPriceError)
                else -> priceItemEditTIL.error = null
            }
        }
        categoryItemEditAT.doOnTextChanged { text, _, _, _ ->
            when {
                text.isNullOrBlank() -> categoryItemEditTIL.error =
                    getString(R.string.emptyCategoryError)
                else -> categoryItemEditTIL.error = null
            }
        }
    }

    private fun checkFields(): Boolean {
        if (titleItemEditET.text.isNullOrBlank())
            titleItemEditTIL.error = getString(R.string.emptyTitleError)
        if (descriptionItemEditET.text.isNullOrBlank())
            descriptionItemEditTIL.error = getString(R.string.emptyDescriptionError)
        if (expiryDateItemEditET.text.isNullOrBlank())
            expiryDateItemEditTIL.error = getString(R.string.emptyExpiryDateError)
        if (priceItemEditET.text.isNullOrBlank())
            priceItemEditTIL.error = getString(R.string.emptyPriceError)
        if (categoryItemEditAT.text.isNullOrBlank())
            categoryItemEditTIL.error = getString(R.string.emptyCategoryError)

        return (titleItemEditTIL.error.isNullOrEmpty() && priceItemEditTIL.error.isNullOrEmpty()
                && expiryDateItemEditTIL.error.isNullOrEmpty() && descriptionItemEditTIL.error.isNullOrEmpty()
                && categoryItemEditTIL.error.isNullOrEmpty() && priceItemEditTIL.error.isNullOrEmpty() && this::latLng.isInitialized)
    }

    private fun checkTakePictureReqs(): Boolean {
        return if (activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)!!) {
            true
        } else {
            Snackbar.make(requireView(), R.string.cameraErrorToast, Snackbar.LENGTH_LONG)
                .show()
            false
        }
    }

    private fun saveItem() {
        if (checkFields()) {
            val tmpItem = itemEditVM.getItem().value?.copy() ?: Item()

            tmpItem.apply {
                status = Constants.ITEM_AVAILABLE
                title = titleItemEditET.text.toString()
                subtitle = subtitleItemEditET.text.toString()
                price = priceItemEditET.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
                expiryDate = expiryDateItemEditET.text.toString()
                description = descriptionItemEditET.text.toString()
                categoryID = resources.getStringArray(R.array.categories)
                    .indexOf(categoryItemEditAT.text.toString())
                lat = latLng.latitude
                lng = latLng.longitude

                Log.d("kkk", "ho salvato lat " + lat + "ho salvato lng: " + lng)
            }
            if (imageModified) {
                tmpItem.let { item ->
                    activity?.contentResolver?.let { cntRes ->
                        val inputStream = cntRes.openInputStream(Uri.parse(item.image))
                        val outputFile = File(
                            activity?.externalCacheDir,
                            "item_${item.itemID}.jpg"
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
                        item.image = outputFile.toString()
                    }
                }
            }
            itemEditVM.storeItem(tmpItem, imageModified)

            if (itemID.isEmpty()) {
                view?.findNavController()?.navigate(
                    R.id.action_itemEditFragment_to_itemDetailsFragment,
                    bundleOf(
                        Constants.BUNDLE_ITEM_ID to itemEditVM.getItem().value?.itemID,
                        Constants.BUNDLE_ITEM_USER_ID to itemEditVM.getItem().value?.userID
                    )
                )
            } else {
                view?.findNavController()?.navigateUp()
            }

            Snackbar.make(requireView(), R.string.savedItemToast, Snackbar.LENGTH_LONG)
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
                "item_",
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

//    private fun getSensorOrientation(): Int {
//        return when {
//            (Build.MANUFACTURER.equals("samsung", ignoreCase = true)) -> 0
//            (Build.VERSION.SDK_INT > 26) -> 0
//            else -> {
//                val manager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
//                try {
//                    manager.let {
//                        val rearCamera = it.cameraIdList[0]
//                        it.getCameraCharacteristics(rearCamera)
//                            .get(CameraCharacteristics.SENSOR_ORIENTATION)
//                    } ?: 0
//                } catch (e: CameraAccessException) {
//                    0
//                }
//            }
//        }
//    }

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
                 val latLng = LatLng(Constants.NOT_A_POSITION, Constants.NOT_A_POSITION)
                 locationCoord = latLng
                 true
             }
         }
         childFragmentManager.beginTransaction().replace(R.id.locationMapItemEditMV, mapFragment)
             .commit()
     } */
}