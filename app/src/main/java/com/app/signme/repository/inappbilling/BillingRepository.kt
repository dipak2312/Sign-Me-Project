package com.app.signme.repository.inappbilling

import android.app.Activity
import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.*
import com.app.signme.commonUtils.utility.helper.LOGApp
import com.app.signme.repository.inappbilling.BillingRepository.AppSku.ADD_FREE
import com.app.signme.repository.inappbilling.BillingRepository.AppSku.CONSUMABLE_SKUS
import com.app.signme.repository.inappbilling.BillingRepository.AppSku.INAPP_SKUS
import com.app.signme.repository.inappbilling.BillingRepository.AppSku.ONE_MONTH
import com.app.signme.repository.inappbilling.BillingRepository.AppSku.SIX_MONTH
import com.app.signme.repository.inappbilling.BillingRepository.AppSku.SUBS_SKUS
import com.app.signme.repository.inappbilling.BillingRepository.AppSku.THREE_MONTH
import com.hb.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class BillingRepository @Inject constructor(private val application: Application) :
    PurchasesUpdatedListener, BillingClientStateListener {

    /**
     * The [BillingClient] is the most reliable and primary source of truth for all purchases
     * made through the Google Play Store. The Play Store takes security precautions in guarding
     * the data. Also, the data is available offline in most cases, which means the app incurs no
     * network charges for checking for purchases using the [BillingClient]. The offline bit is
     * because the Play Store caches every purchase the user owns, in an
     * [eventually consistent manner](https://developer.android.com/google/play/billing/billing_library_overview#Keep-up-to-date).
     * This is the only billing client an app is actually required to have on Android. The other
     * two (webServerBillingClient and localCacheBillingClient) are optional.
     *
     * ASIDE. Notice that the connection to [playStoreBillingClient] is created using the
     * applicationContext. This means the instance is not [Activity]-specific. And since it's also
     * not expensive, it can remain open for the life of the entire [Application]. So whether it is
     * (re)created for each [Activity] or [Fragment] or is kept open for the life of the application
     * is a matter of choice.
     */
    private lateinit var playStoreBillingClient: BillingClient

    var addFreeSKU = MutableLiveData<SkuDetails>()
    var subscriptionSKU = MutableLiveData<SkuDetails>()
    var subscriptionSKUList = MutableLiveData<List<SkuDetails>>()
    var inAppSKUList = MutableLiveData<List<SkuDetails>>()

    /**
     * Notify view that "Go Ad Free" in-app purchased by user and upload order data on server
     */
    var orderReceiptJsonForOneTime = MutableLiveData<String>()
    var orderReceiptJsonForSubscription = MutableLiveData<String>()
    var orderReceiptJsonForUpgradeDowngradeSubscription = MutableLiveData<String>()
    /**
     * Notify view that "Go Ad Free" in-app purchased by user and upload order data on server
     */
    var orderReceiptJson = MutableLiveData<String>()

    private val logger by lazy {
        Logger(this::class.java.simpleName)
    }


    /**
     * Correlated data sources belong inside a repository module so that the rest of
     * the app can have appropriate access to the data it needs. Still, it may be effective to
     * track the opening (and sometimes closing) of data source connections based on lifecycle
     * events. One convenient way of doing that is by calling this
     * [startDataSourceConnections] when the [BillingViewModel] is instantiated and
     * [endDataSourceConnections] inside [ViewModel.onCleared]
     */
    fun startDataSourceConnections() {
        LOGApp.d(LOG_TAG, "startDataSourceConnections")
        instantiateAndConnectToPlayBillingService()
    }

    private fun endDataSourceConnections() {
        playStoreBillingClient.endConnection()
        // normally you don't worry about closing a DB connection unless you have more than
        // one DB open. so no need to call 'localCacheBillingClient.close()'
        LOGApp.d(LOG_TAG, "endDataSourceConnections")
    }

    private fun instantiateAndConnectToPlayBillingService() {
        playStoreBillingClient = BillingClient.newBuilder(application.applicationContext)
            .enablePendingPurchases() // required or app will crash
            .setListener(this).build()
        connectToPlayBillingService()
    }

    private fun connectToPlayBillingService(): Boolean {
        logger.debugEvent("Connect Play Billing Service", "Start connecting to play billing services")
        if (!playStoreBillingClient.isReady) {
            playStoreBillingClient.startConnection(this)
            return true
        }
        return false
    }

    /**
     * This is the callback for when connection to the Play [BillingClient] has been successfully
     * established. It might make sense to get [SkuDetails] and [Purchases][Purchase] at this point.
     */
    override fun onBillingSetupFinished(billingResult: BillingResult) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                LOGApp.d(LOG_TAG, "onBillingSetupFinished successfully")
                logger.debugEvent("Billing setup finished", "Billing setup successfully done")
                querySkuDetailsAsync(BillingClient.SkuType.INAPP, INAPP_SKUS)
                querySkuDetailsAsync(BillingClient.SkuType.SUBS, SUBS_SKUS)
                queryPurchasesAsync()
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                //Some apps may choose to make decisions based on this knowledge.
                logger.debugEvent("Billing setup finished", billingResult.debugMessage)
            }
            else -> {
                //do nothing. Someone else will connect it through retry policy.
                //May choose to send to server though
                logger.debugEvent("Billing setup finished", billingResult.debugMessage)
            }
        }
    }

    /**
     * This method is called when the app has inadvertently disconnected from the [BillingClient].
     * An attempt should be made to reconnect using a retry policy. Note the distinction between
     * [endConnection][BillingClient.endConnection] and disconnected:
     * - disconnected means it's okay to try reconnecting.
     * - endConnection means the [playStoreBillingClient] must be re-instantiated and then start
     *   a new connection because a [BillingClient] instance is invalid after endConnection has
     *   been called.
     **/
    override fun onBillingServiceDisconnected() {
        logger.debugEvent("Billing service connection", "Billing service disconnected.")
        connectToPlayBillingService()
    }

    /**
     * BACKGROUND
     *
     * Google Play Billing refers to receipts as [Purchases][Purchase]. So when a user buys
     * something, Play Billing returns a [Purchase] object that the app then uses to release the
     * [Entitlement] to the user. Receipts are pivotal within the [BillingRepositor]; but they are
     * not part of the repo’s public API, because clients don’t need to know about them. When
     * the release of entitlements occurs depends on the type of purchase. For consumable products,
     * the release may be deferred until after consumption by Google Play; for non-consumable
     * products and subscriptions, the release may be deferred until after
     * [BillingClient.acknowledgePurchaseAsync] is called. You should keep receipts in the local
     * cache for augmented security and for making some transactions easier.
     *
     * THIS METHOD
     *
     * [This method][queryPurchasesAsync] grabs all the active purchases of this user and makes them
     * available to this app instance. Whereas this method plays a central role in the billing
     * system, it should be called at key junctures, such as when user the app starts.
     *
     * Because purchase data is vital to the rest of the app, this method is called each time
     * the [BillingViewModel] successfully establishes connection with the Play [BillingClient]:
     * the call comes through [onBillingSetupFinished]. Recall also from Figure 4 that this method
     * gets called from inside [onPurchasesUpdated] in the event that a purchase is "already
     * owned," which can happen if a user buys the item around the same time
     * on a different device.
     */
    private fun queryPurchasesAsync() {
        LOGApp.d(LOG_TAG, "queryPurchasesAsync called")
        val purchasesResult = HashSet<Purchase>()
        var result = playStoreBillingClient.queryPurchases(BillingClient.SkuType.INAPP)
        logger.debugEvent("Purchase Async", "queryPurchasesAsync INAPP results: ${result?.purchasesList?.size}")
        result?.purchasesList?.apply { purchasesResult.addAll(this) }
        if (isSubscriptionSupported()) {
            result = playStoreBillingClient.queryPurchases(BillingClient.SkuType.SUBS)
            result?.purchasesList?.apply { purchasesResult.addAll(this) }
            logger.debugEvent("Purchase Async", "queryPurchasesAsync SUBS results: ${result?.purchasesList?.size}")
        }
        processPurchases(purchasesResult)
    }

    private fun processPurchases(purchasesResult: Set<Purchase>) =
        CoroutineScope(Job() + Dispatchers.IO).launch {
            LOGApp.d(LOG_TAG, "processPurchases called")
            val validPurchases = HashSet<Purchase>(purchasesResult.size)
            logger.debugEvent("Purchase Result", "processPurchases newBatch content $purchasesResult")
            purchasesResult.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
//                        if (isSignatureValid(purchase)) {
                    validPurchases.add(purchase)

                    if (purchase.sku == ADD_FREE) {
                        CoroutineScope(Dispatchers.Main).launch { orderReceiptJson.value = purchase.originalJson }
                    }
                    if (purchase.sku == ONE_MONTH) {
                        CoroutineScope(Dispatchers.Main).launch { orderReceiptJsonForSubscription.value = purchase.originalJson }
                    }
                    if (purchase.sku == THREE_MONTH) {
                        CoroutineScope(Dispatchers.Main).launch { orderReceiptJsonForSubscription.value = purchase.originalJson }
                    }
  if (purchase.sku == SIX_MONTH) {
                        CoroutineScope(Dispatchers.Main).launch { orderReceiptJsonForSubscription.value = purchase.originalJson }
                    }

//                        }
                } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                    logger.debugEvent("Purchase Result Pending", "Received a pending purchase of SKU: ${purchase.sku}")
                    // handle pending purchases, e.g. confirm with users about the pending
                    // purchases, prompt them to complete it, etc.
                }
            }
            val (consumables, nonConsumables) = validPurchases.partition {
                CONSUMABLE_SKUS.contains(it.sku)
            }
            LOGApp.d(LOG_TAG, "processPurchases consumables content $consumables")
            LOGApp.d(LOG_TAG, "processPurchases non-consumables content $nonConsumables")
            /*
              As is being done in this sample, for extra reliability you may store the
              receipts/purchases to a your own remote/local database for until after you
              disburse entitlements. That way if the Google Play Billing library fails at any
              given point, you can independently verify whether entitlements were accurately
              disbursed. In this sample, the receipts are then removed upon entitlement
              disbursement.
             */
