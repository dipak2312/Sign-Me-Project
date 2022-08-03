package com.app.signme.viewModel


import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.quicklook.dataclasses.NotificationCount
import com.app.quicklook.dataclasses.NotificationDel
import com.app.signme.api.network.NetworkHelper
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.core.BaseViewModel
import com.app.signme.dataclasses.UserNotification
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.request.DeleteCheckInRequestModel
import com.app.signme.repository.NotificationRepository
import com.google.gson.Gson
import com.google.gson.JsonElement
import io.reactivex.disposables.CompositeDisposable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody


class NotificationViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val application: Application,
    private val notificationRepository: NotificationRepository,
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    override fun onCreate() {
        checkForInternetConnection()
    }

    val NotificationLiveData = MutableLiveData<TAListResponse<UserNotification>>()
    var NotificationCountLiveData = MutableLiveData<TAListResponse<NotificationCount>>()
    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    val deleteNotificationLiveData=MutableLiveData<TAListResponse<JsonElement>>()
    val deleteUserNotificationLiveData = MutableLiveData<TAListResponse<JsonElement>>()
    val readNotificationLiveData = MutableLiveData<TAListResponse<NotificationDel>>()
    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

    fun callGetNotification() {
        compositeDisposable.addAll(
            notificationRepository.callGetNotification()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        NotificationLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    fun callGetNotificationCount() {
        compositeDisposable.addAll(
            notificationRepository.callGetNotificationCount()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        NotificationCountLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }


    fun readNotification(map: HashMap<String, String>) {
        compositeDisposable.addAll(
            notificationRepository.readNotificationList(map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        readNotificationLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    fun getDeleteNotificationCall(notiId: String) {
        val map: JsonElement = Gson().toJsonTree(DeleteCheckInRequestModel(notificationId = notiId))
        val body: RequestBody =
            RequestBody.create("application/json".toMediaTypeOrNull(), map.toString())
        compositeDisposable.addAll(
            notificationRepository.callDeleteNotificationRequest(body)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        deleteUserNotificationLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }
}