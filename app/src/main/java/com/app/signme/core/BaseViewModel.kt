package com.app.signme.core

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.signme.ServerError
import com.app.signme.api.network.NetworkHelper
import com.app.signme.commonUtils.common.Resource
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.dataclasses.ValidationResult
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel(
    protected val schedulerProvider: SchedulerProvider,
    protected val compositeDisposable: CompositeDisposable,
    protected val NetworkHelper: NetworkHelper
) : ViewModel() {

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    val messageStringId: MutableLiveData<Resource<Int>> = MutableLiveData()
    val messageString: MutableLiveData<Resource<String>> = MutableLiveData()
    val showDialog: MutableLiveData<Boolean> = MutableLiveData()
    val validationObserver : MutableLiveData<ValidationResult> = MutableLiveData()
    var statusCodeLiveData = MutableLiveData<ServerError>()


    protected fun checkInternetConnection(): Boolean = NetworkHelper.isNetworkConnected()

   /* protected fun handleNetworkError(err: Throwable?) =
        err?.let {
            NetworkHelper.castToNetworkError(it).run {
                when (status) {
                    -1 -> messageStringId.postValue(Resource.error(R.string.network_default_error))
                    0 -> messageStringId.postValue(Resource.error(R.string.server_connection_error))
                    HttpsURLConnection.HTTP_UNAUTHORIZED -> {
                        forcedLogoutUser()
                        messageStringId.postValue(Resource.error(R.string.permission_denied))
                    }
                    HttpsURLConnection.HTTP_INTERNAL_ERROR ->
                        messageStringId.postValue(Resource.error(R.string.network_internal_error))
                    HttpsURLConnection.HTTP_UNAVAILABLE ->
                        messageStringId.postValue(Resource.error(R.string.network_server_not_available))
                    else -> messageString.postValue(Resource.error(message))
                }
            }
        }*/
    protected fun handleServerError(error: Throwable?): ServerError {
        var serverError= ServerError()
        error?.let {
            NetworkHelper.castToServerError(it).run {
               /* when (code) {
                    HttpsURLConnection.HTTP_INTERNAL_ERROR ->
                        messageStringId.postValue(Resource.error(R.string.network_internal_error))
                    HttpsURLConnection.HTTP_UNAVAILABLE ->
                        messageStringId.postValue(Resource.error(R.string.network_server_not_available))
                    HttpsURLConnection.HTTP_GATEWAY_TIMEOUT ->
                        messageStringId.postValue(Resource.error(R.string.network_server_timeout))
                    else -> messageString.postValue(Resource.error(message))
                }*/
                serverError = this
            }
        }
        return serverError
    }
  /*  protected open fun forcedLogoutUser() {
        // do something
    }*/

    abstract fun onCreate()
}