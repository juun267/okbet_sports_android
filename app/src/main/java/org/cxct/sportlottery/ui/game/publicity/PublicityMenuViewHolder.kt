package org.cxct.sportlottery.ui.game.publicity

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.PublicityMenuItemBinding
import org.cxct.sportlottery.databinding.ViewPublicityMenuBinding
import org.cxct.sportlottery.util.LocalUtils

class PublicityMenuViewHolder(val binding: ViewPublicityMenuBinding) : RecyclerView.ViewHolder(binding.root) {
    enum class MenuType {
        SPORTS, EGAMES, CASINO, SABONG, AFFILIATE, CONTACT
    }

    fun bind(data: PublicityMenuData) {
        with(binding.tlGames) {
            //region Sports
            getTabAt(MenuType.SPORTS.ordinal)?.customView?.let {
                val sportBinding = getTabViewBinding(it)

                sportBinding.ivIcon.setImageResource(R.drawable.selector_publicity_football)
                sportBinding.ivName.text = LocalUtils.getString(R.string.sport)
            }
            //endregion

            //region E-game
            getTabAt(MenuType.EGAMES.ordinal)?.customView?.let {
                val eGameBinding = getTabViewBinding(it)

                eGameBinding.ivIcon.setImageResource(R.drawable.selector_publicity_e_game)
                eGameBinding.ivName.text = LocalUtils.getString(R.string.e_game)
            }
            //endregion

            //region Casino
            getTabAt(MenuType.CASINO.ordinal)?.customView?.let {
                val casinoBinding = getTabViewBinding(it)

                casinoBinding.ivIcon.setImageResource(R.drawable.selector_publicity_casino)
                casinoBinding.ivName.text = LocalUtils.getString(R.string.casino)
            }
            //endregion

            //region Sabong
            getTabAt(MenuType.SABONG.ordinal)?.customView?.let {
                val sabongBinding = getTabViewBinding(it)

                sabongBinding.ivIcon.setImageResource(R.drawable.selector_publicity_sabong)
                sabongBinding.ivName.text = LocalUtils.getString(R.string.sabong)
            }
            //endregion
        }
    }

    private fun getTabViewBinding(view: View): PublicityMenuItemBinding = PublicityMenuItemBinding.bind(view)
}