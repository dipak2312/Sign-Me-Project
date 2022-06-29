package com.app.signme.scheduler.aws.cacheUtils

import android.content.SharedPreferences
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Reflection utils to call SharedPreferences$Editor.apply when possible,
 * falling back to commit when apply isn't available.
 */
object CacheCompat {
    private val sApplyMethod = findApplyMethod()
    private fun findApplyMethod(): Method? {
        try {
            val cls: Class<*> = SharedPreferences.Editor::class.java
            return cls.getMethod("apply")
        } catch (unused: NoSuchMethodException) {
            // fall through
        }
        return null
    }

    @JvmStatic
    fun apply(editor: SharedPreferences.Editor) {
        if (sApplyMethod != null) {
            try {
                sApplyMethod.invoke(editor)
                return
            } catch (unused: InvocationTargetException) {
                // fall through
            } catch (unused: IllegalAccessException) {
            }
        }
        editor.commit()
    }
}