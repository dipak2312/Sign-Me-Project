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

import com.hb.logger.Logger

/**
 * @author Mahesh Lipane
 * By using this dialog class we can show all type of alert messages
 * to user with custom view
 */
class MatchesDialog(
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
    private var binding: DialogMatchesBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = false
        return inflater.inflate(R.layout.dialog_matches, container, false)
    }


    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!

        binding?.apply {

            btnSendMessage.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Done Button Click")
                listener?.onSuccess()
                dismiss()
            }
            btnKeepSwiping.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Done Button Click")
                listener?.onSuccess()
                dismiss()
            }
        }
    }
}