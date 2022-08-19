package com.app.signme.core



import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.commonUtils.utility.extension.showSnackBar
import com.app.signme.dagger.components.DaggerFragmentComponent
import com.app.signme.dagger.components.FragmentComponent
import com.app.signme.dagger.modules.FragmentModule
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.hb.logger.Logger
import com.hb.logger.msc.MSCGenerator
import java.util.concurrent.TimeUnit

import javax.inject.Inject
import kotlin.math.pow

/**
 *
 * @author Ramandeep Bhayana
 */
abstract class BaseFragment<VM : BaseViewModel> : Fragment() {

    @Inject
    lateinit var viewModel: VM

    protected var mBaseActivity: BaseActivity<VM>? = null

    var adView: MaxAdView? = null
    private var retryAttempt = 0.0
    private lateinit var interstitialAd: MaxInterstitialAd

    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = activity as BaseActivity<VM>?
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(buildFragmentComponent())
        super.onCreate(savedInstanceState)
        setDataBindingLayout()
        setupObservers()
        viewModel.onCreate()

        MSCGenerator.addLineComment(this::class.java.simpleName)
    }

    private fun buildFragmentComponent() =
        DaggerFragmentComponent
            .builder()
            .applicationComponent((context?.applicationContext as AppineersApplication).applicationComponent)
            .fragmentModule(FragmentModule(this,null))
            .build()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(provideLayoutId(), container, false)

    protected open fun setupObservers() {
        viewModel.messageString.observe(this, Observer {
            it.data?.run { showMessage(this) }
        })

        viewModel.messageStringId.observe(this, Observer {
            it.data?.run { showMessage(this) }
        })

        /*viewModel.showDialog.observe(this, Observer {
            if (it) {
                LoadingDialog.showDialog()
            } else {
                LoadingDialog.dismissDialog()
            }
        })*/
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView(view)
    }

    fun showMessage(@StringRes resId: Int) = showMessage(getString(resId))

    fun showMessage(message: CharSequence) {
        message.toString().showSnackBar(context = activity)
    }

    fun showMessage(message: CharSequence, type: Int) {
        message.toString().showSnackBar(context = activity, type = type)
    }

    fun goBack() {
        if (activity is BaseActivity<*>) (activity as BaseActivity<*>).goBack()
    }

    fun showProgressDialog(isCheckNetwork: Boolean = true,
                           isSetTitle: Boolean,
                           title: String){
        (activity as BaseActivity<*>).showProgressDialog(isCheckNetwork, isSetTitle, title)
    }

    fun hideProgressDialog(){
        (activity as BaseActivity<*>).hideProgressDialog()
    }

    fun checkInternet(): Boolean {
        return when {
            (activity as BaseActivity<*>).checkInternet() -> true
            else -> {
                showMessage(getString(R.string.network_connection_error))
                false
            }
        }
    }

    protected abstract fun setDataBindingLayout()

    @LayoutRes
    protected abstract fun provideLayoutId(): Int

    protected abstract fun injectDependencies(fragmentComponent: FragmentComponent)

    protected abstract fun setupView(view: View)

    fun setFireBaseAnalyticsData(id: String, name: String, contentType: String) {
        (activity as BaseActivity<*>).setFireBaseAnalyticsData(id, name, contentType)
    }

   // fun handleApiError(settings: Settings?) = (activity as BaseActivity<*>).handleApiError(settings)


    /**
     * Called Ads function when returning to the activity
     */
    override fun onResume() {
        super.onResume()
        logger.debugEvent("onResume called", "Fragment resumed")
        Log.i(this::class.java.simpleName, "onResume: ")
    }

    /**
     * Called Ads function when leaving the activity
     */
    override fun onPause() {

        super.onPause()
        logger.debugEvent("onPause called", "Fragment paused")
        Log.i(this::class.java.simpleName, "onPause: ")
    }

    /**
     * Called Ads function before the activity is destroyed
     */
    override fun onDestroy() {

        super.onDestroy()
        logger.debugEvent("onDestroy called", "Fragment destroyed")
        Log.i(this::class.java.simpleName, "onDestroy: ")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        logger.debugEvent("onDestroyView called", "Fragment destroyed")
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
            adView!!.setBackgroundColor(getResources().getColor(R.color.transparent))
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
                Handler().postDelayed({ interstitialAd.loadAd() }, delayMillis)
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

//    fun showBannerAd(context: Context, containerView: MoPubView) {
//
//        if (sharedPreference.isAdRemoved || sharedPreference.androidBannerId.equals(IConstants.EMPTY_LOADING_MSG) || !AppConfig.BANNER_AD ) {
//            containerView.removeAllViews()
//            containerView.visibility = View.GONE
//        }
//        else
//        {
//            moPubBannerView = containerView
//            if (moPubBannerView != null) {
//                moPubBannerView!!.setAdUnitId(getMoPubBannerAdId())
//                moPubBannerView!!.bannerAdListener = object : MoPubView.BannerAdListener {
//                    override fun onBannerExpanded(banner: MoPubView?) {
//                        logger.debugEvent(this::class.java.simpleName, "banner ad is expanded")
//                    }
//
//                    override fun onBannerLoaded(banner: MoPubView) {
//                        logger.debugEvent(this::class.java.simpleName, "banner ad is loaded")
//                    }
//
//                    override fun onBannerCollapsed(banner: MoPubView?) {
//                        logger.debugEvent(this::class.java.simpleName, "banner ad is collapsed")
//                    }
//
//                    override fun onBannerFailed(banner: MoPubView?, errorCode: MoPubErrorCode?) {
//                        logger.debugEvent(
//                            this::class.java.simpleName,
//                            "banner has failed to retrieve an ad " + errorCode.toString()
//                        )
//                    }
//
//                    override fun onBannerClicked(banner: MoPubView?) {
//                        logger.debugEvent(this::class.java.simpleName, "banner ad is clicked by user.")
//                    }
//                }
//                moPubBannerView!!.loadAd(MoPubView.MoPubAdSize.HEIGHT_50)
//                containerView.visibility = View.VISIBLE
//            }
//        }
//
//
//    }
//
//    /**
//     * This method will show mopub interstitial ads
//     * @return Boolean
//     */
//    fun showInterstitial(context: Context) {
//        moPubInterstitial =
//            MoPubInterstitial(
//                context as Activity,
//                getMoPubInterstitialAdId()
//            )
//        moPubInterstitial!!.interstitialAdListener =
//            object : MoPubInterstitial.InterstitialAdListener {
//                override fun onInterstitialLoaded(interstitial: MoPubInterstitial?) {
//                    if (moPubInterstitial!!.isReady) {
//                        moPubInterstitial!!.show()
//                    }
//                    logger.debugEvent(
//                        this@BaseFragment::class.java.simpleName,
//                        "interstitial ad has been cached and is ready to be shown."
//                    )
//                }
//
//                override fun onInterstitialShown(interstitial: MoPubInterstitial?) {
//                    logger.debugEvent(
//                        this@BaseFragment::class.java.simpleName,
//                        "interstitial ad has been shown."
//                    )
//                }
//
//                override fun onInterstitialFailed(
//                    interstitial: MoPubInterstitial?,
//                    errorCode: MoPubErrorCode?
//                ) {
//                    logger.debugEvent(
//                        this@BaseFragment::class.java.simpleName,
//                        "interstitial ad has failed to load. " + errorCode.toString()
//                    )
//                }
//
//                override fun onInterstitialDismissed(interstitial: MoPubInterstitial?) {
//                    logger.debugEvent(
//                        this@BaseFragment::class.java.simpleName,
//                        "interstitial ad  has being dismissed."
//                    )
//                }
//
//                override fun onInterstitialClicked(interstitial: MoPubInterstitial?) {
//                    logger.debugEvent(
//                        this@BaseFragment::class.java.simpleName,
//                        "interstitial ad is clicked by user."
//                    )
//                }
//            }
//        moPubInterstitial!!.load()
//    }
//
//    /**
//     * This method will show banner ads
//     * @param adView AdView
//     */
//    fun showBannerAd(context: Context, container: RelativeLayout) {
//        adView = AdView(context)
//        adView!!.adUnitId = getAdMobBannerAdId()
//        adView!!.adSize = AdSize.BANNER
//        if (sharedPreference.isAdRemoved || sharedPreference.androidBannerId.equals("") || !AppConfig.BANNER_AD ) {
//            container.removeAllViews()
//            container.visibility = View.GONE
//        } else {
//            container.addView(adView)
//            val adRequest = AdRequest.Builder().build()
//            adView!!.adListener = object : AdListener() {
//                override fun onAdLoaded() {
//                    // Code to be executed when an ad finishes loading.
//                    logger.dumpCustomEvent("Banner Ad Loaded", "Ad load successful")
//                    Log.d(this::class.java.simpleName, "Ad Loaded")
//                }
//
//                override fun onAdFailedToLoad(adError: LoadAdError) {
//                    // Code to be executed when an ad request fails.
//                    val error = "domain: ${adError.domain}, code: ${adError.code}, " +
//                            "message: ${adError.message}"
//                    logger.dumpCustomEvent("Banner onAdFailedToLoad", "with error $error")
//                    Log.d(this::class.java.simpleName, "onAdFailedToLoad: " + "with error $error")
//                }
//
//                override fun onAdOpened() {
//                    // Code to be executed when an ad opens an overlay that
//                    // covers the screen.
//                    logger.dumpCustomEvent("Banner Ad Opened", "Ad Open event")
//                    Log.d(this::class.java.simpleName, "Ad Opened")
//                }
//
//                override fun onAdClicked() {
//                    // Code to be executed when the user clicks on an ad.
//                    logger.dumpCustomEvent("Banner Ad Clicked", "Ad Click event")
//                    Log.d(this::class.java.simpleName, "Ad Clicked")
//                }
//
//                override fun onAdClosed() {
//                    // Code to be executed when the user is about to return
//                    // to the app after tapping on an ad.
//                    logger.dumpCustomEvent("Banner Ad Closed", "Ad Close event")
//                    Log.d(this::class.java.simpleName, "Ad Closed")
//                }
//            }
//            adView!!.loadAd(adRequest)
//            container.visibility = View.VISIBLE
//            logger.debugEvent("Banner Ad Request", "Banner Ad Requested")
//        }
//    }
//
//    /**
//     * get banner id according to project debug level (api get config param)
//     * */
//    private fun getAdMobBannerAdId(): String {
//        return when {
//            sharedPreference.projectDebugLevel.equals("development", true) -> {
//                logger.dumpCustomEvent(
//                    "Banner Ad Test Unit Id ",
//                    getString(R.string.admob_banner_unit_id_test).substringAfter('/')
//                )
//                sharedPreference.androidBannerId!!
//               // getString(R.string.admob_banner_unit_id_test)
//            }
//            sharedPreference.androidBannerId.isNullOrEmpty() -> {
//                logger.dumpCustomEvent("Banner Ad Init", "Empty Banner Admob id from backend")
//                ""
//            }
//            else -> {
//                logger.dumpCustomEvent(
//                    "Banner Ad Server Unit Id ",
//                    sharedPreference.androidBannerId!!.substringAfter('/')
//                )
//                sharedPreference.androidBannerId!!
//            }
//        }
//    }
//
//    /**
//     * get banner id according to project debug level (api get config param)
//     * */
//    private fun getMoPubBannerAdId(): String {
//        return when {
//            sharedPreference.projectDebugLevel.equals("development", true) -> {
//                logger.dumpCustomEvent(
//                    "MO PUB Banner Ad ID",
//                    "Using Banner Ad Test Unit Id"
//                )
//                sharedPreference.androidMoPubBannerId!!
//               // getString(R.string.mopub_banner_unit_id_test)
//            }
//            sharedPreference.androidMoPubBannerId.isNullOrEmpty() -> {
//                logger.dumpCustomEvent("MO PUB Banner Ad Init", "Empty Banner MOPUB Ad Unit Id from backend")
//                ""
//            }
//            else -> {
//                sharedPreference.androidMoPubBannerId!!
//            }
//        }
//    }
//
//    /**
//     * get Interstitial id according to project debug level (api get config param)
//     * */
//    private fun getMoPubInterstitialAdId(): String {
//        return when {
//            sharedPreference.projectDebugLevel.equals("development", true) -> {
//                logger.dumpCustomEvent("MO PUB Interstitial Ad Test Unit Id ", "Using Test Unit ID")
//               // getString(R.string.mopub_full_screen_unit_id_test)
//                sharedPreference.androidMopubInterstitialId!!
//            }
//            sharedPreference.androidInterstitialId.isNullOrEmpty() -> {
//                logger.dumpCustomEvent("MO PUB Interstitial Ad Init", "Empty Interstitial MO PUB Ad Unit id from backend")
//                ""
//            }
//            else -> {
//                logger.dumpCustomEvent("MO PUB Interstitial Ad Server Unit Id ", sharedPreference.androidMopubInterstitialId!!.substringAfter('/'))
//                sharedPreference.androidMopubInterstitialId!!
//            }
//        }
//    }

}