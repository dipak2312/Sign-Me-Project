package com.app.signme.view.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.view.View
import androidx.databinding.DataBindingUtil
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.core.AppConfig
import com.app.signme.core.BaseActivity
import com.app.signme.core.BaseFragment
import com.app.signme.dagger.components.FragmentComponent
import com.app.signme.databinding.FragmentProfileBinding
import com.app.signme.dataclasses.UserImage
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.db.repo.MediaFileRepository
import com.app.signme.view.settings.editprofile.EditProfileActivity
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.app.signme.viewModel.UserProfileViewModel
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ProfileFragment : BaseFragment<UserProfileViewModel>(), RecyclerViewActionListener {

    companion object {
        const val FRAGMENT_TAG = "ProfileFragment"
    }

    private lateinit var binding: FragmentProfileBinding
    var mediaFileRepository: MediaFileRepository? = null
    var showWarning = false
    var userInfo: LoginResponse? = null
    var mImageAdapter = PagerImageAdapter(false, this)


    override fun provideLayoutId(): Int {
        return R.layout.fragment_profile
    }

    override fun injectDependencies(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun setupView(view: View) {
        setFireBaseAnalyticsData("id-profileScreen", "view_profileScreen", "view_profileScreen")
        binding = DataBindingUtil.bind(view)!!
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewPagerImages.adapter = mImageAdapter
        viewPagerImages.currentItem = 0
        mTabLayout.touchables?.forEach { tabDots ->
            tabDots.isEnabled = false
        }
        mTabLayout.setupWithViewPager(viewPagerImages)

        mImageAdapter.notifyDataSetChanged()
        if (AppConfig.AdProvider_MoPub) {
            this.activity?.let { showBannerAd(it, binding.moPubAdView) }
        } else {
            this.activity?.let { showBannerAd(it, binding.adView) }
        }
        binding.user = sharedPreference.userDetail
        mediaFileRepository = MediaFileRepository.getInstance(this.activity!!)
        getProfileData()

        binding.apply {
            btnEdit.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Edit Button Click")
                startActivity(Intent(activity, EditProfileActivity::class.java))
            }
        }


    }

    private fun getProfileData() {
        when {
            checkInternet() -> {
                showProgressDialog(
                    isCheckNetwork = true,
                    isSetTitle = false,
                    title = IConstants.EMPTY_LOADING_MSG
                )
                viewModel.callGetProfile()
            }
        }
    }


    override fun setDataBindingLayout() {

    }

    override fun setupObservers() {
        super.setupObservers()
        hideProgressDialog()

        (activity?.application as AppineersApplication).isProfileUpdated.observe(this) {
            hideProgressDialog()
            binding.user = sharedPreference.userDetail
            binding.executePendingBindings()
            getProfileData()
        }

        viewModel.updateUserLiveData.observe(this) {
            hideProgressDialog()
            if (it.settings?.isSuccess == true) {
                binding.user = sharedPreference.userDetail
                binding.executePendingBindings()
                mImageAdapter.notifyDataSetChanged()


            } /*else if (!handleApiError(it.settings)) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "Profile updation failed"
                )
                it?.settings?.message?.showSnackBar(requireActivity())
            } else {
                showMessage(it.settings!!.message)
            }*/
        }

        viewModel.getUserLiveData.observe(this) {
            hideProgressDialog()
            if (it.settings?.isSuccess == true) {
                userInfo = it.data!![0]
                AppineersApplication.sharedPreference.userDetail = it.data!![0]
                localDatabaseImages()
                setData()

            } /*else if (!handleApiError(it.settings)) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "Profile updation failed"
                )
                it?.settings?.message?.showSnackBar(requireActivity())
            } else {
                showMessage(it.settings!!.message)
            }*/
        }

        if ((activity?.application as AppineersApplication).isMediaUpdated.value == true) {
            (activity?.application as AppineersApplication).isMediaUpdated.observe(
                this
            ) {
                if (it) {
                    val count = (activity?.application as AppineersApplication).isImageCout
                    if (count <= 0) {
                        Handler().postDelayed({
                            mImageAdapter.removeAll()
                            localDatabaseImages()
                            getProfileData()

                        }, 2000)
                    }
                }

            }
        }
        (activity?.application as AppineersApplication).isAdRemoved.observe(
            this@ProfileFragment
        ) {
            if (it) {
                binding.adView.visibility = View.GONE
            }
        }

        viewModel.statusCodeLiveData.observe(this) { serverError ->
            (activity as BaseActivity<*>).handleApiStatusCodeError(serverError)
        }

        localDatabaseImages()
    }

    private fun setData() {
        mImageAdapter.removeAll()
        if (userInfo!!.userImages == null) {
            if (!sharedPreference.userDetail?.profileImage.equals("")) {
                mImageAdapter.insertItem(
                    UserImage("", "", userInfo!!.profileImage, "")
                )
                binding.tvNoData.visibility = View.GONE
                binding.mTabLayout.visibility = View.VISIBLE
                mImageAdapter.notifyDataSetChanged()

            }
        } else {
            if (!sharedPreference.userDetail?.profileImage.equals("")) {
                mImageAdapter.insertItem(
                    UserImage("", "", userInfo!!.profileImage, "")
                )
            }
            if (userInfo!!.userImages != null && userInfo!!.userImages!!.size > 0) {
                binding.tvNoData.visibility = View.GONE
                binding.mTabLayout.visibility = View.VISIBLE
                mImageAdapter.insertAllItem(userInfo!!.userImages!!)
                mImageAdapter.notifyDataSetChanged()
            } else {

                binding.tvNoData.visibility = View.VISIBLE
                binding.mTabLayout.visibility = View.GONE
            }
        }


        binding.executePendingBindings()
        btnWarning.visibility = if (showWarning) View.VISIBLE else View.GONE
        mImageAdapter.notifyDataSetChanged()

    }


    override fun onItemClick(viewId: Int, position: Int, childPosition: Int?) {
        TODO("Not yet implemented")
    }

    override fun onLoadMore(itemCount: Int, nextPage: Int) {
        TODO("Not yet implemented")
    }

    private fun localDatabaseImages() {
        mImageAdapter.removeAll()
        CoroutineScope(Dispatchers.IO).launch {
            for (localFile in mediaFileRepository?.getByUserId(userInfo!!.userId!!)!!) {
                val image = UserImage(
                    imageId = localFile.fileId.toString(),
                    imageUri = localFile.filePath,
                    imageUrl = localFile.filePath,
                    uploadStatus = localFile.status
                )
                mImageAdapter.insertItem(image)
                showWarning = true
                mImageAdapter.notifyDataSetChanged()
            }
        }
    }
}