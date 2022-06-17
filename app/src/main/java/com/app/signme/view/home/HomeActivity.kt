package com.app.signme.view.home


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.application.AppineersApplication.Companion.sharedPreference
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.core.BaseActivity
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityHomeBinding
import com.app.signme.dataclasses.Social
import com.app.signme.dataclasses.response.GoogleReceipt
import com.app.signme.view.chat.ChatFragment
import com.app.signme.view.Matches.MatchesFragment
import com.app.signme.view.profile.ProfileFragment
import com.app.signme.viewModel.HomeViewModel
import com.google.gson.Gson


class HomeActivity : BaseActivity<HomeViewModel>() {
    private lateinit var binding: ActivityHomeBinding


    companion object {
        const val TAG = "HomeActivity"

        fun getStartIntent(mContext: Context, social: Social): Intent {
            return Intent(mContext, HomeActivity::class.java).apply {
                putExtra("social", social)
            }
        }
    }

    private var social: Social? = null

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        social = intent.getParcelableExtra("social")
        if (social != null) {
            sharedPreference.socialUserDetails = social
        }

        setFireBaseAnalyticsData("id-homeScreen", "view_homeScreen", "view_homeScreen")
        binding.apply {
            bottomNavigation.setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.action_explore -> {
                        logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Home Button CLick")
                        if (supportFragmentManager.findFragmentById(R.id.frameContainer) is HomeFragment) {
                            true
                        } else {
                            setCurrentFragment(HomeFragment())
                            true
                        }
                    }
                    R.id.action_matches -> {
                        logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Friends Tab CLick")
                        if (supportFragmentManager.findFragmentById(R.id.frameContainer) is MatchesFragment) {
                            true
                        } else {
                            setCurrentFragment(MatchesFragment())
                            true
                        }
                    }
                    R.id.action_chat -> {
                        logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Message Tab Click")
                        if (supportFragmentManager.findFragmentById(R.id.frameContainer) is ChatFragment) {
                            true
                        } else {
                            setCurrentFragment(ChatFragment())
                            true
                        }
                    }

                    R.id.action_profile -> {
                        logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Settings Tab Click")
                        if (supportFragmentManager.findFragmentById(R.id.frameContainer) is ProfileFragment) {
                            true
                        } else {
                            setCurrentFragment(ProfileFragment())
                            true
                        }
                    }
                    else -> false
                }
            }
        }

        addObservers()
        binding.bottomNavigation.selectedItemId = R.id.action_explore
       // checkAdditionalInfo()
    }

    private fun addObservers() {
        viewModel.orderReceiptJsonForSubscription.observe(this@HomeActivity) {
            if (it.isNotEmpty()) {

                val receiptData = Gson().fromJson(it, GoogleReceipt::class.java)
                logger.dumpCustomEvent(IConstants.EVENT_PURCHASED, "Order Receipt: $it")
                when {
                    checkInternet() -> {
                        viewModel.callBuySubscription(receiptData)
                    }
                }

            }
        }
        viewModel.buySubscriptionLiveData.observe(this@HomeActivity) {
            if (it.settings?.isSuccess == true) {
                if (it.data != null) {
                    val subscription = it.data?.get(0)?.subscription
                    (application as AppineersApplication).isSubscriptionTaken.value =
                        it.data!![0].isSubscriptionTaken()
                    val userDetails = sharedPreference.userDetail
                    if (userDetails != null && subscription != null && subscription.size >= 0) {
                        userDetails.subscription = subscription
                        sharedPreference.userDetail = userDetails

                    }
                }

            } /*else {
                it.settings?.message?.showSnackBar(
                    this@HomeActivity,
                    IConstants.SNAKBAR_TYPE_ERROR,
                    duration = IConstants.SNAKE_BAR_SHOW_TIME_INT
                )
            }*/
        }
        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            handleApiStatusCodeError(serverError)
        }

    }


    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameContainer, fragment)
            commit()
        }
}