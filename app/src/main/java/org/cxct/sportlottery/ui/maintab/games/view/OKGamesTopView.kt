package org.cxct.sportlottery.ui.maintab.games.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.widget.LinearLayoutCompat
import com.stx.xhb.androidx.XBanner
import org.cxct.sportlottery.R


class OKGamesTopView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0): LinearLayoutCompat(context, attrs, defStyle) {


    private val okgamesBanner: XBanner by lazy { findViewById(R.id.xbanner) }
    private val edtSearch: EditText by lazy { findViewById(R.id.edtSearchGames) }

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.layout_okgames_top, this, true)
        initView()
    }

    private fun initView() {

    }

}