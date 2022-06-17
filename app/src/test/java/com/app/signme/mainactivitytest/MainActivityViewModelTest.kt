package com.app.signme.mainactivitytest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.app.signme.BuildConfig
import com.app.signme.db.entity.WeatherEntity
import com.app.signme.repository.CentralRepository
import com.app.signme.commonUtils.common.Resource
import com.app.signme.utils.mock
import com.app.signme.utils.whenever
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import io.reactivex.Single
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class MainActivityViewModelTest : KotlinBaseMockObjectsClass() {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()
    private val mockMainActivityRepositoryTest = mock<CentralRepository>()
    private val messageStringObserver = mock<Observer<Resource<String>>>()
    private val weatherLiveDataObserver = mock<Observer<WeatherEntity>>()
    private val temperatureObserver = mock<Observer<CharSequence>>()
    private val temperatureDetailObserver = mock<Observer<CharSequence>>()
    private val sunriseObserver = mock<Observer<CharSequence>>()
    private val sunsetObserver = mock<Observer<CharSequence>>()
    private val humidityObserver = mock<Observer<CharSequence>>()
    private val windSpeedObserver = mock<Observer<CharSequence>>()
    private val generateJobIdLiveDataObserver = mock<Observer<WeatherDataClass>>()
    private val showDialogObserver = mock<Observer<Boolean>>()
    private val viewModel by lazy {
        MainActivityViewModel(
            testSchedulerProvider,
            mockCompositeDisposable,
            mockNetworkHelper,
            mockMainActivityRepositoryTest
        )
    }

    @Before
    fun initTest() {
        Mockito.reset(
            mockCompositeDisposable,
            mockNetworkHelper,
            mockMainActivityRepositoryTest,
            messageStringObserver,
            generateJobIdLiveDataObserver,
            windSpeedObserver,
            humidityObserver,
            sunsetObserver,
            sunriseObserver,
            temperatureDetailObserver,
            temperatureObserver,
            weatherLiveDataObserver,
            showDialogObserver
        )
    }

    @Test
    fun verifyGetWeatherEntityData() {
        val weatherDataClass = WeatherDataClass()
        val weatherEntity = WeatherEntity(
            -1,
            weatherDataClass.name ?: "",
            weatherDataClass.weather?.get(0)?.icon ?: "",
            weatherDataClass.weather?.get(0)?.main ?: "",
            weatherDataClass.main?.temp ?: 0.0,
            weatherDataClass.main?.tempMin ?: 0.0,
            weatherDataClass.main?.humidity ?: 0,
            weatherDataClass.main?.pressure ?: 0,
            weatherDataClass.main?.tempMax ?: 0.0,
            weatherDataClass.sys?.sunrise ?: 0,
            weatherDataClass.sys?.sunset ?: 0,
            weatherDataClass.wind?.deg ?: 0,
            weatherDataClass.wind?.speed ?: 0.0
        )
        val mWeatherEntity = viewModel.getWeatherEntityData(weatherDataClass)
        assertEquals(weatherEntity, mWeatherEntity)
    }

    @Test
    fun verifyGetWeatherByCityNameError() {
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.getWeatherByCity("Delhi", BuildConfig.openweathermapapikey))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockMainActivityRepositoryTest.getWeatherByCity(ArgumentMatchers.anyString()))
            .thenReturn(Single.error(getErrorResponse()))
        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)
        viewModel.getWeatherByCityName("Pune")
        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(2)).onChanged(capture())
            Mockito.verify(messageStringObserver, Mockito.times(1)).onChanged(
                Resource.error(
                    errorMsg
                )
            )
        }
    }

    @Test
    fun verifySetLiveData() {
        viewModel.temperature.observeForever(temperatureObserver)
        viewModel.temperatureDesc.observeForever(temperatureDetailObserver)
        viewModel.sunrise.observeForever(sunriseObserver)
        viewModel.sunset.observeForever(sunsetObserver)
        viewModel.humidity.observeForever(humidityObserver)
        viewModel.windSpeed.observeForever(windSpeedObserver)
        viewModel.setLiveData(weatherEntity)
        val argumentCaptorCharSequence = ArgumentCaptor.forClass(CharSequence::class.java)
        argumentCaptorCharSequence.run {
            Mockito.verify(temperatureObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(temperatureDetailObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(sunriseObserver, Mockito.times(1))
                .onChanged(ArgumentMatchers.anyString())
            Mockito.verify(sunsetObserver, Mockito.times(1)).onChanged(ArgumentMatchers.anyString())
            Mockito.verify(humidityObserver, Mockito.times(1))
                .onChanged(ArgumentMatchers.anyString())
            Mockito.verify(windSpeedObserver, Mockito.times(1))
                .onChanged(ArgumentMatchers.anyString())

        }

    }

    @Test
    fun verifyInsertAndSetWeatherData() {
        viewModel.weatherLiveData.observeForever(weatherLiveDataObserver)
        viewModel.insertAndSetWeatherData(WeatherDataClass())
        val argumentCaptorWeather = ArgumentCaptor.forClass(WeatherEntity::class.java)
        argumentCaptorWeather.run {
            Mockito.verify(weatherLiveDataObserver, Mockito.times(1)).onChanged(capture())
        }
    }

    @Test
    fun verifyGetWeatherByCityName() {
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.getWeatherByCity("Delhi", BuildConfig.openweathermapapikey))
            .thenReturn(Single.just(WeatherDataClass()))
        whenever(mockMainActivityRepositoryTest.getWeatherByCity(ArgumentMatchers.anyString()))
            .thenReturn(Single.just(WeatherDataClass()))
        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.generateJobIdLiveData.observeForever(generateJobIdLiveDataObserver)
        viewModel.getWeatherByCityName("Delhi")
        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(2)).onChanged(capture())
            Mockito.verify(generateJobIdLiveDataObserver, Mockito.times(1))
                .onChanged(WeatherDataClass())
        }
    }

}