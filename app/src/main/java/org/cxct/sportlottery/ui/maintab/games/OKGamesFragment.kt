package org.cxct.sportlottery.ui.maintab.games

import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel

// okgames主Fragment
class OKGamesFragment: BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    override fun layoutId() = R.layout.fragment_okgames

    override fun onBindView(view: View) {

    }
}