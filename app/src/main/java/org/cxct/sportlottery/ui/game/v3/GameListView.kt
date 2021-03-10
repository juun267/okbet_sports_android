package org.cxct.sportlottery.ui.game.v3

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.view_game_list.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.util.SpaceItemDecoration
import java.lang.Exception

class GameListView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        const val IN_PLAY = 0
        const val TODAY = 1
        const val EARLY = 2
        const val PARLAY = 3
        const val OUTRIGHT = 4
        const val AT_START = 5
    }

    var matchType: Int? = null
        set(value) {
            field = value

            field?.let {
                updateMatchType(it)
            }
        }

    var leagueOddList: List<LeagueOdd>? = null
        set(value) {
            field = value

            field?.let {
                updateLeagueOdd(it)
            }
        }

    var leagueOddListener: LeagueOddListener? = null
        set(value) {
            field = value

            field?.let {
                leagueAdapter.leagueOddListener = field
            }
        }

    private val leagueAdapter by lazy {
        LeagueAdapter()
    }

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        inflate(context, R.layout.view_game_list, this).apply {
            setupLeagueList(this)
        }

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.GameListView)

        try {
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    private fun setupLeagueList(view: View) {
        view.game_list_league_list.apply {
            this.layoutManager =
                SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)

            this.adapter = leagueAdapter

            this.addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec
                )
            )
        }
    }

    private fun updateMatchType(type: Int) {
        game_list_league_list.visibility = if (type == IN_PLAY) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun updateLeagueOdd(leagueOddList: List<LeagueOdd>) {
        leagueAdapter.data = leagueOddList
    }
}