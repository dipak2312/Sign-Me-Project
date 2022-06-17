package com.app.signme.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.signme.api.network.NetworkHelper
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.core.BaseViewModel
import com.app.signme.repository.ChatRepository
import io.reactivex.disposables.CompositeDisposable

class ChatViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val chatRepository: ChatRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()

    override fun onCreate() {
        checkForInternetConnection()
    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }
}