package org.cxct.sportlottery.ui.maintab.menu

import android.view.View
import org.cxct.sportlottery.databinding.FragmentLeftOthersBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.menu.viewmodel.SportLeftMenuViewModel

class LeftOthersFragment:BindingSocketFragment<SportLeftMenuViewModel,FragmentLeftOthersBinding>() {

    override fun onInitView(view: View) =binding.run {

    }

    override fun onInitData() {
        super.onInitData()
        binding.nodeHandicap.setNodeData("Handicap Setting", viewModel.getHandicapConfig())
        binding.nodeBetRule.setNodeData("Betting Rules", viewModel.getHandicapData())
        binding.nodeLanguage.setNodeData("Language Selection", viewModel.getHandicapData())

    }
}