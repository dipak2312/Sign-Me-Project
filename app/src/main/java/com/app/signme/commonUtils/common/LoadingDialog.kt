package com.app.signme.commonUtils.common

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.app.signme.R
import timber.log.Timber


class LoadingDialog constructor(activity: Activity) : Dialog(activity),
    DialogInterface.OnShowListener {
    private var SHOWS = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_loading)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setOnShowListener(this)
    }

    override fun onShow(dialog: DialogInterface) {
        try {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        } catch (e: Exception) {
            Timber.e(e.message.toString())
        }

    }

    companion object {

        private var INSTANCE: LoadingDialog? = null
        private var isShowPending = false

        fun bindWith(activity: Activity) {
            if (INSTANCE != null && INSTANCE?.isShowing == true)
                INSTANCE?.dismiss()
            INSTANCE = LoadingDialog(activity)
            if (isShowPending) {
                isShowPending = false
                showDialog()
            }
        }

        fun unbind() {
            if (INSTANCE != null && INSTANCE?.isShowing == true)
                INSTANCE?.dismiss()
            INSTANCE = null
        }

        fun showDialog() {
            if (INSTANCE != null) {
                if (INSTANCE?.isShowing == false && INSTANCE?.SHOWS == 0)
                    try {
                        INSTANCE?.show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                INSTANCE?.SHOWS = INSTANCE?.SHOWS?.plus(1) ?: 1
            } else {
                isShowPending = true
            }
        }


       /* fun dismissDialog() {
            if (INSTANCE != null) {
                if (INSTANCE?.isShowing == true && (INSTANCE?.SHOWS == 1 || INSTANCE?.SHOWS == 0))
                    INSTANCE?.dismiss()
                INSTANCE?.SHOWS = INSTANCE?.SHOWS?.minus(1) ?: 0
            }
        }*/
    }
}