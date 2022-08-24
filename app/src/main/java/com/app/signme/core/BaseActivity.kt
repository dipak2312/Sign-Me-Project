package com.app.signme.core

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.app.signme.R
import com.app.signme.ServerError
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.dialog.DialogUtil
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.commonUtils.utility.extension.showSnackBar
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.dagger.components.DaggerActivityComponent
import com.app.signme.dagger.modules.ActivityModule
import com.app.signme.databinding.DialogLoadingBinding
import com.app.signme.dataclasses.LocationAddressModel
import com.app.signme.view.home.HomeActivity
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.google.android.gms.ads.*
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.firebase.analytics.FirebaseAnalytics
import com.hb.logger.Logger
import com.hb.logger.msc.MSCGenerator
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.pow




abstract class BaseActivity<VM : BaseViewModel> : AppCompatActivity() {

    @Inject
    lateinit var viewModel: VM

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    var adView: MaxAdView? = null
    private var retryAttempt = 0.0
    private lateinit var interstitialAd: MaxInterstitialAd

     private var progressDialog: Dialog? = null
    //private var progressDialog: ACProgressFlower? = null

//  private var mIdlingResource: SimpleIdlingResource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(buildActivityComponent())
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        setDataBindingLayout()
        setupObservers()
        setupView(savedInstanceState)
        viewModel.onCreate()

        MSCGenerator.addLineComment(this::class.java.simpleName)
    }

    private fun buildActivityComponent() =
        DaggerActivityComponent
            .builder()
            .applicationComponent((application as AppineersApplication).applicationComponent)
            .activityModule(ActivityModule(this))
            .build()


    protected open fun setupObservers() {
        viewModel.messageString.observe(this, Observer {
            it.data?.run {
                hideProgressDialog()
                showMessage(this)
            }
        })

        viewModel.messageStringId.observe(this, Observer {
            it.data?.run { showMessage(this) }
        })

        /*viewModel.showDialog.observe(this, Observer {
            if (it) showLoadingDialog() else hideLoadingDialog()
        })*/

        //(application as AppineersApplication).isConnected.observe(this, { internetConnection = it })
    }

    fun checkInternet(): Boolean {
        return when {
            isNetworkAvailable() -> true
            else -> {
                showMessage(getString(R.string.network_connection_error))
                false
            }
        }
    }


    /*fun showLoadingDialog() {
        LoadingDialog.showDialog()
    }

    fun hideLoadingDialog() {
        LoadingDialog.dismissDialog()
    }*/

    open fun goBack() = onBackPressed()

    fun showMessage(@StringRes resId: Int) = showMessage(getString(resId))

    fun showMessage(message: CharSequence) {
        message.toString().showSnackBar(context = this)
    }

    fun showMessage(message: CharSequence, type: Int) {
        message.toString().showSnackBar(context = this, type = type)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0)
            supportFragmentManager.popBackStackImmediate()
        else super.onBackPressed()
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @SuppressLint("NewApi")
    open fun isJobServiceOn(JOB_ID: Int): Boolean {
        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        var hasBeenScheduled = false
        for (jobInfo in scheduler.allPendingJobs) {
            if (jobInfo.id == JOB_ID) {
                hasBeenScheduled = true
                break
            }
        }
        return hasBeenScheduled
    }


    fun enableDisableButton(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        view.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(if (enabled) R.color.app_color else R.color.location_gray)))
    }

    /**
     * Only called from test, creates and returns a new [SimpleIdlingResource].
     */
