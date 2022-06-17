package com.app.signme.view.settings.staticpages

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.signme.R

class StaticPagesActivity : AppCompatActivity() {
    companion object {

        /**
         * Start intent to open StaticPagesActivity
         * @param mContext [ERROR : Context]
         * @param pageCode String Code of static page, which need to show
         * @return Intent
         */
        fun getStartIntent(mContext: Context, pageCode: String, forceUpdate: Boolean = false): Intent {
            return Intent(mContext, StaticPagesActivity::class.java).apply {
                putExtra("page_code", pageCode)
                putExtra("force_update", forceUpdate)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_static_pages)
    }
}