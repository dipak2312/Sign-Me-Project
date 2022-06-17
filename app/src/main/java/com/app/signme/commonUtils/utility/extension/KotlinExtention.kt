@file:Suppress(
    "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS",
    "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS"
)

package com.app.signme.commonUtils.utility.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory.decodeFile
import android.net.Uri
import android.telephony.PhoneNumberUtils
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.IConstants.Companion.SNAKBAR_TYPE_ERROR
import com.app.signme.commonUtils.utility.IConstants.Companion.SNAKBAR_TYPE_MESSAGE
import com.app.signme.commonUtils.utility.IConstants.Companion.SNAKBAR_TYPE_SUCCESS
import com.app.signme.commonUtils.utility.helper.LOGApp
import com.app.signme.dataclasses.generics.Settings
import com.app.signme.db.AppPrefrrences
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import android.widget.TextView





val sharedPreference: AppPrefrrences by lazy {
    AppineersApplication.sharedPreference
}

var snackbar: Snackbar? = null

fun parseBoolean(type: String): Boolean {
    return type == "1"
}

fun String.getNumberOnly(): String {
    return this.replace("[^0-9]", "")
}

fun String.showSnackBar(context: Activity?) {
    showSnackBar(context, SNAKBAR_TYPE_ERROR, null)
}

@SuppressLint("CutPasteId")
fun String.showSnackBar(
    context: Activity?,
    type: Int,
    action: String? = null,
    dismissListener: View.OnClickListener? = null,
    duration: Int = Snackbar.LENGTH_LONG
) {
    if (context != null) {
//        val logger = Logger(this::class.java.simpleName)
        var color = context.resources.getColor(R.color.colorSnackBarError)
        var drawable = R.drawable.ic_snackbar_error
        var actionMessage =context.resources.getString(R.string.app_name)
        when (type) {
            SNAKBAR_TYPE_ERROR -> {
                color = context.resources.getColor(R.color.colorSnackBarError)
                drawable = R.drawable.ic_snackbar_error
                actionMessage = context.resources.getString(R.string.app_name)


            }
            SNAKBAR_TYPE_SUCCESS -> {
                color = context.resources.getColor(R.color.colorSnackBarSuccess)
                drawable = R.drawable.ic_snackbar_success
                actionMessage = context.resources.getString(R.string.app_name)
//                logger.debugEvent("Success Message", this, CustomLog.STATUS_SUCCESS)
            }
            SNAKBAR_TYPE_MESSAGE -> {
                color = context.resources.getColor(R.color.colorSnackBarAlert)
                drawable =R.drawable.ic_snackbar_alert
                actionMessage = context.resources.getString(R.string.str_alert_message)
//                logger.debugEvent("User Message", this)
            }
        }

        if (((snackbar?.isShown == true) || (snackbar?.isShown() == true))) {
            snackbar?.dismiss()
        }
        //Call function custom snackBar
        showCustomSnackBar(context,action,dismissListener,this,duration,color,
            drawable, actionMessage.toString()
        )

       /*  val snackBar = Snackbar.make(context.findViewById(android.R.id.content), this, duration)
             .setBackgroundTint(context.resources.getColor(color))
             .setTextColor(Color.WHITE)
         if (action != null && dismissListener != null) {
             snackBar.setAction(action, dismissListener)
         }
         val view: View = snackBar.view
         val params: FrameLayout.LayoutParams = view.layoutParams as FrameLayout.LayoutParams
         params.gravity = Gravity.TOP
         params.setMargins(160, 0, 0, 0)
         val textView =
             view.findViewById(com.google.android.material.R.id.snackbar_text) as (TextView)
         textView.maxLines = 5
         view.layoutParams = params
         snackbar = snackBar
         snackbar?.show()*/



    }

}

/**
 * Function to call Custom snackBar layout on Success and Error Messages
 */
