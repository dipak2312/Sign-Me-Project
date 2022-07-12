package com.app.signme.application


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.multidex.MultiDexApplication
import com.app.signme.BuildConfig
import com.app.signme.api.network.InternetAvailabilityChecker
import com.app.signme.commonUtils.common.LoadingDialog
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.dagger.components.ApplicationComponent
import com.app.signme.dagger.components.DaggerApplicationComponent
import com.app.signme.dagger.modules.ApplicationModule
import com.app.signme.dataclasses.MediaUpload
import com.app.signme.db.AppPrefrrences
import com.app.signme.core.AppConfig
import com.app.signme.core.BaseActivity
import com.app.signme.core.BaseViewModel
import com.app.signme.dataclasses.RelationshipType
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.scheduler.ExportLogService
import com.app.signme.view.authentication.login.loginwithemailsocial.LoginWithEmailSocialActivity
import com.app.signme.scheduler.aws.cacheUtils.UploadSuccessCallback
import com.facebook.FacebookSdk
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.hb.logger.Logger
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.common.SdkInitializationListener
import com.mopub.common.logging.MoPubLog
import com.mopub.common.privacy.ConsentDialogListener
import com.mopub.common.privacy.PersonalInfoManager
import com.mopub.mobileads.MoPubErrorCode
import java.io.File
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList


class AppineersApplication : MultiDexApplication(), Application.ActivityLifecycleCallbacks {

    lateinit var applicationComponent: ApplicationComponent
    var isLogStatusUpdated = MutableLiveData<Boolean>()
    var isCurrentLocationUpdated = MutableLiveData<Boolean>()
    val isProfileUpdated = MutableLiveData<Boolean>()
    val isAdRemoved = MutableLiveData<Boolean>()
    val isLike=MutableLiveData<Boolean>()
    val isSuperLike=MutableLiveData<Boolean>()
    val isReject=MutableLiveData<Boolean>()
    var isSubscriptionTaken = MutableLiveData<Boolean>()
    var weakActivity: WeakReference<BaseActivity<BaseViewModel>>? = null
    val isMediaUploaded = MutableLiveData<MediaUpload?>()
    var relationshipStatus=ArrayList<RelationshipType>()
    val awsFileUploader = MutableLiveData<UploadSuccessCallback>()
    val isMediaUpdated = MutableLiveData<Boolean>()
    var counterInterstitialAdd: Int = 0// Show interstitial add after 4 count
    var mInterstitialAd: InterstitialAd? = null
    private var mIsLoadAdRequested: Boolean = false
    var isRemoved: Boolean = false
    var isSkipClicked: Boolean = false
    var isImageCout = 0


    companion object {
        lateinit var sharedPreference: AppPrefrrences
    }

    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreference = AppPrefrrences(this)
        sharedPreference.ratingInitTime = System.currentTimeMillis()
        InternetAvailabilityChecker.init(this)
        registerActivityLifecycleCallbacks(this)
        applicationComponent = DaggerApplicationComponent
            .builder()
            .applicationModule(ApplicationModule(this))
            .build()
        applicationComponent.inject(this)

        MobileAds.initialize(this)
        FacebookSdk.sdkInitialize(applicationContext)

