package org.cxct.sportlottery.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import kotlinx.android.synthetic.main.fragment_menu.btn_close
import kotlinx.android.synthetic.main.fragment_menu_left.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameViewModel

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
        initEvent()
    }

    private fun setupCloseBtn() {
        btn_close.setOnClickListener {
            mDownMenuListener?.onClick(btn_close)
        }
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