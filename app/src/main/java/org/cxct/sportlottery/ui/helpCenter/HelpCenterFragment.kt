package org.cxct.sportlottery.ui.helpCenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_help_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment

class HelpCenterFragment: BaseFragment<HelpCenterViewModel>(HelpCenterViewModel::class) {

    private val mNavController by lazy {
        findNavController()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_help_center, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEvent()
    }

    private fun setupEvent() {
        linear_game_rule.setOnClickListener {
            val action = HelpCenterFragmentDirections.actionHelpCenterFragmentToGameRuleFragment()
            mNavController.navigate(action)
        }
    }
}