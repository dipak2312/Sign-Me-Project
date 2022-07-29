package com.app.signme.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.signme.api.network.NetworkHelper
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.core.BaseViewModel
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.BlockUnblockResponse
import com.app.signme.repository.ChatRepository
import com.google.gson.JsonElement
import io.reactivex.disposables.CompositeDisposable

class ChatViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val chatRepository: ChatRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    val blockUnblockUserLiveData = MutableLiveData<TAListResponse<BlockUnblockResponse>>()

    override fun onCreate() {
        checkForInternetConnection()
    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }


    /**
     * Api call for send push if receiver offline
     */
//    fun callSendChatNotificationApi(map: HashMap<String, String>) {
//        compositeDisposable.addAll(
//            chatRepository.callSendChatNotificationApi(
//                map
//            )
//                .subscribeOn(schedulerProvider.io())
//                .subscribe(
//                    { response ->
//                        //  notificationLiveData.postValue(response)
//                    },
//                    { error ->
//                        //   statusCodeLiveData.postValue(handleServerError(error))
//                    }
//                )
//        )
//    }

    fun callBlockUser(map: HashMap<String, String>) {
        compositeDisposable.addAll(
            chatRepository.callBlockUser(map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->

                        blockUnblockUserLiveData.postValue(response)
                    },
                    { error ->

                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

}