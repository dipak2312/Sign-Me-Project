package com.app.signme.view.subscription


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.SkuDetails
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.RemoteIdlingResource
import com.app.signme.commonUtils.utility.extension.getJsonDataFromAsset
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.commonUtils.utility.extension.showSnackBar
import com.app.signme.core.BaseActivity
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivitySubscriptionPlansBinding
import com.app.signme.dataclasses.SubscriptionPlan
import com.app.signme.dataclasses.VersionConfigResponse
import com.app.signme.dataclasses.response.GoogleReceipt
import com.app.signme.dataclasses.response.StaticPage
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.view.settings.staticpages.StaticPagesMultipleActivity
import com.app.signme.viewModel.SettingsViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class SubscriptionPlansActivity : BaseActivity<SettingsViewModel>() {


    lateinit var binding: ActivitySubscriptionPlansBinding
    private lateinit var subscriptionPlanListAdapter: SubscriptionViewAdapter
    private var subscriptionSKUList: List<SkuDetails> = arrayListOf()
    private var inAppSKUList: List<SkuDetails> = arrayListOf()
    private val user = sharedPreference.userDetail
    private var flag_upgrade_downgrade = "0"
    private var purchaseToken = ""
    private var oldSku = ""
    var subscriptionType: String = IConstants.REGULAR
    private var selectedSubscriptionPosition = 0


    companion object {
        fun getStartIntent(context: Context, check_upgrade_downgrade: String): Intent {
            return Intent(context, SubscriptionPlansActivity::class.java).apply {
                putExtra(IConstants.CHECK_UPGRADE_DOWNGRADE, check_upgrade_downgrade)
            }
        }
    }

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscription_plans)
        binding.lifecycleOwner = this

        setFireBaseAnalyticsData(
            "id-subscriptionScreen",
            "view-subscriptionScreen",
            "view-subscriptionScreen"
        )


        intent?.apply {
            if (intent != null && intent.extras != null) {
                if (intent.extras!!.containsKey(IConstants.CHECK_UPGRADE_DOWNGRADE)) {
                    flag_upgrade_downgrade =
                        intent.extras?.getString(IConstants.CHECK_UPGRADE_DOWNGRADE)!!


                }
            }
        }

    }


    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {

        initView()
        initListeners()
        enableDisableUpgradeSubscription(IConstants.REGULAR)


    }

    fun enableDisableUpgradeSubscription(status: String) {
        if (sharedPreference.configDetails!!.subscription.isNullOrEmpty()) {
            enableDisableButton(binding!!.btnSubscribe, true)
        } else {
            when (status) {
                IConstants.REGULAR -> {

                    if (sharedPreference.configDetails!!.subscription!![0].subscriptionType.equals(
                            IConstants.REGULAR
                        ) && sharedPreference.configDetails!!.subscription!![0].subscriptionStatus.equals(
                            "1"
                        )
                    ) {
                        enableDisableButton(binding!!.btnSubscribe, false)
                    } else {
                        enableDisableButton(binding!!.btnSubscribe, true)
                    }
                }

                IConstants.GOLDEN -> {

                    if (sharedPreference.configDetails!!.subscription!![0].subscriptionType.equals(
                            IConstants.GOLDEN
                        ) && sharedPreference.configDetails!!.subscription!![0].subscriptionStatus.equals(
                            "1"
                        )
                    ) {

                        enableDisableButton(binding!!.btnSubscribe, false)
                    } else {
                        enableDisableButton(binding!!.btnSubscribe, true)
                    }
                }
            }
        }
    }

    /**
     * Initialize view
     */
    private fun initView() {

        setBoldAndColorSpannable(
            binding.tvSubscriptionPolicyLinks,
            getString(R.string.terms_n_conditions),
            getString(R.string.eula_policy),
            getString(R.string.privacy_policy)
        )
        //loadSubscriptionViewPager()

        if (user?.subscription != null && user.subscription?.size!! > 0) {
            if (user.subscription?.filter { it.subscriptionStatus == "1" }!!.size > 0) {
                viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
                addObserver()
                loadSubscriptionPlans()
                binding.apply {
                    btnSubscribe.visibility = View.VISIBLE

                }

            } else {
                viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
                //initRecycleView()
                addObserver()
                loadSubscriptionPlans()
                binding.btnSubscribe.visibility = View.VISIBLE
            }
        } else {
            viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
            // initRecycleView()
            addObserver()
            loadSubscriptionPlans()
            binding.btnSubscribe.visibility = View.VISIBLE
        }
    }

    /*  private fun loadSubscriptionViewPager() {
          val viewPager: ViewPager = binding.subscriptionViewPager
          val user = sharedPreference.userDetail
          viewPager.adapter = SubscriptionPagerAdapter(this@SubscriptionPlansActivity,user)
      }
  */
    /**
     * Load subscription plans
     *
     */

    private fun loadSubscriptionPlans() {
        var subscriptionPlanList: ArrayList<SubscriptionPlan> = ArrayList()
        subscriptionPlanList =
            getJsonListDataFromAsset(this@SubscriptionPlansActivity, "subscription_plan_list.json")
        // setSubscriptionListData(subscriptionPlanList)
    }

    /**
     * Get json list data from asset
     * @param context
     * @param fileName
     * @return
     */
    private fun getJsonListDataFromAsset(
        context: Context,
        fileName: String
    ): ArrayList<SubscriptionPlan> {
        val jsonFileString = getJsonDataFromAsset(context, fileName)
        val gson = Gson()
        val listReviewsType = object : TypeToken<ArrayList<SubscriptionPlan>>() {}.type
        val reviews: ArrayList<SubscriptionPlan> =
            gson.fromJson(jsonFileString, listReviewsType)
        reviews.forEachIndexed { idx, review -> Log.i("data", "> Item $idx:\n$review") }
        return reviews
    }


    /**
     * Add Observers to listen changes in view model
     */
    private fun addObserver() {
        viewModel.subscriptionSKUList.observe(this) {
            subscriptionSKUList = it
            if (subscriptionSKUList.isNotEmpty()) {
                subscriptionSKUList.forEach {
                    logger.dumpCustomEvent(
                        IConstants.EVENT_PURCHASED,
                        "Product Description: " + it.originalJson
                    )
                }
            }
        }


        viewModel.inAppSKUList.observe(this) {
            inAppSKUList = it
            if (inAppSKUList.isNotEmpty()) {
                inAppSKUList.forEach {
                    logger.dumpCustomEvent(
                        IConstants.EVENT_PURCHASED,
                        "Product Description: " + it.originalJson
                    )
                }
            }
        }

        viewModel.orderReceiptJsonForOneTime.observe(this@SubscriptionPlansActivity) {
            if (it.isNotEmpty()) {
                val receiptData = Gson().fromJson(it, GoogleReceipt::class.java)
                logger.dumpCustomEvent(IConstants.EVENT_PURCHASED, "Order Receipt: $it")
                (application as AppineersApplication).isSubscriptionTaken.value = true
                sharedPreference.subscriptionItemSelected = selectedSubscriptionPosition
                navigateToHomeScreen()


            }
        }

        viewModel.orderReceiptJsonForUpgradeDowngradeSubscription.observe(
            this@SubscriptionPlansActivity
        ) {
            if (it.isNotEmpty()) {
                it.showSnackBar(
                    this@SubscriptionPlansActivity,
                    type = IConstants.SNAKBAR_TYPE_SUCCESS,
                    duration = IConstants.SNAKE_BAR_SHOW_TIME_INT
                )

                Handler().postDelayed(
                    { navigateToHomeScreen() },
                    IConstants.SNAKE_BAR_PROFILE_SHOW_TIME
                )
                mIdlingResource?.setIdleState(true)

            }
        }

        viewModel.orderReceiptJsonForSubscription.observe(this@SubscriptionPlansActivity) {
            if (it.isNotEmpty()) {
                val receiptData = Gson().fromJson(it, GoogleReceipt::class.java)
                logger.dumpCustomEvent(IConstants.EVENT_PURCHASED, "Order Receipt: $it")
                if (!flag_upgrade_downgrade.equals("1")) {
                    when {
                        checkInternet() -> {
                            showProgressDialog(
                                isCheckNetwork = true,
                                isSetTitle = false,
                                title = IConstants.EMPTY_LOADING_MSG
                            )

                            if (subscriptionType.equals(IConstants.REGULAR)) {
                                viewModel.callBuyRegulerSubscription(receiptData)
                            } else {
                                viewModel.callBuyGoldenSubscription(receiptData)
                            }
                        }
                    }

                } else {
                    flag_upgrade_downgrade = "0"
                    if (user?.subscription != null && user.subscription?.size!! > 0) {
                        if (user.subscription?.filter { it.subscriptionStatus == "1" }!!
                                .isNotEmpty()
                        ) {
                            for (index in user.subscription!!.indices) {
                                if (user.subscription?.get(index)?.subscriptionStatus.equals("1")) {
                                    purchaseToken = user.subscription?.get(index)?.purchaseToken!!
                                    oldSku = user.subscription?.get(index)?.productId!!
                                }
                            }
                        }
                    }
                }

            }
        }
        viewModel.buySubscriptionLiveData.observe(this@SubscriptionPlansActivity) {
            // hideProgressDialog()
            if (it.settings?.isSuccess == true) {
                if (it.data != null) {

                    viewModel.callGetConfigParameters()

                }

                sharedPreference.subscriptionItemSelected = selectedSubscriptionPosition

            } else {
                it.settings?.message?.showSnackBar(
                    this@SubscriptionPlansActivity,
                    IConstants.SNAKBAR_TYPE_ERROR,
                    duration = IConstants.SNAKE_BAR_SHOW_TIME_INT
                )

            }
        }

        viewModel.configParamsLiveData.observe(this, Observer { it ->
            hideProgressDialog()
            if (it.settings?.isSuccess == true) {
                if (!it.data.isNullOrEmpty()) {
                    AppineersApplication.sharedPreference.configDetails =
                        it.data!!.get(0)
                    var configDefaultDetails: VersionConfigResponse? =
                        AppineersApplication.sharedPreference.configDetails
                    AppineersApplication.sharedPreference.isSubscription =
                        configDefaultDetails!!.isSubscriptionTaken()
                    AppineersApplication.sharedPreference.likeCount =
                        it.data!!.get(0).likeUserCount!!
                    AppineersApplication.sharedPreference.superLikeCount =
                        it.data!!.get(0).superLikeUserCount!!
                    (application as AppineersApplication).isSubscriptionTaken.value =
                        it.data!![0].isSubscriptionTaken()

                    finish()
                }
            }
        })
        viewModel.statusCodeLiveData.observe(this) { serverError ->
            handleApiStatusCodeError(serverError)
            hideProgressDialog()
        }

    }


    /**
     * Initialise listeners to listen all click and other actions performed by user
     */
    private fun initListeners() {
        binding.apply {
            btnBack.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                finish()
            }

            linRegularSubscription.setOnClickListener {
                subscriptionType = IConstants.REGULAR
                enableDisableUpgradeSubscription(IConstants.REGULAR)
                linRegularSubscription.setBackgroundResource(R.drawable.bg_subscription_select)
                linGoldenSubscription.setBackgroundResource(R.drawable.bg_button_blak_unselect)
                textUpgradeProfile.text = getString(R.string.label_Regular_subscription_text)
                textRegularPrice.setTextColor(
                    ContextCompat.getColor(
                        this@SubscriptionPlansActivity,
                        R.color.golden
                    )
                )
                textRegularMothly.setTextColor(
                    ContextCompat.getColor(
                        this@SubscriptionPlansActivity,
                        R.color.golden
                    )
                )
                textRegularSubscription.setTextColor(
                    ContextCompat.getColor(
                        this@SubscriptionPlansActivity,
                        R.color.golden
                    )
                )
                textGoldenPrice.setTextColor(
                    ContextCompat.getColor(
                        this@SubscriptionPlansActivity,
                        R.color.white
                    )
                )
                textGoldenMonthly.setTextColor(
                    ContextCompat.getColor(
                        this@SubscriptionPlansActivity,
                        R.color.white
                    )
                )
                textGoldenSubscription.setTextColor(
                    ContextCompat.getColor(
                        this@SubscriptionPlansActivity,
                        R.color.white
                    )
                )
                checkRegularTick.visibility = View.VISIBLE
                checkGoldenTick.visibility = View.INVISIBLE
                tick.setImageResource(R.drawable.ic_subscription_green_tick)
                tick1.setImageResource(R.drawable.ic_subscription_green_tick)
                textSubDesc.setTextColor(
                    ContextCompat.getColor(
                        this@SubscriptionPlansActivity,
                        R.color.white
                    )
                )
                textUpgradeProfile.setTextColor(
                    ContextCompat.getColor(
                        this@SubscriptionPlansActivity,
                        R.color.white
                    )
                )
            }

            linGoldenSubscription.setOnClickListener {
                subscriptionType = IConstants.GOLDEN
                enableDisableUpgradeSubscription(IConstants.GOLDEN)
                linGoldenSubscription.setBackgroundResource(R.drawable.bg_subscription_select)
                linRegularSubscription.setBackgroundResource(R.drawable.bg_button_blak_unselect)
                textUpgradeProfile.text = getString(R.string.label_golden_subscription_text)
                textRegularPrice.setTextColor(
                    ContextCompat.getColor(
                        this@SubscriptionPlansActivity,
                        R.color.white
                    )
                )
                textRegularMothly.setTextColor(
                    ContextCompat.getColor(
                        this@SubscriptionPlansActivity,
                        R.color.white
                    )
                )
                textRegularSubscription.setTextColor(
                    ContextCompat.getColor(
                        this@SubscriptionPlansActivity,
                        R.color.white
                    )
                )
                textGoldenPrice.setTextColor(
                    ContextCompat.getColor(
                        this@SubscriptionPlansActivity,
                        R.color.golden
                    )
                )
                textGoldenMonthly.setTextColor(
                    ContextCompat.getColor(
                        this@SubscriptionPlansActivity,
                        R.color.golden
                    )
                )
                textGoldenSubscription.setTextColor(
                    ContextCompat.getColor(
                        this@SubscriptionPlansActivity,
                        R.color.golden
                    )
                )
                checkRegularTick.visibility = View.INVISIBLE
                checkGoldenTick.visibility = View.VISIBLE
                tick.setImageResource(R.drawable.ic_subscription_light_orange_tick)
                tick1.setImageResource(R.drawable.ic_subscription_light_orange_tick)
                textSubDesc.setTextColor(
                    ContextCompat.getColor(
                        this@SubscriptionPlansActivity,
                        R.color.golden
                    )
                )
                textUpgradeProfile.setTextColor(
                    ContextCompat.getColor(
                        this@SubscriptionPlansActivity,
                        R.color.golden
                    )
                )
            }

            btnSubscribe.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Subscribe Button Click")

                if (inAppSKUList.isNotEmpty()) {
                    val selectedSku = inAppSKUList.find { it.sku == "android.test.purchased" }
                    if (selectedSku != null) {
                        this@SubscriptionPlansActivity.viewModel.makePurchase(
                            this@SubscriptionPlansActivity,
                            skuDetails = selectedSku
                        )
                    }
                }

            }
        }
    }

    /**
     * Set spannable text for TNC and Privacy Policy
     * @param textView TextView
     * @param portions Array<out String>
     */
    private fun setBoldAndColorSpannable(textView: TextView, vararg portions: String) {
        val label = textView.text.toString()
        val spannableString1 = SpannableString(label)
        for (portion in portions) {
            val startIndex = label.indexOf(portion)
            val endIndex = startIndex + portion.length
            try {
                if (portion.equals(getString(R.string.terms_n_conditions), true))
                    spannableString1.setSpan(object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            if (checkInternet()) {
                                val pageCodeList: java.util.ArrayList<StaticPage> =
                                    java.util.ArrayList()
                                pageCodeList.add(
                                    StaticPage(
                                        pageCode = SettingsActivity.STATIC_PAGE_TERMS_CONDITION,
                                        forceUpdate = false
                                    )
                                )
                                val intent =
                                    StaticPagesMultipleActivity.getStartIntent(
                                        this@SubscriptionPlansActivity,
                                        pageCodeList
                                    )
                                startActivity(intent)
                            } else {
                                showMessage(
                                    getString(R.string.network_connection_error)
                                )

                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {// override updateDrawState
                            ds.isUnderlineText = false // set to false to remove underline
                        }
                    }, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                else if (portion.equals(getString(R.string.privacy_policy), true)) {
                    spannableString1.setSpan(object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            if (checkInternet()) {
                                val pageCodeList: java.util.ArrayList<StaticPage> =
                                    java.util.ArrayList()
                                pageCodeList.add(
                                    StaticPage(
                                        pageCode = SettingsActivity.STATIC_PAGE_PRIVACY_POLICY,
                                        forceUpdate = false
                                    )
                                )
                                val intent =
                                    StaticPagesMultipleActivity.getStartIntent(
                                        this@SubscriptionPlansActivity,
                                        pageCodeList
                                    )
                                startActivity(intent)
                            } else {
                                showMessage(
                                    getString(R.string.network_connection_error)
                                )

                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {// override updateDrawState
                            ds.isUnderlineText = false // set to false to remove underline
                        }
                    }, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                } else if (portion.equals(getString(R.string.eula_policy), true)) {
                    spannableString1.setSpan(object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            if (checkInternet()) {
                                val pageCodeList: java.util.ArrayList<StaticPage> =
                                    java.util.ArrayList()
                                pageCodeList.add(
                                    StaticPage(
                                        pageCode = SettingsActivity.STATIC_PAGE_EULA_POLICY,
                                        forceUpdate = false
                                    )
                                )
                                val intent =
                                    StaticPagesMultipleActivity.getStartIntent(
                                        this@SubscriptionPlansActivity,
                                        pageCodeList
                                    )
                                startActivity(intent)
                            } else {
                                showMessage(
                                    getString(R.string.network_connection_error)
                                )

                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {// override updateDrawState
                            ds.isUnderlineText = false // set to false to remove underline
                        }
                    }, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }
                spannableString1.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            this@SubscriptionPlansActivity,
                            R.color.app_color
                        )
                    ), startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                spannableString1.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                textView.movementMethod = LinkMovementMethod.getInstance()
                textView.highlightColor = Color.TRANSPARENT
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        textView.text = spannableString1
    }


    @Nullable
    private var mIdlingResource: RemoteIdlingResource? = null

    /**
     * Only called from test, creates and returns a new [RemoteIdlingResource].
     */
    @VisibleForTesting
    @NonNull
    fun getIdlingResource(): RemoteIdlingResource {
        if (mIdlingResource == null) {
            mIdlingResource = RemoteIdlingResource()
        }

        return mIdlingResource as RemoteIdlingResource
    }


}