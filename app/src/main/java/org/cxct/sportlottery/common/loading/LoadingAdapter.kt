package org.cxct.sportlottery.common.loading

import android.graphics.Color
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.cxct.sportlottery.R


class LoadingAdapter(@StringRes val emptyString: Int = R.string.D024,
                     @DrawableRes val emptyIcon: Int = R.drawable.img_nogames,
                     @StringRes val loadingString: Int = R.string.loading,
                     @DrawableRes val loadingIcon: Int = R.drawable.anim_loading,
                     @StringRes val errorString: Int = R.string.J871,
                     @DrawableRes val errorIcon: Int = R.drawable.img_nogames,
                     val bgColor: Int = Color.WHITE): Gloading.Adapter {

    override fun getView(holder: Gloading.Holder, convertView: View?, status: Int): View {
        var loadingStatusView: GlobalLoadingView? = null
        if (convertView != null && convertView is GlobalLoadingView) {
            loadingStatusView = convertView
        }
        if (loadingStatusView == null) {
            val context = holder.context
            loadingStatusView = GlobalLoadingView(
                context, null, 0
            ).apply {
                setEmptyIcon(emptyIcon)
                setEmptyText(context.getString(emptyString))
                setErrorRes(errorIcon)
                setErrorText(context.getString(errorString))
                setLoadingRes(loadingIcon)
                setLoadingText(context.getString(loadingString))
            }

            loadingStatusView!!.setBackgroundColor(bgColor)
        }
        loadingStatusView.setRetryTask(holder.retryTask)
        loadingStatusView.setStatus(status)
        return loadingStatusView!!
    }
}