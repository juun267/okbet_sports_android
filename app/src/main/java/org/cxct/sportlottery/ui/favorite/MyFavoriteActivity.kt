package org.cxct.sportlottery.ui.favorite

import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_my_favorite.*
import kotlinx.android.synthetic.main.fragment_game_v3.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.ui.base.BaseFavoriteActivity
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeListener
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryListener
import org.cxct.sportlottery.util.SpaceItemDecoration

class MyFavoriteActivity : BaseFavoriteActivity<MyFavoriteViewModel>(MyFavoriteViewModel::class) {

    private val gameTypeAdapter by lazy {
        GameTypeAdapter().apply {
            gameTypeListener = GameTypeListener {
                viewModel.switchGameType(it)
            }
        }
    }

    private val playCategoryAdapter by lazy {
        PlayCategoryAdapter().apply {
            playCategoryListener = PlayCategoryListener {
                viewModel.switchPlay(it)
            }
        }
    }

    private val leagueAdapter by lazy {
        LeagueAdapter(MatchType.MY_EVENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_favorite)

        setupToolbar()
        setupGameTypeList()
        setupPlayCategory()
        setupLeagueOddList()

        initObserver()
    }

    private fun setupToolbar() {
        setSupportActionBar(favorite_toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupGameTypeList() {
        favorite_game_type_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            this.adapter = gameTypeAdapter

            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_sport_type
                )
            )
        }
    }

    private fun setupPlayCategory() {
        favorite_play_category.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            this.adapter = playCategoryAdapter

            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_play_category
                )
            )
        }
    }

    private fun setupLeagueOddList() {
        favorite_game_list.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            this.adapter = leagueAdapter
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.getSportQuery()
        loading()
    }

    private fun initObserver() {
        viewModel.sportQueryData.observe(this, {
            it?.getContentIfNotHandled()?.let { sportQueryData ->

                updateGameTypeList(sportQueryData.items?.map { item ->
                    Item(
                        code = item.code ?: "",
                        name = item.name ?: "",
                        num = item.num ?: 0,
                        play = null,
                        sortNum = item.sortNum ?: 0
                    ).apply {
                        this.isSelected = item.isSelected
                    }
                })

                updatePlayCategory(sportQueryData.items?.find { item ->
                    item.isSelected
                }?.play)

            }
        })

        viewModel.curPlay.observe(this, {
            showPlayCateBottomSheet(it)
        })

        viewModel.favorMatchOddList.observe(this, {
            hideLoading()
            leagueAdapter.data = it
        })
    }

    private fun updateGameTypeList(items: List<Item>?) {
        gameTypeAdapter.dataSport = items ?: listOf()

        favorite_game_type.text = when (items?.find {
            it.isSelected
        }?.code) {
            GameType.FT.key -> getString(GameType.FT.string)
            GameType.BK.key -> getString(GameType.BK.string)
            GameType.TN.key -> getString(GameType.TN.string)
            GameType.VB.key -> getString(GameType.VB.string)
            else -> ""
        }

        Glide.with(this).load(
            when (items?.find {
                it.isSelected
            }?.code) {
                GameType.FT.key -> R.drawable.soccer108
                GameType.BK.key -> R.drawable.basketball108
                GameType.TN.key -> R.drawable.tennis108
                GameType.VB.key -> R.drawable.volleyball108
                else -> null
            }
        ).into(favorite_bg_layer2)
    }

    private fun updatePlayCategory(plays: List<Play>?) {
        playCategoryAdapter.data = plays ?: listOf()
    }

    private fun showPlayCateBottomSheet(play: Play) {
        showBottomSheetDialog(
            play.name,
            play.playCateList?.map { playCate -> StatusSheetData(playCate.code, playCate.name) }
                ?: listOf(),
            StatusSheetData(
                (play.playCateList?.find { it.isSelected } ?: play.playCateList?.first())?.code,
                (play.playCateList?.find { it.isSelected } ?: play.playCateList?.first())?.name
            ),
            StatusSheetAdapter.ItemCheckedListener { _, data ->
                viewModel.switchPlayCategory(data.code)
                bottomSheet.dismiss()
            })
    }
}