        Logger.initializeSession(this, object : Logger.UploadFileListener {
            override fun onDataReceived(file: File) {
                //Call Export Log Service
                Toast.makeText(
                    this@AppineersApplication,
                    "Sending logs.. Check notification..",
                    Toast.LENGTH_SHORT
                ).show()
                val exportService = Intent(this@AppineersApplication, ExportLogService::class.java)
                exportService.putExtra(ExportLogService.KEY_FILE_URI, file)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(exportService)
                } else {
                    startService(exportService)
                }
            }

        })

        Logger.setExceptionCallbackListener(object : Logger.ExceptionCallback {
            @SuppressLint("LogNotTimber")
            override fun onExceptionThrown(exception: Throwable) {
                Log.e("Crash ", "happen")
                Toast.makeText(this@AppineersApplication, "Crash Happen", Toast.LENGTH_SHORT).show()
                FirebaseCrashlytics.getInstance().recordException(exception)
            }
        })

        if (BuildConfig.DEBUG) {
            Logger.enableLogger()
        } else {
            Logger.clearAllLogs()
            Logger.disableLogger()
        }
    }


    override fun onActivityPaused(activity: Activity) {
        LoadingDialog.unbind()
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityResumed(activity: Activity) {
        LoadingDialog.bindWith(activity)
    }


    fun setCurrentActivity(activity: BaseActivity<BaseViewModel>) {
        weakActivity = WeakReference(activity)
    }

    fun getCurrentActivity(): BaseActivity<BaseViewModel>? {
        return weakActivity?.get()
    }

    fun setApplicationLoginType(type: String) {
        sharedPreference.appLoginType = type
    }

    fun getApplicationLoginType(): String {
        return sharedPreference.appLoginType ?: IConstants.LOGIN_TYPE_EMAIL
    }

    /**
     * This will provide application login activity
     * @return Class<*>
     */
    fun getLoginActivity(): Class<*> {
        //return LoginWithEmailActivity::class.java
      // return LoginWithPhoneNumberActivity::class.java
       return LoginWithEmailSocialActivity::class.java
      //  return LoginWithPhoneNumberSocialActivity::class.java
    }

    // Initialize the Mobile Ads SDK.
    fun initGoogleAdMobSDK() {
        if (!sharedPreference.isAdRemoved && (AppConfig.BANNER_AD || AppConfig.INTERSTITIAL_AD)) {
            if (sharedPreference.projectDebugLevel.equals(
                    "development",
                    true
                ) || BuildConfig.DEBUG
            ) {
                val testDeviceIds = Arrays.asList("7F93ADFB8F20D3B4FFA56582EC478DBD")
               // val testDeviceIds = Arrays.asList("")
                val configuration =
                    RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
                MobileAds.setRequestConfiguration(configuration)
            }
            MobileAds.initialize(this) { status ->
                logger.dumpCustomEvent(
                    "AdMob Init",
                    status.adapterStatusMap.entries.joinToString { it.value.initializationState.name })
            }
        }
        initAd()
    }

    // Request a new ad if one isn't already loaded.
    fun initAd() {
        if (!sharedPreference.isAdRemoved || AppConfig.INTERSTITIAL_AD && !mIsLoadAdRequested && mInterstitialAd == null) {
            mIsLoadAdRequested = true
            loadAd()
        }
    }

    /**
     * Initialize interstitial ads
     */
    fun loadAd() {
        InterstitialAd.load(
            this, getInterstitialAdId(), AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    val error = "domain: ${adError.domain}, code: ${adError.code}, " +
                            "message: ${adError.message}"
                    logger.debugEvent("Int Ads onAdFailedToLoad(): ", "with error $error")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "onAdFailedToLoad() with error $error"
                    )
                    mInterstitialAd = null
                    mIsLoadAdRequested = false
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    logger.debugEvent("Int Ads onAdLoaded: ", "Ad successfully loaded.")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "onAdLoaded: Ad successfully loaded."
                    )
                    mInterstitialAd = interstitialAd
                    mIsLoadAdRequested = false
                }
            }
        )
    }

    /**
     * get Interstitial id according to project debug level (api get config param)
     * */
    private fun getInterstitialAdId(): String {
        return when {
            sharedPreference.projectDebugLevel.equals("development", true) -> {
                logger.dumpCustomEvent(
                    "Interstitial Ad Test Unit Id ",
                    sharedPreference.androidInterstitialId!!
                    //getString(R.string.admob_interstitial_unit_id_test).substringAfter('/')
                )
                sharedPreference.androidInterstitialId!!
            }
            sharedPreference.androidInterstitialId.isNullOrEmpty() -> {
                logger.dumpCustomEvent(
                    "Interstitial Ad Init",
                    "Empty Interstitial Admob id from backend"
                )
                ""
            }
            else -> {
                logger.dumpCustomEvent(
                    "Interstitial Ad Server Unit Id ",
                    sharedPreference.androidInterstitialId!!.substringAfter('/')
                )
                sharedPreference.androidInterstitialId!!
            }
        }
    }


    private fun initSdkListener(): SdkInitializationListener? {
        return SdkInitializationListener {

            logger.dumpCustomEvent(
                "MoPUb SDK",
                "MoPub SDK initialized Successfully."
            )
            /* MoPub SDK initialized.
               Check if you should show the consent dialog here, and make your ad requests. */
            showMoPubConsentDialog()

        }
    }

    private fun showMoPubConsentDialog() {

        // Get a PersonalInformationManager instance, used to query the consent dialog
        val mPersonalInfoManager: PersonalInfoManager? = MoPub.getPersonalInformationManager()

        // Check if you must show the consent dialog
        mPersonalInfoManager?.shouldShowConsentDialog()

        // Start loading the consent dialog. This call fails if the user has opted out of ad personalization.
        mPersonalInfoManager?.loadConsentDialog(object : ConsentDialogListener {
            override fun onConsentDialogLoaded() {
                mPersonalInfoManager.showConsentDialog()
            }

            override fun onConsentDialogLoadFailed(moPubErrorCode: MoPubErrorCode) {
                logger.dumpCustomEvent(
                    "MoPUb SDK",
                    "Consent dialog failed to load." + moPubErrorCode.name
                )

            }

        })


    }

    fun initMoPubSDK(appInDevelopment: Boolean) {
        /**
         * Call the MoPub.initializeSdk() once per app’s lifecycle, typically on app launch,
         * using any valid ad unit ID that belongs to the specific app you’re initializing with MoPub.
         * Using a valid ad unit ID from the correct app improves the accuracy of the DAU metric
         * tracking for your app. Make no ad requests until the SDK initialization has completed.
         */

        var moPubAdUnitId = ""
        if (!appInDevelopment) {
            moPubAdUnitId = sharedPreference.androidMoPubBannerId!!
        }
        if (moPubAdUnitId.isNullOrEmpty()) {
            moPubAdUnitId = sharedPreference.androidMoPubBannerId!!
            logger.dumpCustomEvent(
                "MoPub SDK",
                "Empty MoPub id from backend loading with test ids"
            )
        }
        val sdkConfigurationBuilder =
            SdkConfiguration.Builder(moPubAdUnitId)
        if (BuildConfig.DEBUG) {
            sdkConfigurationBuilder.withLogLevel(MoPubLog.LogLevel.DEBUG);
        } else {
            sdkConfigurationBuilder.withLogLevel(MoPubLog.LogLevel.NONE);
        }
        sdkConfigurationBuilder.withLegitimateInterestAllowed(false)
        val sdkConfigurationBannerAd: SdkConfiguration? = sdkConfigurationBuilder.build()
        if (sdkConfigurationBannerAd != null) {
            MoPub.initializeSdk(this, sdkConfigurationBannerAd, initSdkListener())
        }

    }
}