//                val testing = localCacheBillingClient.purchaseDao().getPurchases()
//                LOGApp.d(LOG_TAG, "processPurchases purchases in the lcl db ${testing?.size}")
//                localCacheBillingClient.purchaseDao().insert(*validPurchases.toTypedArray())
            handleConsumablePurchasesAsync(consumables)
            acknowledgeNonConsumablePurchasesAsync(nonConsumables)
        }

    /**
     * Recall that Google Play Billing only supports two SKU types:
     * [in-app products][BillingClient.SkuType.INAPP] and
     * [subscriptions][BillingClient.SkuType.SUBS]. In-app products are actual items that a
     * user can buy, such as a house or food; subscriptions refer to services that a user must
     * pay for regularly, such as auto-insurance. Subscriptions are not consumable.
     *
     * Play Billing provides methods for consuming in-app products because they understand that
     * apps may sell items that users will keep forever (i.e. never consume) such as a house,
     * and consumable items that users will need to keep buying such as food. Nevertheless, Google
     * Play leaves the distinction for which in-app products are consumable entirely up to you.
     *
     * If an app wants its users to be able to keep buying an item, it must call
     * [BillingClient.consumeAsync] each time they buy it. This is because Google Play won't let
     * users buy items that they've previously bought but haven't consumed. In Trivial Drive, for
     * example, consumeAsync is called each time the user buys gas; otherwise they would never be
     * able to buy gas or drive again once the tank becomes empty.
     */
    private fun handleConsumablePurchasesAsync(consumables: List<Purchase>) {
        LOGApp.d(LOG_TAG, "handleConsumablePurchasesAsync called")
        consumables.forEach {
            logger.debugEvent("Handle consumable item", "handleConsumablePurchasesAsync foreach it is $it")
            val params =
                ConsumeParams.newBuilder().setPurchaseToken(it.purchaseToken).build()
            playStoreBillingClient.consumeAsync(params) { billingResult, purchaseToken ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        // Update the appropriate tables/databases to grant user the items
                        purchaseToken.apply { disburseConsumableEntitlements(it) }
                    }
                    else -> {
                        logger.warningEvent("Handle consumable item", billingResult.debugMessage)
                    }
                }
            }
        }
    }

    /**
     * If you do not acknowledge a purchase, the Google Play Store will provide a refund to the
     * users within a few days of the transaction. Therefore you have to implement
     * [BillingClient.acknowledgePurchaseAsync] inside your app.
     */
    private fun acknowledgeNonConsumablePurchasesAsync(nonConsumables: List<Purchase>) {
        nonConsumables.forEach { purchase ->
            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase
                .purchaseToken).build()
            playStoreBillingClient.acknowledgePurchase(params) { billingResult ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        disburseNonConsumableEntitlement(purchase)
                    }
                    else -> logger.debugEvent("Acknowledge non consumable item", "acknowledgeNonConsumablePurchasesAsync response is ${billingResult.debugMessage}")
                }
            }

        }
    }

    /**
     * This is the final step, where purchases/receipts are converted to premium contents.
     * In this sample, once the entitlement is disbursed the receipt is thrown out.
     */
    private fun disburseNonConsumableEntitlement(purchase: Purchase) =
        CoroutineScope(Job() + Dispatchers.IO).launch {
            when (purchase.sku) {
                ADD_FREE -> {
//                        val premiumCar = PremiumCar(true)
//                        insert(premiumCar)
//                        localCacheBillingClient.skuDetailsDao()
//                                .insertOrUpdate(purchase.sku, premiumCar.mayPurchase())
                }
                else -> {
                    logger.warningEvent("Disburse Non Consumable Item", purchase.sku + " Not Handled")
                }
            }
//                localCacheBillingClient.purchaseDao().delete(purchase)
        }

   /* *//**
     * Ideally your implementation will comprise a secure server, rendering this check
     * unnecessary. @see [Security]
     *//*
    private fun isSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(
            Security.BASE_64_ENCODED_PUBLIC_KEY, purchase.originalJson, purchase.signature
        )
    }*/

    /**
     * Checks if the user's device supports subscriptions
     */
    private fun isSubscriptionSupported(): Boolean {
        val billingResult =
            playStoreBillingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
        var succeeded = false
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> connectToPlayBillingService()
            BillingClient.BillingResponseCode.OK -> succeeded = true
            else -> logger.warningEvent("Check for subscription",
                "isSubscriptionSupported() error: ${billingResult.debugMessage}")
        }
        return succeeded
    }

    /**
     * Presumably a set of SKUs has been defined on the Google Play Developer Console. This
     * method is for requesting a (improper) subset of those SKUs. Hence, the method accepts a list
     * of product IDs and returns the matching list of SkuDetails.
     *
     * The result is passed to [onSkuDetailsResponse]
     */
    private fun querySkuDetailsAsync(
        @BillingClient.SkuType skuType: String,
        skuList: List<String>) {
        val params = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(skuType).build()
        logger.debugEvent("Query for SKU Details", "querySkuDetailsAsync for $skuType")
        playStoreBillingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (skuDetailsList.orEmpty().isNotEmpty()) {
                        skuDetailsList?.forEach {
                            if (it.sku == ADD_FREE) {
                                addFreeSKU.value = it
                            }
                            if (skuType == BillingClient.SkuType.INAPP) {
                                inAppSKUList.value = skuDetailsList
                            } else if (skuType == BillingClient.SkuType.SUBS) {
                                subscriptionSKUList.value = skuDetailsList
                            }
                        }
                    }
                }
                else -> {
                    logger.debugEvent("Query for SKU Details", billingResult.debugMessage)
                }
            }
        }
    }

    /**
     * This is the function to call when user wishes to make a purchase. This function will
     * launch the Google Play Billing flow. The response to this call is returned in
     * [onPurchasesUpdated]
     */
