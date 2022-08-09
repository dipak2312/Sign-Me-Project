package com.app.signme.view


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.core.BaseActivity
import com.app.signme.application.AppineersApplication
import com.app.signme.application.AppineersApplication.Companion.sharedPreference
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.dialog.AppUpdateDialog
import com.app.signme.databinding.ActivitySplashBinding
import com.app.signme.dataclasses.VersionConfigResponse
import com.app.signme.dataclasses.response.StaticPage
import com.app.signme.core.AppConfig
import com.app.signme.view.home.HomeActivity
import com.app.signme.view.onboarding.OnBoardingActivity
import com.app.signme.viewModel.SplashViewModel
import com.hb.logger.Logger
import java.lang.StringBuilder
import java.util.*
import androidx.lifecycle.Observer
import com.app.signme.BuildConfig
import com.app.signme.commonUtils.utility.extension.showSnackBar
import com.app.signme.R
import com.app.signme.view.enablePermission.PermissionEnableActivity
import com.app.signme.view.settings.editprofile.EditProfileActivity
import com.app.signme.view.settings.staticpages.StaticPagesMultipleActivity
import org.json.JSONObject


@Suppress("DEPRECATION")
class SplashActivity : BaseActivity<SplashViewModel>() {
    var dataBinding: ActivitySplashBinding? = null
    var launchByNotification = false
    //To check need to show update dialog or not
    var isShown = false
    var isTaken = false
    private var updateVersion: VersionConfigResponse? = null


    companion object {
        const val TAG = "SplashActivity"
        private const val NOTIFICATION_PERMISSION_CODE = 100
    }


    override fun setDataBindingLayout() {
        dataBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_splash)
        dataBinding?.viewModel = viewModel
        dataBinding?.lifecycleOwner = this


    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun setupView(savedInstanceState: Bundle?) {
        addObservers()
        dataBinding?.progressSplash?.visibility = View.VISIBLE
        //checkNotificationPermission()
        /*  val test = ("0")
          println(test[1])*/
        //  throw RuntimeException("Test Crash") // Force a crash
        Log.i(TAG, "setupView: " + intent.extras)
        Log.i(TAG, "setupView: " + intent.extras)
        if (intent != null && intent.extras != null  && intent.hasExtra(
                IConstants.OTHERS
            )
        ){
            Log.i(TAG, "setupView: " + intent.extras)
            val data = intent.extras!!.getString(IConstants.OTHERS)
            val jsonObject = JSONObject(data)
            Log.i(TAG, "setupView: " + data)
            Log.i(TAG, "setupView: " + jsonObject)
            logger.dumpCustomEvent(
                IConstants.EVENT_PUT_DATA,
                "PUSH_NOTIFICATION_PAYLOAD " + data
            )

            /*
                Log.i(TAG, "setupView: " + type)
                // handleNotificationData(type, businessId)*/
            launchByNotification = true
            startLaunchActivity(data)
        }else {
            //startLaunchActivity(null)
            viewModel.callGetConfigParameters()
        }
    }

    private fun startLaunchActivity(payload: String?) {
        Handler().postDelayed({
            if (launchByNotification) {
                Log.i(TAG, "startLaunchActivity: Payload " + payload)
                startActivity(
                    HomeActivity.getStartIntent(this, payload)
                )
            } else {
                startActivity(Intent(this@SplashActivity, getLaunchClass()))
            }
            finish()
        }, 1000)
    }

/*    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkNotificationPermission() {
        // Function to check and request permission.
        if (ContextCompat.checkSelfPermission(
                this@SplashActivity,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY
            ) == PackageManager.PERMISSION_DENIED
        ) {

            // Requesting the permission
            ActivityCompat.requestPermissions(
                this@SplashActivity,
                arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY),
                NOTIFICATION_PERMISSION_CODE
            )
        } else {
            Toast.makeText(this@SplashActivity, "Permission already granted", Toast.LENGTH_SHORT)
                .show()
        }

    }*/

