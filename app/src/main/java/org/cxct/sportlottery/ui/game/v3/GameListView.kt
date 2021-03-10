package org.cxct.sportlottery.ui.game.v3

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.view_game_list.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.league.Row
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.ui.common.SocketLinearManager
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

    var countryList: List<Row>? = null
        set(value) {
            field = value

            field?.let {
                updateCountry(it)
            }
        }

    var countryLeagueListener: CountryLeagueListener? = null
        set(value) {
            field = value

            field?.let {
                countryAdapter.countryLeagueListener = it
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

    private val countryAdapter by lazy {
        CountryAdapter()
    }

    private val leagueAdapter by lazy {
        LeagueAdapter()
    }

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        inflate(context, R.layout.view_game_list, this).apply {
            setupCountryList(this)
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

    private fun setupCountryList(view: View) {
        view.game_list_country_list.apply {
            this.layoutManager =
                SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)

            this.adapter = countryAdapter

            this.addItemDecoration(
                DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            )
        }
    }

    private fun setupLeagueList(view: View) {
        view.game_list_league_list.apply {
            this.layoutManager =
                SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)

            this.adapter = leagueAdapter

            this.addItemDecoration(
                DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            )
        }
    }

    private fun updateMatchType(type: Int) {
        game_list_league_list.visibility = if (type == IN_PLAY) {
            View.VISIBLE
        } else {
            View.GONE
        }

        game_list_country_list.visibility = if (type == TODAY || type == EARLY || type == PARLAY) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun updateCountry(countryList: List<Row>) {
        game_list_league_list.visibility = View.GONE
        game_list_country_list.visibility = View.VISIBLE

        countryAdapter.data = countryList
    }

    private fun updateLeagueOdd(leagueOddList: List<LeagueOdd>) {
        game_list_country_list.visibility = View.GONE
        game_list_league_list.visibility = View.VISIBLE

        leagueAdapter.data = leagueOddList
    }
}