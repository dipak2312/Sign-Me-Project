package com.app.signme.view.subscription


import com.app.signme.R

enum class CustomPagerEnum(val titleResId: Int, val layoutResId: Int) {
    TWO(
            R.string.TWO,
            R.layout.layout_two_premium_user_view
    ),

    THREE(
            R.string.THREE,
            R.layout.layout_four_premium_user_view
    );


}