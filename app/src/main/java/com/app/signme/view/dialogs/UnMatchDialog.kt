package com.app.signme.view.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.databinding.DialogMatchesBinding
import com.app.signme.databinding.DialogUnMatchesBinding
import com.hb.logger.Logger
import kotlinx.android.synthetic.main.dialog_un_matches.*

class UnMatchDialog (
    name:String?,
    mListener: ClickListener?
) : DialogFragment() {
    interface ClickListener {
        fun onSuccess()
        fun onCancel()
    }

    val logger by lazy {
        Logger(this::class.java.simpleName)
    }
    private var listener: ClickListener? = mListener
    private var binding: DialogUnMatchesBinding? = null
    var userName:String?=name

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = false
        return inflater.inflate(R.layout.dialog_un_matches, container, false)
    }


    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!

        binding!!.textUnmatchUser.text=getString(R.string.label_unmatch)+" "+userName

        binding?.apply {

            btnNevermind.setOnClickListener {

                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Done Button Click")
                listener?.onCancel()
                dismiss()
            }
            btnUnmatch.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Done Button Click")
                listener?.onSuccess()
                dismiss()

            }
        }
    }
}