    /**
     *
     * @return Use default configuration when error occurs
     */
    private fun handleDefaultConfigDetails() {
        var configDefaultDetails: VersionConfigResponse? = sharedPreference.configDetails
        dataBinding?.progressSplash?.visibility = View.GONE
        updateUserSubscriptionData(configDefaultDetails)
        (application as AppineersApplication).isAdRemoved.value = configDefaultDetails?.isAdsFree()
        sharedPreference.isSubscription=configDefaultDetails!!.isSubscriptionTaken()
        sharedPreference.isAdRemoved = configDefaultDetails!!.isAdsFree()
        updateAdConfiguration(configDefaultDetails)
        val config = configDefaultDetails


        checkUpdateAndTC(config)
//        if (config.address.equals("")
//            && !sharedPreference.isDoNotShowMeAgainClicked
//        ) {
//            startActivity(Intent(this@SplashActivity, AddAddressActivity::class.java))
//            finish()
//        } else {
//
//        }


        if (!BuildConfig.DEBUG) {
            (application as AppineersApplication).isLogStatusUpdated.value =
                (config.logStatusUpdated.toLowerCase(
                    Locale.getDefault()
                )) == IConstants.ACTIVE_LOG_STATUS
            sharedPreference.logStatusUpdated =
                config.logStatusUpdated.toLowerCase(Locale.getDefault())
            if (sharedPreference.logStatusUpdated.equals(IConstants.INACTIVE_LOG_STATUS, true)) {
                Logger.clearAllLogs()
                Logger.disableLogger()
            } else if (sharedPreference.logStatusUpdated.equals(
                    IConstants.ACTIVE_LOG_STATUS,
                    true
                )
            ) {
                Logger.enableLogger()
            }
        } else {
            Logger.enableLogger()
        }


        val userDetails = sharedPreference.userDetail
        userDetails?.subscription = configDefaultDetails.subscription
        sharedPreference.userDetail = userDetails
        sharedPreference.isAddress = configDefaultDetails.address
        sharedPreference.isFirstLogin = configDefaultDetails.isFirstLogin
        sharedPreference.applicationTermsCondition =
            configDefaultDetails.termsConditionsVersionApplication
        sharedPreference.applicationPrivacyPolicy =
            configDefaultDetails.privacyPolicyVersionApplication
        sharedPreference.isAddressMandatory =
            configDefaultDetails.mandatoryArray!![0].isAddressMandatory

    }

    /**
     *
     * @return if update comes for Privacy Policy & Terms & Conditions & version
     */
    private fun checkUpdateAndTC(configDetails: VersionConfigResponse) {
        if (configDetails.termsConditionsVersion.equals("")
            && configDetails.privacyPolicyVersion.equals(
                ""
            )
        ) {
            goNext()
        } else if (!configDetails.termsConditionsVersion.equals(configDetails.termsConditionsVersionApplication)
            && !configDetails.privacyPolicyVersion.equals(configDetails.privacyPolicyVersionApplication)
            && configDetails.shouldShowVersionDialog(
                this@SplashActivity
            )
        ) {
            isShown = true
            updateVersion = configDetails
            callAgreePrivacyPolicyTermsConditions(updateVersion)


        } else if (!configDetails.termsConditionsVersion.equals(configDetails.termsConditionsVersionApplication)
            && !configDetails.privacyPolicyVersion.equals(configDetails.privacyPolicyVersionApplication)
        ) {
            Log.i(TAG, "checkUpdateAndTC: " + configDetails.termsConditionsVersion)
            callAgreePrivacyPolicyTermsConditions(configDetails)

        } else if (!configDetails.termsConditionsVersion.equals(configDetails.termsConditionsVersionApplication)
            || !configDetails.privacyPolicyVersion.equals(configDetails.privacyPolicyVersionApplication)
        ) {
            callAgreePrivacyPolicyTermsConditions(configDetails)
            Log.i(TAG, "checkUpdateAndTC: " + configDetails.termsConditionsVersion)

        } else if (configDetails.shouldShowVersionDialog(this@SplashActivity)) {
            showNewVersionAvailableDialog(configDetails)
        } else {
            goNext()
        }
    }

