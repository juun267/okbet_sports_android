package org.cxct.sportlottery.util.selectpicture

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.luck.picture.lib.engine.CropFileEngine
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropImageEngine
import org.cxct.sportlottery.common.extentions.loadByOverride

/**
 * 自定义裁剪
 */
class ImageFileCropEngine(
    private val rotateEnabled: Boolean = false,
    private val circleDimmedLayer: Boolean = false,
    private val showCropFrame: Boolean = false,
    private val showCropGrid: Boolean = false,
    private val ratio_x: Int,
    private val ratio_y:Int,) : CropFileEngine {

    override fun onStartCrop(
        fragment: Fragment,
        srcUri: Uri,
        destinationUri: Uri,
        dataSource: ArrayList<String>,
        requestCode: Int,
    ) {
        val options = UCrop.Options()
        options.setCircleDimmedLayer(circleDimmedLayer)
        options.setShowCropFrame(showCropFrame)
        options.setShowCropGrid(false)
        options.setShowCropGrid(showCropGrid)
        options.withAspectRatio(ratio_x.toFloat(), ratio_y.toFloat())

        val uCrop: UCrop = UCrop.of(srcUri, destinationUri, dataSource)
        uCrop.withOptions(options)

        uCrop.setImageEngine(object : UCropImageEngine {

            override fun loadImage(context: Context, url: String, imageView: ImageView) {
                if (!assertValidRequest(context)) {
                    return
                }
                imageView.loadByOverride(url, 180, 180)
            }

            override fun loadImage(
                context: Context,
                url: Uri?,
                maxWidth: Int,
                maxHeight: Int,
                call: UCropImageEngine.OnCallbackListener<Bitmap>?,
            ) {
                Glide.with(context!!).asBitmap().load(url).override(maxWidth, maxHeight)
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                            call?.onCall(resource)
                        }
                        override fun onLoadCleared(@Nullable placeholder: Drawable?) {
                            call?.onCall(null)
                        }
                    })
            }
        })
        uCrop.start(fragment.requireActivity(), fragment, requestCode)
    }

    private fun assertValidRequest(context:Context):Boolean {
        if (context is Activity) {
            return !isDestroy(context)
        } else if (context is ContextWrapper){
            if (context.baseContext is Activity){
                return !isDestroy(context.baseContext as Activity)
            }
        }
        return true;
    }

    private fun isDestroy(activity: Activity):Boolean {
        if (activity == null) {
            return true
        }
        return activity.isFinishing || activity.isDestroyed
    }

}