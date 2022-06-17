package com.app.signme.commonUtils.common

import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import java.lang.Exception

/**
 * Firebase auth provider
 * consist function required to create user, check status and manage access to
 * Firestore, Realtime database etc.*/
class FirebaseAuthenticator {
    companion object {
        private val authenticator: FirebaseAuth = FirebaseAuth.getInstance()

        /** check whether user is already logged in or not*/
        fun isFirebaseUserExist(): Boolean {
            return authenticator.currentUser != null
        }

        /** sign out from firebase*/
        fun signOutFromFirebase() {
            authenticator.signOut()
        }

        /** sign in Firebase as a anonymous user*/
        fun signInAnonymously(
            onAuthSuccess: (user: FirebaseUser?) -> Unit,
            onAuthFailure: (exception: Exception?) -> Unit
        ) {
            authenticator.signInAnonymously().addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = authenticator.currentUser
                    user?.let {
                        onAuthSuccess(user)
                    }
                    onAuthFailure(it.exception)
                } else {
                    onAuthFailure(it.exception)
                }
            }
        }

        /** create account in Firebase using email and password combination*/
        fun firebaseCreateAccount(
            email: String, password: String, onAuthSuccess: (user: FirebaseUser?) -> Unit,
            onAuthFailure: (exception: Exception?) -> Unit
        ) {
            authenticator.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        onAuthSuccess(it.result?.user!!)
                    }
                    else -> {
                        onAuthFailure(Exception("Firebase user creation failed.."))
                    }
                }
            }.addOnFailureListener {
                onAuthFailure(it)
            }
        }

        /** Sign in Firebase using email and password
         * @note - Need to create user before using this method*/
        fun firebaseSignIn(
            email: String, password: String, onAuthSuccess: (user: FirebaseUser?) -> Unit,
            onAuthFailure: (exception: Exception?) -> Unit
        ) {
            authenticator.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        onAuthSuccess(it.result?.user!!)
                    }
                    it.exception != null -> {
                        onAuthFailure(it.exception!!)
                    }
                    else -> {
                        onAuthFailure(Exception("Unable to connect to Firebase.."))
                    }
                }
            }.addOnFailureListener {
                onAuthFailure(it)
            }
        }

        /** Sign in Firebase using facebook credentials
         * @param idToken - token received after login using Facebook*/
        fun firebaseSignInWithFacebook(
            idToken: String, onAuthSuccess: (user: FirebaseUser?) -> Unit,
            onAuthFailure: (exception: Exception?) -> Unit
        ) {
            val credential = FacebookAuthProvider.getCredential(idToken)
            authenticator.signInWithCredential(credential)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        onAuthSuccess(it.result?.user!!)
                    } else {
                        onAuthFailure(it.exception!!)
                    }
                }
                .addOnFailureListener {
                    onAuthFailure(it)
                }
        }

        /** Sign in Firebase using google credentials
         * @param idToken - token received after login using Google*/
        fun firebaseSignInWithGoogle(
            idToken: String, onAuthSuccess: (user: FirebaseUser?) -> Unit,
            onAuthFailure: (exception: Exception?) -> Unit
        ) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            authenticator.signInWithCredential(credential).addOnCompleteListener {
                if (it.isSuccessful) {
                    onAuthSuccess(it.result?.user!!)
                } else {
                    onAuthFailure(it.exception!!)
                }
            }.addOnFailureListener {
                onAuthFailure(it)
            }
        }

    }
}