    /**
     *
     * @return if update comes for Privacy Policy & Terms & Conditions
     */
    private fun callAgreePrivacyPolicyTermsConditions(
        version: VersionConfigResponse?
    ) {
        var pageCodeList: ArrayList<StaticPage> = ArrayList()
        if (!version!!.termsConditionsVersion.equals(version.termsConditionsVersionApplication)) {
            isTaken = true
            pageCodeList.add(
                StaticPage(
                    pageCode = IConstants.STATIC_PAGE_TERMS_CONDITION,
                    forceUpdate = true
                )
            )
        }

        if (!version.privacyPolicyVersion.equals(version.privacyPolicyVersionApplication)) {
            isTaken = true
            pageCodeList.add(
                StaticPage(
                    pageCode = IConstants.STATIC_PAGE_PRIVACY_POLICY,
                    forceUpdate = true
                )
            )
        }

        if (!pageCodeList.isNullOrEmpty()) {
            isTaken = true
            startActivityForResult(
                StaticPagesMultipleActivity.getStartIntent(
                    mContext = this@SplashActivity,
                    pageCodeList = pageCodeList
                ), IConstants.REQUEST_STATIC_PAGE
            )

        }


    }

    fun locationEnableOrNot():Boolean
    {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return hasPermission && gpsStatus

    }
    /**
     * Choose activity to open
     *
     * @return if user already login, then open home activity, else open login activity
     */
    private fun getLaunchClass(): Class<*> {

        return if (sharedPreference.isLogin) {
            // Return Home activity
            val locationStatus=locationEnableOrNot()
            if(locationStatus)
            {
                Logger.setUserInfo(sharedPreference.userDetail?.email ?: "")
                HomeActivity::class.java
            }
            else{
                PermissionEnableActivity::class.java
            }

        } else {

            //Return Login Activity
            if (sharedPreference.isOnBoardingShown) {
                (application as AppineersApplication).getLoginActivity()
            } else {
                OnBoardingActivity::class.java
            }
        }
    }

    private fun addObservers() {
        dataBinding?.progressSplash?.visibility = View.GONE
        viewModel.configParamsPhoneLiveData.observe(this, Observer { it ->
            if (it.settings?.isSuccess == true) {
                if (!it.data.isNullOrEmpty()) {
                    sharedPreference.configDetails = it.data!!.get(0)
                    sharedPreference.likeCount= it.data!!.get(0).likeUserCount!!
                    sharedPreference.superLikeCount= it.data!!.get(0).superLikeUserCount!!

                    handleDefaultConfigDetails()
                }
            } else if (sharedPreference.configDetails != null) {
                handleDefaultConfigDetails()
            } else {
                //Handle config failed message
                if (it.equals("") || it == null) {
                    if (sharedPreference.configDetails != null) {
                        handleDefaultConfigDetails()
                    }

                    showMessage(
                        getString(R.string.str_config_message),
                        IConstants.SNAKBAR_TYPE_ERROR
                    )
                    goNext()
                }
            }
        })
        dataBinding?.progressSplash?.visibility = View.GONE
        viewModel.statusCodeLiveData.observe(this, Observer { serverError ->
            dataBinding?.progressSplash?.visibility = View.GONE
            // showMessage(getString(R.string.str_config_message), IConstants.SNAKBAR_TYPE_ERROR)
            hideProgressDialog()
            serverError.message.showSnackBar(this@SplashActivity)
            if (sharedPreference.configDetails != null) {
                handleDefaultConfigDetails()
            }
        })

        // goNext()


    }


