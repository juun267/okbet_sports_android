package org.cxct.sportlottery.ui.profileCenter.taskCenter

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemLimitedGameBinding
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.quest.info.LimitedGame
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.LogUtil

class LimitedGameAdapter: BindingAdapter<LimitedGame, ItemLimitedGameBinding>() {

    private val defaultCoverUrl = "${Constants.getH5BaseUrl()}assets/logo/OK.png"

    override fun onBinding(
        position: Int,
        binding: ItemLimitedGameBinding,
        item: LimitedGame,
    ) {
        if (item.type==LimitedGame.TYPE_SPORT){
            binding.ivCover.load(GameType.getTaskImg(item.gameType))
            val gameType = GameType.getGameType(item.gameType)
            binding.tvName.text = if(gameType!=null) context.getString(gameType.string) else context.getString(R.string.B001)
        }else{
            val gameFirmValues = OKGamesRepository.gameFiremEvent.value?.firstOrNull { it.firmCode== item.firmCode }
            val coverUrl = "${Constants.getH5BaseUrl()}assets/logo/${gameFirmValues?.firmCode}.png"
            binding.ivCover.load(coverUrl, defaultCoverUrl)
            binding.tvName.text = gameFirmValues?.firmShowName
        }
    }
}