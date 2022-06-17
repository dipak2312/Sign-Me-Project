package com.app.signme.dagger.components

import com.app.signme.view.notification.NotificationActivity
import com.app.signme.view.authentication.signup.SignUpWithEmailSocialActivity
import com.app.signme.dagger.ActivityScope
import com.app.signme.dagger.modules.ActivityModule
import com.app.signme.view.*
import com.app.signme.view.subscription.SubscribedUserActivity
import com.app.signme.view.subscription.SubscriptionPlansActivity
import com.app.signme.view.settings.staticpages.StaticPagesMultipleActivity
import com.app.signme.view.authentication.resetpassword.ResetPasswordActivity
import com.app.signme.view.authentication.forgotpassword.email.ForgotPasswordWithEmailActivity
import com.app.signme.view.onboarding.OnBoardingActivity
import com.app.signme.view.settings.feedback.SendFeedbackActivity
import com.app.signme.view.settings.changepassword.ChangePasswordActivity
import com.app.signme.view.settings.editprofile.EditProfileActivity


import dagger.Component

import com.app.signme.view.authentication.login.loginwithemailsocial.LoginWithEmailSocialActivity

import com.app.signme.view.authentication.otp.otpsignup.OTPSignUpActivity
import com.app.signme.view.authentication.otp.otpforgotpassword.OTPForgotPasswordActivity
import com.app.signme.view.home.HomeActivity
import com.app.signme.view.settings.SettingsActivity

@ActivityScope
@Component(
    dependencies = [ApplicationComponent::class],
    modules = [ActivityModule::class]
)
interface ActivityComponent{
    fun inject(splashActivity: SplashActivity)
    fun inject(HomeActivity: HomeActivity)
    fun inject(staticPagesMultipleActivity: StaticPagesMultipleActivity)
    fun inject(forgotPasswordWithEmailActivity: ForgotPasswordWithEmailActivity)
    fun inject(resetPasswordActivity: ResetPasswordActivity)
    fun inject(signUpWithEmailSocialActivity: SignUpWithEmailSocialActivity)
    fun inject(otpSignUpActivity: OTPSignUpActivity)
    fun inject(loginWithEmailSocialActivity: LoginWithEmailSocialActivity)
    fun inject(sendFeedbackActivity: SendFeedbackActivity)
    fun inject(changePasswordActivity: ChangePasswordActivity)
    fun inject(onBoardingActivity: OnBoardingActivity)
    fun inject(otpForgotPasswordActivity: OTPForgotPasswordActivity)
    fun inject(editProfileActivity: EditProfileActivity)
    fun inject(SubscriptionPlansActivity: SubscriptionPlansActivity)
    fun inject(SubscribedUserActivity: SubscribedUserActivity)
    fun inject(notificationActivity: NotificationActivity)
    fun inject(settingsActivity: SettingsActivity)

}