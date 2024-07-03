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
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.runWithCatch
import java.net.URLEncoder
import com.facebook.FacebookCallback as FacebookCallback1


object ShareUtil {
    fun shareFacebook(activity: Activity, content: String, url: String){
        if (!ShareDialog.canShow(ShareLinkContent::class.java)) {
            shareError(activity)
            return
        }
        val content = ShareLinkContent.Builder()
            .setContentUrl(Uri.parse(url))
            .setQuote(content)
            .build()
        ShareDialog(activity).apply {
            registerCallback(CallbackManager.Factory.create(),
               object: FacebookCallback1<Sharer.Result> {
                   override fun onCancel() {
                   }

                   override fun onError(error: FacebookException) {
                       LogUtil.e("onError:"+error.message)
                       ToastUtil.showToast(activity,error.message)
                   }

                   override fun onSuccess(result: Sharer.Result) {
                       LogUtil.d("onSuccess:"+result.postId)
                   }
               })
        }.show(content)
//        val sharingIntent = Intent(Intent.ACTION_SEND)
//        sharingIntent.setType("text/plain")
//        sharingIntent.putExtra(Intent.EXTRA_TEXT, content)
//        sharingIntent.setPackage("com.facebook.katana")
//        activity.startActivity(Intent.createChooser(sharingIntent, null))
    }
    fun shareMessenger(activity: Activity, content: String, url: String){
        if (!MessageDialog.canShow(ShareLinkContent::class.java)) {
            shareError(activity)
            return
        }
//        val content = ShareLinkContent.Builder()
//            .setContentUrl(Uri.parse(url))
//            .setQuote(content)
//            .setPageId("104843916055222")
//            .build()
//        MessageDialog(activity).apply {
//            registerCallback(CallbackManager.Factory.create(),
//                object: FacebookCallback1<Sharer.Result> {
//                    override fun onCancel() {
//                    }
//
//                    override fun onError(error: FacebookException) {
//                        LogUtil.e("onError:"+error.message)
//                        ToastUtil.showToast(activity,error.message)
//                    }
//
//                    override fun onSuccess(result: Sharer.Result) {
//                        LogUtil.d("onSuccess:"+result.postId)
//                    }
//                })
//
//        }.show(content)
        runWithCatch({
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.setType("text/plain")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, content)
        sharingIntent.setPackage("com.facebook.orca")
        activity.startActivity(Intent.createChooser(sharingIntent, null))
        },{
            shareError(activity)
        })
    }
    fun shareInstagram(activity: Activity, content: String){
        runWithCatch({
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.setPackage("com.instagram.android")
            sharingIntent.setType("text/plain")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, content)
            activity.startActivity(sharingIntent)
        },{
            shareError(activity)
        })
    }
    fun shareBySystem(context: Context, content: String){
        runWithCatch({
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.setType("text/plain")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, content)
            context.startActivity(Intent.createChooser(sharingIntent, null))
        },{
            shareError(context)
        })
    }
    fun shareViber(context: Context, content: String){
        runWithCatch({
            val sharingIntent = Intent(Intent.ACTION_VIEW, Uri.parse("viber://forward?text=${URLEncoder.encode(content,"utf-8")}"))
            sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(sharingIntent)},{
            shareError(context)
        })
    }
    fun sendSMS(context: Context,content: String){
        runWithCatch({
               val sharingIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
                sharingIntent.putExtra( "sms_body", content)
                context.startActivity(sharingIntent) },
            {
                shareError(context)
            }
        )
    }
    private fun shareError(context: Context){
        ToastUtil.showToast(context,context.getString(R.string.P467))
    }

}