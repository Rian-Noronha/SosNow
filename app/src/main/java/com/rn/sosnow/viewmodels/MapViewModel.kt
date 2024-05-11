package com.rn.sosnow.viewmodels
import android.annotation.SuppressLint
import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.rn.sosnow.map.RouteHttp
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
class MapViewModel(app: Application) : AndroidViewModel(app), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var googleApiClient: GoogleApiClient? = null
    private val locationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(getContext())
    }

    private val connectionStatus = MutableLiveData<GoogleApiConnectionStatus>()
    private val currentLocationError = MutableLiveData<LocationError>()
    private val addresses = MutableLiveData<List<Address>?>()
    private val loading = MutableLiveData<Boolean>()
    private val loadingRoute = MutableLiveData<Boolean>()
    private val currentLocation = MutableLiveData<LatLng>()

    private val mapState = MutableLiveData<MapState>().apply {
        value = MapState()
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

    fun getConnectionStatus(): LiveData<GoogleApiConnectionStatus> {
        return connectionStatus
    }

    fun getCurrentLocationError(): LiveData<LocationError> {
        return currentLocationError
    }

    fun getMapState(): LiveData<MapState> {
        return mapState
    }

    fun getAddresses(): LiveData<List<Address>?> {
        return addresses
    }

    fun isLoading(): LiveData<Boolean> {
        return loading
    }

    fun isLoadingRoute(): LiveData<Boolean> {
        return loadingRoute
    }

    fun getCurrentLocation(): LiveData<LatLng> {
        return currentLocation
    }

    fun connectGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                    override fun onConnected(args: Bundle?) {
                        connectionStatus.value = GoogleApiConnectionStatus(true)
                    }
                    override fun onConnectionSuspended(i: Int) {
                        connectionStatus.value = GoogleApiConnectionStatus(false)
                        googleApiClient?.connect()
                    }
                })
                .addOnConnectionFailedListener { connectionResult ->
                    connectionStatus.value =
                        GoogleApiConnectionStatus(false, connectionResult)
                }
                .build()
        }
        googleApiClient?.connect()
    }

    fun disconnectGoogleApiClient() {
        connectionStatus.value = GoogleApiConnectionStatus(false)
        if (googleApiClient != null && googleApiClient?.isConnected == true) {
            googleApiClient?.disconnect()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun loadLastLocation(): Boolean = suspendCoroutine { continuation ->
        fun updateOriginByLocation(location: Location) {
            val latLng = LatLng(location.latitude, location.longitude)
            mapState.value = mapState.value?.copy(origin = latLng)
            continuation.resume(true)
        }
        fun waitForLocation() {
            val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5 * 1000)
                .setFastestInterval(1 * 1000)
            locationClient.requestLocationUpdates(locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(p0: LocationResult) {
                        super.onLocationResult(p0)
                        locationClient.removeLocationUpdates(this)
                        val location = p0?.lastLocation
                        if (location != null) {
                            updateOriginByLocation(location)
                        } else {
                            continuation.resume(false)
                        }
                    }
                }, null)
        }
        locationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location == null) {
                    waitForLocation()
                } else {
                    updateOriginByLocation(location)
                }
            }
            .addOnFailureListener {
                waitForLocation()
            }
            .addOnCanceledListener {
                continuation.resume(false)
            }
    }

    private suspend fun checkGpsStatus(): Boolean = suspendCoroutine { continuation ->
        val request = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .setAlwaysShow(true)
            .addLocationRequest(request)
        LocationServices.getSettingsClient(getContext()).checkLocationSettings(locationSettingsRequest.build())
            .addOnCompleteListener { task ->
                try {
                    task.getResult(ApiException::class.java)
                    continuation.resume(true)
                } catch (exception: ApiException) {
                    continuation.resumeWithException(exception)
                }
            }
            .addOnCanceledListener {
                continuation.resume(false)
            }
    }

    fun requestLocation() {
        launch {
            currentLocationError.value = try {
                checkGpsStatus()
                val success = withTimeout(2000) { loadLastLocation() }
                if (success) {
                    startLocationUpdates()
                    null
                } else {
                    LocationError.ErrorLocationUnavailable
                }
            } catch (timeout: TimeoutCancellationException) {
                LocationError.ErrorLocationUnavailable
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        LocationError.GpsDisabled(exception as ResolvableApiException)
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                        LocationError.GpsSettingUnavailable
                    else ->
                        LocationError.ErrorLocationUnavailable
                }
            }
        }
    }

    fun searchAddress(s: String) {
        launch {
            loading.value = true
            val geoCoder = Geocoder(getContext(), Locale.getDefault())
            addresses.value = withContext(Dispatchers.IO) {
                geoCoder.getFromLocationName(s, 1)
            }
            loading.value = false
        }
    }

    fun clearSearchAddressResult() {
        addresses.value = null
    }

    fun setDestination(latLng: LatLng) {
        addresses.value = null
        mapState.value = mapState.value?.copy(destination = latLng)
        loadRoute()
    }

    private fun loadRoute() {
        if (mapState.value != null) {
            val orig = mapState.value?.origin
            val dest = mapState.value?.destination
            if (orig != null && dest != null) {
                launch {
                    loadingRoute.value = true
                    val route = withContext(Dispatchers.IO) {
                        RouteHttp.searchRoute(orig, dest)
                    }
                    mapState.value = mapState.value?.copy(route = route)
                    loadingRoute.value = false
                }
            }
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            val location = p0?.lastLocation
            if (location != null) {
                currentLocation.value = LatLng(location.latitude, location.longitude)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(5 * 1000)
            .setFastestInterval(1 * 1000)
        locationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    fun stopLocationUpdates() {
        LocationServices.getFusedLocationProviderClient(getContext())
            .removeLocationUpdates(locationCallback)
    }


    private fun getContext() = getApplication<Application>()

    // Data classes -----------
    data class MapState(
        val origin: LatLng? = null,
        val destination: LatLng? = null,
        val route: List<LatLng>? = null,
    )
    data class GoogleApiConnectionStatus(
        val success: Boolean,
        val connectionResult: ConnectionResult? = null
    )
    sealed class LocationError {
        object ErrorLocationUnavailable : LocationError()
        data class GpsDisabled(val exception: ResolvableApiException): LocationError()
        object GpsSettingUnavailable: LocationError()
    }
}