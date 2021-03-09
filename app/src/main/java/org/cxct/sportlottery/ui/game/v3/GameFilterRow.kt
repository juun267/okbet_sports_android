package org.cxct.sportlottery.ui.game.v3

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.row_game_filter.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayType
import java.lang.Exception

class GameFilterRow @JvmOverloads constructor(
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
                setupMatchType(it)
            }
        }

    var sportType: String? = null
        set(value) {
            field = value

            field?.let {
                game_filter_sport_type.text = it
            }
        }

    var playType: PlayType? = null
        set(value) {
            field = value

            field?.let {
                setupPlayType(it)
            }
        }

    var backClickListener: OnClickListener? = null
        set(value) {
            field = value

            game_filter_back.setOnClickListener(field)
        }

    var ouHDPClickListener: OnClickListener? = null
        set(value) {
            field = value

            game_filter_ou.setOnClickListener(field)
        }

    var x12ClickListener: OnClickListener? = null
        set(value) {
            field = value

            game_filter_1x2.setOnClickListener(field)
        }

    var queryTextListener: SearchView.OnQueryTextListener? = null
        set(value) {
            field = value

            game_filter_search.setOnQueryTextListener(field)
        }

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        inflate(context, R.layout.row_game_filter, this)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.GameFilterRow)

        try {
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    private fun setupMatchType(type: Int) {
        game_filter_inplay.visibility = if (type == IN_PLAY) {
            View.VISIBLE
        } else {
            View.GONE
        }

        game_filter_1x2.visibility = if (type == IN_PLAY) {
            View.VISIBLE
        } else {
            View.GONE
        }

        game_filter_ou.visibility = if (type == IN_PLAY) {
            View.VISIBLE
        } else {
            View.GONE
        }

        game_filter_search.visibility = if (type == IN_PLAY) {
            View.GONE
        } else {
            View.VISIBLE
        }

        game_filter_sport_type.isSelected = (type != IN_PLAY)
    }

    private fun setupPlayType(type: PlayType) {
        game_filter_1x2.isSelected = (type == PlayType.X12)
        game_filter_ou.isSelected = (type == PlayType.OU_HDP)

    }
}