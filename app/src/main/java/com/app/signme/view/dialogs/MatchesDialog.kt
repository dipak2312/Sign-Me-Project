package com.app.signme.view.dialogs

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.databinding.DialogMatchesBinding
import com.app.signme.dataclasses.SwiperViewResponse
import com.app.signme.view.chat.ChatRoomActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

import com.hb.logger.Logger

/**
 * @author Mahesh Lipane
 * By using this dialog class we can show all type of alert messages
 * to user with custom view
 */
class MatchesDialog(
    response: SwiperViewResponse,
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
    var res=response

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
       // binding!!.btnSendMessage.text="Send"+" "+res.name+" "+getString(R.string.label_send_message)
        binding!!.textMatchDesc.text="You and"+" "+res.name+" "+getString(R.string.label_like_each_other)
        Glide.with(this)
            .load(res.profileImage)
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .centerCrop()
            .error(R.drawable.ic_no_image)
            .placeholder(R.drawable.ic_empty_img)
            .into(binding!!.imgOtherUser)
        Glide.with(this)
            .load(sharedPreference.userDetail!!.profileImage)
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .centerCrop()
            .error(R.drawable.ic_no_image)
            .placeholder(R.drawable.ic_empty_img)
            .into(binding!!.imgMy)

        binding?.apply {

            btnSendMessage.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Done Button Click")
                listener?.onSuccess()
                dismiss()
            }
            btnKeepSwiping.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Done Button Click")
                listener?.onCancel()
                dismiss()
            }
        }
    }
}