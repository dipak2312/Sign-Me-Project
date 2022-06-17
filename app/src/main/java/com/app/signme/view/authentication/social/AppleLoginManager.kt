package com.app.signme.view.authentication.social

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.dataclasses.Social
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import com.hb.logger.Logger

class AppleLoginManager : AppCompatActivity() {

    // [START declare_vars]
    private lateinit var auth: FirebaseAuth
    private val provider = OAuthProvider.newBuilder("apple.com")
    private var social: Social? = null
    private val logger by lazy {
        Logger(this::class.java.simpleName)
    }
    // [END declare_vars]

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        signOut()

        // Look for a pending auth result
        val pending = auth.pendingAuthResult
        if (pending != null) {
            pending.addOnSuccessListener { authResult ->
                logger.debugEvent("checkPending:", "onSuccess:$authResult")
                // Get the user profile with authResult.getUser() and
                // authResult.getAdditionalUserInfo(), and the ID
                // token from Apple with authResult.getCredential().
                updateUISocial(setUserDetails(authResult.user))
            }.addOnFailureListener { e ->
                updateUIWithNullUser()
                logger.debugEvent("checkPending:", "onFailure$e")
            }
        } else {
            signIn()
            logger.debugEvent("pending:", "null")
        }
    }


    /**
     * Call Apple Sign in and based on result update the UI
     */
    private fun signIn() {
        auth.startActivityForSignInWithProvider(this, provider.build())
            .addOnSuccessListener { authResult ->
                // Sign-in successful!
                logger.debugEvent("activitySignIn:onSuccess:", "${authResult.user}")
                updateUISocial(setUserDetails(authResult.user))
            }
            .addOnFailureListener { e ->
                //   if (!e.cause?.equals("ERROR_WEB_CONTEXT_ALREADY_PRESENTED")!!) {
                updateUIWithNullUser()
                logger.debugEvent("activitySignIn:", "onFailure$e")
                // }
            }
    }

    /**
     * Call Apple Sign out
     */
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    /**
     * Set details to Social object that retrieves from Apple
     */
    private fun setUserDetails(firebaseUser: FirebaseUser?): Social {
        val social = Social("", "", "", "", "", "", "", "")
        val nameList = firebaseUser?.displayName?.split("\\s".toRegex())

        social.emailId = firebaseUser?.email ?: ""
        social.name = firebaseUser?.displayName ?: ""
        social.firstName = firebaseUser?.displayName ?: ""
        try {
            social.firstName = nameList?.get(0) ?: ""
            social.lastName = nameList?.get(1) ?: ""
        } catch (e: Exception) {

        }
        social.socialId = firebaseUser?.uid ?: ""
        social.profileImageUrl = firebaseUser?.photoUrl.toString()
        social.type = IConstants.SOCIAL_TYPE_APPLE
        return social
    }


    /**
     * Update UI based on the details fetched from apple
     */
    private fun updateUISocial(socialUser: Social?) {
        if (socialUser != null) {
            social = socialUser
            onExit(true)
        } else {
            onExit(false)
        }
    }

    /**
     * Update UI accordingly if failed to fetch the details from Apple
     */
    private fun updateUIWithNullUser(user: FirebaseUser? = null) {

        if (user != null) {
            val social = Social("", "", "", "", "", "", "")
            val nameList = user.displayName!!.split("\\s".toRegex())
            social.emailId = user.email!!
            social.name = user.displayName!!
            social.firstName = user.displayName!!
            try {
                social.firstName = nameList[0]
                social.lastName = nameList[1]
            } catch (e: Exception) {
                e.printStackTrace()
            }
            social.socialId = user.uid
            social.profileImageUrl = user.photoUrl.toString()
            onExit(true)

        } else {
            onExit(false)
        }
    }

    public override fun onStop() {
        super.onStop()
    }

    private fun onExit(isSuccess: Boolean) {

        val intent = Intent()
        if (isSuccess) {
            intent.putExtra("apple_data", social)
            setResult(Activity.RESULT_OK, intent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }


}
