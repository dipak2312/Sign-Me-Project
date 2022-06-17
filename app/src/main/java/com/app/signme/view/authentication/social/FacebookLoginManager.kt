package com.app.signme.view.authentication.social

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.dataclasses.Social
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.hb.logger.Logger
import com.facebook.AccessToken

import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth

class FacebookLoginManager : AppCompatActivity() {
    private var callbackManager: CallbackManager? = null
    private var loginButton: LoginButton? = null
    private var social: Social? = null
    private lateinit var auth: FirebaseAuth
    private val logger by lazy {
        Logger(this::class.java.simpleName)
    }
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        LoginManager.getInstance().logOut()
       // LoginManager.getInstance().loginBehavior = LoginBehavior.NATIVE_WITH_FALLBACK
        //showProgress()
        social = Social("", "", "", "", "", "", "")

        if (loginButton == null) {
            loginButton = LoginButton(this)
            LoginManager.getInstance().logOut()
           // LoginManager.getInstance().loginBehavior = LoginBehavior.NATIVE_WITH_FALLBACK


            callbackManager = CallbackManager.Factory.create()

            loginButton?.setPermissions(listOf("email","public_profile"))

            loginButton?.performClick()



            // Callback registration
            loginButton?.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                   // hideProgress()
                   // handleFacebookAccessToken(loginResult.accessToken)
                    setUserDetails(loginResult.accessToken)
                }

                override fun onCancel() {
                    logger.debugEvent("Facebook Login Failed", "Canceled by user")
                   // disconnectFromFacebook()
                    onExit(false)
                }

                override fun onError(exception: FacebookException) {
                    logger.debugEvent("Facebook Login Failed", exception.toString())
                   // disconnectFromFacebook()
                    onExit(false)
                }

            })
        }


    }

    private fun handleFacebookAccessToken(token: AccessToken) {
       // Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                  //  Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    setUserDetails(token)
                } else {
                    // If sign in fails, display a message to the user.
                   // Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    //setUserDetails(null)
                }
            }
    }



    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }


    fun setUserDetails(accessToken: AccessToken) {

        val request = GraphRequest.newMeRequest(accessToken) { mJSONObject, _ ->

            try {
                val pictureObject = mJSONObject.getJSONObject("picture")
                val dataObject = pictureObject.getJSONObject("data")
                val picUrl = dataObject.optString("url")
                social?.profileImageUrl = picUrl
                social?.name = mJSONObject.optString("name").toString()
                social?.emailId = mJSONObject.optString("email").toString()
                social?.accessToken = accessToken.token
                social?.firstName = mJSONObject.optString("first_name").toString()
                social?.lastName = mJSONObject.optString("last_name").toString()
                social?.socialId = mJSONObject.optString("id").toString()
                social?.type = IConstants.SOCIAL_TYPE_FB
               // disconnectFromFacebook()
                onExit(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val parameters = Bundle()
        parameters.putString("fields",
            "id,name,link,email,first_name,last_name,picture.type(large)")
        request.parameters = parameters
        request.executeAsync()
    }

  /*  private fun disconnectFromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return  // already logged out
        }

        GraphRequest(
            AccessToken.getCurrentAccessToken(),
            "/me/permissions/",
            null,
            HttpMethod.DELETE
        ) {  LoginManager.getInstance().loginBehavior = LoginBehavior.NATIVE_WITH_FALLBACK; }
            .executeAsync()
    }*/


    private fun onExit(isSuccess: Boolean) {
        //hideProgress()

        val intent = Intent()
        if (isSuccess) {
            intent.putExtra("facebook_data", social)
            setResult(Activity.RESULT_OK, intent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

}