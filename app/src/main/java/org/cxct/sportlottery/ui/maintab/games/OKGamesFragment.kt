package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.fitsSystemStatus
import org.cxct.sportlottery.databinding.FragmentOkgamesBinding
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.FragmentHelper

// okgames主Fragment
class OKGamesFragment: BaseBottomNavigationFragment<OKGamesViewModel>(OKGamesViewModel::class) {

    private lateinit var binding: FragmentOkgamesBinding

    private val fragmentHelper by lazy {
        FragmentHelper(childFragmentManager, R.id.fragmentContainer, arrayOf(
            Pair(AllGamesFragment::class.java, null),
            Pair(PartGamesFragment::class.java, null)))
    }

    private inline fun mainTabActivity() = activity as MainTabActivity
    override fun createRootView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOkgamesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onBindView(view: View) {
        initToolBar()
        showGameAll()
    }

    private fun initToolBar() = binding.homeToolbar.run {
        attach(this@OKGamesFragment, mainTabActivity(), viewModel)
        fitsSystemStatus()
        ivMenuLeft.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            mainTabActivity().showLeftFrament(0, 5)
        }
    }

    open fun showGameResult(list: MutableList<QueryGameEntryData>) {
        (fragmentHelper.getFragment(1) as PartGamesFragment).setItemList(list)
        fragmentHelper.showFragment(1)
    }

    open fun showGameAll() {
        fragmentHelper.showFragment(0)
    }
}