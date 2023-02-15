package org.cxct.sportlottery.util

import android.app.Activity
import android.content.Intent
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R

object AuthManager {

    init {
        FacebookSdk.sdkInitialize(MultiLanguagesApplication.mInstance)
        AppEventsLogger.activateApp(MultiLanguagesApplication.mInstance)
    }

    const val RC_SIGN_IN = 0x123
    private val callbackManager = CallbackManager.Factory.create()

    open fun authGoogle(activity: Activity) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.server_client_id))
            .requestEmail()
            .build()
        var mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
        val signInIntent = mGoogleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    open fun authFacebook(
        activity: Activity,
        successCallback: (token: String) -> Unit,
        failCallback: (errorMsg: String?) -> Unit,
    ) {
        if (LoginManager.getInstance() != null) {
            LoginManager.getInstance().logOut();
        }
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: com.facebook.login.LoginResult) {
                    LogUtil.toJson(result)
                    successCallback.invoke(result.accessToken.token)
                }

                override fun onError(error: FacebookException) {
                    error.printStackTrace()
                    failCallback.invoke(error.message)
                }

                override fun onCancel() {
                    failCallback.invoke(null)
                }
            })
        LoginManager.getInstance()
            .logInWithReadPermissions(activity, arrayListOf("public_profile"));
    }

    open fun facebookCallback(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    open fun googleCallback(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        callback: (Boolean, String?) -> Unit,
    ) {
        if (requestCode === RC_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                return callback.invoke(true, account.idToken)
            } catch (e: Exception) {
                e.printStackTrace()
                return callback.invoke(false, e.message)
            }
        }
    }
}