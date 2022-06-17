package com.app.signme.view.Matches

import android.view.View
import androidx.databinding.DataBindingUtil
import com.app.signme.R
import com.app.signme.dagger.components.FragmentComponent

import com.app.signme.core.BaseActivity
import com.app.signme.core.BaseFragment
import com.app.signme.databinding.FragmentMatchesBinding
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

        addListeners()
        addObservers()
    }

    private fun addListeners() {

    }

    private fun addObservers() {

        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            (activity as BaseActivity<*>).handleApiStatusCodeError(serverError)
        }
    }
}