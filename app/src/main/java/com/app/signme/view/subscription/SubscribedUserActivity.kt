package com.app.signme.view.subscription

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivitySubscribedUserBinding
import com.app.signme.dataclasses.SubscriptionPlan
import com.app.signme.dataclasses.response.StaticPage
import com.app.signme.core.BaseActivity
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.view.settings.staticpages.StaticPagesMultipleActivity
import com.app.signme.viewModel.SettingsViewModel
import java.util.ArrayList

class SubscribedUserActivity : BaseActivity<SettingsViewModel>(), SubscriptionClickListener {

    lateinit var binding: ActivitySubscribedUserBinding
    private var selectedSubscriptionPlan: SubscriptionPlan? = null
    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscribed_user)
        binding.lifecycleOwner = this

        setFireBaseAnalyticsData(
            "id-subscriptionScreen",
            "view-subscriptionScreen",
            "view-subscriptionScreen"
        )


    }
    override fun setupView(savedInstanceState: Bundle?) {
        initListeners()
        addObserver()
        setBoldAndColorSpannable(
            binding.tvSubscriptionPolicyLinks,
            getString(R.string.terms_n_conditions),
            getString(R.string.eula_policy),
            getString(R.string.privacy_policy)
        )
        val user = sharedPreference.userDetail
        if (user?.subscription?.size!!>0) {
            if (user.subscription?.filter { it.subscriptionStatus == "1" }!!.isNotEmpty()) {
                binding.premiumUserViewPager.visibility = View.VISIBLE
                loadPremiumUserView()
            } else {
                startActivity(Intent(this@SubscribedUserActivity, SubscriptionPlansActivity::class.java))
                finish()
            }
        } else {
            startActivity(Intent(this@SubscribedUserActivity, SubscriptionPlansActivity::class.java))
            finish()
        }
    }

    /*
    * Load features as per Subscription plan */
    private fun loadPremiumUserView() {
        val viewPager: ViewPager = binding.premiumUserViewPager
        val user = sharedPreference.userDetail
        viewPager.adapter = CustomPagerAdapter(this@SubscribedUserActivity,user)
    }


    /**
     * Add Observers to listen changes in view model
     */
    private fun addObserver() {
        viewModel.statusCodeLiveData.observe(this) { serverError ->
            handleApiStatusCodeError(serverError)
        }

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
            btnUpgradeSubscribe.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Update subscription Button Click")

                startActivity(
                    SubscriptionPlansActivity.getStartIntent(
                        context = this@SubscribedUserActivity,
                        check_upgrade_downgrade = "1"
                    )
                )

            }
        }
    }

    /**
     * Set spannable text for TNC,Eula and Privacy Policy
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
                                val pageCodeList: ArrayList<StaticPage> = ArrayList()
                                pageCodeList.add(
                                    StaticPage(
                                        pageCode = SettingsActivity.STATIC_PAGE_TERMS_CONDITION,
                                        forceUpdate = false
                                    )
                                )
                                val intent =
                                    StaticPagesMultipleActivity.getStartIntent(
                                        this@SubscribedUserActivity,
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
                                val pageCodeList: ArrayList<StaticPage> = ArrayList()
                                pageCodeList.add(
                                    StaticPage(
                                        pageCode = SettingsActivity.STATIC_PAGE_PRIVACY_POLICY,
                                        forceUpdate = false
                                    )
                                )
                                val intent =
                                    StaticPagesMultipleActivity.getStartIntent(
                                        this@SubscribedUserActivity,
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
                }else if (portion.equals(getString(R.string.eula_policy), true)) {
                    spannableString1.setSpan(object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            if (checkInternet()) {
                                val pageCodeList: ArrayList<StaticPage> = ArrayList()
                                pageCodeList.add(
                                    StaticPage(
                                        pageCode = SettingsActivity.STATIC_PAGE_EULA_POLICY,
                                        forceUpdate = false
                                    )
                                )
                                val intent =
                                    StaticPagesMultipleActivity.getStartIntent(
                                        this@SubscribedUserActivity,
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
                            this@SubscribedUserActivity,
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

    override fun onSubscriptionClick(position:Int , data: SubscriptionPlan) {
        selectedSubscriptionPlan = data
    }


    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }


}