package org.cxct.sportlottery.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import org.cxct.sportlottery.R

object SingleToast {
    private var mToast:Toast? = null
     fun  getInstance(context: Context):Toast{
        if (mToast == null){
            synchronized(SingleToast::class.java){
                if (mToast == null){
                    mToast = Toast(context)
                }
            }
        }
        return mToast!!
    }

    /**
     * @param context  上下文
     * @param isSuccess  成功还是失败的toast
     * @param toastMassage 消息文本//建议四个字
     * @param duration  1=Toast.LENGTH_LONG 0=Toast.LENGTH_SHORT
     */
    fun showSingleToast(context: Context, isSuccess:Boolean,toastMassage:String,duration:Int){
        var singleToast = getInstance(context)
        var customView:View = LayoutInflater.from(context).inflate(R.layout.single_toast,null)
        var imageView:ImageView = customView.findViewById(R.id.iv_success_error)
        var text = customView.findViewById<TextView>(R.id.tv_massage)
        if (isSuccess){
            imageView.setImageResource(R.drawable.icon_toast_success)
        }else{
            imageView.setImageResource(R.drawable.icon_toast_error)
        }
        text.text = toastMassage
        singleToast.view = customView
        singleToast.setGravity(Gravity.CENTER,0,0)
        singleToast.duration = duration
        singleToast.show()
    }
}