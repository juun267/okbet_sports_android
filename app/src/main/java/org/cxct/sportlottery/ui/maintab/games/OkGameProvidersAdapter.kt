package org.cxct.sportlottery.ui.maintab.games

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.net.games.data.OKGamesFirm
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator

class OkGameProvidersAdapter :
    BaseQuickAdapter<OKGamesFirm, BaseViewHolder>(0) {


    private val bgDrawable by lazy {
        DrawableCreator.Builder()
            .setSolidColor(Color.WHITE)
            .setCornersRadius(4.dp.toFloat())
            .build()
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val frameLayout = FrameLayout(parent.context)
        frameLayout.background = bgDrawable
        frameLayout.layoutParams = FrameLayout.LayoutParams(110.dp, 40.dp).apply { rightMargin = 8.dp }
        val ivLogo = AppCompatImageView(parent.context)
        ivLogo.scaleType = ImageView.ScaleType.CENTER_CROP
        frameLayout.addView(ivLogo, FrameLayout.LayoutParams(78.dp, 30.dp).apply { gravity = Gravity.CENTER })
        return BaseViewHolder(frameLayout)
    }
    override fun convert(holder: BaseViewHolder, item: OKGamesFirm) {
        ((holder.itemView as ViewGroup).getChildAt(0) as ImageView).load(item.img, R.drawable.img_banner01)
    }
}