//    @VisibleForTesting
//    open fun getIdlingResource(): IdlingResource? {
//        if (mIdlingResource == null) {
//            mIdlingResource = SimpleIdlingResource()
//        }
//        return mIdlingResource
//    }

    protected abstract fun setDataBindingLayout()

    protected abstract fun injectDependencies(activityComponent: ActivityComponent)

    protected abstract fun setupView(savedInstanceState: Bundle?)

    fun getParseAddressComponents(addressComponents: AddressComponents): LocationAddressModel {
        return LocationAddressModel().apply {
            for (addressComponent in addressComponents.asList()) {
                for (type in addressComponent.types) {
                    if (type == "street_number") {
                        address = addressComponent.name
                    } else if (type == "route") {
                        address +=
                            if (address.isEmpty()) addressComponent.name else " " + addressComponent.name
                    } else if (type == "locality") {
                        city = addressComponent.name
                    } else if (type == "administrative_area_level_1") {
                        state = addressComponent.shortName.toString()
                    } else if (type == "postal_code") {
                        zipCode = addressComponent.name
                    } else if (type == "country") {
                        country = addressComponent.name.toString()
                    }
                }
            }
        }
    }

    /**
     * Navigate user to Login Screen if user not logged in or want to logout
     */
    fun navigateToLoginScreen(isLogOut: Boolean) {
        if (isLogOut) {
            sharedPreference.userDetail = null
            sharedPreference.isLogin = false
            sharedPreference.authToken = ""
            sharedPreference.configDetails = null
            Logger.setUserInfo("")
        }
        val intent =
            Intent(this@BaseActivity, (application as AppineersApplication).getLoginActivity())
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Navigate user to Home Screen after successfully login/signup
     */
    fun navigateToHomeScreen() {
        val intent = Intent(this@BaseActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        //   overridePendingTransition(0, 0);
        finish()
    }

    /**
     * Navigate user to Address Screen after successfully login/signup
     */
//    fun navigateToAddressScreen() {
//        val intent = Intent(this@BaseActivity, AddAddressActivity::class.java)
//        // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(intent)
//        finish()
//
//    }


    /**
     * If user login in another device, then show session expire dialog to user and navigate to login screen
     */
    fun showSessionExpireDialog(isLogOut: Boolean) {
        if (isLogOut) {
            sharedPreference.userDetail = null
            sharedPreference.isLogin = false
            sharedPreference.authToken = ""
            sharedPreference.configDetails = null
            Logger.setUserInfo("")
        }

        DialogUtil.alert(
            context = this@BaseActivity,
            msg = getString(R.string.msg_logged_in_from_other_device),
            positiveBtnText = getString(R.string.ok),
            negativeBtnText = "",
            il = object : DialogUtil.IL {
                override fun onSuccess() {
                    navigateToLoginScreen(true)
                }

                override fun onCancel(isNeutral: Boolean) {
                    navigateToLoginScreen(true)
                }
            },
            isCancelable = false
        )
    }

    /**
     * Handle api error
     * @param settings [ERROR : Settings]
     * @param showError Boolean
     * @param showSessionExpire Boolean
     * @return Boolean
     */
   /* fun handleApiError(
        settings: Settings?,
        showError: Boolean = true,
        showSessionExpire: Boolean = true
    ): Boolean {
        return when (settings?.success) {
            Settings.AUTHENTICATION_ERROR -> {
                if (showSessionExpire) showSessionExpireDialog(true)
                true
            }
            Settings.NETWORK_ERROR -> {
                settings.message.showSnackBar(this@BaseActivity)
                true
            }
            Settings.SOCIAL_LOGIN_FAILURE_ERROR -> {
                settings.message.showSnackBar(this@BaseActivity)
                true
            }
            Settings.CONNECTION_TIME_OUT_ERROR -> {
                getString(R.string.str_config_message).showSnackBar(this@BaseActivity)
                true
            }
            Settings.ERR_CERT_COMMON_NAME_INVALID -> {
                getString(R.string.str_config_message).showSnackBar(this@BaseActivity)
                true
            }
            Settings.TIME_OUT -> {
                getString(R.string.str_config_message).showSnackBar(this@BaseActivity)
                true
            }
            "0" -> false
            else -> {
                if (showError) settings?.message?.showSnackBar(this@BaseActivity)
                true
            }
        }
    }
*/
    /**
     * Handle api status code error
     * @param serverError
     * @return
     */
    fun handleApiStatusCodeError(
        serverError: ServerError?,
    ): Boolean {
        return when (serverError?.code) {
            400 -> {
                if (serverError.success == "0") showSessionExpireDialog(true)
                true
            }
            401 -> {
                if(sharedPreference.isLogin)
                {
                    if (serverError.success == "0") showSessionExpireDialog(true)
                }else
                {
                    serverError?.message?.showSnackBar(this@BaseActivity)
                }
                true
            }
            else -> {
                serverError?.message?.showSnackBar(this@BaseActivity)
                true
            }
        }
    }

    /**
     * Log all firebase events
     * @param id String Id of event
     * @param name String Name of event
     * @param contentType String content type
     */
    fun setFireBaseAnalyticsData(id: String, name: String, contentType: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        logger.dumpCustomEvent(name, contentType)
    }

    /**
     * Check wether internet connection service is available or not
     */
    private fun isNetworkAvailable(): Boolean {
        val conMgr = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = conMgr.activeNetworkInfo
        return netInfo != null
    }


    /**
     * Called Ads function when returning to the activity
     */
    override fun onResume() {
        super.onResume()
        logger.debugEvent("onResume called", "Activity resumed")
    }

    /**
     * Called Ads function when leaving the activity
     */
    override fun onPause() {
        super.onPause()
        logger.debugEvent("onPause called", "Activity paused")
        Log.i(this@BaseActivity::class.java.simpleName, "onPause: ")
    }

    /**
     * Called Ads function before the activity is destroyed
     */
    override fun onDestroy() {
        if (adView != null) {
            adView!!.destroy()
        }
        super.onDestroy()
        logger.debugEvent("onDestroy called", "Activity destroyed")
        Log.i(this@BaseActivity::class.java.simpleName, "onDestroy: ")
    }


    fun showAppLovinBannerAd(context: Context, view: MaxAdView) {
        if(sharedPreference.goAdFree)
        {
            return
        }
        if (getAppLovinBannerAdId().isEmpty()) {
            view.visibility = View.GONE
            return
        }
        adView = MaxAdView(getAppLovinBannerAdId(), context)

        val maxBannerAdListener = object : MaxAdViewAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                Log.d("App Lovin banner ads", "onAdLoaded")
                logger.debugEvent(this::class.java.simpleName, "banner ad is loaded")
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                Log.d("App Lovin banner ads", "onAdDisplayed")
                logger.debugEvent(this::class.java.simpleName, "banner ad is displayed")
            }

            override fun onAdHidden(ad: MaxAd?) {
                Log.d("App Lovin banner ads", "onAdHidden")
                logger.debugEvent(this::class.java.simpleName, "banner ad is hidden")
            }

            override fun onAdClicked(ad: MaxAd?) {
                Log.d("App Lovin banner ads", "onAdClicked")
                logger.debugEvent(this::class.java.simpleName, "banner ad clicked")
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                Log.d("add load failed unit id", adUnitId!!)
                Log.d("add load failed unit id", error!!.message)
                logger.debugEvent(this::class.java.simpleName, "banner ad load failed")
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                Log.d("App Lovin banner ads", "onAdDisplayFailed")
                logger.debugEvent(this::class.java.simpleName, "banner ad display failed")
            }

            override fun onAdExpanded(ad: MaxAd?) {
                Log.d("App Lovin banner ads", "onAdExpanded")
                logger.debugEvent(this::class.java.simpleName, "banner ad expanded")
            }

            override fun onAdCollapsed(ad: MaxAd?) {
                Log.d("App Lovin banner ads", "onAdCollapsed")
                logger.debugEvent(this::class.java.simpleName, "banner ad collapsed")
            }
        }

        adView!!.setListener(maxBannerAdListener)

        // Stretch to the width of the screen for banners to be fully functional
        val width = ViewGroup.LayoutParams.MATCH_PARENT

        // Banner height on phones and tablets is 50 and 90, respectively
        val heightPx = resources.getDimensionPixelSize(R.dimen.banner_height)

        adView!!.layoutParams = FrameLayout.LayoutParams(width, heightPx)
        // Set background or background color for banners to be fully functional
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            adView!!.setBackgroundColor(getColor(R.color.transparent))
        }

        view.addView(adView)

        // Load the ad
        adView!!.loadAd()
    }

    fun showAppLovinInterstitialAdAd(activity: Activity) {
        if(sharedPreference.goAdFree)
        {
            return
        }
        if (getAppLovinInterstitialAdId().isEmpty()) {
            return
        }
        interstitialAd = MaxInterstitialAd(getAppLovinInterstitialAdId(), activity)

        val maxInterstitialAdListener = object : MaxAdViewAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                retryAttempt = 0.0
                Log.d("ApLovin interstitial ad", "onAdLoaded")
                logger.debugEvent(this::class.java.simpleName, "banner ad is loaded")
                interstitialAd.showAd()
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                Log.d("ApLovin interstitial ad", "onAdDisplayed")
                logger.debugEvent(this::class.java.simpleName, "banner ad is displayed")
            }

            override fun onAdHidden(ad: MaxAd?) {
                Log.d("ApLovin interstitial ad", "onAdHidden")
                logger.debugEvent(this::class.java.simpleName, "banner ad is hidden")
            }

            override fun onAdClicked(ad: MaxAd?) {
                Log.d("ApLovin interstitial ad", "onAdClicked")
                logger.debugEvent(this::class.java.simpleName, "banner ad clicked")
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                Log.d("add load failed unit id", adUnitId!!)
                Log.d("add load failed unit id", error!!.message)
                logger.debugEvent(this::class.java.simpleName, "banner ad load failed")
                // Interstitial ad failed to load
                // AppLovin recommends that you retry with exponentially higher delays up to a maximum delay (in this case 64 seconds)
                retryAttempt++
                val delayMillis =
                    TimeUnit.SECONDS.toMillis(2.0.pow(6.0.coerceAtMost(retryAttempt)).toLong())
                Handler(mainLooper).postDelayed({ interstitialAd.loadAd() }, delayMillis)
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                Log.d("ApLovin interstitial ad", "onAdDisplayFailed")
                logger.debugEvent(this::class.java.simpleName, "banner ad display failed")
                // Interstitial ad failed to display. AppLovin recommends that you load the next ad.
                interstitialAd.loadAd()
            }

            override fun onAdExpanded(ad: MaxAd?) {
                Log.d("ApLovin interstitial ad", "onAdExpanded")
                logger.debugEvent(this::class.java.simpleName, "banner ad expanded")
            }

            override fun onAdCollapsed(ad: MaxAd?) {
                Log.d("ApLovin interstitial ad", "onAdCollapsed")
                logger.debugEvent(this::class.java.simpleName, "banner ad collapsed")
            }
        }

        interstitialAd.setListener(maxInterstitialAdListener)

        // Load the ad
        interstitialAd.loadAd()
    }

    /**
     * get banner id according to project debug level (api get config param)
     * */
    private fun getAppLovinBannerAdId(): String {
        return when {
            AppineersApplication.sharedPreference.projectDebugLevel.equals("development", true) -> {
                AppineersApplication.sharedPreference.androidMoPubBannerId!!
            }
            AppineersApplication.sharedPreference.androidMoPubBannerId.isNullOrEmpty() -> {
                logger.dumpCustomEvent(
                    "App Lovin Banner Ad Init",
                    "Empty Banner App Lovin Ad Unit id from backend"
                )
                ""
            }
            else -> {
                AppineersApplication.sharedPreference.androidMoPubBannerId!!
            }
        }
    }

    /**
     * get Interstitial id according to project debug level (api get config param)
     * */
    private fun getAppLovinInterstitialAdId(): String {
        return when {
            AppineersApplication.sharedPreference.projectDebugLevel.equals("development", true) -> {
                sharedPreference.androidMopubInterstitialId!!
            }
            AppineersApplication.sharedPreference.androidMopubInterstitialId.isNullOrEmpty() -> {
                logger.dumpCustomEvent(
                    "App Lovin Interstitial Ad Init",
                    "Empty Interstitial App Lovin Ad Unit id from backend"
                )
                ""
            }
            else -> {
                logger.dumpCustomEvent(
                    "App Lovin Interstitial Ad Server Unit Id ",
                    AppineersApplication.sharedPreference.androidMopubInterstitialId!!
                )
                AppineersApplication.sharedPreference.androidMopubInterstitialId!!
            }
        }
    }

