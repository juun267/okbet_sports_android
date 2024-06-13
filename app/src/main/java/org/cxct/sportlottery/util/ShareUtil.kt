package org.cxct.sportlottery.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.facebook.CallbackManager
import com.facebook.FacebookException
import com.facebook.share.Sharer
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.MessageDialog
import com.facebook.share.widget.ShareDialog
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.runWithCatch
import com.facebook.FacebookCallback as FacebookCallback1


object ShareUtil {
    const val hostUrl = "https://www.okbet.com"
    fun shareFacebook(activity: Activity, content: String){
//        if (!ShareDialog.canShow(ShareLinkContent::class.java)) {
//            ToastUtil.showToast(activity,"cant share facebook ")
//            return
//        }
//        val content = ShareLinkContent.Builder()
//            .setContentUrl(Uri.parse(hostUrl))
//            .setQuote(content)
//            .build()
//        ShareDialog(activity).apply {
//            registerCallback(CallbackManager.Factory.create(),
//               object: FacebookCallback1<Sharer.Result> {
//                   override fun onCancel() {
//                       LogUtil.d("onCancel")
//                   }
//
//                   override fun onError(error: FacebookException) {
//                       LogUtil.d("onError:"+error.message)
//                       ToastUtil.showToast(activity, error.message)
//                   }
//
//                   override fun onSuccess(result: Sharer.Result) {
//                       LogUtil.d("onSuccess:"+result.postId)
//                   }
//               })
//
//        }.show(content)
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.setType("text/plain")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, content)
        sharingIntent.setPackage("com.facebook.katana")
        activity.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }
    fun shareMessenger(activity: Activity, content: String){
//        if (!MessageDialog.canShow(ShareLinkContent::class.java)) {
//            ToastUtil.showToast(activity,"cant share Messenger ")
//            return
//        }
//        val content = ShareLinkContent.Builder()
//            .setContentUrl(Uri.parse("https://www.google.com"))
//            .setPageId("104843916055222")
//            .build()
//        MessageDialog(activity).apply {
//            registerCallback(CallbackManager.Factory.create(),
//                object: FacebookCallback1<Sharer.Result> {
//                    override fun onCancel() {
//                        LogUtil.d("onCancel")
//                    }
//
//                    override fun onError(error: FacebookException) {
//                        LogUtil.d("onError:"+error.message)
//                        ToastUtil.showToast(activity, error.message)
//                    }
//
//                    override fun onSuccess(result: Sharer.Result) {
//                        LogUtil.d("onSuccess:"+result.postId)
//                    }
//                })
//
//        }.show(content)

        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.setType("text/plain")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, content)
        sharingIntent.setPackage("com.facebook.orca")
        activity.startActivity(Intent.createChooser(sharingIntent, "Share via"))

    }
    fun shareInstagram(activity: Activity, content: String){
        runWithCatch({
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.setPackage("com.instagram.android")
            sharingIntent.putExtra("com.instagram.platform.extra.APPLICATION_ID", activity.getString(R.string.facebook_app_id))
            sharingIntent.setType("text/plain")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, content)
            sharingIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            activity.startActivity(sharingIntent)
        },{
            ToastUtil.showToast(activity, it.message)
        })
    }
    fun shareBySystem(context: Context, content: String){
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.setType("text/plain")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, content)
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }
    fun shareViber(context: Context, content: String){
        runWithCatch({
            JumpUtil.toExternalWeb(context,"viber://forward?text=$content")
        },{
            ToastUtil.showToast(context,it.message)
        })
    }
    fun sendSMS(context: Context,content: String){
        runWithCatch(
            {
                context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data =  Uri.parse( "smsto:" )
                    putExtra( "sms_body", content)
                })
            },
            {
                ToastUtil.showToast(context,it.message)
            }
        )
    }

}