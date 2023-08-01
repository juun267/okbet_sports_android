package org.cxct.sportlottery.ui.maintab.home.view

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseAlertDialog
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimeUtil.NEWS_TIME_FORMAT2
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils

class WinsDialog(private val betRecode: RecordNewEvent,
                 act: AppCompatActivity,
                 private val onEnterGame: (RecordNewEvent) -> Unit)
    : BaseAlertDialog(act), OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setBackgroundDrawable(null)
        setContentView(R.layout.dialog_wins_betdetails)
        initStyle()
        bindInfo()
    }

    private fun bindInfo() {

        if (betRecode.isSportBet()) {
            var gameIcon = GameType.getGameTypeMenuIcon(betRecode.firmType)
            if (gameIcon == R.drawable.ic_game_champ) {
                gameIcon = R.drawable.ic_okgames_nodata
            }
            findViewById<ImageView>(R.id.ivGamesIcon)!!.setImageResource(gameIcon)
        } else {
            findViewById<ImageView>(R.id.ivGamesIcon)!!.load(betRecode.iconUrl)
        }

        setText(R.id.tvGameName, betRecode.games)
        setText(R.id.tvPlayer, "${context.getString(R.string.N710)}: ${betRecode.player}")

        if (betRecode.betTime != 0L) {
            setText(R.id.tvBetTime, "${context.getString(R.string.I039)}: ${TimeUtil.timeFormat(betRecode.betTime, NEWS_TIME_FORMAT2)}")
        }
        showCurrencySign.let {
            setText(R.id.tvBet, "$it ${betRecode.betAmount}")
            val color = if (betRecode.profitAmount.toIntS() < 0) {
                context.getColor(R.color.color_FF2E00)
            } else {
                context.getColor(R.color.color_1CD219)
            }
            setText(R.id.tvPayout, "$it ${betRecode.profitAmount}").setTextColor(color)
        }
    }

    private inline fun setText(viewId: Int, text: String): TextView {
        return findViewById<TextView>(viewId)!!.apply { setText(text) }
    }


    private fun initStyle() {
        findViewById<View>(R.id.ivClose)!!.setOnClickListener { dismiss() }
        val bgDrawable = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.white, R.color.color_ebf1fc)
        findViewById<View>(R.id.llBet)!!.background = bgDrawable
        findViewById<View>(R.id.llPayout)!!.background = bgDrawable
        val btnPlay = findViewById<View>(R.id.btnPlay)!!
        btnPlay.background = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.color_025BE8)
        btnPlay.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        dismiss()
        onEnterGame.invoke(betRecode)
    }

}