//    fun launchBillingFlow(activity: Activity, augmentedSkuDetails: AugmentedSkuDetails) =
//            launchBillingFlow(activity, SkuDetails(augmentedSkuDetails.originalJson))

    fun launchBillingFlow(activity: Activity, skuDetails: SkuDetails) {
        // Old SKU is used when user is upgrading or downgrading from.
//        val oldSku: String? = getOldSku(skuDetails.sku)
//        val purchaseParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails)
//                .setOldSku(oldSku).build()
        val purchaseParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails)
            .build()
        playStoreBillingClient.launchBillingFlow(activity, purchaseParams)
    }

    /**
     * This sample app offers only one item for subscription: GoldStatus. And there are two
     * ways a user can subscribe to GoldStatus: monthly or yearly. The BillingRepository can access
     * the old SKU if one exists.
     */
    /*  private fun getOldSku(sku: String?): String? {
          var result: String? = null
          if (AppSku.SUBS_SKUS.contains(sku)) {
              goldStatusLiveData.value?.apply {
                  result = when (sku) {
                      AppSku.GOLD_MONTHLY -> AppSku.GOLD_YEARLY
                      else -> AppSku.GOLD_YEARLY
                  }
              }
          }
          return result
      }*/


    fun launchBillingFlowUpgrading(activity: Activity, skuDetails: SkuDetails, purchaseToken: String,oldSku:String) {
        // Old SKU is used when user is upgrading or downgrading from.
       // val oldSku: String? = getOldSku(skuDetails.sku)
        val purchaseParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails)
            .setOldSku(oldSku, purchaseToken)
            .setReplaceSkusProrationMode(BillingFlowParams.ProrationMode.DEFERRED).build()
        playStoreBillingClient.launchBillingFlow(activity, purchaseParams)
    }

   /* *//**
     * This sample app offers only one item for subscription: GoldStatus. And there are two
     * ways a user can subscribe to GoldStatus: monthly or yearly. The BillingRepository can access
     * the old SKU if one exists.
     *//*
    private fun getOldSku(sku: String?): String? {
        var result: String? = null
        if (AppSku.SUBS_SKUS.contains(sku)) {
            subscriptionSKU.value?.apply {
                when (sku) {
                    AppSku.ONE_MONTH -> ({
                        result = AppSku.ONE_MONTH
                    }).toString()
                    AppSku.THREE_MONTH -> ({
                        result = AppSku.THREE_MONTH
                    }).toString()
                    AppSku.YEARLY -> ({
                        result = AppSku.YEARLY
                    }).toString()
                    AppSku.ADD_FREE -> ({
                        result = AppSku.ADD_FREE
                    }).toString()
                }
            }
        }
        return result
    }*/

    /**
     * This method is called by the [playStoreBillingClient] when new purchases are detected.
     * The purchase list in this method is not the same as the one in
     * [queryPurchases][BillingClient.queryPurchases]. Whereas queryPurchases returns everything
     * this user owns, [onPurchasesUpdated] only returns the items that were just now purchased or
     * billed.
     *
     * The purchases provided here should be passed along to the secure server for
     * [verification](https://developer.android.com/google/play/billing/billing_library_overview#Verify)
     * and safekeeping. And if this purchase is consumable, it should be consumed, and the secure
     * server should be told of the consumption. All that is accomplished by calling
     * [queryPurchasesAsync].
     */
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                // will handle server verification, consumables, and updating the local cache
                if(purchases==null){
                    orderReceiptJsonForUpgradeDowngradeSubscription.value="Successfully purchase subscription. It's activate in 24 hrs."
                }else{
                    purchases.apply { processPurchases(this.toSet()) }
                }
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                // item already owned? call queryPurchasesAsync to verify and process all such items
                logger.debugEvent("Item already owned", billingResult.debugMessage)
                queryPurchasesAsync()
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                connectToPlayBillingService()
            }
            else -> {
                logger.warningEvent("Purchase Update", billingResult.debugMessage)
            }
        }
    }

    private fun disburseConsumableEntitlements(purchase: Purchase) =
        CoroutineScope(Job() + Dispatchers.IO).launch {
            //                if (purchase.sku == AppSku.GAS) {
//                    updateGasTank(GasTank(GAS_PURCHASE))
            /**
             * This disburseConsumableEntitlements method was called because Play called onConsumeResponse.
             * So if you think of a Purchase as a receipt, you no longer need to keep a copy of
             * the receipt in the local cache since the user has just consumed the product.
             */
//                    localCacheBillingClient.purchaseDao().delete(purchase)
//                }
        }

    companion object {
        private const val LOG_TAG = "BillingRepository"

        @Volatile
        private var INSTANCE: BillingRepository? = null

        fun getInstance(application: Application): BillingRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: BillingRepository(application)
                        .also { INSTANCE = it }
            }
    }

    /**
     * [INAPP_SKUS], [SUBS_SKUS], [CONSUMABLE_SKUS]:
     *
     * If you don't need customization ,then you can define these lists and hardcode them here.
     * That said, there are use cases where you may need customization:
     *
     * - If you don't want to update your APK (or Bundle) each time you change your SKUs, then you
     *   may want to load these lists from your secure server.
     *
     * - If your design is such that users can buy different items from different Activities or
     * Fragments, then you may want to define a list for each of those subsets. I only have two
     * subsets: INAPP_SKUS and SUBS_SKUS
     */

    private object AppSku {
        val ADD_FREE = "android.test.purchased"
        val ONE_MONTH = "android.test.purchased"
        val SIX_MONTH = "com.appineers.whitelabel.6months"
        val PURCHASE_COUNT = "purchase_count"
        val YEARLY = "yearly"
        val THREE_MONTH = "com.appineers.whitelabel.3months"


        val INAPP_SKUS = listOf(ADD_FREE, PURCHASE_COUNT)
        val SUBS_SKUS = listOf(ONE_MONTH,THREE_MONTH, SIX_MONTH, YEARLY)
        val CONSUMABLE_SKUS = listOf(ADD_FREE, PURCHASE_COUNT)
    }
}