package com.app.signme.view.authentication.social

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.dataclasses.Social
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.hb.logger.Logger


/**
 * Created by hb on 11/5/18.
 */
class GoogleLoginManager : AppCompatActivity() {

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_client_id)).requestEmail().build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mGoogleSignInClient?.revokeAccess()
        signOut()
    }

    // [START on_start_check_user]
    public override fun onStart() {
        super.onStart()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                // Google Sign In was successful,
                val account = task.getResult(ApiException::class.java)
                updateUISocial(getSocialUserFromGoogleSignInAccount(account))

            } else {
                // Google Sign In failed, update UI appropriately
                logger.debugEvent("Google Login Failed", task.exception?.toString()?:"")
                updateUI(null, true)
            }
        }
    }

    private fun getSocialUserFromGoogleSignInAccount(acc: GoogleSignInAccount?): Social {
        val social = Social("", "", "", "", "", "", "", "")
        val nameList = acc?.displayName?.split("\\s".toRegex())

        social.emailId = acc?.email ?: ""
        social.name = acc?.displayName ?: ""
        social.firstName = acc?.displayName ?: ""
        try {
            social.firstName = nameList?.get(0) ?: ""
            social.lastName = nameList?.get(1) ?: ""
        } catch (e: Exception) {

        }
        social.socialId = acc?.id ?: ""
        social.profileImageUrl = acc?.photoUrl.toString()
        social.type = IConstants.SOCIAL_TYPE_GOOGLE
        return social
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient?.signInIntent

        startActivityForResult(signInIntent, SIGN_IN_REQUEST_CODE)
    }


    private fun signOut() {
        // Google sign out
        mGoogleSignInClient?.signOut()?.addOnCompleteListener(this) {
            signIn()
        }
    }

    private fun revokeAccess() {
        // Google revoke access
        mGoogleSignInClient?.revokeAccess()?.addOnCompleteListener(this) {
            updateUI(null)
        }
    }

    private fun updateUISocial(socialUser: Social?, fromResponse: Boolean = false) {
        if (socialUser != null) {
            val i = Intent()
            i.putExtra("google_data", socialUser)
            setResult(Activity.RESULT_OK, i)
            finish()
        } else {
            if (fromResponse) {
                val i = Intent()
                setResult(Activity.RESULT_CANCELED, i)
                finish()
            }
        }
    }

    private fun updateUI(user: FirebaseUser?, fromResponse: Boolean = false) {

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
            revokeAccess()
            val i = Intent()
            i.putExtra("google_data", social)
            setResult(Activity.RESULT_OK, i)
            finish()

        } else {
            if (fromResponse) {
                val i = Intent()
                setResult(Activity.RESULT_CANCELED, i)
                finish()
            }
        }
    }

    public override fun onStop() {
        super.onStop()
    }

    companion object {
        private const val SIGN_IN_REQUEST_CODE = 9001
    }

}