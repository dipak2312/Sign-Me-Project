package com.app.signme.scheduler.aws.cacheUtils

import android.content.SharedPreferences
import com.app.signme.scheduler.aws.cacheUtils.CacheCompat.apply
import org.json.JSONArray
import org.json.JSONException
import java.util.*

object CacheUtils {
    @JvmStatic
    fun setStringArrayPref(prefs: SharedPreferences, key: String?, values: ArrayList<String>) {
        val editor = prefs.edit()
        val a = JSONArray()
        for (i in values.indices) {
            a.put(values[i])
        }
        if (values.isNotEmpty()) {
            editor.putString(key, a.toString())
        } else {
            editor.putString(key, null)
        }
        apply(editor)
    }

    @JvmStatic
    fun getStringArrayPref(prefs: SharedPreferences, key: String?): ArrayList<String> {
        val json = prefs.getString(key, null)
        val values = ArrayList<String>()
        if (json != null) {
            try {
                val a = JSONArray(json)
                for (i in 0 until a.length()) {
                    val `val` = a.optString(i)
                    values.add(`val`)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return values
    }
}