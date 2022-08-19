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
import com.app.signme.core.BaseActivity
import com.app.signme.core.BaseViewModel
import com.app.signme.dataclasses.LikeSuperlikeCancelCallback
import com.app.signme.dataclasses.LikeUserIdCallback
import com.app.signme.dataclasses.RelationshipType
import com.app.signme.scheduler.ExportLogService
import com.app.signme.view.authentication.login.loginwithemailsocial.LoginWithEmailSocialActivity
import com.app.signme.scheduler.aws.cacheUtils.UploadSuccessCallback
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.facebook.FacebookSdk
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.hb.logger.Logger
import java.io.File
import java.lang.ref.WeakReference
import kotlin.collections.ArrayList


class AppineersApplication : MultiDexApplication(), Application.ActivityLifecycleCallbacks {

    lateinit var applicationComponent: ApplicationComponent
    var isLogStatusUpdated = MutableLiveData<Boolean>()
    var isCurrentLocationUpdated = MutableLiveData<Boolean>()
    val isProfileUpdated = MutableLiveData<Boolean>()
    val isSwiperUpdated=MutableLiveData<Boolean>()
    val isAdRemoved = MutableLiveData<Boolean>()
    val isBlockUnblock = MutableLiveData<Boolean>()
    val LikeSuperlikeCancelRequest=MutableLiveData<LikeSuperlikeCancelCallback>()
    val LikeUserIdRequest=MutableLiveData<LikeUserIdCallback>()
    val isMatchesUpdated=MutableLiveData<Boolean>()
    var isSubscriptionTaken = MutableLiveData<Boolean>()
    var weakActivity: WeakReference<BaseActivity<BaseViewModel>>? = null
    val isMediaUploaded = MutableLiveData<MediaUpload?>()
    var relationshipStatus=ArrayList<RelationshipType>()
    val awsFileUploader = MutableLiveData<UploadSuccessCallback>()
    val isMediaUpdated = MutableLiveData<Boolean>()
    var counterInterstitialAdd: Int = 0// Show interstitial add after 4 count
    //var mInterstitialAd: InterstitialAd? = null
    private var mIsLoadAdRequested: Boolean = false
    var isRemoved: Boolean = false
    var isSkipClicked: Boolean = false
    var notificationsCount = MutableLiveData<String>()
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

        AppLovinSdk.getInstance(this).mediationProvider = "max"
        AppLovinSdk.getInstance(this).initializeSdk { configuration ->
            // AppLovin SDK is initialized, start loading ads
            when (configuration.consentDialogState) {
                AppLovinSdkConfiguration.ConsentDialogState.APPLIES -> {
                    // Show user consent dialog
                }
                AppLovinSdkConfiguration.ConsentDialogState.DOES_NOT_APPLY -> {
                    // No need to show consent dialog, proceed with initialization
                }
                else -> {
                    // Consent dialog state is unknown. Proceed with initialization, but check if the consent
                    // dialog should be shown on the next application initialization
                }
            }
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

}