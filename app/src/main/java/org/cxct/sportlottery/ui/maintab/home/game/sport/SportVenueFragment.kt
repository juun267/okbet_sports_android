package org.cxct.sportlottery.ui.maintab.home.game.sport

import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentGamevenueBinding
import org.cxct.sportlottery.network.sport.Sport
import org.cxct.sportlottery.ui.maintab.home.game.GameVenueFragment
import org.cxct.sportlottery.ui.sport.SportTabViewModel

// 体育分类
class SportVenueFragment: GameVenueFragment<SportTabViewModel, FragmentGamevenueBinding>() {

    private val matchTabAdapter = MatchTableAdapter()
    private val sportTypeAdapter = SportTypeAdapter()

    override fun onInitView(view: View) {
        super.onInitView(view)
        binding.rvcGameType.adapter = matchTabAdapter
        binding.rvcGameList.adapter = sportTypeAdapter
    }

    override fun onBindViewStatus(view: View) {
        initObserver()
    }

    override fun onInitData() {
        loading()
        viewModel.getMatchData()
    }

    private fun initObserver() {
        viewModel.sportMenuResult.observe(viewLifecycleOwner) {
            hideLoading()
            val menu = it.getData()?.menu ?: return@observe

            val datas = mutableListOf<Pair<Int, Sport>>()
            menu.bkEnd?.let { datas.add(Pair(R.string.home_tab_end_score, it)) }
            menu.inPlay?.let { datas.add(Pair(R.string.home_tab_in_play, it)) }
            menu.today?.let { datas.add(Pair(R.string.home_tab_today, it)) }
            menu.early?.let { datas.add(Pair(R.string.home_tab_early, it)) }
            menu.parlay?.let { datas.add(Pair(R.string.home_tab_parlay, it)) }
            menu.outright?.let { datas.add(Pair(R.string.home_tab_outright, it)) }
            matchTabAdapter.setNewInstance(datas)
            sportTypeAdapter.setUp(datas)
        }
    }


}