package org.cxct.sportlottery.ui.maintab

import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ScreenUtil

class HomeChessAdapter(data: MutableList<QueryGameEntryData>):
    BaseQuickAdapter<QueryGameEntryData, BaseViewHolder>(0, data) {

    private val params by lazy {  ViewGroup.LayoutParams((ScreenUtil.getScreenWidth(context) - 40.dp) / 2, -2) }
    private val options = RequestOptions.bitmapTransform(RoundedCorners(8.dp))
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .placeholder(R.drawable.icon_chess_and_card)

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val imageView = ImageView(context)
        imageView.layoutParams = params
        imageView.adjustViewBounds = true
        imageView.scaleType = ImageView.ScaleType.FIT_XY
        return BaseViewHolder(imageView)
    }

    override fun convert(helper: BaseViewHolder, item: QueryGameEntryData) {

        Glide.with(context)
            .load(item.entryImage)
            .apply(options)
            .into((helper.itemView as ImageView))

    }

}