    private fun showNewVersionAvailableDialog(version: VersionConfigResponse?) {
        AppUpdateDialog(this, version).show(supportFragmentManager, "update dialog")

    }

    /** Update AdConfiguration based on config api response*/
    private fun updateAdConfiguration(data: VersionConfigResponse?) {
        if (data != null) {
            sharedPreference.projectDebugLevel = data.projectDebugLevel
            sharedPreference.androidBannerId = data.androidBannerId
            sharedPreference.androidInterstitialId = data.androidInterstitialId
            sharedPreference.androidNativeId = data.androidNativeId
            sharedPreference.androidRewardedId = data.androidRewardedId
            sharedPreference.androidMoPubBannerId = data.androidMoPubBannerId
            sharedPreference.androidMopubInterstitialId = data.androidMopubInterstitialId

            if (AppConfig.AdProvider_ADMob) {
                (application as AppineersApplication).initGoogleAdMobSDK()

            } else if (AppConfig.AdProvider_MoPub) {
                (application as AppineersApplication).initMoPubSDK(data.isAppInDevelopment())
            }

            val adConfig = StringBuilder()
            adConfig.append("projectDebugLevel= " + data.projectDebugLevel)
            adConfig.append(", androidBannerId= " + data.androidBannerId)
            adConfig.append(", androidInterstitialId= " + data.androidInterstitialId)
            adConfig.append(", androidNativeId= " + data.androidNativeId)
            adConfig.append(", androidRewardedId= " + data.androidRewardedId)
            adConfig.append(", androidMoPubBannerId= " + data.androidMoPubBannerId)
            adConfig.append(", androidMopubInterstitialId= " + data.androidMopubInterstitialId)
            Log.i(HomeActivity.TAG, "adConfig Info: " + adConfig.toString())
            logger.dumpCustomEvent("adConfig Info", adConfig.toString())
        }
    }

    /**
     * Update user subscription data
     */
    private fun updateUserSubscriptionData(data: VersionConfigResponse?) {
        if (data != null) {
            val userDetails = sharedPreference.userDetail
            if (userDetails != null) {
                if (data.subscription != null) {
                    if (data.subscription?.size!! >= 0) {
                        userDetails.subscription = data.subscription
                        sharedPreference.userDetail = userDetails
                    }
                }

            }
        }
    }

    private fun goNext() {
        if (sharedPreference.isSkip) {
            startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
            finish()
        } else {
            val locationStatus=locationEnableOrNot()
            if(locationStatus && sharedPreference.isLogin)
            {
                if(sharedPreference.configDetails!!.isUpdated.equals("0"))
                {
                 startActivity(EditProfileActivity.getStartIntent(this@SplashActivity,IConstants.ADD))
                    finish()
                }
                else
                {
                    navigateToHomeScreen()
                }
            }
            else{
                Handler().postDelayed({
                    startActivity(Intent(this@SplashActivity, getLaunchClass()))
                    finish()
                }, 1000)
            }
        }
    }

    /**
     * To handle static pages
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            IConstants.REQUEST_STATIC_PAGE -> {
                isTaken = true
                if (isShown) {
                    showNewVersionAvailableDialog(updateVersion)
                } else {
                    goNext()
                }

            }
            IConstants.APP_UPDATE_REQUEST_CODE -> {
                goNext()
                Toast.makeText(this@SplashActivity, "Optional", Toast.LENGTH_LONG).show()
            }
            IConstants.APP_FORCE_UPDATE_REQUEST_CODE -> {
                recreate()
                Toast.makeText(this@SplashActivity, "Force", Toast.LENGTH_LONG).show()
            }

        }


    }

/*

    */
    /**This function is called when the user accepts or decline the permission.
    Request Code is used to check which permission called this function.
    This request code is provided when the user is prompt for permission.
     **//*

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this@SplashActivity,
                    "Notification Permission Granted",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@SplashActivity,
                    "Notification Permission Denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }
*/


}