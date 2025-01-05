package org.cxct.sportlottery.common.extentions

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import org.cxct.sportlottery.util.GlideRoundTransform
import org.cxct.sportlottery.util.LogUtil
import java.io.File

fun ImageView.load(@DrawableRes res: Int = 0) {
    runWithCatch { Glide.with(context).load(res).into(this) }
}
fun ImageView.loadAsGif(@DrawableRes res: Int = 0) {
    runWithCatch { Glide.with(context).asGif().load(res).into(this) }
}
fun ImageView.load(url: String?, @DrawableRes placeHolder: Int = 0,  @DrawableRes error: Int = 0) {
    runWithCatch {
        Glide.with(context)
            .load("$url")
            .apply {
                if (placeHolder != 0) {
                    placeholder(placeHolder)
                }
            }.apply {
                if (error != 0) {
                    error(error)
                }
            }.into(this)
    }
}
fun ImageView.load(url: String?,errorUrl: String) {
    runWithCatch {
        Glide.with(context)
            .load("$url")
            .listener(object :RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean,
                ): Boolean {
                    post { load(errorUrl) }
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean,
                ): Boolean {
                    return false
                }
            })
            .into(this)
    }
}


fun ImageView.loadByOverride(url: String?, width:Int,height:Int,@DrawableRes placeHolder: Int = 0,  @DrawableRes error: Int = 0) {
    runWithCatch {
        Glide.with(context)
            .load("$url")
            .override(width,height)
            .apply {
                if (placeHolder != 0) {
                    placeholder(placeHolder)
                }
            }.apply {
                if (error != 0) {
                    error(error)
                }
            }.into(this)
    }
}

fun ImageView.load(url: Int?) {
    runWithCatch { Glide.with(context).load(url).into(this) }
}

fun ImageView.load(file: File?) {
    runWithCatch { Glide.with(context).load(file).into(this) }
}

fun ImageView.circleOf(url: String?, @DrawableRes placeHolder: Int = 0) {
    val requestOptions = RequestOptions().optionalCircleCrop()
    if (placeHolder != 0) {
        requestOptions.placeholder(placeHolder)
    }
    runWithCatch { Glide.with(context).load("$url").apply(requestOptions).into(this) }
}

fun ImageView.roundOf(url: String?, radius: Int = 0, @DrawableRes placeHolder: Int = 0) {
    val requestOptions = RequestOptions().transform(GlideRoundTransform(radius))
    if (placeHolder != 0) {
        requestOptions.placeholder(placeHolder)
    }
    runWithCatch {
        Glide.with(context).load("$url")
            .apply(requestOptions).into(this)
    }
}
fun ImageView.roundOf(@DrawableRes drawableResId: Int =0 , radius: Int = 0) {
    val requestOptions = RequestOptions().transform(RoundedCorners(radius))
    runWithCatch {
        Glide.with(context).load(drawableResId)
            .apply(requestOptions).into(this)
    }
}