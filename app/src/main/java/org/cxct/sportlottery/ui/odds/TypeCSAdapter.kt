package org.cxct.sportlottery.ui.odds


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.widget.OddsButton
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TextUtil


class TypeCSAdapter(
    private val oddsDetail: OddsDetailListData,
    private val oddsList: List<Odd?>,
    private val onOddClickListener: OnOddClickListener,
    private val oddsType: OddsType
) : RecyclerView.Adapter<TypeCSAdapter.ViewHolder>() {


    private val mOddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                notifyItemChanged(oddsList.indexOf(oddsList.find { o -> o == odd }))
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_grid_item, parent, false))


    override fun getItemCount(): Int = oddsList.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bindModel(oddsList[position])


    inner class ViewHolder(view: View) : OddStateViewHolder(view) {

        private val btnOdds = itemView.findViewById<OddsButton>(R.id.button_odds)

        private fun checkKey(key: String): Boolean {
            return TextUtil.compareWithGameKey(oddsDetail.gameType, key)
        }

        fun bindModel(odd: Odd?) {
            btnOdds?.apply {
                setupOdd(odd, oddsType)
                setupOddState(this, odd)
                setOnClickListener {
                    odd?.let { o -> onOddClickListener.getBetInfoList(o, oddsDetail) }
                }

                when {
                    checkKey(PlayCate.SINGLE_OU.value) || checkKey(PlayCate.DC_OU.value) -> {
                        tv_name.text = when (adapterPosition) {
                            0 -> itemView.context.getString(R.string.odd_button_ou_o)
                            else -> itemView.context.getString(R.string.odd_button_ou_u)
                        }
                    }

                    checkKey(PlayCate.OU_BTS.value) -> {
                        val oddsName = (odd?.nameMap?.get(LanguageManager.getSelectLanguage(context).key) ?: odd?.name)?.substringAfter("&")
                        tv_name.text = oddsName
                    }

                    checkKey(PlayCate.SINGLE_BTS.value) || checkKey(PlayCate.DC_BTS.value) -> {
                        tv_name.text = when (adapterPosition) {
                            0 -> itemView.context.getString(R.string.odds_button_name_y)
                            else -> itemView.context.getString(R.string.odds_button_name_n)
                        }
                    }

                    checkKey(PlayCate.OU_OE.value) -> {
                        tv_name.text = when (odd?.playCode) {
                            "OU&OE-O-O"-> this.context.getString(R.string.odds_button_name_singular)
                            "OU&OE-O-E"-> this.context.getString(R.string.odds_button_name_dual)
                            "OU&OE-U-O"-> this.context.getString(R.string.odds_button_name_singular)
                            "OU&OE-U-E"-> this.context.getString(R.string.odds_button_name_dual)
                            else -> ""
                        }
                    }

                    checkKey(PlayCate.OU_TTS1ST.value) -> {
                        tv_name.text = when (odd?.playCode) {
                            "OU-TTS1ST-O-H"-> this.context.getString(R.string.odds_button_name_home)
                            "OU-TTS1ST-U-H"-> this.context.getString(R.string.odds_button_name_home)
                            "OU-TTS1ST-O-C"-> this.context.getString(R.string.odds_button_name_away)
                            "OU-TTS1ST-U-C"-> this.context.getString(R.string.odds_button_name_away)
                            else -> ""
                        }
                    }

                    checkKey(PlayCate.DC_FLG.value) -> {
                        tv_name.text = when (odd?.playCode) {
                            "DC&FLG-HD-H" -> this.context.getString(R.string.odds_button_name_hdh)
                            "DC&FLG-HD-C" -> this.context.getString(R.string.odds_button_name_hdc)
                            "DC&FLG-CD-H" -> this.context.getString(R.string.odds_button_name_cdh)
                            "DC&FLG-CD-C" -> this.context.getString(R.string.odds_button_name_cdc)
                            "DC&FLG-HC-H" -> this.context.getString(R.string.odds_button_name_hch)
                            "DC&FLG-HC-C" -> this.context.getString(R.string.odds_button_name_hcc)
                            else -> ""
                        }
                    }
                        
                    checkKey(PlayCate.SINGLE_FLG.value) -> {
                        tv_name.text = when (odd?.playCode) {
                            "1X2&FLG-H-H" -> this.context.getString(R.string.odds_button_name_hdh)
                            "1X2&FLG-H-C" -> this.context.getString(R.string.odds_button_name_hdc)
                            "1X2&FLG-D-H" -> this.context.getString(R.string.odds_button_name_cdh)
                            "1X2&FLG-D-C" -> this.context.getString(R.string.odds_button_name_cdc)
                            "1X2&FLG-C-H" -> this.context.getString(R.string.odds_button_name_hch)
                            "1X2&FLG-C-C" -> this.context.getString(R.string.odds_button_name_hcc)
                            else -> ""
                        }
                    }
                }
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = mOddStateRefreshListener

    }


}