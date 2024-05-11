package com.rn.sosnow.common
import android.graphics.Color
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.rn.sosnow.R
import com.rn.sosnow.viewmodels.MapViewModel

class AppMapFragment : SupportMapFragment() {
    private val viewModel: MapViewModel by lazy {
        ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
    }
    private var googleMap: GoogleMap? = null
    private var markerCurrentLocation: Marker? = null

    override fun getMapAsync(callback: OnMapReadyCallback) {
        super.getMapAsync {
            googleMap = it
            setupMap()
            callback?.onMapReady(googleMap!!)
        }
    }
    private fun setupMap() {
        googleMap?.run {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            uiSettings.isMapToolbarEnabled = false
            uiSettings.isZoomControlsEnabled = true
        }
        viewModel.getMapState()
            .observe(this, Observer { mapState ->
                if (mapState != null) {
                    updateMap(mapState)
                }
            })
        viewModel.getCurrentLocation()
            .observe(this, Observer { currentLocation ->
                if (currentLocation != null) {
                    if (markerCurrentLocation == null) {
                        val icon = BitmapDescriptorFactory
                            .fromResource(R.drawable.blue_marker)
                        markerCurrentLocation = googleMap?.addMarker(
                            MarkerOptions()
                                .title(getString(R.string.map_current_location))
                                .icon(icon)
                                .position(currentLocation)
                        )
                    }
                    markerCurrentLocation?.position = currentLocation
                }
            })
    }
    private fun updateMap(mapState: MapViewModel.MapState) {
        googleMap?.run {
            clear()
            markerCurrentLocation = null
            val area = LatLngBounds.Builder()
            val origin = mapState.origin
            if (origin != null) {
                addMarker(MarkerOptions()
                    .position(origin)
                    .title(getString(R.string.map_marker_origin)))
                area.include(origin)
            }
            val destination = mapState.destination
            if (destination != null) {
                addMarker(MarkerOptions()
                    .position(destination)
                    .title(getString(R.string.map_marker_destination))
                )
                area.include(destination)
            }
            val route = mapState.route
            if (!route.isNullOrEmpty()) {
                val polylineOptions = PolylineOptions()
                    .addAll(route)
                    .width(5f)
                    .color(Color.RED)
                    .visible(true)
                addPolyline(polylineOptions)
                route.forEach { area.include(it) }
            }
            if (origin != null) {
                if (destination != null) {
                    animateCamera(CameraUpdateFactory.newLatLngBounds(area.build(), 50))
                } else {
                    animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 17f))
                }
            }
        }
    }

}