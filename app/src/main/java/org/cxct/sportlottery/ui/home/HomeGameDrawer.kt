package org.cxct.sportlottery.ui.home

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.home_game_drawer.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.match.Match
import org.cxct.sportlottery.network.match.MatchPreloadData
import org.cxct.sportlottery.ui.home.gameDrawer.GameEntity
import org.cxct.sportlottery.ui.home.gameDrawer.ItemType
import org.cxct.sportlottery.ui.home.gameDrawer.RvGameDrawerAdapter

class HomeGameDrawer @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    private val homeGameDrawerAdapter = RvGameDrawerAdapter()

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.home_game_drawer, this, false)
        addView(view)

        try {
            val typedArray = context.theme
                .obtainStyledAttributes(attrs, R.styleable.HomeGameDrawer, 0, 0)

            view.tv_title.text = typedArray.getText(R.styleable.HomeGameDrawer_hgDrawer_title)
            view.tv_count.text = typedArray.getText(R.styleable.HomeGameDrawer_hgDrawer_count)

            rv_game.layoutManager = LinearLayoutManager(context)
            rv_game.adapter = homeGameDrawerAdapter

            //展開/收合
            view.titleBar.setOnClickListener {
                view.expandableLayout.toggle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setTitle(title: String?) {
        tv_title.text = title
    }

    fun setCount(count: String?) {
        tv_count.text = count
    }

    fun setOnSelectItemListener(onSelectItemListener: OnSelectItemListener<Match>?) {
        homeGameDrawerAdapter.setOnSelectItemListener(onSelectItemListener)
    }

    fun setRvGameData(matchPreloadData: MatchPreloadData?) {
        val gameDataList = mutableListOf<GameEntity>()
        matchPreloadData?.datas?.forEach { data ->
            //TODO simon test review gameName 之後 API 會帶
            val headerEntity = GameEntity(ItemType.HEADER, getGameName(data.code))
            gameDataList.add(headerEntity)

            data.matchs.forEachIndexed { index, match ->

                val itemEntity = GameEntity(
                    ItemType.ITEM, getGameName(data.code), match, index != data.matchs.lastIndex
                )
                gameDataList.add(itemEntity)
            }
        }

        homeGameDrawerAdapter.setData(gameDataList)
    }

    private fun getGameName(code: String?): String? {
        return when (code) {
            "FT" -> context.getString(R.string.football)
            "BK" -> context.getString(R.string.basketball)
            "TN" -> context.getString(R.string.tennis)
            "VB" -> context.getString(R.string.volleyball)
            else -> null
        }
    }


}