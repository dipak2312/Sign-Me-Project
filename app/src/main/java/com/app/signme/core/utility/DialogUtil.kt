package com.app.signme.core.utility

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.app.signme.R
import java.lang.Exception

object DialogUtil {

    interface IL {
        fun onSuccess()

        fun onCancel(isNeutral : Boolean)

    }

    fun createDialogWithoutBounds(activity: Activity, view: View): Dialog {
        val dialog = Dialog(activity, R.style.ProgressDialogTheme)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        setDialogMatchWindow(dialog)
        return dialog
    }

    private fun setDialogMatchWindow(dialog: Dialog) {
        val mWindowLayoutParams = WindowManager.LayoutParams()
        val mWindow = dialog.window
        mWindowLayoutParams.apply {
            copyFrom(mWindow!!.attributes)
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }
       mWindow!!.attributes = mWindowLayoutParams
    }

//    fun Long.timeToMinuteSecond(): String {
//        var millisUntilFinished: Long = this
//        val days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
//        millisUntilFinished -= TimeUnit.DAYS.toMillis(days)
//
//        val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
//        millisUntilFinished -= TimeUnit.HOURS.toMillis(hours)
//
//        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
//        millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes)
//
//        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
//
//        // Format the string
//        return String.format(
//                Locale.getDefault(),
//                "%02d", seconds
//        )
//    }

    private fun getBuilder(context: Context, isCancelable: Boolean = true): AlertDialog.Builder {
        val alertDialogBuilder = AlertDialog.Builder(context,R.style.DatePickerTheme)
        alertDialogBuilder.setCancelable(isCancelable)
        return alertDialogBuilder
    }
      fun alert(
            context: Context,
            title:String = "",
            msg:String = "",
            positiveBtnText:String = "Yes",
            negativeBtnText:String = "No",
            il: IL? = null,
            isCancelable : Boolean = true
          ) {

           try {
               val alertDialogBuilder = getBuilder(context)
               alertDialogBuilder.setTitle(title)
               alertDialogBuilder.setMessage(msg)
               alertDialogBuilder.setCancelable(isCancelable)
               if(positiveBtnText.isNotEmpty()){
                  alertDialogBuilder.setPositiveButton(
                          positiveBtnText
                  ) {
                      dialog, _ ->
                      il?.onSuccess()
                      dialog.dismiss()
                  }
               }
               if(negativeBtnText.isNotEmpty()) {
                  alertDialogBuilder.setNegativeButton(
                          negativeBtnText
                  ) {
                      dialog, _ ->
                      il?.onCancel(false)
                      dialog.dismiss()
                  }
               }

               alertDialogBuilder
                       .setOnKeyListener { dialog, keyCode, _ ->
                           if (keyCode == KeyEvent.KEYCODE_BACK) {
                               il?.onCancel(true)
                               dialog.dismiss()
                           }
                           false
                       }
               val alertDialog = alertDialogBuilder.create()

               alertDialog.show()
               alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
               alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK)

           }catch (ex : Exception){
               ex.printStackTrace()
           }

    }
}