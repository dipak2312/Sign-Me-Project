package com.app.signme.view.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.android.billingclient.api.SkuDetails
import com.app.signme.BuildConfig
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.application.AppineersApplication.Companion.sharedPreference
import com.app.signme.commonUtils.common.CommonUtils
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.NotificationUtils
import com.app.signme.commonUtils.utility.dialog.RateUsDialog
import com.app.signme.core.BaseActivity
import com.app.signme.core.utility.DialogUtil
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivitySettingsBinding
import com.app.signme.dataclasses.response.StaticPage
import com.app.signme.view.settings.changepassword.ChangePasswordActivity
import com.app.signme.view.settings.feedback.SendFeedbackActivity
import com.app.signme.view.settings.staticpages.StaticPagesMultipleActivity
import com.app.signme.view.subscription.SubscribedUserActivity
import com.app.signme.view.subscription.SubscriptionPlansActivity
import com.app.signme.viewModel.SettingsViewModel

import com.hb.logger.Logger
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : BaseActivity<SettingsViewModel>() {

    companion object {
        const val STATIC_PAGE_ABOUT_US: String = "aboutus"
        const val STATIC_PAGE_TERMS_CONDITION: String = "termsconditions"
        const val STATIC_PAGE_PRIVACY_POLICY: String = "privacypolicy"
        const val STATIC_PAGE_EULA_POLICY: String = "eula"
        fun getStartIntent(mContext: Context): Intent {
            return Intent(mContext, SettingsActivity::class.java).apply {

            }
        }
    }

    private var binding: ActivitySettingsBinding? = null
    private var addFreeSKU: SkuDetails? = null

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        binding?.viewSetting = viewModel.settingConfig
        binding?.user = sharedPreference.userDetail
        binding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        binding?.tvLogs?.visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.GONE
        setFireBaseAnalyticsData("id-settingsscreen", "view-settingsscreen", "view-settingsscreen")

        initListeners()
        addObservers()
        //initStripeConnectionUI()

    }

    private fun initListeners() {
        binding.apply {

            btn_back.setOnClickListener {
                setFireBaseAnalyticsData("id_btnback", "click_btnback", "click_btnback")
                finish()
            }

            tvBlockedUser.setOnClickListener {
//                setFireBaseAnalyticsData("id-blocklist", "click_blocklist", "click_blocklist")
//                startActivity(Intent(this@SettingsActivity, BlokedUserActivity::class.java))
            }



            tbPushNotification.setOnClickListener {
                setFireBaseAnalyticsData("id-notification", "click_notification", "click_notification")
                logger.dumpCustomEvent(
                    IConstants.EVENT_CLICK,
                    "Push Notification Setting Button Click"
                )
                confirmOpenNotificationSettings()
            }
            tvGoAddFree.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Add Free Button Click")
                setFireBaseAnalyticsData(
                    "id-goadfree",
                    "click_goadfreeclick",
                    "click_goadfreeclick"
                )
                when {
                    checkInternet() -> viewModel.makePurchase(
                        this@SettingsActivity,
                        skuDetails = addFreeSKU
                    )
                }

            }
            tvSubscription.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Add Free Button Click")
                setFireBaseAnalyticsData(
                    "id-subcription",
                    "click_subcriptionclick",
                    "click_subcriptionclick"
                )
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Try Premium Button Click")
                startActivity(
                    Intent(
                        this@SettingsActivity,
                        SubscriptionPlansActivity::class.java
                    )
                )
//                if (user?.subscription != null && user.subscription?.size!! > 0) {
//                    if (user.subscription?.filter { it.subscriptionStatus == "1" }!!.isNotEmpty()) {
//                        startActivity(
//                            Intent(
//                                this@SettingsActivity,
//                                SubscribedUserActivity::class.java
//                            )
//                        )
//                    } else {
//                        startActivity(
//                            Intent(
//                                this@SettingsActivity,
//                                SubscriptionPlansActivity::class.java
//                            )
//                        )
//                    }
//                } else {
//                    startActivity(
//                        Intent(
//                            this@SettingsActivity,
//                            SubscriptionPlansActivity::class.java
//                        )
//                    )
//                }

            }
            tvShareApp.setOnClickListener {
                setFireBaseAnalyticsData("id_shareapp", "click_shareapp", "click_shareapp")
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Share App Button Click")
                openShareIntent()
            }

            /* tvEditProfile.setOnClickListener {
                 logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Edit Profile Button Click")
                 startActivity(Intent(activity, EditProfileActivity::class.java))
             }*/

            tvChangePassword.setOnClickListener {
                setFireBaseAnalyticsData("id-changepassword", "click_changepassword", "click_changepassword")
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Change Password Button Click")
                startActivity(Intent(this@SettingsActivity, ChangePasswordActivity::class.java))
            }

            tvRateApp.setOnClickListener {
                setFireBaseAnalyticsData("id-rateus", "click_rateus", "click_rateus")
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Rate App Button Click")
                RateUsDialog(this@SettingsActivity).show(
                    this@SettingsActivity.supportFragmentManager,
                    "Rating"
                )
            }

            tvDeleteAccount.setOnClickListener {
                setFireBaseAnalyticsData("id-deleteaccount", "click_deleteaccount", "click_deleteaccount")
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Delete Button Click")
                performDeleteAccount()
            }

            tvLogout.setOnClickListener {
                setFireBaseAnalyticsData("id-logout", "click_logout", "click_logout")
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Log out Button Click")
                performLogOut()
            }


            tvSendFeedback.setOnClickListener {
                setFireBaseAnalyticsData("id_sendfeedback", "click_sendfeedback", "click_sendfeedback")
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Report Problem Button Click")
                startActivity(Intent(this@SettingsActivity, SendFeedbackActivity::class.java))
            }

            tvAboutUs.setOnClickListener {
                setFireBaseAnalyticsData("id-aboutus", "click_aboutus", "click_aboutus")
                val pageCodeList: ArrayList<StaticPage> = ArrayList()
                pageCodeList.add(
                    StaticPage(
                        pageCode = STATIC_PAGE_ABOUT_US,
                        forceUpdate = false
                    )
                )
                val intent =
                    StaticPagesMultipleActivity.getStartIntent(this@SettingsActivity, pageCodeList)
                startActivity(intent)

            }

            tvPrivacyPolicy.setOnClickListener {
                setFireBaseAnalyticsData("id-privacypolicy", "click-privacypolicy", "click-privacypolicy")
                val pageCodeList: ArrayList<StaticPage> = ArrayList()
                pageCodeList.add(
                    StaticPage(
                        pageCode = STATIC_PAGE_PRIVACY_POLICY,
                        forceUpdate = false
                    )
                )
                val intent =
                    StaticPagesMultipleActivity.getStartIntent(this@SettingsActivity, pageCodeList)
                startActivity(intent)

            }
            tvEulaPolicy.setOnClickListener {
                val pageCodeList: ArrayList<StaticPage> = ArrayList()
                pageCodeList.add(
                    StaticPage(
                        pageCode = STATIC_PAGE_EULA_POLICY,
                        forceUpdate = false
                    )
                )
                val intent =
                    StaticPagesMultipleActivity.getStartIntent(this@SettingsActivity, pageCodeList)
                startActivity(intent)

            }



            tvTermsCondition.setOnClickListener {
                setFireBaseAnalyticsData("id-termsansconditions", "click-termsandcondition", "click-termsandcondition")
                val pageCodeList: ArrayList<StaticPage> = ArrayList()
                pageCodeList.add(
                    StaticPage(
                        pageCode = STATIC_PAGE_TERMS_CONDITION,
                        forceUpdate = false
                    )
                )
                val intent =
                    StaticPagesMultipleActivity.getStartIntent(this@SettingsActivity, pageCodeList)
                startActivity(intent)

            }
            tvLogs.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Report Problem Button Click")
                Logger.launchActivity()
            }
            tvVersion.text = "Version : " + BuildConfig.VERSION_NAME

        }

    }

    private fun addObservers() {
        viewModel.logoutLiveData.observe(this) {
            hideProgressDialog()
            if (it?.settings?.isSuccess == true) {
                viewModel.performLogout()
                navigateToLoginScreen()
            } /*else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(activity)
            } else {
                showMessage(it.settings?.message!!)
            }*/
        }

        viewModel.deleteAccountLiveData.observe(this) {
            hideProgressDialog()
            if (it?.settings?.isSuccess == true) {
                viewModel.performLogout()
                navigateToLoginScreen()
            }
        }


        viewModel.addFreeSKU.observe(this@SettingsActivity) {
            addFreeSKU = it
        }

        viewModel.orderReceiptJson.observe(this@SettingsActivity as BaseActivity<*>) {
            if (it.isNotEmpty()) {

                viewModel.callGoAdFree(it)
            }
        }

        viewModel.goAdFreeLiveData.observe(this) {
            hideProgressDialog()
            if (it?.settings?.isSuccess == true) {
                showMessage(it.settings?.message!!,IConstants.SNAKBAR_TYPE_SUCCESS)
                AppineersApplication.sharedPreference.isAdRemoved = true
                viewModel.settingConfig.showRemoveAdd = false
                binding?.viewSetting = viewModel.settingConfig
                binding?.executePendingBindings()
                //sharedPreference.goAdFree=true
                binding!!.tvGoAddFree.visibility=View.GONE
                //(AppineersApplication()).isAdRemoved.value = true
            }
        }

        (this@SettingsActivity?.application as AppineersApplication).isLogStatusUpdated.observe(
            this@SettingsActivity
        ) {
            showProgressDialog(
                isCheckNetwork = true,
                isSetTitle = false,
                title = IConstants.EMPTY_LOADING_MSG
            )
            showOrHideLogOption()
        }
        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            (this@SettingsActivity as BaseActivity<*>).handleApiStatusCodeError(serverError)
        }
    }


    /**
     * Navigate user to Login Screen if user not logged in or want to logout
     */
    private fun navigateToLoginScreen() {
        AppineersApplication.sharedPreference.userDetail = null
        AppineersApplication.sharedPreference.isLogin = false
        AppineersApplication.sharedPreference.authToken = ""
        AppineersApplication.sharedPreference.configDetails = null
        Logger.setUserInfo("")
        val intent = Intent(
            this@SettingsActivity,
            (this@SettingsActivity?.application as AppineersApplication).getLoginActivity()
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        this@SettingsActivity?.finish()
    }

    private fun showOrHideLogOption() {
        if (!BuildConfig.DEBUG) {
            if (AppineersApplication.sharedPreference.logStatusUpdated.equals(
                    IConstants.INACTIVE_LOG_STATUS,
                    true
                )
            ) {
                binding?.tvLogs?.visibility = View.GONE
            } else if (AppineersApplication.sharedPreference.logStatusUpdated.equals(
                    IConstants.ACTIVE_LOG_STATUS,
                    true
                )
            ) {
                binding?.tvLogs?.visibility = View.VISIBLE
            }
        }
        hideProgressDialog()
    }



    /**
     * Show confirmation message to delete account
     */
    private fun performDeleteAccount() {
        DialogUtil.alert(
            context = this@SettingsActivity,
            msg = getString(R.string.delete_account_alert),
            positiveBtnText = getString(R.string.label_yes_button),
            negativeBtnText = getString(R.string.label_no_button),
            il = object : DialogUtil.IL {
                override fun onSuccess() {
                    when {
                        checkInternet() -> {
                            showProgressDialog(
                                isCheckNetwork = true,
                                isSetTitle = false,
                                title = IConstants.EMPTY_LOADING_MSG
                            )
                            viewModel.callDeleteAccount()
                        }
                    }
                }

                override fun onCancel(isNeutral: Boolean) {}
            },
            isCancelable = false
        )
    }

    /**
     * Show confirmation message to log out
     */
    private fun performLogOut() {
        DialogUtil.alert(
            context = this@SettingsActivity, msg = getString(R.string.logout_alert),
            positiveBtnText = getString(R.string.ok), negativeBtnText = getString(R.string.cancel),
            il = object : DialogUtil.IL {
                override fun onSuccess() {
                    when {
                        checkInternet() -> {
                            showProgressDialog(
                                isCheckNetwork = true,
                                isSetTitle = false,
                                title = IConstants.EMPTY_LOADING_MSG
                            )
                            viewModel.callLogout()
                        }
                    }

                }

                override fun onCancel(isNeutral: Boolean) {}
            }, isCancelable = false
        )
    }

    /**
     * Share application
     */
    private fun openShareIntent() {
        //setFireBaseAnalyticsData("id-shareapp", "click_shareapp", "click_shareapp")
        val shareBody =
            "Try The QuickLook App on Playstore:\nhttps://play.google.com/store/apps/details?id=${this@SettingsActivity?.packageName}"
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            "Download ${this@SettingsActivity?.getString(R.string.app_name)}"
        )
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(
            Intent.createChooser(
                sharingIntent,
                resources.getString(R.string.share_using)
            )
        )
    }

    /**
     * Show confirmation message to opening system notfication settings
     */
    private fun confirmOpenNotificationSettings() {
        DialogUtil.alert(
            context = this@SettingsActivity,
            msg = getString(R.string.open_notification_settings_alert),
            positiveBtnText = getString(R.string.ok),
            negativeBtnText = getString(R.string.cancel),
            il = object : DialogUtil.IL {
                override fun onSuccess() {
                    CommonUtils.openAppNotificationSettings(this@SettingsActivity)
                }

                override fun onCancel(isNeutral: Boolean) {
                    binding?.tbPushNotification?.isChecked =
                        NotificationUtils.areNotificationsEnabled(
                            mContext = this@SettingsActivity,
                            application = this@SettingsActivity.application as AppineersApplication
                        )
                }
            },
            isCancelable = false
        )
    }

    override fun onResume() {
        super.onResume()
        binding?.tbPushNotification?.isChecked = NotificationUtils.areNotificationsEnabled(
            mContext = this@SettingsActivity,
            application = this@SettingsActivity.application as AppineersApplication
        )
    }
}