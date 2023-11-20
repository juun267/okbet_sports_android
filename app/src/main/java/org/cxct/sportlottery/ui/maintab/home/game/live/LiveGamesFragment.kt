package org.cxct.sportlottery.ui.maintab.home.game.live

import androidx.core.view.postDelayed
import org.cxct.sportlottery.databinding.FragmentGamevenueBinding
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.ui.maintab.games.OKLiveFragment
import org.cxct.sportlottery.ui.maintab.home.game.slot.ElectGamesFragement

class LiveGamesFragment: ElectGamesFragement<OKGamesViewModel, FragmentGamevenueBinding>() {

    override fun onSearch(key: String) {
        (requireActivity() as MainTabActivity).apply {
            jumpToOkLive()
            binding.root.postDelayed(500){
                (getCurrentFragment() as? OKLiveFragment)?.search(key)
            }
        }
    }


    override fun onInitData() {
        loading()
        viewModel.getOKLiveHall()
    }

}