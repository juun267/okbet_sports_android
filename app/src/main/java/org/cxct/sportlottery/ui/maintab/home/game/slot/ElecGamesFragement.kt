package org.cxct.sportlottery.ui.maintab.home.game.slot

import android.view.View
import org.cxct.sportlottery.databinding.FragmentGamevenueBinding
import org.cxct.sportlottery.ui.maintab.home.game.GameVenueFragment
import org.cxct.sportlottery.ui.sport.SportTabViewModel

class ElecGamesFragement: GameVenueFragment<SportTabViewModel, FragmentGamevenueBinding>() {

    private val tabAdapter = ElecTabAdapter()
    private val gameAdapter = ElecGameAdapter()

    override fun onInitView(view: View) {
        super.onInitView(view)
        binding.rvcGameType.adapter = tabAdapter
        binding.rvcGameList.adapter = gameAdapter
        loading()
    }

}