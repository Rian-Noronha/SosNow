package com.rn.sosnow.common
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.rn.sosnow.R
import com.rn.sosnow.databinding.ActivityInformationBinding
import com.rn.sosnow.model.Contact
import com.rn.sosnow.view.AddressListFragment
import com.rn.sosnow.viewmodels.MapViewModel


class InformationActivity : AppCompatActivity() {
    private val viewModel: MapViewModel by lazy {
        ViewModelProvider(this).get(MapViewModel::class.java)
    }
    private val fragment: AppMapFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.fragmentMap) as AppMapFragment
    }
    private var isGpsDialogOpened: Boolean = false

    private lateinit var binding: ActivityInformationBinding

    private lateinit var contact: Contact

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        contact = (intent.getSerializableExtra("CONTACT") as? Contact)!!
        isGpsDialogOpened = savedInstanceState?.getBoolean(EXTRA_GPS_DIALOG) ?: false

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = contact.name

        configureInformationLayout()
        binding.fabCall.setOnClickListener{
            call()
        }

    }
    override fun onStart() {
        super.onStart()
        fragment.getMapAsync {
            initUi()
            viewModel.connectGoogleApiClient()
        }
    }
    override fun onStop() {
        super.onStop()
        viewModel.disconnectGoogleApiClient()
        viewModel.stopLocationUpdates()
    }

    private fun call(){
        if (contact.number.isNotEmpty()) {
            val uri = Uri.parse("tel:${contact.number}")
            val intent = Intent(Intent.ACTION_DIAL, uri)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Número de telefone inválido.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configureInformationLayout(){
        binding.txtContactName.text = contact.name
        binding.txtContactAddress.text = contact.address
        binding.txtContactDescription.text = contact.description
        binding.txtContactNumber.text = contact.number
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState?.putBoolean(EXTRA_GPS_DIALOG, isGpsDialogOpened)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ERROR_PLAY_SERVICES &&
            resultCode == Activity.RESULT_OK) {
            viewModel.connectGoogleApiClient()
        } else if (requestCode == REQUEST_CHECK_GPS) {
            isGpsDialogOpened = false
            if (resultCode == RESULT_OK) {
                loadLastLocation()
            } else {
                Toast.makeText(this, R.string.map_error_gps_disabled, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS && permissions.isNotEmpty()) {
            if (permissions.firstOrNull() == Manifest.permission.ACCESS_FINE_LOCATION &&
                grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                loadLastLocation()
            } else {
                showError(R.string.map_error_permissions)
                finish()
            }
        }
    }
    private fun initUi() {
        viewModel.getConnectionStatus()
            .observe(this, Observer { status ->
                if (status != null) {
                    if (status.success) {
                        loadLastLocation()
                    } else {
                        status.connectionResult?.let {
                            handleConnectionError(it)
                        }
                    }
                }
            })
        viewModel.getCurrentLocationError()
            .observe(this, Observer { error ->
                handleLocationError(error)
            })
        viewModel.isLoading()
            .observe(this, Observer { value ->
                if (value != null) {
                    binding.btnTraceRoute.isEnabled = !value
                    if (value) {
                        showProgress(getString(R.string.map_msg_search_address))
                    } else {
                        hidePogress()
                    }
                }
            })
        viewModel.getAddresses()
            .observe(this, Observer { addresses ->
                if (addresses != null) {
                    showAddressListDialog(addresses)
                }
            })
        viewModel.isLoadingRoute()
            .observe(this, Observer { value ->
                if (value != null) {
                    binding.btnTraceRoute.isEnabled = !value
                    if (value) {
                        showProgress(getString(R.string.map_msg_search_route))
                    } else {
                        hidePogress()
                    }
                }
            })

        binding.btnTraceRoute.setOnClickListener {
            searchAddress()
        }
    }
    private fun handleLocationError(error: MapViewModel.LocationError?) {
        if (error != null) {
            when (error) {
                is MapViewModel.LocationError.ErrorLocationUnavailable ->
                    showError(R.string.map_error_get_current_location)
                is MapViewModel.LocationError.GpsDisabled -> {
                    if (!isGpsDialogOpened) {
                        isGpsDialogOpened = true
                        error.exception.startResolutionForResult(
                            this, REQUEST_CHECK_GPS
                        )
                    }
                }
                is MapViewModel.LocationError.GpsSettingUnavailable ->
                    showError(R.string.map_error_gps_settings)
            }
        }
    }
    private fun loadLastLocation() {
        if (!hasPermission()) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS
            )
            return
        }
        viewModel.requestLocation()
    }
    private fun handleConnectionError(result: ConnectionResult) {
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(
                    this, REQUEST_ERROR_PLAY_SERVICES
                )
            } catch (e: IntentSender.SendIntentException) {
                e.printStackTrace()
            }
        } else {
            showPlayServicesErrorMessage(result.errorCode)
        }
    }
    private fun hasPermission(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        return ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == granted
    }
    private fun showError(@StringRes errorMessage: Int) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
    private fun showPlayServicesErrorMessage(errorCode: Int) {
        GoogleApiAvailability.getInstance()
            .getErrorDialog(this, errorCode, REQUEST_ERROR_PLAY_SERVICES)
            ?.show()
    }
    private fun searchAddress() {
        viewModel.searchAddress("${contact.destinationLatitude}  ${contact.destinationLongitude}")
    }
    private fun showProgress(message: String) {
        binding.layoutLoading.txtProgress.text = message
        binding.layoutLoading.llProgress.visibility = View.VISIBLE
    }
    private fun hidePogress() {
        binding.layoutLoading.llProgress.visibility = View.GONE
    }
    private fun showAddressListDialog(addresses: List<Address>) {
        AddressListFragment.newInstance(addresses).show(supportFragmentManager, null)
    }

    companion object {
        private const val REQUEST_ERROR_PLAY_SERVICES = 1
        private const val REQUEST_PERMISSIONS = 2
        private const val REQUEST_CHECK_GPS = 3
        private const val EXTRA_GPS_DIALOG = "gpsDialogIsOpen"
    }
}