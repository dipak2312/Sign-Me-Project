package com.app.signme.view.home

import android.view.View
import androidx.databinding.DataBindingUtil
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.core.BaseActivity
import com.app.signme.core.BaseFragment
import com.app.signme.dagger.components.FragmentComponent
import com.app.signme.databinding.FragmentHomeBinding
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.viewModel.HomeViewModel


class HomeFragment : BaseFragment<HomeViewModel>() {

    private lateinit var binding:FragmentHomeBinding

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