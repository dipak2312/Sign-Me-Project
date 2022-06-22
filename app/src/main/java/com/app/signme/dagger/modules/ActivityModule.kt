package com.app.signme.dagger.modules

import android.app.Application
import androidx.lifecycle.ViewModelProviders
import com.app.signme.core.BaseActivity
import com.app.signme.core.ViewModelProviderFactory
import com.app.signme.api.network.NetworkHelper
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.repository.*
import com.app.signme.viewModel.*
import com.app.signme.repository.ForgotPasswordEmailRepository
import com.app.signme.viewModel.ForgotPasswordEmailViewModel
import com.app.signme.repository.ForgotPasswordPhoneRepository
import com.app.signme.viewModel.ForgotPasswordPhoneViewModel
import com.app.signme.repository.ResetPasswordRepository
import com.app.signme.repository.ChangePasswordRepository
import com.app.signme.repository.inappbilling.BillingRepository
import com.app.signme.viewModel.NotificationViewModel
import com.app.signme.viewModel.ResetPasswordViewModel
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class ActivityModule(private val activity: BaseActivity<*>) {

    @Provides
    fun provideSettingsViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        billingRepository: BillingRepository,
        settingsRepository: SettingsRepository
    ): SettingsViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(
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
    fun provideForgotPasswordWithPhoneActivityViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        forgotPasswordPhoneRepository: ForgotPasswordPhoneRepository
    ): ForgotPasswordPhoneViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(ForgotPasswordPhoneViewModel::class) {
            ForgotPasswordPhoneViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                forgotPasswordPhoneRepository
            )

        }).get(ForgotPasswordPhoneViewModel::class.java)

    @Provides
    fun provideForgotPasswordWithEmailActivityViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        forgotPasswordEmailRepository: ForgotPasswordEmailRepository
    ): ForgotPasswordEmailViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(ForgotPasswordEmailViewModel::class) {
            ForgotPasswordEmailViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                forgotPasswordEmailRepository
            )
        }).get(ForgotPasswordEmailViewModel::class.java)

    @Provides
    fun provideResetPasswordActivityModule(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        resetPasswordRepository: ResetPasswordRepository
    ): ResetPasswordViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(ResetPasswordViewModel::class) {
            ResetPasswordViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                resetPasswordRepository
            )
        }).get(ResetPasswordViewModel::class.java)

    @Provides
    fun provideHomeActivityViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        billingRepository: BillingRepository,
        homeRepository: HomeRepository
    ): HomeViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(HomeViewModel::class) {
            HomeViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                billingRepository,
                homeRepository
            )
        }).get(HomeViewModel::class.java)

    @Provides
    fun provideStaticPagesMultipleActivityViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        staticPagesRepository: StaticPagesRepository
    ): StaticPagesViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(StaticPagesViewModel::class) {
            StaticPagesViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                staticPagesRepository
            )
        }).get(StaticPagesViewModel::class.java)

    @Provides
    fun provideSignUpWithEmailActivityViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        application: Application,
        signUpWithEmailRepository: SignUpWithEmailRepository
    ): SignUpWithEmailViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(SignUpWithEmailViewModel::class) {
            SignUpWithEmailViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                application,
                signUpWithEmailRepository
            )
        }).get(SignUpWithEmailViewModel::class.java)

    @Provides
    fun provideOTPSignUpActivityViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        otpSignUpRepository: OTPSignUpRepository
    ): OTPSignUpViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(OTPSignUpViewModel::class) {
            OTPSignUpViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                otpSignUpRepository
            )
        }).get(OTPSignUpViewModel::class.java)

    @Provides
    fun providePhoneNumberLoginActivityViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        loginWithPhoneNumberRepository: LoginWithPhoneNumberRepository
    ): LoginWithPhoneNumberViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(LoginWithPhoneNumberViewModel::class) {
            LoginWithPhoneNumberViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                loginWithPhoneNumberRepository
            )
        }).get(LoginWithPhoneNumberViewModel::class.java)

    @Provides
    fun provideLoginWithPhoneNumberSocialActivityViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        loginWithPhoneNumberSocialRepository: LoginWithPhoneNumberSocialRepository
    ): LoginWithPhoneNumberSocialViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(LoginWithPhoneNumberSocialViewModel::class) {
            LoginWithPhoneNumberSocialViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                loginWithPhoneNumberSocialRepository
            )
        }).get(LoginWithPhoneNumberSocialViewModel::class.java)

    @Provides
    fun provideLoginWithEmailActivityViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        loginWithEmailRepository: LoginWithEmailRepository
    ): LoginWithEmailViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(LoginWithEmailViewModel::class) {
            LoginWithEmailViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                loginWithEmailRepository
            )
        }).get(LoginWithEmailViewModel::class.java)

    @Provides
    fun provideLoginWithEmailSocialActivityViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        loginWithEmailSocialRepository: LoginWithEmailSocialRepository
    ): LoginWithEmailSocialViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(LoginWithEmailSocialViewModel::class) {
            LoginWithEmailSocialViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                loginWithEmailSocialRepository
            )
        }).get(LoginWithEmailSocialViewModel::class.java)

    @Provides
    fun provideFeedbackViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        staticPagesRepository: FeedbackRepository
    ): FeedbackViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(FeedbackViewModel::class) {
            FeedbackViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                staticPagesRepository
            )
        }).get(FeedbackViewModel::class.java)

    @Provides
    fun provideChangePasswordViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        changePasswordRepository: ChangePasswordRepository
    ): ChangePasswordViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(ChangePasswordViewModel::class) {
            ChangePasswordViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                changePasswordRepository
            )
        }).get(ChangePasswordViewModel::class.java)

    @Provides
    fun provideOTPForgotPasswordViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        otpForgotPasswordRepository: OTPForgotPasswordRepository,
        app: Application
    ): OTPForgotPasswordViewModel = ViewModelProviders.of(
        activity,
        ViewModelProviderFactory(OTPForgotPasswordViewModel::class) {
            OTPForgotPasswordViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                otpForgotPasswordRepository,
                app
            )

        }).get(OTPForgotPasswordViewModel::class.java)

    @Provides
    fun provideOnBoardingActivityModule(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper
    ): OnBoardingActivityViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(OnBoardingActivityViewModel::class) {
            OnBoardingActivityViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper
            )
        }
    ).get(OnBoardingActivityViewModel::class.java)

    @Provides
    fun provideChangePhoneNumberOTPViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        application: Application,
        changePhoneNumberOTPRepository: ChangePhoneNumberOTPRepository
    ): ChangePhoneNumberOTPViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(ChangePhoneNumberOTPViewModel::class) {
            ChangePhoneNumberOTPViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                application,
                changePhoneNumberOTPRepository
            )
        }).get(ChangePhoneNumberOTPViewModel::class.java)

    @Provides
    fun provideEditProfileActivityModule(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        application: Application,
        userProfileRepository: UserProfileRepository
    ): UserProfileViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(UserProfileViewModel::class) {
            UserProfileViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                application,
                userProfileRepository
            )
        }).get(UserProfileViewModel::class.java)

    //************* AddAddressActivity Provides **************

    @Provides
    fun provideAddAddressActivityModule(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        application: Application,
        addAddressRepository: AddAddressRepository
    ): AddAddressViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(AddAddressViewModel::class) {
            AddAddressViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                application,
                addAddressRepository
            )
        }).get(AddAddressViewModel::class.java)

    @Provides
    fun provideSplashActivityModule(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
       splashRepository: SplashRepository
    ): SplashViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(SplashViewModel::class) {
            SplashViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                splashRepository
            )
        }).get(SplashViewModel::class.java)

    @Provides
    fun provideNotificationActivityModule(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        application: Application,
        notificationRepository: NotificationRepository
    ): NotificationViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(NotificationViewModel::class) {
            NotificationViewModel(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                application,
                notificationRepository
            )
        }).get(NotificationViewModel::class.java)


    @Provides
    fun provideLocationEnableActivityModule(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
       landingRepository: LandingRepository
    ): LandingViewMode = ViewModelProviders.of(
        activity, ViewModelProviderFactory(LandingViewMode::class) {
            LandingViewMode(
                schedulerProvider,
                compositeDisposable,
                networkHelper,
                landingRepository
            )
        }).get(LandingViewMode::class.java)



}