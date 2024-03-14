package org.cxct.sportlottery.ui.maintab.home.game.live

import android.widget.EditText
import androidx.core.view.postDelayed
import org.cxct.sportlottery.databinding.FragmentGamevenueBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.ui.maintab.games.OKLiveFragment
import org.cxct.sportlottery.ui.maintab.home.game.slot.ElectGamesFragment

class LiveGamesFragment: ElectGamesFragment<OKGamesViewModel, FragmentGamevenueBinding>() {

    override fun showMoreGames(okGameBean: OKGameBean) {
        (requireActivity() as MainTabActivity).jumpToOkLive()
    }

    override fun onSearch(key: String, editText: EditText) {
        editText.setText("")
        (requireActivity() as MainTabActivity).apply {
            jumpToOkLive()
            binding.root.postDelayed(500){
                (getCurrentFragment() as? OKLiveFragment)?.search(key)
            }
        }
    }


    override fun onInitData() {
        if(gameAdapter2.itemCount > 0) {
            return
        }
        val okLiveGamesHall = OKGamesViewModel.okLiveGamesHall
        if (okLiveGamesHall == null || !setData(okLiveGamesHall.first)) {
//            showLoadingView()
            if (okLiveGamesHall != null && okLiveGamesHall.second - System.currentTimeMillis() > 10_000) {
                viewModel.getOKLiveHall()
            }
        }

    }

}