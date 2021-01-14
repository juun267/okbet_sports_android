package org.cxct.sportlottery.ui.game.common

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.row_match_type.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayType
import java.lang.Exception


class MatchTypeRow @JvmOverloads constructor(
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

    var type = IN_PLAY
        set(value) {
            field = value

            setupMatchType(field)
        }

    var sport: String? = null
        set(value) {
            field = value

            row_sport.text = field
        }

    var curPlayType: PlayType = PlayType.OU_HDP
        set(value) {
            field = value

            setupPlayType(field)
        }

    var ouHDPClickListener: OnClickListener? = null
        set(value) {
            field = value

            row_ou.setOnClickListener(field)
        }

    var x12ClickListener: OnClickListener? = null
        set(value) {
            field = value

            row_1x2.setOnClickListener(field)
        }

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        inflate(context, R.layout.row_match_type, this)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MatchTypeRow)
        try {
            type = typedArray.getInteger(R.styleable.MatchTypeRow_matchType, -1)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    private fun setupMatchType(type: Int) {
        when (type) {
            IN_PLAY -> {
                row_match_type.apply {
                    visibility = VISIBLE
                    text = context.getString(R.string.inplay_row_title)
                }
                row_sport.visibility = VISIBLE
                row_1x2.visibility = VISIBLE
                row_ou.visibility = VISIBLE
                row_match.visibility = GONE
                row_outright.visibility = GONE
            }
            TODAY -> {
                this.visibility = GONE
            }
            EARLY, PARLAY -> {
                row_match.apply {
                    visibility = VISIBLE
                    isSelected = true
                }
                row_match_type.visibility = GONE
                row_sport.visibility = GONE
                row_1x2.visibility = GONE
                row_ou.visibility = GONE
                row_outright.visibility = GONE
            }
            OUTRIGHT -> {
                row_outright.apply {
                    visibility = VISIBLE
                    isSelected = true
                }
                row_match.visibility = GONE
                row_match_type.visibility = GONE
                row_sport.visibility = GONE
                row_1x2.visibility = GONE
                row_ou.visibility = GONE
            }

            AT_START -> {
                row_match_type.apply {
                    visibility = VISIBLE
                    text = context.getString(R.string.match_type_row_at_start)
                }
                row_sport.visibility = VISIBLE
                row_1x2.visibility = VISIBLE
                row_ou.visibility = VISIBLE
                row_match.visibility = GONE
                row_outright.visibility = GONE
            }
        }
    }

    private fun setupPlayType(playType: PlayType) {
        row_ou.isSelected = (playType == PlayType.OU_HDP)
        row_1x2.isSelected = (playType == PlayType.X12)
    }
}