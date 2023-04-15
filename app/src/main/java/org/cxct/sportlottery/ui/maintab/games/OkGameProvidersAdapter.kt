package org.cxct.sportlottery.ui.maintab.games

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R

class OkGameProvidersAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_okgame_p3_porviders) {

    private val options = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).dontTransform()
    override fun convert(holder: BaseViewHolder, item: String) {
        Glide.with(context)
            .load(item)
            .apply(options)
            .into((holder.itemView as ImageView))
    }
}