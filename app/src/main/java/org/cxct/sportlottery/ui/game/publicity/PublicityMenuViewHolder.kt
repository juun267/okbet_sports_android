package org.cxct.sportlottery.ui.game.publicity

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.PublicityMenuItemBinding
import org.cxct.sportlottery.databinding.ViewPublicityMenuBinding
import org.cxct.sportlottery.util.LocalUtils

class PublicityMenuViewHolder(val binding: ViewPublicityMenuBinding) : RecyclerView.ViewHolder(binding.root) {
    enum class MenuType {
        SPORTS, EGAMES, CASINO, SABONG, AFFILIATE, CONTACT
    }

    private val mPublicitySportPagerAdapter by lazy {
        PublicitySportPagerAdapter()
    }

    private val mPublicitySportIndicatorAdapter by lazy {
        PublicitySportIndicatorAdapter()
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

        //region SportMenu

        with(binding) {
            //view pager
            vpSports.adapter = mPublicitySportPagerAdapter

            val sportPageList = data.sportMenuDataList?.chunked(PublicitySportPageItemSize)

            sportPageList?.let {
                mPublicitySportPagerAdapter.setSportPageData(it)
            }

            vpSports.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    mPublicitySportIndicatorAdapter.setupSportSelectedList(sportPageList, position)
                }
            })

            //indicator
            rvIndicator.layoutManager = LinearLayoutManager(root.context, LinearLayoutManager.HORIZONTAL, false)
            rvIndicator.adapter = mPublicitySportIndicatorAdapter
            if ((sportPageList?.size ?: 0) > 1) {
                rvIndicator.visibility = View.VISIBLE
                //預設第一項
                mPublicitySportIndicatorAdapter.setupSportSelectedList(sportPageList, 0)
            } else {
                rvIndicator.visibility = View.GONE
            }
        }
        //endregion
    }

    private fun getTabViewBinding(view: View): PublicityMenuItemBinding = PublicityMenuItemBinding.bind(view)
}