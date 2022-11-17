package org.cxct.sportlottery.ui.maintab.worldcup

import org.cxct.sportlottery.R
import org.cxct.sportlottery.adapter.recyclerview.BindingAdapter
import org.cxct.sportlottery.databinding.ItemTabHomeCupBinding

class HomeWorldCupTabAdapter: BindingAdapter<Pair<Int, Int>, ItemTabHomeCupBinding>(getItems()) {

    companion object {
        fun getItems() = mutableListOf(
                Pair(R.drawable.selector_home_tab_cup_recommend, R.string.home_recommend),
                Pair(R.drawable.selector_home_tab_cup_live, R.string.home_live),
                Pair(R.drawable.selector_home_tab_cup, R.string.home_word_cup),
                Pair(R.drawable.selector_home_tab_cup_sport, R.string.home_sports),
//                Pair(R.drawable.selector_home_tab_cup_inplay, R.string.home_in_play),
                Pair(R.drawable.selector_home_tab_cup_slot, R.string.home_slot),
                Pair(R.drawable.selector_home_tab_cup_okgame, R.string.home_on_game),
            )
        }

    override fun onBinding(viewBinding: ItemTabHomeCupBinding, item: Pair<Int, Int>) = viewBinding.run  {
        ivLogo.setImageResource(item.first)
        tvName.setText(item.second)
        root.isSelected = item.second.equals(R.string.home_word_cup)
    }

}
