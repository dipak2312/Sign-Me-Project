@file:Suppress("DEPRECATION")

package com.app.signme.commonUtils.utility.extension

import android.os.Build
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView

/**
 * get trimmedtext
 */
fun TextView.getTrimmedText(): String {
    return this.text.toString().trim()
}


interface TextChanges {
    fun onTextChange(text: String, view: View?)
}
/**
 * Edittext addOnTextChangeListener
 */
fun EditText.textChange(textChanges: TextChanges, view: View? = null) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            textChanges.onTextChange(p0.toString().trim(), view)
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }
    })
}

/**
 * to get the html formatted text
 */
fun String.getHtmlFormattedText(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}





