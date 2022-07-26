package com.app.signme.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.signme.api.network.NetworkHelper
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.commonUtils.utility.extension.getStringRequestBody
import com.app.signme.dataclasses.VersionConfigResponse
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.GoogleReceipt
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.core.BaseViewModel
import com.app.signme.dataclasses.LikeUnLikeResponse
import com.app.signme.dataclasses.OtherUserDetailsResponse
import com.app.signme.dataclasses.SwiperViewResponse
import com.app.signme.repository.HomeRepository
import com.app.signme.repository.inappbilling.BillingRepository
import com.google.gson.JsonElement
import io.reactivex.disposables.CompositeDisposable

class HomeViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val billingRepository: BillingRepository,
    private val homeRepository: HomeRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    val configParamsPhoneLiveData = MutableLiveData<TAListResponse<VersionConfigResponse>>()
    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()

    var orderReceiptJsonForOneTime = MutableLiveData<String>()
    var orderReceiptJsonForSubscription = MutableLiveData<String>()
    var orderReceiptJsonForUpgradeDowngradeSubscription = MutableLiveData<String>()
    var buySubscriptionLiveData = MutableLiveData<TAListResponse<LoginResponse>>()
    val swiperListLiveData= MutableLiveData<TAListResponse<SwiperViewResponse>>()
    val otherUserDetailsLiveData= MutableLiveData<TAListResponse<OtherUserDetailsResponse>>()
    val userLikeSuperLikeLiveData= MutableLiveData<TAListResponse<LikeUnLikeResponse>>()
    val unMatchUserLiveData= MutableLiveData<TAListResponse<JsonElement>>()

    override fun onCreate() {
        checkForInternetConnection()
    }
    init {

        orderReceiptJsonForOneTime = billingRepository.orderReceiptJsonForOneTime
        orderReceiptJsonForSubscription = billingRepository.orderReceiptJsonForSubscription
        orderReceiptJsonForUpgradeDowngradeSubscription = billingRepository.orderReceiptJsonForUpgradeDowngradeSubscription

    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

    /**
     * Api call for send transaction data on server
     * @param receiptData In-App purchase receipt data
     */


    fun callBuySubscription(receiptData: GoogleReceipt?) {
        val map = HashMap<String, okhttp3.RequestBody>()
        if (receiptData != null) {

            map["subscription_id"] = getStringRequestBody(receiptData.productId)
            map["receipt_type"] = getStringRequestBody(receiptData.receiptType)
            map["purchase_token"] = getStringRequestBody(receiptData.purchaseToken)
            map["PACKAGE_NAME"] = getStringRequestBody(receiptData.packageName)
        }
        compositeDisposable.addAll(
            homeRepository.callBuySubscription(map)
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

    fun getSwiperList(pageIndex: String?,signId:String?) {
        compositeDisposable.addAll(
            homeRepository.getSwiperList(pageIndex,signId)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        swiperListLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    fun callOtherUserDetailsList(otherUserId: String?) {
        compositeDisposable.addAll(
            homeRepository.callGetOtherUserDetailsList(otherUserId)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        otherUserDetailsLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    fun callLikeSuperLikeCancel(map:HashMap<String,String>) {
        compositeDisposable.addAll(
            homeRepository.callLikeSuperlikeCancel(map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        userLikeSuperLikeLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    fun unMatchUser(userId:String) {
        compositeDisposable.addAll(
            homeRepository.unMatchUser(userId)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        unMatchUserLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

}