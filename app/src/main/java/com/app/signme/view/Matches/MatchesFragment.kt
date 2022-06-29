package com.app.signme.view.Matches

import android.view.View
import androidx.databinding.DataBindingUtil
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.dagger.components.FragmentComponent

import com.app.signme.core.BaseActivity
import com.app.signme.core.BaseFragment
import com.app.signme.databinding.FragmentMatchesBinding
import com.app.signme.view.enablePermission.PermissionEnableActivity
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.viewModel.MatchesViewModel

class MatchesFragment : BaseFragment<MatchesViewModel>() {

    private lateinit var binding: FragmentMatchesBinding

    override fun setDataBindingLayout() {}

    override fun provideLayoutId(): Int {
        return R.layout.fragment_matches
    }

    override fun injectDependencies(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun setupView(view: View) {
        binding = DataBindingUtil.bind(view)!!
        binding.lifecycleOwner = this


       initListeners()
        addObservers()
    }

    private fun initListeners() {

        binding?.let {
            with(it) {

                btnsetting.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                    startActivity(SettingsActivity.getStartIntent(this@MatchesFragment.requireContext()))
                }
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