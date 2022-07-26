package org.cxct.sportlottery.ui.game.publicity

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.PublicityMenuItemBinding
import org.cxct.sportlottery.databinding.ViewPublicityMenuBinding
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.isCreditSystem

class PublicityMenuViewHolder(
    val binding: ViewPublicityMenuBinding,
    private val publicityAdapterListener: GamePublicityNewAdapter.PublicityAdapterNewListener
) : RecyclerView.ViewHolder(binding.root) {
    enum class MenuType {
        SPORTS, EGAMES, CASINO, SABONG, AFFILIATE, CONTACT
    }

    private val mPublicitySportPagerAdapter by lazy {
        PublicitySportPagerAdapter(publicityAdapterListener)
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

            //20220725 PM-BiLL 暫時替換真人、鬥雞
            /*//region Casino
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
            //endregion*/

            clearOnTabSelectedListeners()
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when (selectedTabPosition) {
                        MenuType.SPORTS.ordinal -> {
                            setSportViewPagerBlockVisibility(true)
                            binding.ivThirdGame.visibility = View.GONE
                        }
                        else -> {
                            setSportViewPagerBlockVisibility(false)
                            binding.ivThirdGame.visibility = View.VISIBLE
                            setupThirdGameInfo(data)
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

            })
        }

        //version update
        binding.menuVersionUpdate.setOnClickListener {
            publicityAdapterListener.onClickVersionUpdateListener()

        }

        //appearance
        binding.menuAppearance.setOnClickListener {
            publicityAdapterListener.onClickAppearanceListener()

        }

        //affiliate or faqs
        with(binding.menuAffiliate) {
            if (isCreditSystem()) {
                setIconResource(R.drawable.ic_publicity_faqs)
                setTitle(LocalUtils.getString(R.string.publicity_faqs))
                setOnClickListener {
                    publicityAdapterListener.onClickFAQsListener()
                }
            } else {
                setIconResource(R.drawable.ic_publicity_affiliate)
                setTitle(LocalUtils.getString(R.string.btm_navigation_affiliate))
                setOnClickListener {
                    publicityAdapterListener.onClickAffiliateListener()
                }
            }
        }

        //contact
        binding.menuContact.setOnClickListener {
            publicityAdapterListener.onClickContactListener()

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

    /**
     * 顯示或隱藏體育球種清單
     * @param isVisible true: 顯示, false: 隱藏
     */
    private fun setSportViewPagerBlockVisibility(isVisible: Boolean) {
        with(binding) {
            vpSports.isVisible = isVisible
            rvIndicator.isVisible = isVisible
        }
    }

    //TODO 若無遊戲時該如何顯示尚待PM確認
    private fun setupThirdGameInfo(data: PublicityMenuData) {
        when (binding.tlGames.selectedTabPosition) {
            MenuType.EGAMES.ordinal -> {
                when {
                    data.eGameMenuData != null -> {
                        binding.ivThirdGame.setImageResource(R.drawable.image_e_game_play_now)
                        binding.ivThirdGame.setOnClickListener {
                            data.eGameMenuData?.let { thirdDictValues ->
                                publicityAdapterListener.onGoThirdGamesListener(thirdDictValues)
                            }
                        }
                    }
                    else -> {
                        binding.ivThirdGame.setImageResource(R.drawable.image_e_game_coming_soon)
                        binding.ivThirdGame.setOnClickListener {
                            //do nothing
                        }
                    }
                }
            }
            //20220725 PM-BiLL 暫時替換真人、鬥雞
            /*selectedPosition == MenuType.CASINO.ordinal && data.casinoMenuData != null -> {
                binding.ivThirdGame.setImageResource(R.drawable.image_casino_coming_soon)
                binding.ivThirdGame.setOnClickListener {
                    //TODO 點擊第三方遊戲事件
                }
            }
            selectedPosition == MenuType.SABONG.ordinal && data.sabongMenuData != null -> {
                binding.ivThirdGame.setImageResource(R.drawable.image_sabong_coming_soon)
                binding.ivThirdGame.setOnClickListener {
                    //TODO 點擊第三方遊戲事件
                }
            }*/
        }
    }

    private fun getTabViewBinding(view: View): PublicityMenuItemBinding = PublicityMenuItemBinding.bind(view)
}