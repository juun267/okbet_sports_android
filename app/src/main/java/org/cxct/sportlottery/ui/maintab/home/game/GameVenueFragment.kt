package org.cxct.sportlottery.ui.maintab.home.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.FragmentGamevenueBinding
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel

open class GameVenueFragment: BindingFragment<OKGamesViewModel, FragmentGamevenueBinding>() {

    private val loadingHolder by lazy { Gloading.wrapView(binding.root) }

    override fun dismissLoading() = loadingHolder.showLoadSuccess()
    override fun showLoading() = loadingHolder.showLoading()

    override fun createRootView(inflater: LayoutInflater,
                                container: ViewGroup?,
                                savedInstanceState: Bundle?) = loadingHolder.wrapper

    override fun onInitView(view: View) {
        showLoading()
    }
}