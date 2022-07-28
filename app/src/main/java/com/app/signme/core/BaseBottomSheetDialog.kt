package com.app.signme.core

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.extension.showSnackBar
import com.app.signme.core.utility.DialogUtil
import com.app.signme.dagger.components.DaggerFragmentComponent
import com.app.signme.dagger.components.FragmentComponent
import com.app.signme.dagger.modules.FragmentModule
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hb.logger.Logger
import com.hb.logger.msc.MSCGenerator
import javax.inject.Inject

/**
 * @author Mahesh Lipane
 */
abstract class BaseBottomSheetDialog<VM : BaseViewModel> : BottomSheetDialogFragment() {

    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    @Inject
    lateinit var viewModel: VM

    protected var mBaseActivity: BaseActivity<VM>? = null


    private fun buildFragmentComponent() =
        DaggerFragmentComponent
            .builder()
            .applicationComponent((context?.applicationContext as AppineersApplication).applicationComponent)
            .fragmentModule(FragmentModule(null, this))
            .build()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(provideLayoutId(), container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(buildFragmentComponent())
        super.onCreate(savedInstanceState)
        setDataBindingLayout()
        //setupObservers()
        //initListener()
        viewModel.onCreate()
        MSCGenerator.addLineComment(this::class.java.simpleName)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = activity as BaseActivity<VM>?
    }

    protected open fun setupObservers() {
        viewModel.messageString.observe(this, androidx.lifecycle.Observer {
            it.data?.run { showMessage(this) }
        })
        viewModel.messageStringId.observe(this, androidx.lifecycle.Observer {
            it.data?.run { showMessage(this) }
        })

        viewModel.showDialog.observe(this, androidx.lifecycle.Observer {
            if (it) showProgress() else hideProgress()
        })
    }

    abstract fun initListener()

    fun showMessage(@StringRes resId: Int) = showMessage(getString(resId))

    fun showMessage(message: CharSequence) {
        //message.toString().showSnackBar(context = activity)
        Toast.makeText(activity,message,Toast.LENGTH_LONG).show()
    }

    fun showMessage(message: CharSequence, type: Int) {
        message.toString().showSnackBar(context = activity, type = type)
    }

    /**
     * If user login in another device, then show session expire dialog to user and navigate to login screen
     */
    private fun showSessionExpireDialog() {
        DialogUtil.alert(
            context = mBaseActivity!!,
            msg = getString(R.string.msg_logged_in_from_other_device),
            positiveBtnText = getString(R.string.ok),
            negativeBtnText = "",
            il = object : DialogUtil.IL {
                override fun onSuccess() {
                    mBaseActivity!!.navigateToLoginScreen(true)
                }

                override fun onCancel(isNeutral: Boolean) {
                    mBaseActivity!!.navigateToLoginScreen(true)
                }
            },
            isCancelable = false
        )
    }

    /**
     * Handle api error
     * @param settings [ERROR : Settings]
     * @param showError Boolean
     * @param showSessionExpire Boolean
     * @return Boolean
     */
    fun handleApiError(
        settings: com.app.signme.dataclasses.generics.Settings?,
        showError: Boolean = true,
        showSessionExpire: Boolean = true
    ): Boolean {
        return when (settings?.success) {
            com.app.signme.dataclasses.generics.Settings.AUTHENTICATION_ERROR -> {
                if (showSessionExpire) showSessionExpireDialog()
                true
            }
            com.app.signme.dataclasses.generics.Settings.NETWORK_ERROR -> {
                settings.message.showSnackBar(mBaseActivity!!)
                true
            }
            com.app.signme.dataclasses.generics.Settings.SOCIAL_LOGIN_FAILURE_ERROR -> {
                settings.message.showSnackBar(mBaseActivity!!)
                true
            }
            com.app.signme.dataclasses.generics.Settings.CONNECTION_TIME_OUT_ERROR -> {
                getString(R.string.str_config_message).showSnackBar(mBaseActivity!!)
                true
            }
            com.app.signme.dataclasses.generics.Settings.ERR_CERT_COMMON_NAME_INVALID -> {
                getString(R.string.str_config_message).showSnackBar(mBaseActivity!!)
                true
            }
            com.app.signme.dataclasses.generics.Settings.TIME_OUT -> {
                getString(R.string.str_config_message).showSnackBar(mBaseActivity!!)
                true
            }
            "0" -> false
            else -> {
                if (showError) settings?.message?.showSnackBar(mBaseActivity!!)
                true
            }
        }
    }

    fun handleApiStatusCodeError(responseCode: Int): Boolean {
        when (responseCode) {
            com.app.signme.dataclasses.generics.Settings.AUTHENTICATION_ERROR_MESSAGE -> {
                Log.e("exception : ", "AUTHENTICATION_ERROR")
            }
            com.app.signme.dataclasses.generics.Settings.NETWORK_ERROR_MESSAGE -> {
                Log.e("exception : ", "NETWORK_ERROR")
            }
            com.app.signme.dataclasses.generics.Settings.UNPROCESSABLE_ENTITY -> {
                Log.e("exception : ", "UNPROCESSABLE_ENTITY")
            }
            com.app.signme.dataclasses.generics.Settings.CONFLICT -> {
                Log.e("exception : ", "CONFLICT")
            }
            com.app.signme.dataclasses.generics.Settings.RESOURCE_WAS_NOT_FOUND -> {
                Log.e("exception : ", "RESOURCE_WAS_NOT_FOUND")
            }
            com.app.signme.dataclasses.generics.Settings.RESOURCE_IS_FORBIDDEN_OR_TIMEOUT -> {
                Log.e("exception : ", "RESOURCE_IS_FORBIDDEN_OR_TIMEOUT")
            }
            com.app.signme.dataclasses.generics.Settings.CONNECTION_TIME_OUT_ERROR_MESSAGE -> {
                Log.e("exception : ", "CONNECTION_TIME_OUT_ERROR")
            }
        }
        return true
    }

    protected open fun onInvalidDataListener(id: Int) {

    }


    open fun showProgress(isCheckNetwork: Boolean = true) {
        try {
            mBaseActivity!!.showProgressDialog(isCheckNetwork,false,"")
        } catch (e: Exception) {

        }

    }

    open fun hideProgress() {
        try {
            mBaseActivity!!.hideProgressDialog()
        } catch (e: Exception) {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView(view)
    }

    protected abstract fun setDataBindingLayout()

    @LayoutRes
    protected abstract fun provideLayoutId(): Int

    protected abstract fun injectDependencies(fragmentComponent: FragmentComponent)

    protected abstract fun setupView(view: View)



    fun showToast(message: String) {
        Toast.makeText(mBaseActivity, message, Toast.LENGTH_LONG).show()
    }

    fun setFireBaseAnalyticsData(id: String, name: String, contentType: String) {
        (activity as BaseActivity<*>).setFireBaseAnalyticsData(id, name, contentType)
    }

    // fun handleApiError(settings: Settings?) = (activity as BaseActivity<*>).handleApiError(settings)

    /**
     * This method use to enable disable view.
     * @param view is view we want to enable/disable.
     * @param enabled is status of view user want to set.
     */
    fun enableDisableButton(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        view.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(if (enabled) R.color.bg_primary_btn else R.color.warm_grey)))
    }
}