fun showCustomSnackBar(context: Activity?,action: String?,
                       dismissListener: View.OnClickListener?,
                       message: String, duration: Int,
                       color: Int, drawable: Int, actionMessage: String)
{
    /*val objLayoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )*/
     val snack =
        context?.let {

          /*  Snackbar.make(context.window.decorView.findViewById(android.R.id.content),
                "", duration)*/
            Snackbar.make(it.findViewById(android.R.id.content), "", duration)
           // .setBackgroundTint(context.resources.getColor(color))
            //.setTextColor(Color.WHITE)
        }

    if (action != null && dismissListener != null) {
        snack?.setAction(action, dismissListener)
    }
    val layout = snack?.view as Snackbar.SnackbarLayout
    val textView =
        layout.findViewById(com.google.android.material.R.id.snackbar_text) as (TextView)
    textView.visibility = View.INVISIBLE

    val parentParams = layout.layoutParams as FrameLayout.LayoutParams
    parentParams.gravity = Gravity.TOP
    parentParams.setMargins(0, 0, 0, 0)
    val snackView: View = LayoutInflater.from(context).inflate(R.layout.custom_snackbar, null)

    val actionIcon = snackView.findViewById<View>(R.id.ivSnackBar) as ImageView
    actionIcon.setImageResource(drawable)
    val cardSnackBar = snackView.findViewById<View>(R.id.cvCardSnackBar) as CardView
    cardSnackBar.setCardBackgroundColor(color)
    val messageTitle = snackView.findViewById<View>(R.id.tvSnackBarTitle) as TextView
    messageTitle.text = actionMessage
    val messageTextView = snackView.findViewById<View>(R.id.tvSnackbarSubTitle) as TextView
    messageTextView.text = message
    messageTextView.maxLines = 5
    layout.setPadding(0,0,0,0);
    layout.addView(snackView, 0);
    layout.setBackgroundColor(context.resources.getColor(R.color.transparent));
  //  layout.addView(snackView, objLayoutParams)
    snackbar = snack
    snackbar?.show()
}


fun String.showNoInternetMessage(context: Activity?) {
    this.showSnackBar(context, SNAKBAR_TYPE_MESSAGE, null)
}

fun String.showNoInternetMessage(context: Context?) {
    if (context is Activity)
        this.showSnackBar(context, SNAKBAR_TYPE_MESSAGE, null)
}


fun EditText.getTrimText(): String {
    return this.text?.toString()?.trim() ?: ""
}

fun String.isValidFloat(): Boolean {
    val valid: Boolean = try {
        this.toDouble()
        true
    } catch (ex: Exception) {
        false
    }
    return valid
}

fun getJsonDataFromAsset(context: Context, fileName: String): String? {
    val jsonString: String
    try {
        jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return null
    }
    return jsonString
}

fun String.isValidInt(): Boolean {
    val valid: Boolean = try {
        this.toInt()
        true
    } catch (ex: Exception) {
        false
    }
    return valid
}

fun String.isZero(): Boolean {
    return if (this.isEmpty()) true
    else this.isValidFloat() && this.toDouble() == 0.0
}


fun String.getFailureSettings(code: String = "0"): Settings {
    val settings = Settings()
    settings.success = code
    settings.message = this
    return settings
}


/**
 * This function will return formatted phone number according to given country
 * @countryCode : the ISO 3166-1 two letters country code whose convention will be used if the phoneNumberE164 is null or invalid, or if phoneNumber contains IDD.
 */
fun String.getFormattedPhoneNumber(countryCode: String): String {
    return PhoneNumberUtils.formatNumber(this, "E164", countryCode)
}


