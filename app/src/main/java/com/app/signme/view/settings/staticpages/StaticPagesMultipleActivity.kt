package com.app.signme.view.settings.staticpages

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityStaticPagesMultipleBinding
import com.app.signme.dataclasses.response.StaticPage
import com.app.signme.core.BaseActivity
import com.app.signme.viewModel.StaticPagesViewModel
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants

class StaticPagesMultipleActivity : BaseActivity<StaticPagesViewModel>() {
    private var internetConnection: Boolean = false
    private var totalSize: Int = 0
    private var forceUpdate = false
    private var pageCodeList: ArrayList<StaticPage> = ArrayList()
    private var currentIndex: Int = 0
    private var iterationNeeded: Int = 0
    private lateinit var binding: ActivityStaticPagesMultipleBinding

    companion object {
        /**
         * Start intent to open StaticPagesMultipleActivity
         * @param mContext [ERROR : Context]
         * @param pageCode String Code of static page, which need to show
         * @return Intent
         */
        fun getStartIntent(
            mContext: Context,
            pageCodeList: ArrayList<StaticPage> = ArrayList()
        ): Intent {
            return Intent(mContext, StaticPagesMultipleActivity::class.java).apply {
                putExtra("page_code_list", pageCodeList)
            }
        }
    }

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_static_pages_multiple)
        binding.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        addObserver()
        pageCodeList =
            intent?.getParcelableArrayListExtra<StaticPage>("page_code_list")
                ?: ArrayList<StaticPage>()
        binding.apply {
            srl.setOnRefreshListener {
                when {
                    checkInternet() -> initializeDataAndCallApi(false)
                }
            }
           /* tvStaticData.movementMethod = LinkMovementMethod.getInstance()
            tvStaticData.setLinkTextColor(
                ContextCompat.getColor(
                    this@StaticPagesMultipleActivity,
                    R.color.black
                )
            )*/

            ibtnBack.setOnClickListener { finish() }

            btnAgree.setOnClickListener {
                if (!pageCodeList[currentIndex].pageCode.isNullOrEmpty() && checkInternet()) {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "Agree"
                    )
                    showProgressDialog(isCheckNetwork = true, isSetTitle = false, title = IConstants.EMPTY_LOADING_MSG)
                    viewModel.callUpdateTNCPrivacyPolicy(pageCodeList[currentIndex].pageCode!!)
                } else {
                    hideProgressDialog()
                }
            }

        }

        /*Call Static Page Api*/
        if (!pageCodeList.isNullOrEmpty() && checkInternet()) {
            totalSize = pageCodeList.size
            if (totalSize == 2) {
                iterationNeeded = 2
                currentIndex = 0
            } else if (totalSize == 1) {
                iterationNeeded = 1
                currentIndex = 0
            }
            if (currentIndex < totalSize) {
                initializeDataAndCallApi(true)
            }

        } else {
            currentIndex = 0
            iterationNeeded = 0
            totalSize = 0
        }
    }

    private fun addObserver() {
        viewModel.staticPageResponseLiveData.observe(this) {
            hideProgressDialog()
            binding.srl.isRefreshing = false
            when (it.settings?.isSuccess) {
                true -> {
                    if (!it.data.isNullOrEmpty()) {
                        MSCGenerator.addAction(
                            GenConstants.ENTITY_USER,
                            GenConstants.ENTITY_APP,
                            "Updated"
                        )
                        binding.tvScreenName.text = it.data!![0].pageTitle
                        if (it.data!!.size > 0) {
                            var urldata =
                                "<font size='3'color='#ffffff'>" + it.data!![0].content!! + "</font>"
                            binding.webView.loadDataWithBaseURL(
                                null,
                                urldata,
                                "text/html; charset=utf-8",
                                "utf8",
                                null
                            )

                            binding.webView.settings.javaScriptEnabled
                            binding.webView.setBackgroundColor(Color.TRANSPARENT)
                        }
                    } else {
                        MSCGenerator.addAction(
                            GenConstants.ENTITY_USER,
                            GenConstants.ENTITY_APP,
                            "Updated failed"
                        )
                        showMessage(it?.settings!!.message)
                        binding.btnAgree.visibility = View.INVISIBLE
                    }
                }
                false -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "Updated failed"
                    )
                    showMessage(it?.settings!!.message)
                    binding.btnAgree.visibility = View.INVISIBLE
                }
                else -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "Updated failed"
                    )
                    showMessage(it?.settings!!.message)
                    binding.btnAgree.visibility = View.INVISIBLE
                }
            }
        }

        viewModel.updateTNCResponseLiveData.observe(this) {
            hideProgressDialog()
            if (it?.settings?.isSuccess == true) {
                if (totalSize > 1 && iterationNeeded == 2) {
                    currentIndex++
                    if (currentIndex < totalSize) {
                        initializeDataAndCallApi(true)
                    }
                    if (currentIndex == iterationNeeded) {
                        finish()
                    }
                } else {
                    finish()
                }

            } else {
                showMessage(it?.settings!!.message)
            }
        }


        viewModel.checkForInternetConnectionLiveData.observe(this) { isInternetAvailable ->
            internetConnection = isInternetAvailable
        }
        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            handleApiStatusCodeError(serverError)
        }
    }

    private fun initializeDataAndCallApi(progressStatus:Boolean) {
        pageCodeList[currentIndex].forceUpdate?.let { forceUpdate = it }
        binding.ibtnBack.visibility = if (forceUpdate) View.INVISIBLE else View.VISIBLE
        binding.btnAgree.visibility = if (forceUpdate) View.VISIBLE else View.INVISIBLE
        if (!pageCodeList[currentIndex].pageCode.isNullOrEmpty()) {
            when {
                checkInternet() -> {
                    if(progressStatus)
                    {
                        showProgressDialog(
                            isCheckNetwork = true,
                            isSetTitle = false,
                            title = IConstants.EMPTY_LOADING_MSG
                        )
                    }

                    viewModel.callStaticPage(pageCodeList[currentIndex].pageCode!!)
                }
            }
        } else {
           hideProgressDialog()

        }
    }

   /* *//**
     * to get the html formatted text
     *//*
    fun String.getHtmlFormattedText(): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY);
        } else {
            Html.fromHtml(this)
        }
    }*/

    override fun onBackPressed() {
        if (!forceUpdate)
            super.onBackPressed()
    }
}