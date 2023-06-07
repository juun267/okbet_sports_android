package org.cxct.sportlottery.ui.maintab.menu

import android.content.Intent
import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.databinding.FragmentLeftOthersBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.menu.viewmodel.SportLeftMenuViewModel
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.setVisibilityByMarketSwitch
import org.cxct.sportlottery.view.onClick

class LeftOthersFragment:BindingSocketFragment<SportLeftMenuViewModel,FragmentLeftOthersBinding>() {

    override fun onInitView(view: View) =binding.run {
        //赛果
        constrainResults.onClick {
            EventBusUtil.post(MenuEvent(false))
            startActivity(Intent(requireActivity(), ResultsSettlementActivity::class.java))
        }

        //规则
        constrainRules.setVisibilityByMarketSwitch()
        constrainRules.onClick {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getGameRuleUrl(requireContext()),
                getString(R.string.game_rule)
            )
        }

        tvNight.onClick {
            //黑夜模式
           viewModel.changeUIMode(true)
        }
    }

    override fun onInitData() {
        super.onInitData()
        //初始化盘口列表
        binding.nodeHandicap
            .setTitle("Handicap Setting")
            .setNodeChild(viewModel.getHandicapConfig())
            .setOnChildClick {
                //改变盘口选择
                viewModel.changeHandicap(it.data as String)
            }

        //初始化投注玩法
        binding.nodeBetRule
            .setTitle("Betting Rules")
            .setNodeChild(viewModel.getBettingRulesData())
            .setOnChildClick {
                //更新投注玩法
                viewModel.updateOddsChangeOption(it.data as Int)
            }

        //初始化语言切换
        binding.nodeLanguage
            .setTitle("Language Selection")
            .setNodeChild(viewModel.getLanguageConfig())
            .setOnChildClick {
                //切换语言
                viewModel.changeLanguage(it.data as LanguageManager.Language)
                MainTabActivity.reStart(requireContext(), true)
            }
    }
}