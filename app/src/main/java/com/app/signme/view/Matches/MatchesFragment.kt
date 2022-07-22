package com.app.signme.view.Matches

import android.opengl.Visibility
import android.view.View
import androidx.databinding.DataBindingUtil
import com.app.signme.R
import com.app.signme.adapter.LikesAdapter
import com.app.signme.adapter.MatchesAdapter
import com.app.signme.adapter.SuperLikesAdapter
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.core.BaseActivity
import com.app.signme.core.BaseFragment
import com.app.signme.dagger.components.FragmentComponent
import com.app.signme.databinding.FragmentMatchesBinding
import com.app.signme.dataclasses.OtherUserDetailsResponse
import com.app.signme.dataclasses.SwiperViewResponse
import com.app.signme.view.dialogs.UnMatchDialog
import com.app.signme.view.home.OtherUserDetailsActivity
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.app.signme.viewModel.MatchesViewModel
import kotlinx.android.synthetic.main.fragment_home.relEmptyMessage
import kotlinx.android.synthetic.main.fragment_matches.*

class MatchesFragment : BaseFragment<MatchesViewModel>(), RecyclerViewActionListener {

    private lateinit var binding: FragmentMatchesBinding
    private var likesAdapter: LikesAdapter? = null
    private var superLikesAdapter: SuperLikesAdapter? = null
    private var matchesAdapter: MatchesAdapter? = null


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

        likesAdapter = LikesAdapter(this, mBaseActivity!!)
        binding!!.mRecyclerLikes.adapter = likesAdapter
        superLikesAdapter = SuperLikesAdapter(this, mBaseActivity!!)
        binding!!.mRecyclerSuperLikes.adapter = superLikesAdapter
        matchesAdapter = MatchesAdapter(this, mBaseActivity!!)
        binding!!.mRecyclerMatches.adapter = matchesAdapter

        initListeners()
        addObservers()
        getLikeSuperlikeMatchesList()
    }


    fun getLikeSuperlikeMatchesList() {
        when {
            checkInternet() -> {

                showProgressDialog(
                    isCheckNetwork = true,
                    isSetTitle = false,
                    title = IConstants.EMPTY_LOADING_MSG
                )

                viewModel.getLikeSuperlikeMatchesList()
            }
        }
    }

    private fun initListeners() {

        binding?.let {
            with(it) {

                btnsetting.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                    startActivity(SettingsActivity.getStartIntent(this@MatchesFragment.requireContext()))
                 }

                textViewAll.setOnClickListener {

                    startActivity(
                        ShowLikesMatchesActivity.getStartIntent(
                            this@MatchesFragment.requireContext(),
                            IConstants.MATCH
                        )
                    )
                }

                btnWhoLike.setOnClickListener {
                    startActivity(
                        ShowLikesMatchesActivity.getStartIntent(
                            this@MatchesFragment.requireContext(),
                            IConstants.LIKE
                        )
                    )

                }

                textMatches.setOnClickListener {
                    UnMatchDialog(mListener = object :
                        UnMatchDialog.ClickListener {
                        override fun onSuccess() {
                        }

                        override fun onCancel() {
                        }

                    }).show(requireFragmentManager(), "Tag")
                }
            }
        }
    }

    private fun addObservers() {

        viewModel.LikeSuperlikeMatchesLiveData.observe(this) { response ->
            hideProgressDialog()
            if (response?.settings?.isSuccess == true) {

                scrollview.visibility = View.VISIBLE

                if (response.data!!.superlikeUserList.isNullOrEmpty() && response.data!!.likeUserList.isNullOrEmpty() && response.data!!.matchUserList.isNullOrEmpty()) {
                    relEmptyMessage.visibility = View.VISIBLE
                } else {
                    relEmptyMessage.visibility = View.GONE
                }
                if (response.data!!.superlikeUserList.isNullOrEmpty()) {
                    binding.linsuperlike.visibility = View.GONE
                } else {
                    binding.linsuperlike.visibility = View.VISIBLE
                    superLikesAdapter!!.addAllItem(response.data!!.superlikeUserList!!)
                    superLikesAdapter!!.totalCount = response.data!!.superlikeUserCount!!
                }

                if (response.data!!.likeUserList.isNullOrEmpty()) {
                    binding.linlike.visibility = View.GONE
                } else {
                    binding.linlike.visibility = View.VISIBLE
                    likesAdapter!!.addAllItem(response.data!!.likeUserList!!)
                    likesAdapter!!.totalCount = response.data!!.likeUserCount!!
                }

                if (response.data!!.matchUserList.isNullOrEmpty()) {
                    binding.linMatch.visibility = View.GONE
                } else {
                    binding.linMatch.visibility = View.VISIBLE
                    matchesAdapter!!.addAllItem(response.data!!.matchUserList!!)
                    if(response.data!!.matchUserCount!!.toInt()>4)
                    {
                       binding.textViewAll.visibility=View.VISIBLE
                    }else
                    {
                        binding.textViewAll.visibility=View.GONE
                    }
                }
            }
        }

        (activity?.application as AppineersApplication).isMatchesUpdated.observe(this){isMatches->

            if(isMatches)
            {
                likesAdapter!!.removeAll()
                superLikesAdapter!!.removeAll()
                matchesAdapter!!.removeAll()
                getLikeSuperlikeMatchesList()
                (activity?.application as AppineersApplication).isMatchesUpdated.postValue(false)
            }
        }

        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            (activity as BaseActivity<*>).handleApiStatusCodeError(serverError)
        }
    }


    override fun onItemClick(viewId: Int, position: Int, childPosition: Int?) {
        when (viewId) {
            R.id.imgLike -> {
//                if (position == 3) {
//                    // startActivity(ShowLikesMatchesActivity.getStartIntent(this@MatchesFragment.requireContext(),IConstants.LIKE))
//                } else {
//                    //startActivity(SubscriptionPlansActivity.getStartIntent(this@MatchesFragment.requireContext(),"1"))
//                }
            }

            R.id.imgSuperlike -> {
                if (position == 3) {
                    startActivity(ShowLikesMatchesActivity.getStartIntent(this@MatchesFragment.requireContext(),IConstants.SUPERLIKE))
                }
                else
                {
                    var otherUserResponse=SwiperViewResponse(isLike = "Yes",name = superLikesAdapter!!.getItem(position).firstName,profileImage = superLikesAdapter!!.getItem(position).profileImage)
                    startActivity(OtherUserDetailsActivity.getStartIntent(this@MatchesFragment.requireContext(),superLikesAdapter!!.getItem(position).userId,otherUserResponse,IConstants.EXPLORE))
                }
            }

            R.id.imgMatches->{
                var otherUserResponse=SwiperViewResponse(isLike = "No",name = matchesAdapter!!.getItem(position).firstName,profileImage = matchesAdapter!!.getItem(position).profileImage)
                startActivity(OtherUserDetailsActivity.getStartIntent(this@MatchesFragment.requireContext(),matchesAdapter!!.getItem(position).userId,otherUserResponse,IConstants.MATCHES))
            }
        }
    }

    override fun onLoadMore(itemCount: Int, nextPage: Int) {

    }
}