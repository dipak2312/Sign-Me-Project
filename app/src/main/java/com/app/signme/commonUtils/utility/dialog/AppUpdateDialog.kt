package com.app.signme.commonUtils.utility.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.databinding.DialogAppUpdateBinding
import com.app.signme.dataclasses.VersionConfigResponse
import com.app.signme.view.authentication.login.loginwithemailsocial.LoginWithEmailSocialActivity
import com.app.signme.view.home.HomeActivity
import com.app.signme.view.onboarding.OnBoardingActivity
import com.hb.logger.Logger
import android.content.Context
import com.app.signme.commonUtils.utility.IConstants


class AppUpdateDialog(
    private val activity: Activity,
    private val version: VersionConfigResponse?,
) : DialogFragment() {

    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = false
        val binding = DataBindingUtil.inflate<DialogAppUpdateBinding>(
            inflater,
            R.layout.dialog_app_update,
            container,
            false
        )

        binding.tvMessage.text = version?.versionCheckMessage
            ?: getString(R.string.label_new_version_available_text)

        binding.mbtnUpdate.setOnClickListener {
            val appPackageName = activity.packageName
            try {
                openAppFromGooglePlayConsole(activity)
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                    )
                )
            }
            this.dismiss()
        }

        binding.mbtnNotNow.setOnClickListener {
            // this.dismiss()
            activity.finish()
            when {
                AppineersApplication.sharedPreference.isLogin -> {
                    startActivity(
                        Intent(
                            activity,
                            HomeActivity::class.java
                        )
                    )
                }
                AppineersApplication.sharedPreference.isOnBoardingShown -> {
                    startActivity(
                        Intent(
                            activity,
                            LoginWithEmailSocialActivity::class.java
                        )
                    )
                }

                else -> {
                    startActivity(
                        Intent(
                            activity,
                            OnBoardingActivity::class.java
                        )
                    )
                }

            }
            this.dismiss()
        }

        if (version!!.isUpdateMandatory()) {
            binding.mbtnNotNow.visibility = View.GONE
        } else {
            binding.mbtnNotNow.visibility = View.VISIBLE
        }

        return binding.root
    }


    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    private fun openAppFromGooglePlayConsole(context: Context) {
        // you can also use BuildConfig.APPLICATION_ID
        val appId: String = context.packageName
        val rateIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$appId")
        )
        var marketFound = false


        // if GP not present on device, open web browser
        if (!marketFound) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appId")
            )
           if (version!!.isUpdateMandatory())
           {
               activity.startActivityForResult(webIntent,IConstants.APP_UPDATE_REQUEST_CODE)
           }
            else
           {
               activity.startActivityForResult(webIntent,IConstants.APP_FORCE_UPDATE_REQUEST_CODE)
           }

        }


    }
}