package com.app.signme.view.bottomsheet

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.Nullable
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.core.BaseBottomSheetDialog
import com.app.signme.dagger.components.FragmentComponent
import com.app.signme.databinding.BottomSheetAbusiveReportBinding
import com.app.signme.dataclasses.Reason
import com.app.signme.viewModel.AbusiveReportViewModel


class AbusiveReportBSD(
    mListener: ClickListener?,
    var userid:String="",
    val mActivity: Activity
    ): BaseBottomSheetDialog<AbusiveReportViewModel>(), AdapterView.OnItemSelectedListener  {
    var adapter: ArrayAdapter<String>? = null
    var dataBinding: BottomSheetAbusiveReportBinding? = null
    var selectedReason: Reason? = null
    var reportReason = ArrayList<Reason>()
    var spinnerid:String? = null
    var selectedReasonId:Reason?=null
    var selectedPosition:Int=0

    interface ClickListener {
        fun onClick(viewId: Int?)
    }

    private var listener: AbusiveReportBSD.ClickListener? = mListener

    override fun provideLayoutId(): Int {
        return R.layout.bottom_sheet_abusive_report
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = BottomSheetDialog(requireContext(), theme)
//        dialog.setOnShowListener {
//
//            val bottomSheetDialog = it as BottomSheetDialog
//            val parentLayout =
//                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//            parentLayout?.let { it1 ->
//                val behaviour = BottomSheetBehavior.from(it1)
//                setupFullHeight(it1)
//                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
//            }
//        }
//        return dialog
//    }
//
//    /**
//     * This method is responsible to open bottom sheet in full screen.
//     */
//    fun setupFullHeight(bottomSheet: View) {
//        val layoutParams = bottomSheet.layoutParams
//        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
//        bottomSheet.layoutParams = layoutParams
//    }
    override fun injectDependencies(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun setupView(view: View) {
        setFireBaseAnalyticsData(
            "id-reportscreen",
            "view-reportscreen",
            "view-reportscreen"
        )
        dataBinding = DataBindingUtil.bind(view)!!


        dataBinding!!.apply {
            viewModel.inputTextSizeLiveData.postValue("0")
            tietReason.doAfterTextChanged { text ->
                viewModel.inputTextSizeLiveData.value = text?.toString()!!.length.toString()
            }
        }
        adapter = ArrayAdapter(
            mActivity,
            R.layout.dropdown_menu_popup_item,
            arrayListOf<String>()
        )
        dataBinding!!.dropdownEditable.setAdapter(adapter)

        dataBinding!!.dropdownEditable.onItemSelectedListener = this

        showProgressDialog()
        viewModel.callGetReportReason(IConstants.REPORT_TYPE_USER)

        setupObservers()
        initListener()
    }


    override fun setupObservers() {
        super.setupObservers()
        viewModel.reasonLiveData.observe(this) { response ->
            hideProgressDialog()
            adapter!!.clear()
            reportReason.clear()
            if(response==null)
            {
                return@observe
            }
            if (response.settings?.isSuccess == true) {
                if (response.data != null && response.data!!.size > 0) {
                    reportReason = response.data!!
                    adapter!!.add("Select Reason")
                    for ((index,reason) in reportReason.withIndex()) {
                        adapter!!.add(reason.reasonName)

                        if (reason.reasonName.equals("other", true)) {
                           // dataBinding!!.dropdownEditable.setText(adapter!!.getItem(index),false)
                            selectedReason = reason
                        }
                    }
                }
            }
        }

        viewModel.reportLiveData.observe(this) { response ->
            hideProgressDialog()
            if (response != null&&response.settings?.isSuccess == true) {
                viewModel.reportLiveData.postValue(null)
                this.dismiss()
                onCancel(dialog!!)
                showMessage(response.settings!!.message, IConstants.SNAKBAR_TYPE_SUCCESS)
            }
        }

        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
        }
    }

    override fun initListener() {
        dataBinding?.apply {

            btnSubmit.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Submit Button Click")
                sendReport()

            }
            imgClose.setOnClickListener{
                dismiss()
                onCancel(dialog!!)
            }
        }

    }

    private fun sendReport() {


        if(selectedPosition==0)
        {
            showMessage(R.string.please_enter_add_reason)
            return
        }

        if (dataBinding!!.tietReason.text.toString().isEmpty()) {
            showMessage(R.string.alert_add_reason)
            return
        }

        /********report_abusive_user url*********/
        val map = HashMap<String, String>()
        map["reason_id"] = selectedReasonId!!.reasonId.toString()
        map["report_on"]=userid
        map["message"] = dataBinding!!.tietReason.text.toString()
        map["reason_description"] = dataBinding!!.tietReason.text.toString()
        showProgressDialog()
        viewModel.callReportAbusiveUser(map)
    }


    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener?.onClick(null)

    }
    private fun showProgressDialog() {
        dataBinding!!.progressBar.visibility=View.VISIBLE
        dataBinding!!.btnSubmit.visibility=View.GONE
    }

    private fun hideProgressDialog() {
        dataBinding!!.progressBar.visibility=View.GONE
        dataBinding!!.btnSubmit.visibility=View.VISIBLE
    }

    override fun setDataBindingLayout() {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
         spinnerid = parent?.getItemAtPosition(position).toString()
         selectedPosition=position

        selectedReasonId = reportReason.find {
            it.reasonName == spinnerid
        }

    }
    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}