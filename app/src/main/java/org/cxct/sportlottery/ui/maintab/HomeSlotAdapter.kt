package org.cxct.sportlottery.ui.maintab

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData
import org.cxct.sportlottery.util.LanguageManager

class HomeSlotAdapter(data: MutableList<QueryGameEntryData>) :
    BaseQuickAdapter<QueryGameEntryData, BaseViewHolder>(
        R.layout.item_home_slot, data) {

    override fun convert(helper: BaseViewHolder, item: QueryGameEntryData) {
        Glide.with(mContext)
            .load(item.entryImage)
            .apply(RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontTransform())
            .into(helper.getView(R.id.iv_people))
//        helper.setImageResource(R.id.iv_people, when (helper.layoutPosition) {
//            0 -> R.drawable.ic_game_01
//            1 -> R.drawable.ic_game_02
//            2 -> R.drawable.ic_game_03
//            else -> R.drawable.ic_game_01
//        })
        helper.setText(R.id.tv_firm_name, item.firmName)
        helper.setText(R.id.tv_game_name, when (LanguageManager.getSelectLanguage(mContext)) {
            LanguageManager.Language.ZH -> item.chineseName
            else -> item.englishName
        })
        helper.setGone(R.id.tv_game_name, true)
        helper.setGone(R.id.iv_repair, false)
        helper.setText(R.id.tv_status,
            if (item.status == 1) R.string.new_games_beta else R.string.comingsoon)
    }

    fun getFirmCode(position: Int): String {
        return when (position) {
            0 -> "CGQP"
            1 -> "TPG"
            2 -> "FKG"
            else -> ""
        }
    }
}