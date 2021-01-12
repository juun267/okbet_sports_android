package org.cxct.sportlottery.ui.helpCenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment

class GameRuleFragment: BaseFragment<HelpCenterViewModel>(HelpCenterViewModel::class) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_game_rule, container, false)
    }
}