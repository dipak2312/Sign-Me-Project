package com.app.signme.dagger.modules

import android.app.Application
import androidx.lifecycle.ViewModelProviders
import com.app.signme.api.network.NetworkHelper
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.core.BaseFragment
import com.app.signme.core.ViewModelProviderFactory
import com.app.signme.repository.*
import com.app.signme.repository.inappbilling.BillingRepository
import com.app.signme.viewModel.*
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class FragmentModule(private val fragment: BaseFragment<*>) {

    @Provides
    fun provideSettingsViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        billingRepository: BillingRepository,
        settingsRepository: SettingsRepository
    ): SettingsViewModel = ViewModelProviders.of(
        fragment, ViewModelProviderFactory(
            SettingsViewModel::
            class
        ) {
            SettingsViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                billingRepository,
                settingsRepository
            )
        }).get(SettingsViewModel::class.java)

    @Provides
    fun provideUserProfileViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        application: Application,
        userProfileRepository: UserProfileRepository
    ): UserProfileViewModel = ViewModelProviders.of(
        fragment, ViewModelProviderFactory(
            UserProfileViewModel::
            class
        ) {
            UserProfileViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                application,
                userProfileRepository
            )
        }).get(UserProfileViewModel::class.java)

    @Provides
    fun provideHomeViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        billingRepository: BillingRepository,
        homeRepository: HomeRepository
    ): HomeViewModel = ViewModelProviders.of(
        fragment, ViewModelProviderFactory(
            HomeViewModel::
            class
        ) {
            HomeViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                billingRepository,
                homeRepository
            )
        }).get(HomeViewModel::class.java)

    @Provides
    fun provideMatchesViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        matchesRepository: MatchesRepository
    ): MatchesViewModel = ViewModelProviders.of(
        fragment, ViewModelProviderFactory(
            MatchesViewModel::
            class
        ) {
            MatchesViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                matchesRepository
            )
        }).get(MatchesViewModel::class.java)

    @Provides
    fun provideChatViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        chatRepository: ChatRepository
    ): ChatViewModel = ViewModelProviders.of(
        fragment, ViewModelProviderFactory(
            ChatViewModel::
            class
        ) {
            ChatViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                chatRepository
            )
        }).get(ChatViewModel::class.java)

    @Provides
    fun myActivityViewmodel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        application: Application,
        myActivityRepository: MyActivityRepository
    ): MyActivityViewModel = ViewModelProviders.of(
        fragment, ViewModelProviderFactory(
            MyActivityViewModel::
            class
        ) {
            MyActivityViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                application,
                myActivityRepository
            )
        }).get(MyActivityViewModel::class.java)
}