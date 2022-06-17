package com.app.signme.dataclasses.response

import com.fasterxml.jackson.annotation.JsonProperty

data class PurchasedSubscription(
    @JsonProperty("product_id")
    val productId: String? = null,
    @JsonProperty("subscription_status")
    val subscriptionStatus: String? = null,
    @JsonProperty("purchase_token")
    val purchaseToken: String? = null
)