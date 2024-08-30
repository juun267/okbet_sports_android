package org.cxct.sportlottery.ui.maintab.home.view

import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.databinding.DialogWinsBetdetailsBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.RecentDataManager
import org.cxct.sportlottery.util.RecentRecord
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimeUtil.NEWS_TIME_FORMAT2
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils

class WinsDialog : BaseDialog<BaseViewModel, DialogWinsBetdetailsBinding>() {

    companion object{
        fun newInstance(betRecode: RecordNewEvent)=WinsDialog().apply {
            arguments = Bundle().apply {
                putParcelable("betRecode",betRecode)
            }
        }
    }
    init {
        setStyle(R.style.FullScreen)
    }
    private val betRecode by lazy { requireArguments().getParcelable<RecordNewEvent>("betRecode")!! }

    override fun onInitView() {
        initView()
    }

    private fun initView()=binding.run {
        binding.ivClose.setOnClickListener {
            dismiss()
        }
        val bgDrawable = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.white, R.color.color_ebf1fc)

        llBet.background = bgDrawable
        llPayout.background = bgDrawable
        btnPlay.background = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.color_025BE8)
        btnPlay.setOnClickListener{
            dismiss()
            onClickPlay()
        }
        if (betRecode.isSportBet()) {
            var gameIcon = GameType.getSportGameImg(betRecode.firmType)
            if (gameIcon == R.drawable.ic_game_champ) {
                gameIcon = R.drawable.ic_okgames_nodata
            }
            ivGamesIcon.setImageResource(gameIcon)
        } else {
            ivGamesIcon.load(betRecode.h5ImgGame, R.drawable.ic_okgames_nodata)
        }
        tvGameName.text = betRecode.games
        tvPlayer.text = "${getString(R.string.N710)}: ${betRecode.player}"
        betRecode.betTime?.let { tvBetTime.text = "${getString(R.string.J652)}: ${TimeUtil.timeFormat(it, NEWS_TIME_FORMAT2)}" }
        showCurrencySign.let {
            tvBet.text = "$it ${TextUtil.formatMoney(betRecode.betAmount, 2)}"
            tvPayout.apply {
                text = "$it ${TextUtil.formatMoney(betRecode.profitAmount, 2)}"
                setTextColor(if (betRecode.profitAmount.toIntS() < 0) {
                    requireContext().getColor(R.color.color_FF2E00)
                } else {
                    requireContext().getColor(R.color.color_1CD219)
                })
            }
        }
    }
    private fun onClickPlay(){
        if (!betRecode.isSportBet()) {
            val okGameBean = OKGameBean(
                id = 0,
                firmId = 0,
                firmCode = betRecode.gameCode,
                firmType = betRecode.firmType,
                firmName = betRecode.games,
                gameName = betRecode.games,
                gameCode = betRecode.gameCode,
                gameType = betRecode.firmType,
                gameEntryTagName = null,
                imgGame = betRecode.h5ImgGame ,
                thirdGameCategory = null,
                markCollect = false,
                maintain = 0,
                jackpotAmount = 0.0,
                jackpotOpen = 0,
                gameEntryType = betRecode.gameEntryType,
            )
            (activity as? MainTabActivity)?.let {
                it.enterThirdGame(okGameBean, "大奖播报详情弹窗")
            }
            return
        }
        (activity as? MainTabActivity)?.let {  mainTabActivity->
            if ("ES".equals(betRecode.firmType, true)) {
                mainTabActivity.jumpToESport()
            } else {
                GameType.getGameType(betRecode.firmType)?.let { mainTabActivity.jumpToSport(it) }
            }
        }
    }

}