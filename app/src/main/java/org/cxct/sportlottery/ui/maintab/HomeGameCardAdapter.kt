package org.cxct.sportlottery.ui.maintab

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues
import org.cxct.sportlottery.util.LanguageManager

class HomeGameCardAdapter(data: MutableList<ThirdDictValues?>) :
    BaseQuickAdapter<ThirdDictValues?, BaseViewHolder>(
        R.layout.item_home_game_card, data) {

    override fun convert(helper: BaseViewHolder, item: ThirdDictValues?) {
        helper.setImageResource(R.id.iv_people, when (helper.layoutPosition) {
            0 -> R.drawable.ic_game_01
            1 -> R.drawable.ic_game_02
            2 -> R.drawable.ic_game_03
            else -> R.drawable.ic_game_01
        })
        if (item != null) {
            helper.setText(R.id.tv_firm_name, item.firmCode)
            helper.setText(R.id.tv_game_name, when (LanguageManager.getSelectLanguage(mContext)) {
                LanguageManager.Language.ZH -> item.chineseName
                else -> item.englishName
            })
            helper.setGone(R.id.tv_game_name, true)
            helper.setGone(R.id.iv_repair, false)
            helper.setText(R.id.tv_status,
                if (item.open == 1) R.string.new_games_beta else R.string.comingsoon)
        } else {
            helper.setText(R.id.tv_firm_name, getFirmCode(helper.layoutPosition))
            helper.setGone(R.id.tv_game_name, false)
            helper.setGone(R.id.iv_repair, true)
            helper.setText(R.id.tv_status, R.string.comingsoon)
        }
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