package org.cxct.sportlottery.ui.favorite

import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_my_favorite.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.base.BaseFavoriteActivity
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeListener
import org.cxct.sportlottery.util.SpaceItemDecoration

class MyFavoriteActivity : BaseFavoriteActivity<MyFavoriteViewModel>(MyFavoriteViewModel::class) {

    private val gameTypeAdapter by lazy {
        GameTypeAdapter().apply {
            gameTypeListener = GameTypeListener {
                viewModel.switchGameType(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_favorite)

        setupToolbar()
        setupGameTypeList()

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

    override fun onStart() {
        super.onStart()

        viewModel.getSportQuery()
    }

    private fun initObserver() {
        viewModel.gameTypeList.observe(this, {
            it?.getContentIfNotHandled()?.let { gameTypeList ->

                updateGameTypeList(gameTypeList)
            }
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
}