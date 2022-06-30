package com.app.signme.view.home

import android.content.Intent
import android.view.View
import androidx.databinding.DataBindingUtil
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.core.BaseActivity
import com.app.signme.core.BaseFragment
import com.app.signme.dagger.components.FragmentComponent
import com.app.signme.databinding.FragmentHomeBinding
import com.app.signme.scheduler.aws.AwsService
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.viewModel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : BaseFragment<HomeViewModel>() {

    private lateinit var binding:FragmentHomeBinding
    var uploadedFiles = ArrayList<String>()

    override fun setDataBindingLayout() {}

    override fun provideLayoutId(): Int {
        return R.layout.fragment_home
    }

    override fun injectDependencies(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun setupView(view: View) {
        binding = DataBindingUtil.bind(view)!!
        binding.lifecycleOwner = this

        addObservers()
        initListener()

        if(!sharedPreference.userProfileUrl.isNullOrEmpty())
        {
            uploadedFiles = sharedPreference.userProfileUrl!!.split(",").map { it.trim() } as ArrayList<String>
            if(!uploadedFiles.isNullOrEmpty())
            {
                deleteAllUploadedFiles()
            }
        }

    }

    private fun deleteAllUploadedFiles() {
        CoroutineScope(Dispatchers.IO).launch {
            if (!uploadedFiles.isNullOrEmpty()) {
                uploadedFiles.forEach { image ->
                    AwsService.deleteFile(image.substringAfter(".com/"))
                }
                uploadedFiles.clear()
                sharedPreference.userProfileUrl=""

                (activity?.application as AppineersApplication).awsFileUploader.postValue(
                    null
                )
                activity?.sendBroadcast(Intent(AwsService.UPLOAD_CANCELLED_ACTION))
            }
        }
    }

    private fun initListener() {
        binding?.apply {

            btnSetting.setOnClickListener{
                startActivity(SettingsActivity.getStartIntent(this@HomeFragment.requireContext()))
            }
        }
    }


    private fun addObservers() {


        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            (activity as BaseActivity<*>).handleApiStatusCodeError(serverError)
        }
    }


}