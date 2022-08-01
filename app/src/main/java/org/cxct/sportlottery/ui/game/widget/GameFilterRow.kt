package org.cxct.sportlottery.ui.game.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.row_game_filter.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayCate
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
                updateMatchType(it)
            }
        }

    var isPlayCateVisible: Boolean? = null
        set(value) {
            field = value

            field?.let {
                game_filter_1x2.visibility = if (it && matchType == IN_PLAY) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                game_filter_ou.visibility = if (it && matchType == IN_PLAY) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }

    var playCate: PlayCate? = null
        set(value) {
            field = value

            field?.let {
                updatePlayCate(it)
            }
        }

    var sportName: String? = null
        set(value) {
            field = value
            game_filter_sport_type.text = sportName
        }

    var isSearchViewVisible: Boolean? = null
        set(value) {
            field = value

            field?.let {
                game_filter_search.visibility = if (it) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }

    var searchHint: String? = null
        set(value) {
            field = value

            game_filter_search.queryHint = field
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
        inflate(context, R.layout.row_game_filter, this).apply {
            this.game_filter_sport_type.isSelected = true

            this.game_filter_search.findViewById<View>(R.id.search_src_text).apply {
                setPadding(0, 0, 0, 0)
            }
        }

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.GameFilterRow)

        try {
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    private fun updateMatchType(type: Int) {
        game_filter_inplay.apply {
            visibility = if (type == IN_PLAY || type == AT_START) {
                View.VISIBLE
            } else {
                View.GONE
            }
            text = when (type) {
                IN_PLAY -> resources.getString(R.string.match_type_row_in_play)
                AT_START -> resources.getString(R.string.game_soon)
                else -> null
            }
        }

        game_filter_1x2.visibility = if (type == IN_PLAY || type == AT_START) {
            View.VISIBLE
        } else {
            View.GONE
        }

        game_filter_ou.visibility = if (type == IN_PLAY || type == AT_START) {
            View.VISIBLE
        } else {
            View.GONE
        }

        game_filter_sport_type.isSelected = (type != IN_PLAY && type != AT_START)
    }

    private fun updatePlayCate(type: PlayCate) {
        game_filter_1x2.isSelected = (type == PlayCate.SINGLE)
        game_filter_ou.isSelected = (type == PlayCate.OU_HDP)
    }
}