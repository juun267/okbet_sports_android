package org.cxct.sportlottery.common.extentions

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

fun ImageView.load(url: String?, @DrawableRes placeHolder: Int = 0) {
    if (placeHolder == 0) {
        runWithCatch { Glide.with(context).load("$url").into(this) }
    } else {
        runWithCatch { Glide.with(context).load("$url").placeholder(placeHolder).into(this) }
    }
}

fun ImageView.circleOf(url: String?, @DrawableRes placeHolder: Int = 0) {
    val requestOptions = RequestOptions().optionalCircleCrop()
    if (placeHolder != 0) {
        requestOptions.placeholder(placeHolder)
    }
    runWithCatch { Glide.with(context).load("$url").apply(requestOptions).into(this) }
}

fun ImageView.roundOf(url: String?, radius: Int = 0, @DrawableRes placeHolder: Int = 0) {
    val requestOptions = RequestOptions().centerCrop().transform(RoundedCorners(radius))
    if (placeHolder != 0) {
        requestOptions.placeholder(placeHolder)
    }
    runWithCatch {
        Glide.with(context).load("$url")
            .apply(requestOptions).into(this)
    }
}