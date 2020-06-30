package it.polito.mad.runtimeterrormad.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.navGraphViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import it.polito.mad.runtimeterrormad.R
import it.polito.mad.runtimeterrormad.viewmodels.ItemDetailsViewModel
import it.polito.mad.runtimeterrormad.viewmodels.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_map.*


class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var latLngSrc: LatLng
    private lateinit var latLngDest: LatLng
    private val profileVM: ProfileViewModel by activityViewModels()
    private val itemDetailsVM: ItemDetailsViewModel by navGraphViewModels(R.id.item_details_graph)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationMapFragmentMV.onCreate(savedInstanceState)

        profileVM.getProfile().value!!.let {
            latLngSrc = LatLng(it.lat, it.lng)
        }
        itemDetailsVM.getItem().value!!.let {
            latLngDest = LatLng(it.lat, it.lng)
        }

        locationMapFragmentMV.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        locationMapFragmentMV.onStart()
    }

    override fun onResume() {
        super.onResume()
        locationMapFragmentMV.onResume()
    }

    override fun onPause() {
        super.onPause()
        locationMapFragmentMV.onPause()
    }

    override fun onStop() {
        super.onStop()
        locationMapFragmentMV.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationMapFragmentMV.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        locationMapFragmentMV.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (view != null) {
            locationMapFragmentMV.onSaveInstanceState(outState)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.uiSettings.apply {
            isCompassEnabled = true
            isMapToolbarEnabled = true
            isRotateGesturesEnabled = true
            isScrollGesturesEnabled = true
            isTiltGesturesEnabled = false
            isZoomControlsEnabled = true
        }
        if (this::latLngSrc.isInitialized && this::latLngDest.isInitialized) {
            googleMap.clear()
            val bounds = LatLngBounds.builder().include(latLngSrc).include(latLngDest).build()
            googleMap.setLatLngBoundsForCameraTarget(bounds)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 128))
            googleMap.addMarker(MarkerOptions().apply {
                position(latLngSrc)
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            })
            googleMap.addMarker(MarkerOptions().apply {
                position(latLngDest)
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            })
            googleMap.addPolyline(PolylineOptions().apply {
                add(latLngSrc, latLngDest)
                width(3F)
                geodesic(true)
            })
        }
    }

}