package org.cxct.sportlottery.ui.maintab.home.game.live

import android.view.View
import androidx.core.view.postDelayed
import org.cxct.sportlottery.common.extentions.onConfirm
import org.cxct.sportlottery.databinding.FragmentGamevenueBinding
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKGamesFragment
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.ui.maintab.games.OKLiveFragment
import org.cxct.sportlottery.ui.maintab.home.game.slot.ElecGamesFragement

class LiveGamesFragement: ElecGamesFragement<OKGamesViewModel, FragmentGamevenueBinding>() {

    override fun onInitView(view: View) {
        super.onInitView(view)
        binding.etSearch.onConfirm {
            (requireActivity() as MainTabActivity).apply {
                jumpToOkLive()
                binding.etSearch.postDelayed(500){
                    (getCurrentFragment() as? OKLiveFragment)?.search(it)
                }
            }
        }
    }
    override fun onInitData() {
        loading()
        viewModel.getOKLiveHall()
    }

}