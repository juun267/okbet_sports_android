package org.cxct.sportlottery.ui.maintab.menu

import android.view.View
import org.cxct.sportlottery.databinding.FragmentLeftOthersBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.menu.viewmodel.SportLeftMenuViewModel
import org.cxct.sportlottery.util.LanguageManager

class LeftOthersFragment:BindingSocketFragment<SportLeftMenuViewModel,FragmentLeftOthersBinding>() {

    override fun onInitView(view: View) =binding.run {

    }

    override fun onInitData() {
        super.onInitData()
        //初始化盘口列表
        binding.nodeHandicap
            .setTitle("Handicap Setting")
            .setNodeChild(viewModel.getHandicapConfig())
            .setOnChildClick {
                viewModel.changeHandicap(it.data as String)
            }

        //初始化赔率规则
        binding.nodeBetRule
            .setTitle("Betting Rules")
            .setNodeChild(viewModel.getHandicapData())
            .setOnChildClick {

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