package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.databinding.FragmentPartOkgamesBinding
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment

// 指定类别的三方游戏
class PartGamesFragment: BaseBottomNavigationFragment<OKGamesViewModel>(OKGamesViewModel::class) {

    private lateinit var binding: FragmentPartOkgamesBinding
    private inline fun okgamesFragment() = parentFragment as OKGamesFragment
    override fun createRootView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPartOkgamesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onBindView(view: View) {

    }

}