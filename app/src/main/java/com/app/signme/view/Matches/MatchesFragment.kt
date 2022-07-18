package com.app.signme.view.Matches

import android.view.View
import androidx.databinding.DataBindingUtil
import com.app.signme.R
import com.app.signme.adapter.LikesAdapter
import com.app.signme.adapter.MatchesAdapter
import com.app.signme.adapter.SuperLikesAdapter
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.dagger.components.FragmentComponent

import com.app.signme.core.BaseActivity
import com.app.signme.core.BaseFragment
import com.app.signme.databinding.FragmentMatchesBinding
import com.app.signme.view.enablePermission.PermissionEnableActivity
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.app.signme.viewModel.MatchesViewModel

class MatchesFragment : BaseFragment<MatchesViewModel>(), RecyclerViewActionListener {

    private lateinit var binding: FragmentMatchesBinding
    private var likesAdapter:LikesAdapter?=null
    private var superLikesAdapter:SuperLikesAdapter?=null
    private var matchesAdapter:MatchesAdapter?=null

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

        likesAdapter= LikesAdapter(this,mBaseActivity!!)
        binding!!.mRecyclerLikes.adapter = likesAdapter
        superLikesAdapter= SuperLikesAdapter(this,mBaseActivity!!)
        binding!!.mRecyclerSuperLikes.adapter = superLikesAdapter
        matchesAdapter= MatchesAdapter(this,mBaseActivity!!)
        binding!!.mRecyclerMatches.adapter = matchesAdapter


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

    override fun onItemClick(viewId: Int, position: Int, childPosition: Int?) {
        TODO("Not yet implemented")
    }

    override fun onLoadMore(itemCount: Int, nextPage: Int) {
        TODO("Not yet implemented")
    }
}