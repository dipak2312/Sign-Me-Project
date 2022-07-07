package com.app.signme.view.profile

import android.annotation.SuppressLint
import android.os.Handler
import android.view.View
import androidx.databinding.DataBindingUtil
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.core.BaseActivity
import com.app.signme.core.BaseFragment
import com.app.signme.dagger.components.FragmentComponent
import com.app.signme.databinding.FragmentProfileBinding
import com.app.signme.dataclasses.UserImage
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.db.repo.MediaFileRepository
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.view.settings.editprofile.EditProfileActivity
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.app.signme.viewModel.UserProfileViewModel
import com.google.android.material.chip.Chip
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
    var lookingFor: ArrayList<String>? = null

    var mImageAdapter = PagerImageAdapter(false, this)


    override fun provideLayoutId(): Int {
        return R.layout.fragment_profile
    }

    override fun injectDependencies(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun setupView(view: View) {
        getContext()?.getTheme()?.applyStyle(R.style.ProfileTheme, true)
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

        binding.user = sharedPreference.userDetail
        mediaFileRepository = MediaFileRepository.getInstance(this.activity!!)
        //getProfileData()


        initListeners()
        getProfileData()

    }

    fun initListeners() {
        binding.apply {
            btnEditProfile.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Edit Button Click")
                startActivity(
                    EditProfileActivity.getStartIntent(
                        this@ProfileFragment.requireContext(),
                        IConstants.EDIT
                    )
                )
            }
            btnsetting.setOnClickListener {
                startActivity(SettingsActivity.getStartIntent(this@ProfileFragment.requireContext()))
            }
        }
    }

    fun addLookingFor() {
        binding!!.lookingForChipGroup.removeAllViews()
        for (lookingfor in lookingFor!!) {
            val chip = Chip(this@ProfileFragment.requireContext())
            chip.text = lookingfor
            chip.setTextAppearanceResource(R.style.mychipText);
            binding?.lookingForChipGroup?.addView(chip)
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

        (activity?.application as AppineersApplication).isProfileUpdated.observe(this) { isUpdate ->

            if (isUpdate) {
                hideProgressDialog()
                binding.user = sharedPreference.userDetail
                binding.executePendingBindings()
//
//                if(sharedPreference.userDetail!!.lookingForRelationType!!.isNotEmpty())
//                {
//                    lookingFor?.clear()
//                    for(response in sharedPreference.userDetail!!.lookingForRelationType!!)
//                    {
//                        lookingFor?.add(response.relationshipStatus)
//                    }
//
//                    addLookingFor()
//                }

//                if(!sharedPreference.userDetail?.UserMedia.isNullOrEmpty())
//                {
//                    mImageAdapter.removeAll()
//                    for (respons in sharedPreference.userDetail?.UserMedia!!)
//                    {
//                        mImageAdapter.insertItem(UserImage(respons.mediaId,"",respons.imageUrl,""))
//                    }
//                }

                getProfileData()

                (activity?.application as AppineersApplication).isProfileUpdated.postValue(false)
            }
        }

        viewModel.updateUserLiveData.observe(this) {
            hideProgressDialog()
            if (it.settings?.isSuccess == true) {
                binding.user = sharedPreference.userDetail
                binding.executePendingBindings()
                mImageAdapter.notifyDataSetChanged()

            }
        }

        viewModel.getUserLiveData.observe(this) {
            hideProgressDialog()
            lookingFor = ArrayList<String>()
            if (it.settings?.isSuccess == true) {
                if (!it.data.isNullOrEmpty()) {
                    mImageAdapter.removeAll()
                    userInfo = it.data!![0]
                    AppineersApplication.sharedPreference.userDetail = it.data!![0]
                    binding!!.user = it.data!![0]
                    binding.mTabLayout.visibility = View.VISIBLE
                    for (respons in it.data!![0].UserMedia!!) {
                        mImageAdapter.insertItem(
                            UserImage(
                                respons.mediaId,
                                "",
                                respons.imageUrl,
                                ""
                            )
                        )
                    }

                    mImageAdapter.notifyDataSetChanged()
                    for (response in it.data!![0].lookingForRelationType!!) {
                        lookingFor?.add(response.relationshipStatus)
                    }
                    addLookingFor()
                }
            }
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


        viewModel.statusCodeLiveData.observe(this) { serverError ->
            (activity as BaseActivity<*>).handleApiStatusCodeError(serverError)
        }

        localDatabaseImages()
    }

//    private fun setData() {
//        mImageAdapter.removeAll()
//        if (userInfo!!.userImages == null) {
//            if (!sharedPreference.userDetail?.profileImage.equals("")) {
//                mImageAdapter.insertItem(
//                    UserImage("", "", userInfo!!.profileImage, "")
//                )
//                binding.mTabLayout.visibility = View.VISIBLE
//                mImageAdapter.notifyDataSetChanged()
//
//            }
//        } else {
//            if (!sharedPreference.userDetail?.profileImage.equals("")) {
//                mImageAdapter.insertItem(
//                    UserImage("", "", userInfo!!.profileImage, "")
//                )
//            }
//            if (userInfo!!.userImages != null && userInfo!!.userImages!!.size > 0) {
//
//                binding.mTabLayout.visibility = View.VISIBLE
//                mImageAdapter.insertAllItem(userInfo!!.userImages!!)
//                mImageAdapter.notifyDataSetChanged()
//            } else {
//
//                binding.mTabLayout.visibility = View.GONE
//            }
//        }
//
//        binding.executePendingBindings()
//        mImageAdapter.notifyDataSetChanged()
//
//    }


    override fun onItemClick(viewId: Int, position: Int, childPosition: Int?) {

    }

    override fun onLoadMore(itemCount: Int, nextPage: Int) {

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