package com.app.signme.view.subscription

import com.app.signme.dataclasses.SubscriptionPlan


interface SubscriptionClickListener {
    fun onSubscriptionClick(position:Int , data: SubscriptionPlan)
}