fun getAppVersion(context: Context?, withoutLabel: Boolean = false): String {
    try {
        val pInfo = context?.packageManager?.getPackageInfo(context.packageName, 0)
        return if (withoutLabel) pInfo?.versionName ?: "" else "Version: " + pInfo?.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return ""
}

fun String.makePhoneCall(context: Activity?) {
    try {
        context?.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", this, null)))
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun String.openWebBrowser(context: Activity?) {
    val url = if (!this.startsWith("http://") && !this.startsWith("https://"))
        "http://$this";
    else
        this
    try {
        context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun String.removeBR(): String {
    return this.replace("<br/>", " ")
}

fun Int.getDurationFromSecond(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60

    return String.format("%02d h %02d min", hours, minutes, seconds)
}

fun String?.parseInt(defaultVal: Int = 0): Int {

    return when {
        this.isNullOrBlank() -> defaultVal
        this.isValidInt() -> this.toInt()
        else -> defaultVal
    }
}

/**
 * Encode giving string into base64
 */
fun String.encodeToBase64(): String {
    try {
        val data = this.toByteArray(charset("UTF-8"))
        LOGApp.i("Base 64 ", Base64.encodeToString(data, Base64.DEFAULT))
        return Base64.encodeToString(data, Base64.DEFAULT)
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
    }
    return ""
}

/**
 * Decode giving string into base64
 */
fun String.decodeToBase64(): String {
    return String(Base64.decode(this, Base64.DEFAULT))
}

/**Convert UTC time to Local*/
fun String?.utcToLocal(): String {
    LOGApp.e("UTC Date:-$this")
    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    df.timeZone = TimeZone.getTimeZone("UTC")
    return try {
        val date = df.parse(this)
        LOGApp.e("Local Date:-$date")
        date.timeAgo()
    } catch (e: Exception) {
        e.printStackTrace()
        this ?: ""
    }
}

/**Time Ago Logic**/
fun Date.timeAgo(): String {
    LOGApp.e("millis:- $this")
    val different = Date().time - this.time
    LOGApp.e("Difference in time:- $different")
    val seconds = different.div(1000)
    LOGApp.e("seconds$seconds")
    val secondsInMilli = 1
    val minutesInSec = secondsInMilli * 60 //60
    val hoursInSec = minutesInSec * 60   //3600
    val daysInSec = hoursInSec * 24  // 86400
    val weekInSec = daysInSec * 7   // 604800
    val yearInSec = weekInSec * 52
    return when {
        seconds < minutesInSec -> "Just now"
        seconds < hoursInSec -> seconds.div(minutesInSec).toString() + " min ago"
        seconds < daysInSec -> seconds.div(hoursInSec).toString() + " hour ago"
        seconds < weekInSec -> seconds.div(daysInSec).toString() + " day ago"
        seconds < yearInSec -> seconds.div(weekInSec).toString() + " week ago"
        else -> seconds.div(yearInSec).toString() + " year ago"
    }
}

fun getObjectFromJsonString(jsonData: String, modelClass: Class<*>): Any? {
    return try {
        GsonBuilder().create().fromJson(jsonData, modelClass)
    } catch (e: java.lang.Exception) {
        null
    }
}

fun readStringFileFromAsset(context: Context, fileName: String): String {
    return try {
        val inputStream = context.assets.open(fileName)
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        String(buffer, Charsets.UTF_8)
    } catch (ex: IOException) {
        ex.printStackTrace()
        ""
    }
}

suspend fun Activity.compressImageFile(
    uri: Uri
): File? {
    return withContext(Dispatchers.IO) {
        try {
            // Part 1: Decode image
            val unscaledBitmap =
                decodeFile(uri.path, null)

            // Store to tmp file
            val mFolder = File("$cacheDir")
            if (!mFolder.exists()) {
                mFolder.mkdir()
            }

            val tmpFile = File(mFolder.absolutePath, "IMG_${getTimestampString()}.png")
            if (!tmpFile.exists())
                tmpFile.createNewFile()

            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(tmpFile)
                unscaledBitmap?.compress(
                    Bitmap.CompressFormat.PNG,
                    getImageQualityPercent(tmpFile),
                    fos
                )
                fos.flush()
                fos.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return@withContext tmpFile
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return@withContext null
    }
}

fun getTimestampString(): String {
    val date = Calendar.getInstance()
    return SimpleDateFormat("yyyy MM dd hh mm ss", Locale.US).format(date.time)
        .replace(" ", "")
}

fun getImageQualityPercent(file: File): Int {
    val sizeInBytes = file.length()
    val sizeInKB = sizeInBytes / 1024
    val sizeInMB = sizeInKB / 1024

    return when {
        sizeInMB <= 1 -> 80
        sizeInMB <= 2 -> 60
        else -> 40
    }
}
