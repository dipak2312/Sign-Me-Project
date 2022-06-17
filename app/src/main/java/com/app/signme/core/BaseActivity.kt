package com.app.signme.core

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import com.google.android.gms.ads.*
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.NativeCustomTemplateAd
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.firebase.analytics.FirebaseAnalytics
import com.hb.logger.Logger
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import com.mopub.mobileads.MoPubView
import kotlinx.android.synthetic.main.ad_unified.*
import java.util.*
import javax.inject.Inject


const val AD_MANAGER_AD_UNIT_ID = "/6499/example/native"
const val SIMPLE_TEMPLATE_ID = "10104090"

var currentUnifiedNativeAd: UnifiedNativeAd? = null
var currentCustomTemplateAd: NativeCustomTemplateAd? = null


abstract class BaseActivity<VM : BaseViewModel> : AppCompatActivity() {

    @Inject
    lateinit var viewModel: VM

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    var adView: AdView? = null
    var moPubBannerView: MoPubView? = null
    var moPubInterstitial: MoPubInterstitial? = null

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
                if (serverError.success == "0") showSessionExpireDialog(true)
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
        if (adView != null) {
            adView!!.resume()
        }


        logger.debugEvent("onResume called", "Activity resumed")
    }

    /**
     * Called Ads function when leaving the activity
     */
    override fun onPause() {
        if (adView != null) {
            adView!!.pause()
        }
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


    fun showBannerAd(context: Context, containerView: MoPubView) {
        moPubBannerView = containerView
        if (moPubBannerView != null) {
            moPubBannerView!!.setAdUnitId(getMoPubBannerAdId())
            moPubBannerView!!.bannerAdListener = object : MoPubView.BannerAdListener {
                override fun onBannerExpanded(banner: MoPubView?) {
                    logger.debugEvent(this::class.java.simpleName, "banner ad is expanded")
                }

                override fun onBannerLoaded(banner: MoPubView) {
                    logger.debugEvent(this::class.java.simpleName, "banner ad is loaded")
                }

                override fun onBannerCollapsed(banner: MoPubView?) {
                    logger.debugEvent(this::class.java.simpleName, "banner ad is collapsed")
                }

                override fun onBannerFailed(banner: MoPubView?, errorCode: MoPubErrorCode?) {
                    logger.debugEvent(
                        this::class.java.simpleName,
                        "banner has failed to retrieve an ad " + errorCode.toString()
                    )
                }

                override fun onBannerClicked(banner: MoPubView?) {
                    logger.debugEvent(this::class.java.simpleName, "banner ad is clicked by user.")
                }
            }
            moPubBannerView!!.loadAd(MoPubView.MoPubAdSize.HEIGHT_50)
        }
    }


    /**
     * This method will show banner ads
     * @param adView AdView
     */
    fun showBannerAd(context: Context, container: RelativeLayout) {
        adView = AdView(context)
        adView!!.adUnitId = getAdMobBannerAdId()
        adView!!.adSize = AdSize.BANNER
        if (sharedPreference.isAdRemoved || AppineersApplication.sharedPreference.androidBannerId.equals(
                ""
            ) || !AppConfig.BANNER_AD
        ) {
            container.removeAllViews()
            container.visibility = View.GONE
        } else {
            container.addView(adView)
            val adRequest = AdRequest.Builder().build()
            adView!!.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    logger.dumpCustomEvent("Banner Ad Loaded", "Ad load successful")
                    Log.d(this@BaseActivity::class.java.simpleName, "Ad Loaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Code to be executed when an ad request fails.
                    val error = "domain: ${adError.domain}, code: ${adError.code}, " +
                            "message: ${adError.message}"
                    logger.dumpCustomEvent("Banner onAdFailedToLoad", "with error $error")
                    Log.d(
                        this@BaseActivity::class.java.simpleName,
                        "onAdFailedToLoad: " + "with error $error"
                    )
                }

                override fun onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                    logger.dumpCustomEvent("Banner Ad Opened", "Ad Open event")
                    Log.d(this@BaseActivity::class.java.simpleName, "Ad Opened")
                }

                override fun onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                    logger.dumpCustomEvent("Banner Ad Clicked", "Ad Click event")
                    Log.d(this@BaseActivity::class.java.simpleName, "Ad Clicked")
                }

                override fun onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                    logger.dumpCustomEvent("Banner Ad Closed", "Ad Close event")
                    Log.d(this@BaseActivity::class.java.simpleName, "Ad Closed")
                }
            }
            adView!!.loadAd(adRequest)
            container.visibility = View.VISIBLE
            logger.debugEvent("Banner Ad Request", "Banner Ad Requested")
        }
    }

    /**
     * get banner id according to project debug level (api get config param)
     * */
    private fun getAdMobBannerAdId(): String {
        return when {
            AppineersApplication.sharedPreference.projectDebugLevel.equals("development", true) -> {
                logger.dumpCustomEvent(
                    "Banner Ad Test Unit Id ",
                    getString(R.string.admob_banner_unit_id_test).substringAfter('/')
                )
                AppineersApplication.sharedPreference.androidBannerId!!
            }
            AppineersApplication.sharedPreference.androidBannerId.isNullOrEmpty() -> {
                logger.dumpCustomEvent("Banner Ad Init", "Empty Banner Admob id from backend")
                ""
            }
            else -> {
                logger.dumpCustomEvent(
                    "Banner Ad Server Unit Id ",
                    AppineersApplication.sharedPreference.androidBannerId!!.substringAfter('/')
                )
                AppineersApplication.sharedPreference.androidBannerId!!
            }
        }
    }

    /**
     * get banner id according to project debug level (api get config param)
     * */
    private fun getMoPubBannerAdId(): String {
        return when {
            AppineersApplication.sharedPreference.projectDebugLevel.equals("development", true) -> {
                logger.dumpCustomEvent(
                    "MO PUB Banner Ad ID",
                    "Using Banner Ad Test Unit Id"
                )
                //getString(R.string.mopub_banner_unit_id_test)
                AppineersApplication.sharedPreference.androidMoPubBannerId!!
            }
            AppineersApplication.sharedPreference.androidMoPubBannerId.isNullOrEmpty() -> {
                logger.dumpCustomEvent(
                    "MO PUB Banner Ad Init",
                    "Empty Banner MO PUB Ad Unit id from backend"
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
    private fun getMoPubInterstitialAdId(): String {
        return when {
            AppineersApplication.sharedPreference.projectDebugLevel.equals("development", true) -> {
                logger.dumpCustomEvent("MO PUB Interstitial Ad Test Unit Id ", "Using Test Unit ID")
                //getString(R.string.mopub_full_screen_unit_id_test)
                sharedPreference.androidMopubInterstitialId!!
            }
            AppineersApplication.sharedPreference.androidMopubInterstitialId.isNullOrEmpty() -> {
                logger.dumpCustomEvent(
                    "MO PUB Interstitial Ad Init",
                    "Empty Interstitial MO PUB Ad Unit id from backend"
                )
                ""
            }
            else -> {
                logger.dumpCustomEvent(
                    "MO PUB Interstitial Ad Server Unit Id ",
                    AppineersApplication.sharedPreference.androidMopubInterstitialId!!.substringAfter(
                        '/'
                    )
                )
                AppineersApplication.sharedPreference.androidMopubInterstitialId!!
            }
        }
    }

    /**
     * This method will show mopub interstitial ads
     * @return Boolean
     */
    fun showInterstitial(context: Context) {
        moPubInterstitial =
            MoPubInterstitial(
                context as Activity,
                getMoPubInterstitialAdId()
            )
        moPubInterstitial!!.interstitialAdListener =
            object : MoPubInterstitial.InterstitialAdListener {
                override fun onInterstitialLoaded(interstitial: MoPubInterstitial?) {
                    if (moPubInterstitial!!.isReady) {
                        moPubInterstitial!!.show()
                    }
                    logger.debugEvent(
                        this@BaseActivity::class.java.simpleName,
                        "interstitial ad has been cached and is ready to be shown."
                    )
                }

                override fun onInterstitialShown(interstitial: MoPubInterstitial?) {
                    logger.debugEvent(
                        this@BaseActivity::class.java.simpleName,
                        "interstitial ad has been shown."
                    )
                }

                override fun onInterstitialFailed(
                    interstitial: MoPubInterstitial?,
                    errorCode: MoPubErrorCode?
                ) {
                    logger.debugEvent(
                        this@BaseActivity::class.java.simpleName,
                        "interstitial ad has failed to load. " + errorCode.toString()
                    )
                }

                override fun onInterstitialDismissed(interstitial: MoPubInterstitial?) {
                    logger.debugEvent(
                        this@BaseActivity::class.java.simpleName,
                        "interstitial ad  has being dismissed."
                    )
                }

                override fun onInterstitialClicked(interstitial: MoPubInterstitial?) {
                    logger.debugEvent(
                        this@BaseActivity::class.java.simpleName,
                        "interstitial ad is clicked by user."
                    )
                }
            }
        moPubInterstitial!!.load()
    }

    /**
     * This method will show interstitial adds after every 1 event
     * @return Boolean
     */
    fun showInterstitial(): Boolean {
        // Show the ad if it is ready. Otherwise toast and reload the ad.
        if (!sharedPreference.isAdRemoved || AppConfig.INTERSTITIAL_AD && (application as AppineersApplication).mInterstitialAd != null) {
            (application as AppineersApplication).mInterstitialAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        logger.debugEvent(
                            "Int Ads onAdDismissedFullScreenContent: ",
                            "Ad was dismissed."
                        )
                        MSCGenerator.addAction(
                            GenConstants.ENTITY_USER,
                            GenConstants.ENTITY_APP,
                            "onAdDismissedFullScreenContent: Ad was dismissed."
                        )
                        Log.d("BaseActivity", "Ad was dismissed.")
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        (application as AppineersApplication).mInterstitialAd = null
                        (application as AppineersApplication).loadAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                        logger.debugEvent(
                            "Int Ads onAdFailedToShowFullScreenContent: ",
                            "Ad failed to show."
                        )
                        MSCGenerator.addAction(
                            GenConstants.ENTITY_USER,
                            GenConstants.ENTITY_APP,
                            "onAdFailedToShowFullScreenContent: Ad failed to show."
                        )
                        Log.d(
                            "BaseActivity",
                            "onAdFailedToShowFullScreenContent: Ad failed to show."
                        )
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        (application as AppineersApplication).mInterstitialAd = null
                    }

                    // Called when ad is dismissed.
                    override fun onAdShowedFullScreenContent() {
                        logger.debugEvent(
                            "Int Ads onAdShowedFullScreenContent: ",
                            "Ad showed fullscreen content."
                        )
                        MSCGenerator.addAction(
                            GenConstants.ENTITY_USER,
                            GenConstants.ENTITY_APP,
                            "onAdShowedFullScreenContent: Ad showed fullscreen content."
                        )
                        Log.d(
                            "BaseActivity",
                            "onAdShowedFullScreenContent: Ad showed fullscreen content."
                        )

                    }
                }

            //This will show interstitial adds after every 4 event
            return if (++(application as AppineersApplication).counterInterstitialAdd % 1 == 0) {
                (application as AppineersApplication).mInterstitialAd?.show(this)
                true
            } else {
                false
            }

        } else {
            logger.debugEvent("Int Ads: ", "Ad wasn't loaded.")
            MSCGenerator.addAction(
                GenConstants.ENTITY_USER,
                GenConstants.ENTITY_APP,
                "Ad wasn't loaded."
            )
            (application as AppineersApplication).initAd()
            return false
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
    private fun populateUnifiedNativeAdView(
        nativeAd: UnifiedNativeAd,
        adView: UnifiedNativeAdView
    ) {

        logger.dumpCustomEvent("NativeAd", "UnifiedNative Ads loaded")

        // Set the media view.
        adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        adView.mediaView.setMediaContent(nativeAd.mediaContent)

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView.visibility = View.INVISIBLE
        } else {
            adView.bodyView.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView.visibility = View.INVISIBLE
        } else {
            adView.callToActionView.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon.drawable
            )
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView.visibility = View.INVISIBLE
        } else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView.visibility = View.INVISIBLE
        } else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        val vc = nativeAd.videoController

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {

            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
                override fun onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.

                    super.onVideoEnd()
                }
            }
        } else {
            //videostatus_text.text = "Video status: Ad does not contain a video asset."
            //refresh_button.isEnabled = true
            logger.dumpCustomEvent("NativeAd", "Video status: Ad does not contain a video asset.")

        }
    }

    /**
     * Populates a [View] object with data from a [NativeCustomTemplateAd]. This method
     * handles a particular "simple" custom native ad format.
     *
     * @param nativeCustomTemplateAd the object containing the ad's assets
     *
     * @param adView the view to be populated
     */
    private fun populateSimpleTemplateAdView(
        nativeCustomTemplateAd: NativeCustomTemplateAd,
        adView: View
    ) {
        val headlineView = adView.findViewById<TextView>(R.id.simplecustom_headline)
        val captionView = adView.findViewById<TextView>(R.id.simplecustom_caption)

        headlineView.text = nativeCustomTemplateAd.getText("Headline")
        captionView.text = nativeCustomTemplateAd.getText("Caption")

        val mediaPlaceholder = adView.findViewById<FrameLayout>(R.id.simplecustom_media_placeholder)

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        val vc = nativeCustomTemplateAd.videoController

        // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
        // VideoController will call methods on this object when events occur in the video
        // lifecycle.
        vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
            override fun onVideoEnd() {
                // Publishers should allow native ads to complete video playback before refreshing
                // or replacing them with another ad in the same UI location.
                //refresh_button.isEnabled = true
                //videostatus_text.text = "Video status: Video playback has ended."
                logger.dumpCustomEvent("NativeAd", "Video status: Video playback has ended.")

                super.onVideoEnd()
            }
        }

        // Apps can check the VideoController's hasVideoContent property to determine if the
        // NativeCustomTemplateAd has a video asset.
        if (vc.hasVideoContent()) {
            mediaPlaceholder.addView(nativeCustomTemplateAd.getVideoMediaView())
            // Kotlin doesn't include decimal-place formatting in its string interpolation, but
            // good ol' String.format works fine.
            /* videostatus_text.text = String.format(
                 Locale.getDefault(),
                 "Video status: Ad contains a %.2f:1 video asset.",
                 vc.aspectRatio
             )*/
        } else {
            val mainImage = ImageView(this)
            mainImage.adjustViewBounds = true
            mainImage.setImageDrawable(nativeCustomTemplateAd.getImage("MainImage").drawable)

            mainImage.setOnClickListener { nativeCustomTemplateAd.performClick("MainImage") }
            mediaPlaceholder.addView(mainImage)
            //refresh_button.isEnabled = true
            //videostatus_text.text = "Video status: Ad does not contain a video asset."
        }
    }

    /**
     * Creates a request for a new native ad based on the boolean parameters and calls the
     * corresponding "populate" method when one is successfully returned.
     *
     * @param requestUnifiedNativeAds indicates whether unified native ads should be requested
     *
     * @param requestCustomTemplateAds indicates whether custom template ads should be requested
     */
    fun refreshAd(
        requestUnifiedNativeAds: Boolean,
        requestCustomTemplateAds: Boolean,
        startMutedCheck: Boolean = false,
        adFrame: FrameLayout
    ) {
        if (!requestUnifiedNativeAds && !requestCustomTemplateAds) {
            logger.dumpCustomEvent(
                "NativeAd",
                "At least one ad format must be checked to request an ad."
            )
            Toast.makeText(
                this, "At least one ad format must be checked to request an ad.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        //refresh_button.isEnabled = true
        logger.dumpCustomEvent("NativeAd", "Ad Loaded.")
        val builder = AdLoader.Builder(this, getString(R.string.admob_native_unit_id_test))

        if (requestUnifiedNativeAds) {
            builder.forUnifiedNativeAd { unifiedNativeAd ->
                // If this callback occurs after the activity is destroyed, you must call
                // destroy and return or you may get a memory leak.
                var activityDestroyed = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    activityDestroyed = isDestroyed
                }
                if (activityDestroyed || isFinishing || isChangingConfigurations) {
                    unifiedNativeAd.destroy()
                    return@forUnifiedNativeAd
                }
                // You must call destroy on old ads when you are done with them,
                // otherwise you will have a memory leak.
                currentUnifiedNativeAd?.destroy()
                currentUnifiedNativeAd = unifiedNativeAd
                val adView = layoutInflater
                    .inflate(R.layout.ad_unified, null) as UnifiedNativeAdView
                populateUnifiedNativeAdView(unifiedNativeAd, adView)
                adFrame.removeAllViews()
                adFrame.addView(adView)
            }
        }

        if (requestCustomTemplateAds) {
            builder.forCustomTemplateAd(
                SIMPLE_TEMPLATE_ID,
                { ad: NativeCustomTemplateAd ->
                    // If this callback occurs after the activity is destroyed, you must call
                    // destroy and return or you may get a memory leak.
                    var activityDestroyed = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        activityDestroyed = isDestroyed
                    }
                    if (activityDestroyed || isFinishing || isChangingConfigurations) {
                        ad.destroy()
                        return@forCustomTemplateAd
                    }
                    // You must call destroy on old ads when you are done with them,
                    // otherwise you will have a memory leak.
                    currentCustomTemplateAd?.destroy()
                    currentCustomTemplateAd = ad
                    val frameLayout = adFrame
                    val adView = layoutInflater
                        .inflate(R.layout.ad_simple_custom_template, null)
                    populateSimpleTemplateAdView(ad, adView)
                    frameLayout.removeAllViews()
                    frameLayout.addView(adView)
                },
                { ad: NativeCustomTemplateAd, s: String ->
                    logger.dumpCustomEvent(
                        "NativeAd",
                        "A custom click has occurred in the simple template"
                    )

                    Toast.makeText(
                        this,
                        "A custom click has occurred in the simple template",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        val videoOptions = VideoOptions.Builder()
            .setStartMuted(startMutedCheck)
            .build()

        val adOptions = com.google.android.gms.ads.formats.NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()

        builder.withNativeAdOptions(adOptions)

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                //refresh_button.isEnabled = true
                val error =
                    """"
            domain: ${loadAdError.domain}, code: ${loadAdError.code}, message: ${loadAdError.message}
          """
                logger.dumpCustomEvent("NativeAd", "Failed to load native ad with error $error")

                Toast.makeText(
                    this@BaseActivity, "Failed to load native ad with error $error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }).build()

        adLoader.loadAd(PublisherAdRequest.Builder().build())

        // videostatus_text.text = ""
    }

}