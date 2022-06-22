package com.app.signme.view.enablePermission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.core.BaseActivity
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityLocationPermissionBinding
import com.app.signme.permission.PermissionHandler
import com.app.signme.permission.Permissions
import com.app.signme.view.settings.editprofile.EditProfileActivity
import com.app.signme.viewModel.LandingViewMode
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*


class PermissionEnableActivity : BaseActivity<LandingViewMode>() {

    var binding: ActivityLocationPermissionBinding? = null

    companion object {

        fun getStartIntent(mContext: Context): Intent {
            return Intent(mContext, PermissionEnableActivity::class.java).apply {

            }
        }
    }

    //Location parameters
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    var lastKnownLocation: Location? = null
    private lateinit var locationCallback: LocationCallback
    var locationPermissionGranted: Boolean? = null

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_permission)
        binding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {

        binding!!.rippleBackground.startRippleAnimation()
        setFireBaseAnalyticsData(
            "id-locationPermissionScreen",
            "view_locationPermissionScreen",
            "view_locationPermissionScreen"
        )

        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        initListeners()

    }

    private fun initListeners() {
        binding?.let {
            with(it) {

                btnAllow.setOnClickListener {
                    setFireBaseAnalyticsData(
                        "id-allowLocationClick",
                        "click_allowLocationClick",
                        "click_allowLocationClick"
                    )
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Allow Button Click")
                    getDeviceLocation()
                }
            }
        }
    }


    // GET LOCATION
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted != null && locationPermissionGranted!!) {
                val locationRequest = LocationRequest.create()
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                locationRequest.interval = 5 * 1000
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {

                        if (locationResult == null) {
                            hideProgressDialog()
                            logger.dumpCustomEvent("Location", "Location not found.")
                            Toast.makeText(
                                this@PermissionEnableActivity,
                                "Unable to fetch location..",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }

                        if (locationResult.locations.isEmpty()) {
                            hideProgressDialog()
                            logger.dumpCustomEvent("Location", "Location not found.")
                            Toast.makeText(
                                this@PermissionEnableActivity,
                                "Unable to fetch location..",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        } else {
                            hideProgressDialog()
                            lastKnownLocation = locationResult.locations[0]
                            mFusedLocationProviderClient?.removeLocationUpdates(this)
                            if(AppineersApplication.sharedPreference.configDetails!!.isUpdated.equals("0"))
                            {
                                startActivity(EditProfileActivity.getStartIntent(this@PermissionEnableActivity,IConstants.ADD))
                                finish()
                            }
                            else
                            {
                                navigateToHomeScreen()
                            }
                        }
                    }
                }

                mFusedLocationProviderClient?.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } else {
                hideProgressDialog()
                if (locationPermissionGranted == null || (locationPermissionGranted == false)) {
                    checkLocationPermission()
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    private fun checkLocationPermission() {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val rationale =
            "Please provide location permission, So that you can find out profile near to your location."
        val options = Permissions.Options()
            .setRationaleDialogTitle(getString(R.string.app_name))
            .setSettingsDialogTitle(getString(R.string.app_name))
            .setSettingsDialogMessage(getString(R.string.alert_we_need_location_permission_allow_from_settings))

        Permissions.check(
            this/*context*/,
            permissions,
            rationale,
            options,
            object : PermissionHandler() {
                override fun onGranted() {
                    // do your task.
                    locationPermissionGranted = true
                    showEnableLocationSetting()
                }

                override fun onDenied(
                    context: Context?,
                    deniedPermissions: java.util.ArrayList<String?>?
                ) {
                    // permission denied, block the feature.
                    hideProgressDialog()
                    if (locationPermissionGranted != false) {
                        locationPermissionGranted = false
                        // openPlacePicker()
                    }

                }
            })
    }

    /**
     *  CHECK GPS
     * Show GPS enable dialog
     * */
    private fun showEnableLocationSetting() {
        this.let {
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

            val task = LocationServices.getSettingsClient(it)
                .checkLocationSettings(builder.build())

            task.addOnSuccessListener { response ->
                val states = response.locationSettingsStates

                when {
                    states.isLocationPresent -> {
                        getDeviceLocation()
                    }
                }
            }
            task.addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        // Handle result in onActivityResult()
                        startIntentSenderForResult(
                            e.resolution.intentSender,
                            IConstants.REQUEST_CODE_LOCATION,
                            null,
                            0,
                            0,
                            0,
                            null
                        );
                    } catch (sendEx: IntentSender.SendIntentException) {

                    }
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                IConstants.REQUEST_CODE_LOCATION -> {
                    getDeviceLocation()
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED && requestCode == IConstants.REQUEST_CODE_LOCATION) {
            hideProgressDialog()
            checkLocationPermission()
        }
    }

}