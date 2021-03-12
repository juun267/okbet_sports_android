package org.cxct.sportlottery.ui.game.home

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.home_game_table.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.match.MatchPreloadData
import org.cxct.sportlottery.network.service.match_clock.MatchClockCO
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusCO
import org.cxct.sportlottery.ui.game.home.gameDrawer.GameEntity
import org.cxct.sportlottery.ui.game.home.gameDrawer.ItemType
import org.cxct.sportlottery.ui.game.home.gameDrawer.RvGameDrawerAdapter

class HomeGameTable @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    private val homeGameDrawerAdapter = RvGameDrawerAdapter()

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.home_game_table, this, false)
        addView(view)

        val typedArray = context.theme
            .obtainStyledAttributes(attrs, R.styleable.CustomView, 0, 0)
        try {
            view.tv_title.text = typedArray.getText(R.styleable.CustomView_cvTitle)
            view.tv_count.text = typedArray.getText(R.styleable.CustomView_cvCount)

            rv_game.layoutManager = LinearLayoutManager(context)
            rv_game.adapter = homeGameDrawerAdapter
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    fun setTitle(title: String?) {
        tv_title.text = title
    }

    fun setCount(count: String?) {
        tv_count.text = count
    }

    fun setOnSelectItemListener(onSelectItemListener: OnSelectItemListener<GameEntity>?) {
        homeGameDrawerAdapter.setOnSelectItemListener(onSelectItemListener)
    }

    fun setOnSelectFooterListener(onSelectFooterListener: OnSelectItemListener<GameEntity>?) {
        homeGameDrawerAdapter.setOnSelectFooterListener(onSelectFooterListener)
    }

    fun setRvGameData(matchPreloadData: MatchPreloadData?) {
        val gameDataList = mutableListOf<GameEntity>()
        matchPreloadData?.datas?.forEach { data ->
            val headerEntity = GameEntity(ItemType.HEADER, data.code, data.name, data.num)
            gameDataList.add(headerEntity)

            data.matchs.forEachIndexed { index, match ->
                val itemType = if (index == data.matchs.lastIndex) ItemType.FOOTER else ItemType.ITEM
                val itemEntity = GameEntity(itemType, data.code, data.name, data.num, match)
                gameDataList.add(itemEntity)
            }
        }

        homeGameDrawerAdapter.setData(gameDataList)
    }

    fun setMatchStatusData(matchStatusCO: MatchStatusCO?) {
        homeGameDrawerAdapter.setMatchStatusData(matchStatusCO)
    }

    fun setMatchClockData(matchClockCO: MatchClockCO?) {
        homeGameDrawerAdapter.setMatchClockData(matchClockCO)
    }

    fun release() {
        homeGameDrawerAdapter.stopAllTimer()
    }
}