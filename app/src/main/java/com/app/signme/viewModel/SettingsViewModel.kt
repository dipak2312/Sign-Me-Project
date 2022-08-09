package com.app.signme.viewModel

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.SkuDetails
import com.app.signme.BuildConfig
import com.app.signme.api.network.NetworkHelper
import com.app.signme.application.AppineersApplication
import com.app.signme.application.AppineersApplication.Companion.sharedPreference
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.getStringRequestBody
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.GoogleReceipt
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.core.AppConfig
import com.app.signme.core.BaseViewModel
import com.app.signme.repository.SettingsRepository
import com.app.signme.core.SettingViewConfig
import com.app.signme.dataclasses.BlockedUser
import com.app.signme.dataclasses.VersionConfigResponse
import com.app.signme.dataclasses.response.BlockUnblockResponse
import com.app.signme.repository.inappbilling.BillingRepository
import com.google.gson.JsonElement
import io.reactivex.disposables.CompositeDisposable
import okhttp3.RequestBody

class SettingsViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val billingRepository: BillingRepository,
    private val settingsRepository: SettingsRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    var subscriptionSKUList = MutableLiveData<List<SkuDetails>>()
    val logoutLiveData = MutableLiveData<TAListResponse<JsonElement>>()
    val deleteAccountLiveData = MutableLiveData<TAListResponse<JsonElement>>()
    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    var goAdFreeLiveData = MutableLiveData<TAListResponse<JsonElement>>()
    var addFreeSKU = MutableLiveData<SkuDetails>()
    val configParamsLiveData = MutableLiveData<TAListResponse<VersionConfigResponse>>()
    var orderReceiptJson = MutableLiveData<String>()
    var settingConfig: SettingViewConfig
    var inAppSKUList = MutableLiveData<List<SkuDetails>>()
    var orderReceiptJsonForOneTime = MutableLiveData<String>()
    var orderReceiptJsonForSubscription = MutableLiveData<String>()
    var orderReceiptJsonForUpgradeDowngradeSubscription = MutableLiveData<String>()
    var buySubscriptionLiveData = MutableLiveData<TAListResponse<LoginResponse>>()
    val blockedUserLiveData = MutableLiveData<TAListResponse<BlockedUser>>()
    val unblockUserLiveData = MutableLiveData<TAListResponse<BlockUnblockResponse>>()



    override fun onCreate() {
        checkForInternetConnection()
    }

    init {
        billingRepository.startDataSourceConnections()
        addFreeSKU = billingRepository.addFreeSKU
        orderReceiptJson = billingRepository.orderReceiptJson
        subscriptionSKUList = billingRepository.subscriptionSKUList
        inAppSKUList = billingRepository.inAppSKUList
        orderReceiptJsonForOneTime = billingRepository.orderReceiptJsonForOneTime
        orderReceiptJsonForSubscription = billingRepository.orderReceiptJsonForSubscription
        orderReceiptJsonForUpgradeDowngradeSubscription = billingRepository.orderReceiptJsonForUpgradeDowngradeSubscription
        settingConfig = setUpSettingConfig()
    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

    fun callLogout() {
        compositeDisposable.addAll(
            settingsRepository.callLogout()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        logoutLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    fun callDeleteAccount() {
        compositeDisposable.addAll(
            settingsRepository.callDeleteAccount()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        deleteAccountLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }


    /**
     * Api call for getting config parameters
     */
    fun callGetConfigParameters() {

        compositeDisposable.addAll(
            settingsRepository.callConfigParameters()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        configParamsLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    /**
     * Code to set up all settings config of user
     */
    private fun setUpSettingConfig(): SettingViewConfig {
        val user = sharedPreference.userDetail
        return SettingViewConfig().apply {
            showNotification = !sharedPreference.isSkip
            showRemoveAdd =
                ((AppConfig.BANNER_AD || AppConfig.INTERSTITIAL_AD) && !sharedPreference.isAdRemoved && !sharedPreference.isSkip)
            showEditProfile = !sharedPreference.isSkip
            showChangePassword = !(user?.isSocialLogin() == true || sharedPreference.isSkip)

              /*  .equals(IConstants.LOGIN_TYPE_PHONE, true) ||
                    (AppineersApplication()).getApplicationLoginType().equals(
                        IConstants.LOGIN_TYPE_PHONE_SOCIAL,
                        true
                    )) && !sharedPreference.isSkip*/
            showChangePhone = ((AppineersApplication()).getApplicationLoginType()
                .equals(IConstants.LOGIN_TYPE_PHONE, true))
                    && !sharedPreference.isSkip
            showDeleteAccount = !sharedPreference.isSkip
            showSendFeedback = !sharedPreference.isSkip
            showLogOut = !sharedPreference.isSkip
            appVersion = "Version: " + BuildConfig.VERSION_NAME
        }
    }

    fun makePurchase(activity: Activity?, skuDetails: SkuDetails?,purchaseToken:String="",oldSku:String="" ) {

        if (activity != null && skuDetails != null&&purchaseToken.isEmpty())
            billingRepository.launchBillingFlow(activity, skuDetails = skuDetails)
        if (activity != null && skuDetails != null&&purchaseToken.isNotEmpty())
            billingRepository.launchBillingFlowUpgrading(activity, skuDetails = skuDetails,purchaseToken = purchaseToken,oldSku = oldSku)

    }



    fun callGoAdFree(receiptData: String) {
        val map = HashMap<String, String>()
        map["one_time_transaction_data"] = receiptData
        compositeDisposable.addAll(
            settingsRepository.callGoAdFree(map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        goAdFreeLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    /**
     * Clear all user saved data
     */
    fun performLogout() {
        sharedPreference.userDetail = null
        sharedPreference.isLogin = false
        sharedPreference.authToken = ""
        sharedPreference.configDetails = null
        sharedPreference.isAdditionalInfo = false
        sharedPreference.isDoNotShowMeAgainClicked = false
    }

    /**
     * Api call for send transaction data on server
     * @param receiptData In-App purchase receipt data
     */


    fun callGetBlockedUser() {

        compositeDisposable.addAll(
            settingsRepository.callGetBlockedUser()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->

                        blockedUserLiveData.postValue(response)
                    },
                    { error ->

                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    fun callBlockUser(block_id: String,block_type:String) {
        val map = java.util.HashMap<String, String>()
        map["block_user_id"] = block_id
        compositeDisposable.addAll(
            settingsRepository.callBlockUser(map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->

                        unblockUserLiveData.postValue(response)
                    },
                    { error ->

                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    fun callBuyRegulerSubscription(receiptData: GoogleReceipt?) {
        val map = HashMap<String, RequestBody>()
        if (receiptData != null) {

            map["subscription_id"] = getStringRequestBody(receiptData.productId)
            map["receipt_type"] = getStringRequestBody(receiptData.receiptType)
            map["purchase_token"] = getStringRequestBody(receiptData.purchaseToken)
            map["PACKAGE_NAME"] = getStringRequestBody(receiptData.packageName)
            map["subscription_type"] = getStringRequestBody(IConstants.REGULER)
        }
        compositeDisposable.addAll(
            settingsRepository.callBuyRegulerSubscription(map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        buySubscriptionLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    fun callBuyGoldenSubscription(receiptData: GoogleReceipt?) {
        val map = HashMap<String, RequestBody>()
        if (receiptData != null) {

            map["subscription_id"] = getStringRequestBody(receiptData.productId)
            map["receipt_type"] = getStringRequestBody(receiptData.receiptType)
            map["purchase_token"] = getStringRequestBody(receiptData.purchaseToken)
            map["PACKAGE_NAME"] = getStringRequestBody(receiptData.packageName)
            map["subscription_type"] = getStringRequestBody(IConstants.GOLDEN)
        }
        compositeDisposable.addAll(
            settingsRepository.callBuyGoldenSubscription(map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        buySubscriptionLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }


}