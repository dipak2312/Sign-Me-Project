package com.app.signme.view

import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.app.signme.databinding.DialogCustomBinding
import com.app.signme.dataclasses.ImageCustomDialog
import com.hb.logger.Logger

/**
 * @author Anita Chipkar
 * By using this dialog class we can show all type of alert messages
 * to user with custom view
 */
class CustomDialog(
    var title: String = "",
    var message: String = "",
    var positiveButtonText: String = "",
    var negativeButtonText: String = "",
    var cancellable: Boolean = false,
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
    private var binding: DialogCustomBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = cancellable
        return inflater.inflate(R.layout.dialog_custom, container, false)
    }


    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        if (title.isEmpty()) {
            title = getString(R.string.app_name)
        }
        binding?.apply {
            dialog = ImageCustomDialog(title, message, positiveButtonText, negativeButtonText)
            btnPositive.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Yes Button Click")
                listener?.onSuccess()
                dismiss()
            }

            btnNegative.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "No Button Click")
                listener?.onCancel()
                dismiss()
            }

            mLayoutRoot.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Root View Click")
                if (cancellable) {
                    dismiss()
                }

            }
        }
    }
}