package org.cxct.sportlottery.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import kotlinx.android.synthetic.main.fragment_menu.btn_close
import kotlinx.android.synthetic.main.fragment_menu_left.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory

/**
 * 遊戲左側功能選單
 */
class MenuLeftFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {
    private var mDownMenuListener: View.OnClickListener? = null
    private var mMenuLeftListener: MenuLeftListener? = null

    interface MenuLeftListener {
        fun onClick(@IdRes id: Int)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu_left, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCloseBtn()
        initObserve()
        initEvent()
    }

    private fun setupCloseBtn() {
        btn_close.setOnClickListener {
            mDownMenuListener?.onClick(btn_close)
        }
    }

    private fun initObserve() {
        viewModel.sportMenuResult.observe(viewLifecycleOwner, {
            updateUI(it)
        })

        //第三方遊戲清單
        viewModel.gameCateDataList.observe(viewLifecycleOwner, {
            updateUI(it)
        })
    }

    private fun initEvent() {

        btn_lobby.setOnClickListener {
            mMenuLeftListener?.onClick(btn_lobby.id)
            mDownMenuListener?.onClick(btn_lobby)
        }

        menu_sport_game.setOnClickListener {
            mMenuLeftListener?.onClick(menu_sport_game.id)
            mDownMenuListener?.onClick(menu_sport_game)
        }

        menu_in_play.setOnClickListener {
            mMenuLeftListener?.onClick(menu_in_play.id)
            mDownMenuListener?.onClick(menu_in_play)
        }

        menu_date_row_today.setOnClickListener {
            mMenuLeftListener?.onClick(menu_date_row_today.id)
            mDownMenuListener?.onClick(menu_date_row_today)
        }

        menu_early.setOnClickListener {
            mMenuLeftListener?.onClick(menu_early.id)
            mDownMenuListener?.onClick(menu_early)
        }

        menu_parlay.setOnClickListener {
            mMenuLeftListener?.onClick(menu_parlay.id)
            mDownMenuListener?.onClick(menu_parlay)
        }

        menu_champion.setOnClickListener {
            mMenuLeftListener?.onClick(menu_champion.id)
            mDownMenuListener?.onClick(menu_champion)
        }

        menu_soccer.setOnClickListener {
            mMenuLeftListener?.onClick(menu_soccer.id)
            mDownMenuListener?.onClick(menu_soccer)
        }

        menu_basketball.setOnClickListener {
            mMenuLeftListener?.onClick(menu_basketball.id)
            mDownMenuListener?.onClick(menu_basketball)
        }

        menu_tennis.setOnClickListener {
            mMenuLeftListener?.onClick(menu_tennis.id)
            mDownMenuListener?.onClick(menu_tennis)
        }

        menu_badminton.setOnClickListener {
            mMenuLeftListener?.onClick(menu_badminton.id)
            mDownMenuListener?.onClick(menu_badminton)
        }

        menu_volleyball.setOnClickListener {
            mMenuLeftListener?.onClick(menu_volleyball.id)
            mDownMenuListener?.onClick(menu_volleyball)
        }

        menu_cg_lottery.setOnClickListener {
            mMenuLeftListener?.onClick(menu_cg_lottery.id)
            mDownMenuListener?.onClick(menu_cg_lottery)
        }

        menu_live_game.setOnClickListener {
            mMenuLeftListener?.onClick(menu_live_game.id)
            mDownMenuListener?.onClick(menu_live_game)
        }

        menu_poker_game.setOnClickListener {
            mMenuLeftListener?.onClick(menu_poker_game.id)
            mDownMenuListener?.onClick(menu_poker_game)
        }

        menu_slot_game.setOnClickListener {
            mMenuLeftListener?.onClick(menu_slot_game.id)
            mDownMenuListener?.onClick(menu_slot_game)
        }

        menu_fish_game.setOnClickListener {
            mMenuLeftListener?.onClick(menu_fish_game.id)
            mDownMenuListener?.onClick(menu_fish_game)
        }
    }

    private fun updateUI(sportMenuResult: SportMenuResult?) {
        val countInPlay =
            sportMenuResult?.sportMenuData?.menu?.inPlay?.items?.sumBy { it.num } ?: 0
        val countToday =
            sportMenuResult?.sportMenuData?.menu?.today?.items?.sumBy { it.num } ?: 0
        val countEarly =
            sportMenuResult?.sportMenuData?.menu?.early?.items?.sumBy { it.num } ?: 0
        val countParlay =
            sportMenuResult?.sportMenuData?.menu?.parlay?.items?.sumBy { it.num } ?: 0
        val countOutright =
            sportMenuResult?.sportMenuData?.menu?.outright?.items?.sumBy { it.num } ?: 0

        menu_in_play.setCount(countInPlay.toString())
        menu_date_row_today.setCount(countToday.toString())
        menu_early.setCount(countEarly.toString())
        menu_parlay.setCount(countParlay.toString())
        menu_champion.setCount(countOutright.toString())
    }

    private fun updateUI(gameCateList: List<GameCateData>?) {
        val isShowThirdGame = sConfigData?.thirdOpen == FLAG_OPEN
        val lotteryCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.CGCP }?.tabDataList?.sumBy { it.gameList.size } ?: 0
        val liveCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.LIVE }?.tabDataList?.sumBy { it.gameList.size } ?: 0
        val pokerCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.QP }?.tabDataList?.sumBy { it.gameList.size } ?: 0
        val slotCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.DZ }?.tabDataList?.sumBy { it.gameList.size } ?: 0
        val fishingCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.BY }?.tabDataList?.sumBy { it.gameList.size } ?: 0

        menu_cg_lottery.visibility = if (isShowThirdGame && lotteryCount > 0) View.VISIBLE else View.GONE
        menu_live_game.visibility = if (isShowThirdGame && liveCount > 0) View.VISIBLE else View.GONE
        menu_poker_game.visibility = if (isShowThirdGame && pokerCount > 0) View.VISIBLE else View.GONE
        menu_slot_game.visibility = if (isShowThirdGame && slotCount > 0) View.VISIBLE else View.GONE
        menu_fish_game.visibility = if (isShowThirdGame && fishingCount > 0) View.VISIBLE else View.GONE
    }


    /**
     * 選單選擇結束，需透過 listener 讓上層關閉 選單
     */
    fun setDownMenuListener(listener: View.OnClickListener?) {
        mDownMenuListener = listener
    }

    //紀錄：選單按鈕點擊跳轉
    fun setMenuLeftListener(listener: MenuLeftListener?) {
        mMenuLeftListener = listener
    }
}