/* fun showProgressDialog(
     isCheckNetwork: Boolean = true,
     isSetTitle: Boolean,
     title: String) {
     if (isCheckNetwork) {
         if (isNetworkAvailable()) {
             if (progressDialog == null) {
                 progressDialog = Dialog(this)
                 val binding: DialogLoadingBinding =
                     DialogLoadingBinding.inflate(LayoutInflater.from(this))
                 progressDialog?.setContentView(binding.root)
                 progressDialog?.setCancelable(false)
                 progressDialog?.window!!.setBackgroundDrawable(
                         ColorDrawable(Color.TRANSPARENT))

                 when {
                     isSetTitle -> binding.tvTitle.text = title
                 }
             }
             progressDialog?.show()
         }
     }
     else{
         if (progressDialog == null) {
             progressDialog = Dialog(this)
             val binding: DialogLoadingBinding =
                 DialogLoadingBinding.inflate(LayoutInflater.from(this))
             progressDialog?.setContentView(binding.root)
             progressDialog?.setCancelable(false)
             progressDialog?.window!!.setBackgroundDrawable(
                 ColorDrawable(Color.TRANSPARENT))

             when {
                 isSetTitle -> binding.tvTitle.text = title
             }
         }
         progressDialog?.show()
     }
 }
*/

    fun showProgressDialog(
        isCheckNetwork: Boolean = true,
        isSetTitle: Boolean,
        title: String
    ) {
        if (isCheckNetwork) {
            if (isNetworkAvailable()) {
                if (progressDialog == null) {
                    progressDialog = Dialog(this)
                    val binding: DialogLoadingBinding =
                        DialogLoadingBinding.inflate(LayoutInflater.from(this))
                    progressDialog?.setContentView(binding.root)
                    progressDialog?.setCancelable(false)
                     progressDialog?.window!!.setBackgroundDrawable(
                             ColorDrawable(Color.TRANSPARENT)
                     )

//                    when {
//                        isSetTitle -> binding.tvTitle.text = title
//                    }
                }
                progressDialog?.show()
            }
        } else {
            if (progressDialog == null) {
                progressDialog = Dialog(this)
                val binding: DialogLoadingBinding =
                    DialogLoadingBinding.inflate(LayoutInflater.from(this))
                progressDialog?.setContentView(binding.root)
                progressDialog?.setCancelable(false)
                progressDialog?.window!!.setBackgroundDrawable(
                     ColorDrawable(Color.TRANSPARENT))

//                when {
//                    isSetTitle -> binding.tvTitle.text = title
//                }
            }
            progressDialog?.show()
        }
    }

    fun showSplashProgressDialog(
        isCheckNetwork: Boolean = true,
        isSetTitle: Boolean,
        title: String
    ) {
        if (isCheckNetwork) {
            if (isNetworkAvailable()) {
                if (progressDialog == null) {
                    progressDialog = Dialog(this)
                    val binding: DialogLoadingBinding =
                        DialogLoadingBinding.inflate(LayoutInflater.from(this))
                    progressDialog?.setContentView(binding.root)
                    progressDialog?.setCancelable(false)
                    progressDialog?.window!!.setBackgroundDrawable(
                             ColorDrawable(Color.TRANSPARENT))

//                    when {
//                        isSetTitle -> binding.tvTitle.text = title
//                    }
                }
                progressDialog?.show()
            }
        } else {
            if (progressDialog == null) {
                progressDialog = Dialog(this)
                val binding: DialogLoadingBinding =
                    DialogLoadingBinding.inflate(LayoutInflater.from(this))
                progressDialog?.setContentView(binding.root)
                progressDialog?.setCancelable(false)
                progressDialog?.window!!.setBackgroundDrawable(
                     ColorDrawable(Color.TRANSPARENT))

//                when {
//                    isSetTitle -> binding.tvTitle.text = title
//                }
            }
            progressDialog?.show()
        }
    }


    fun hideProgressDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog?.dismiss()
            progressDialog = null
        }
    }

    /**
     * Populates a [UnifiedNativeAdView] object with data from a given
     * [UnifiedNativeAd].
     *
     * @param nativeAd the object containing the ad's assets
     * @param adView the view to be populated
     */
