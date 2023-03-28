package org.cxct.sportlottery.ui.maintab.home

import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData
import org.cxct.sportlottery.util.DisplayUtil.dp

class HomeElectronicAdapter :
    BaseQuickAdapter<QueryGameEntryData, BaseViewHolder>(0) {

    private val params = ViewGroup.LayoutParams(106.dp, 116.dp)
    private val options = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).dontTransform()

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val imageView = ImageView(context)
        imageView.layoutParams = params
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
