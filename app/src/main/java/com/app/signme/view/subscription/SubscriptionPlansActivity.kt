package com.app.signme.view.subscription

import android.annotation.SuppressLint
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.SkuDetails


import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.RemoteIdlingResource
import com.app.signme.commonUtils.utility.extension.getJsonDataFromAsset
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.commonUtils.utility.extension.showSnackBar
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivitySubscriptionPlansBinding
import com.app.signme.dataclasses.SubscriptionPlan
import com.app.signme.dataclasses.response.GoogleReceipt
import com.app.signme.dataclasses.response.StaticPage
import com.app.signme.core.BaseActivity
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.view.settings.staticpages.StaticPagesMultipleActivity
import com.app.signme.viewModel.SettingsViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_subscription_plans.*


class SubscriptionPlansActivity : BaseActivity<SettingsViewModel>(), SubscriptionClickListener {


    lateinit var binding: ActivitySubscriptionPlansBinding
    private lateinit var subscriptionPlanListAdapter: SubscriptionViewAdapter
    private var subscriptionSKUList: List<SkuDetails> = arrayListOf()
    private var inAppSKUList: List<SkuDetails> = arrayListOf()
    private var selectedSubscriptionPlan: SubscriptionPlan? = null
    private val user = sharedPreference.userDetail
    private var flag_upgrade_downgrade = "0"
    private var purchaseToken = ""
    private var oldSku = ""
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
        initView()
    }


    /**
     * Initialize view
     */
    private fun initView() {
        initListeners()
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
                initRecycleView()
                addObserver()
                loadSubscriptionPlans()
                binding.apply {
                    rvSubscriptionPlanList.visibility = View.VISIBLE
                    llNoData.visibility = View.GONE
                    premiumUserViewPager.visibility = View.GONE
                    btnSubscribe.visibility = View.VISIBLE
                    tvHeaderTitle.setText(getString(R.string.upgrade_and_downgrade))
                }

            } else {
                viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
                initRecycleView()
                addObserver()
                loadSubscriptionPlans()
                binding.rvSubscriptionPlanList.visibility = View.VISIBLE
                binding.llNoData.visibility = View.GONE
                binding.premiumUserViewPager.visibility = View.GONE
                binding.btnSubscribe.visibility = View.VISIBLE
            }
        } else {
            viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
            initRecycleView()
            addObserver()
            loadSubscriptionPlans()
            binding.rvSubscriptionPlanList.visibility = View.VISIBLE
            binding.llNoData.visibility = View.GONE
            binding.premiumUserViewPager.visibility = View.GONE
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
        setSubscriptionListData(subscriptionPlanList)
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
     * Init recycle view
     *
     */
    private fun initRecycleView() {
        subscriptionPlanListAdapter = SubscriptionViewAdapter(
            context = this@SubscriptionPlansActivity,
            list = ArrayList<SubscriptionPlan>(),
            flag_upgrade_downgrade,
            subscriptionClickListener = this@SubscriptionPlansActivity
        )
        // val layoutManager = GridLayoutManager(this@SubscriptionPlansActivity, numberOfColumns)

        val layoutManager = LinearLayoutManager(this@SubscriptionPlansActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        /* layoutManager.spanSizeLookup = object : SpanSizeLookup() {
             override fun getSpanSize(position: Int): Int {
                 return if (subscriptionPlanListAdapter != null) {
                     when (subscriptionPlanListAdapter.getItemViewType(position)) {
                         1 -> 1
                         2 -> 2 //number of columns of the grid
                         else -> -1
                     }
                 } else {
                     -1
                 }
             }
         }*/
        binding.rvSubscriptionPlanList.layoutManager = layoutManager
        binding.rvSubscriptionPlanList.adapter = subscriptionPlanListAdapter
    }


    /**
     * Add Observers to listen changes in view model
     */
    private fun addObserver() {
        viewModel.subscriptionSKUList.observe(this, {
            subscriptionSKUList = it
            if (subscriptionSKUList.isNotEmpty()) {
                subscriptionSKUList.forEach {
                    logger.dumpCustomEvent(
                        IConstants.EVENT_PURCHASED,
                        "Product Description: " + it.originalJson
                    )
                }
            }
        })


        viewModel.inAppSKUList.observe(this, {
            inAppSKUList = it
            if (inAppSKUList.isNotEmpty()) {
                inAppSKUList.forEach {
                    logger.dumpCustomEvent(
                        IConstants.EVENT_PURCHASED,
                        "Product Description: " + it.originalJson
                    )
                }
            }
        })

        viewModel.orderReceiptJsonForOneTime.observe(this@SubscriptionPlansActivity, {
            if (it.isNotEmpty()) {
                val receiptData = Gson().fromJson(it, GoogleReceipt::class.java)
                logger.dumpCustomEvent(IConstants.EVENT_PURCHASED, "Order Receipt: $it")
                (application as AppineersApplication).isSubscriptionTaken.value = true
                sharedPreference.subscriptionItemSelected = selectedSubscriptionPosition
                navigateToHomeScreen()


            }
        })

        viewModel.orderReceiptJsonForUpgradeDowngradeSubscription.observe(
            this@SubscriptionPlansActivity,
             {
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
            })

        viewModel.orderReceiptJsonForSubscription.observe(this@SubscriptionPlansActivity, {
            if (it.isNotEmpty()) {
                val receiptData = Gson().fromJson(it, GoogleReceipt::class.java)
                logger.dumpCustomEvent(IConstants.EVENT_PURCHASED, "Order Receipt: $it")
                if (!flag_upgrade_downgrade.equals("1")) {
                    viewModel.callBuySubscription(receiptData)
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
        })
        viewModel.buySubscriptionLiveData.observe(this@SubscriptionPlansActivity, {
            if (it.settings?.isSuccess == true) {
                if (it.data != null) {
                    val subscription = it.data?.get(0)?.subscription
                    (application as AppineersApplication).isSubscriptionTaken.value =
                        it.data!![0].isSubscriptionTaken()
                    val userDetails = AppineersApplication.sharedPreference.userDetail
                    if (userDetails != null && subscription != null && subscription.size >= 0) {
                        userDetails.subscription = subscription
                        sharedPreference.userDetail = userDetails

                    }

                }

                sharedPreference.subscriptionItemSelected = selectedSubscriptionPosition
                navigateToHomeScreen()
            } else {
                it.settings?.message?.showSnackBar(
                    this@SubscriptionPlansActivity,
                    IConstants.SNAKBAR_TYPE_ERROR,
                    duration = IConstants.SNAKE_BAR_SHOW_TIME_INT
                )

            }
        })
        viewModel.statusCodeLiveData.observe(this, { serverError ->
            handleApiStatusCodeError(serverError)
        })

    }

    /**
     * Set Entertainment List Data
     * @param it ArrayList<Entertainment>
     */
    private fun setSubscriptionListData(it: ArrayList<SubscriptionPlan>) {
        if (it.isNotEmpty()) {
            showData()
        } else {
            showNoData()
        }
        subscriptionPlanListAdapter.list = it
        subscriptionPlanListAdapter.notifyDataSetChanged()
    }

    private fun showData() {
        binding.rvSubscriptionPlanList.visibility = View.VISIBLE
        binding.llNoData.visibility = View.GONE
    }

    private fun showNoData() {
        binding.rvSubscriptionPlanList.visibility = View.GONE
        binding.llNoData.visibility = View.VISIBLE
    }

    /**
     * Initialise listeners to listen all click and other actions performed by user
     */
    private fun initListeners() {
        binding.apply {
            ivBack.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                finish()
            }

            btnSubscribe.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Subscribe Button Click")

                if (subscriptionSKUList.isNotEmpty() && selectedSubscriptionPlan != null) {
                    if (selectedSubscriptionPlan!!.planValidityInDays == "0" || selectedSubscriptionPlan!!.planValidityInDays == "7") {
                        inAppSKUList.forEach {
                            when (selectedSubscriptionPlan!!.planValidityInDays) {
                                "0" -> {
                                    if (it.sku == "com.app.reboundapp.life_time") {

                                        viewModel.makePurchase(
                                            this@SubscriptionPlansActivity,
                                            skuDetails = it,
                                            purchaseToken = purchaseToken,
                                            oldSku = oldSku
                                        )
                                    }
                                }
                                "7" -> {
                                    if (it.sku == "com.app.reboundapp.seven_days") {
                                        viewModel.makePurchase(
                                            this@SubscriptionPlansActivity,
                                            skuDetails = it,
                                            purchaseToken = purchaseToken,
                                            oldSku = oldSku
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        subscriptionSKUList.forEach {
                            when (selectedSubscriptionPlan!!.planValidityInDays) {
                                "30" -> {
                                    if (it.sku == "com.appineers.whitelabel.monthly") {
                                        viewModel.makePurchase(
                                            this@SubscriptionPlansActivity,
                                            skuDetails = it,
                                            purchaseToken = purchaseToken,
                                            oldSku = oldSku
                                        )
                                    }
                                }
                                "90" -> {
                                    if (it.sku == "com.appineers.whitelabel.3months") {
                                        viewModel.makePurchase(
                                            this@SubscriptionPlansActivity,
                                            skuDetails = it,
                                            purchaseToken = purchaseToken,
                                            oldSku = oldSku
                                        )
                                    }
                                }
                                "180" -> {
                                    if (it.sku == "com.appineers.whitelabel.6months") {
                                        viewModel.makePurchase(
                                            this@SubscriptionPlansActivity,
                                            skuDetails = it,
                                            purchaseToken = purchaseToken,
                                            oldSku = oldSku
                                        )
                                    }
                                }
                                "365" -> {
                                    if (it.sku == "yearly") {
                                        viewModel.makePurchase(
                                            this@SubscriptionPlansActivity,
                                            skuDetails = it,
                                            purchaseToken = purchaseToken,
                                            oldSku = oldSku
                                        )
                                    }
                                }
                            }
                        }
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
                            R.color.colorPrimary
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

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onSubscriptionClick(position: Int, data: SubscriptionPlan) {
        selectedSubscriptionPlan = data
        selectedSubscriptionPosition = position

        /**
         * On click of plan change the text content
         */

        if (selectedSubscriptionPlan?.planAmountForDisplay.equals("$14.99") && position == 0) {
            tvSubscriptionContent.text = resources.getText(R.string.str_subscription_msg_1_month)
        } else if (position == 1) {
            tvSubscriptionContent.text = resources.getText(R.string.str_subscription_msg_3_month)
        } else if (selectedSubscriptionPlan?.planAmountForDisplay.equals("$77.99") && position == 2) {
            tvSubscriptionContent.text = resources.getText(R.string.str_subscription_msg_6_month)
        } else if (selectedSubscriptionPlan?.planAmountForDisplay.equals("$119.99") && position == 3) {
            tvSubscriptionContent.text = resources.getText(R.string.str_subscription_msg_1_year)
        } else {
            print("other amount")
        }

        /**
         * On click of plan change view background
         */

        if (selectedSubscriptionPlan?.planAmountForDisplay.equals("$14.99") && position == 0) {
            view_one_month.background = resources.getDrawable(R.drawable.bg_dot_black)
            view_one_month.background.setTint(resources.getColor(R.color.white))
        } else {
            view_one_month.background = resources.getDrawable(R.drawable.bg_dot_grey)
        }
        if (position == 1) {
            view_three_month.background = resources.getDrawable(R.drawable.bg_dot_black)
            view_three_month.background.setTint(resources.getColor(R.color.white))
        } else {
            view_three_month.background = resources.getDrawable(R.drawable.bg_dot_grey)
        }
        if (selectedSubscriptionPlan?.planAmountForDisplay.equals("$77.99") && position == 2) {
            view_six_month.background = resources.getDrawable(R.drawable.bg_dot_black)
            view_six_month.background.setTint(resources.getColor(R.color.white))
        } else {
            view_six_month.background = resources.getDrawable(R.drawable.bg_dot_grey)
        }
        if (selectedSubscriptionPlan?.planAmountForDisplay.equals("$119.99") && position == 3) {
            view_one_year.background = resources.getDrawable(R.drawable.bg_dot_black)
            view_one_year.background.setTint(resources.getColor(R.color.white))
        } else {
            view_one_year.background = resources.getDrawable(R.drawable.bg_dot_grey)
        }


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


    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {

    }

}