//    private fun populateUnifiedNativeAdView(
//        nativeAd: UnifiedNativeAd,
//        adView: UnifiedNativeAdView
//    ) {
//
//        logger.dumpCustomEvent("NativeAd", "UnifiedNative Ads loaded")
//
//        // Set the media view.
//        adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)
//
//        // Set other ad assets.
//        adView.headlineView = adView.findViewById(R.id.ad_headline)
//        adView.bodyView = adView.findViewById(R.id.ad_body)
//        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
//        adView.iconView = adView.findViewById(R.id.ad_app_icon)
//        adView.priceView = adView.findViewById(R.id.ad_price)
//        adView.starRatingView = adView.findViewById(R.id.ad_stars)
//        adView.storeView = adView.findViewById(R.id.ad_store)
//        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
//
//        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
//        (adView.headlineView as TextView).text = nativeAd.headline
//        adView.mediaView.setMediaContent(nativeAd.mediaContent)
//
//        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
//        // check before trying to display them.
//        if (nativeAd.body == null) {
//            adView.bodyView.visibility = View.INVISIBLE
//        } else {
//            adView.bodyView.visibility = View.VISIBLE
//            (adView.bodyView as TextView).text = nativeAd.body
//        }
//
//        if (nativeAd.callToAction == null) {
//            adView.callToActionView.visibility = View.INVISIBLE
//        } else {
//            adView.callToActionView.visibility = View.VISIBLE
//            (adView.callToActionView as Button).text = nativeAd.callToAction
//        }
//
//        if (nativeAd.icon == null) {
//            adView.iconView.visibility = View.GONE
//        } else {
//            (adView.iconView as ImageView).setImageDrawable(
//                nativeAd.icon.drawable
//            )
//            adView.iconView.visibility = View.VISIBLE
//        }
//
//        if (nativeAd.price == null) {
//            adView.priceView.visibility = View.INVISIBLE
//        } else {
//            adView.priceView.visibility = View.VISIBLE
//            (adView.priceView as TextView).text = nativeAd.price
//        }
//
//        if (nativeAd.store == null) {
//            adView.storeView.visibility = View.INVISIBLE
//        } else {
//            adView.storeView.visibility = View.VISIBLE
//            (adView.storeView as TextView).text = nativeAd.store
//        }
//
//        if (nativeAd.starRating == null) {
//            adView.starRatingView.visibility = View.INVISIBLE
//        } else {
//            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
//            adView.starRatingView.visibility = View.VISIBLE
//        }
//
//        if (nativeAd.advertiser == null) {
//            adView.advertiserView.visibility = View.INVISIBLE
//        } else {
//            (adView.advertiserView as TextView).text = nativeAd.advertiser
//            adView.advertiserView.visibility = View.VISIBLE
//        }
//
//        // This method tells the Google Mobile Ads SDK that you have finished populating your
//        // native ad view with this native ad.
//        adView.setNativeAd(nativeAd)
//
//        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
//        // have a video asset.
//        val vc = nativeAd.videoController
//
//        // Updates the UI to say whether or not this ad has a video asset.
//        if (vc.hasVideoContent()) {
//
//            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
//            // VideoController will call methods on this object when events occur in the video
//            // lifecycle.
//            vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
//                override fun onVideoEnd() {
//                    // Publishers should allow native ads to complete video playback before
//                    // refreshing or replacing them with another ad in the same UI location.
//
//                    super.onVideoEnd()
//                }
//            }
//        } else {
//            //videostatus_text.text = "Video status: Ad does not contain a video asset."
//            //refresh_button.isEnabled = true
//            logger.dumpCustomEvent("NativeAd", "Video status: Ad does not contain a video asset.")
//
//        }
//    }
//
//    /**
//     * Populates a [View] object with data from a [NativeCustomTemplateAd]. This method
//     * handles a particular "simple" custom native ad format.
//     *
//     * @param nativeCustomTemplateAd the object containing the ad's assets
//     *
//     * @param adView the view to be populated
//     */
//    private fun populateSimpleTemplateAdView(
//        nativeCustomTemplateAd: NativeCustomTemplateAd,
//        adView: View
//    ) {
//        val headlineView = adView.findViewById<TextView>(R.id.simplecustom_headline)
//        val captionView = adView.findViewById<TextView>(R.id.simplecustom_caption)
//
//        headlineView.text = nativeCustomTemplateAd.getText("Headline")
//        captionView.text = nativeCustomTemplateAd.getText("Caption")
//
//        val mediaPlaceholder = adView.findViewById<FrameLayout>(R.id.simplecustom_media_placeholder)
//
//        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
//        // have a video asset.
//        val vc = nativeCustomTemplateAd.videoController
//
//        // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
//        // VideoController will call methods on this object when events occur in the video
//        // lifecycle.
//        vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
//            override fun onVideoEnd() {
//                // Publishers should allow native ads to complete video playback before refreshing
//                // or replacing them with another ad in the same UI location.
//                //refresh_button.isEnabled = true
//                //videostatus_text.text = "Video status: Video playback has ended."
//                logger.dumpCustomEvent("NativeAd", "Video status: Video playback has ended.")
//
//                super.onVideoEnd()
//            }
//        }
//
//        // Apps can check the VideoController's hasVideoContent property to determine if the
//        // NativeCustomTemplateAd has a video asset.
//        if (vc.hasVideoContent()) {
//            mediaPlaceholder.addView(nativeCustomTemplateAd.getVideoMediaView())
//            // Kotlin doesn't include decimal-place formatting in its string interpolation, but
//            // good ol' String.format works fine.
//            /* videostatus_text.text = String.format(
//                 Locale.getDefault(),
//                 "Video status: Ad contains a %.2f:1 video asset.",
//                 vc.aspectRatio
//             )*/
//        } else {
//            val mainImage = ImageView(this)
//            mainImage.adjustViewBounds = true
//            mainImage.setImageDrawable(nativeCustomTemplateAd.getImage("MainImage").drawable)
//
//            mainImage.setOnClickListener { nativeCustomTemplateAd.performClick("MainImage") }
//            mediaPlaceholder.addView(mainImage)
//            //refresh_button.isEnabled = true
//            //videostatus_text.text = "Video status: Ad does not contain a video asset."
//        }
//    }
//
//    /**
//     * Creates a request for a new native ad based on the boolean parameters and calls the
//     * corresponding "populate" method when one is successfully returned.
//     *
//     * @param requestUnifiedNativeAds indicates whether unified native ads should be requested
//     *
//     * @param requestCustomTemplateAds indicates whether custom template ads should be requested
//     */
//    fun refreshAd(
//        requestUnifiedNativeAds: Boolean,
//        requestCustomTemplateAds: Boolean,
//        startMutedCheck: Boolean = false,
//        adFrame: FrameLayout
//    ) {
//        if (!requestUnifiedNativeAds && !requestCustomTemplateAds) {
//            logger.dumpCustomEvent(
//                "NativeAd",
//                "At least one ad format must be checked to request an ad."
//            )
//            Toast.makeText(
//                this, "At least one ad format must be checked to request an ad.",
//                Toast.LENGTH_SHORT
//            ).show()
//            return
//        }
//
//        //refresh_button.isEnabled = true
//        logger.dumpCustomEvent("NativeAd", "Ad Loaded.")
//        val builder = AdLoader.Builder(this, getString(R.string.admob_native_unit_id_test))
//
//        if (requestUnifiedNativeAds) {
//            builder.forUnifiedNativeAd { unifiedNativeAd ->
//                // If this callback occurs after the activity is destroyed, you must call
//                // destroy and return or you may get a memory leak.
//                var activityDestroyed = false
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                    activityDestroyed = isDestroyed
//                }
//                if (activityDestroyed || isFinishing || isChangingConfigurations) {
//                    unifiedNativeAd.destroy()
//                    return@forUnifiedNativeAd
//                }
//                // You must call destroy on old ads when you are done with them,
//                // otherwise you will have a memory leak.
//                currentUnifiedNativeAd?.destroy()
//                currentUnifiedNativeAd = unifiedNativeAd
//                val adView = layoutInflater
//                    .inflate(R.layout.ad_unified, null) as UnifiedNativeAdView
//                populateUnifiedNativeAdView(unifiedNativeAd, adView)
//                adFrame.removeAllViews()
//                adFrame.addView(adView)
//            }
//        }
//
//        if (requestCustomTemplateAds) {
//            builder.forCustomTemplateAd(
//                SIMPLE_TEMPLATE_ID,
//                { ad: NativeCustomTemplateAd ->
//                    // If this callback occurs after the activity is destroyed, you must call
//                    // destroy and return or you may get a memory leak.
//                    var activityDestroyed = false
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                        activityDestroyed = isDestroyed
//                    }
//                    if (activityDestroyed || isFinishing || isChangingConfigurations) {
//                        ad.destroy()
//                        return@forCustomTemplateAd
//                    }
//                    // You must call destroy on old ads when you are done with them,
//                    // otherwise you will have a memory leak.
//                    currentCustomTemplateAd?.destroy()
//                    currentCustomTemplateAd = ad
//                    val frameLayout = adFrame
//                    val adView = layoutInflater
//                        .inflate(R.layout.ad_simple_custom_template, null)
//                    populateSimpleTemplateAdView(ad, adView)
//                    frameLayout.removeAllViews()
//                    frameLayout.addView(adView)
//                },
//                { ad: NativeCustomTemplateAd, s: String ->
//                    logger.dumpCustomEvent(
//                        "NativeAd",
//                        "A custom click has occurred in the simple template"
//                    )
//
//                    Toast.makeText(
//                        this,
//                        "A custom click has occurred in the simple template",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            )
//        }
//
//        val videoOptions = VideoOptions.Builder()
//            .setStartMuted(startMutedCheck)
//            .build()
//
//        val adOptions = com.google.android.gms.ads.formats.NativeAdOptions.Builder()
//            .setVideoOptions(videoOptions)
//            .build()
//
//        builder.withNativeAdOptions(adOptions)
//
//        val adLoader = builder.withAdListener(object : AdListener() {
//            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                //refresh_button.isEnabled = true
//                val error =
//                    """"
//            domain: ${loadAdError.domain}, code: ${loadAdError.code}, message: ${loadAdError.message}
//          """
//                logger.dumpCustomEvent("NativeAd", "Failed to load native ad with error $error")
//
//                Toast.makeText(
//                    this@BaseActivity, "Failed to load native ad with error $error",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }).build()
//
//        adLoader.loadAd(PublisherAdRequest.Builder().build())
//
//        // videostatus_text.text = ""
//    }

}