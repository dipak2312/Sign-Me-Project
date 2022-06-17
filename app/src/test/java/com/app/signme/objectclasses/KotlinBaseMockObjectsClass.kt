package com.app.signme.objectclasses

import com.app.signme.utils.TestSchedulerProvider
import com.app.signme.api.network.NetworkHelper
import com.app.signme.api.network.NetworkService
import com.app.signme.application.AppineersApplication
import com.app.signme.utils.mock
import io.reactivex.disposables.CompositeDisposable

open class KotlinBaseMockObjectsClass {
    val testSchedulerProvider = TestSchedulerProvider()
    val mockCompositeDisposable = mock<CompositeDisposable>()
    val mockNetworkService = mock<NetworkService>()
    val mockApplication = mock<AppineersApplication>()
    val mockNetworkHelper = mock<NetworkHelper>()
    val mockDataRepository = mock<WeatherDataRepository>()
}