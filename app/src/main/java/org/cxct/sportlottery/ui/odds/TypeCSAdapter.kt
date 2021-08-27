package org.cxct.sportlottery.ui.odds


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
                    checkKey(PlayCate.SINGLE_OU.value) || checkKey(PlayCate.DC_OU.value) || checkKey(PlayCate.OU_BTS.value) -> {
                        tv_name.text = when (adapterPosition) {
                            0 -> itemView.context.getString(R.string.odds_button_name_o)
                            else -> itemView.context.getString(R.string.odds_button_name_u)
                        }
                    }

                    checkKey(PlayCate.SINGLE_BTS.value) || checkKey(PlayCate.DC_BTS.value) -> {
                        tv_name.text = when (adapterPosition) {
                            0 -> itemView.context.getString(R.string.odds_button_name_y)
                            else -> itemView.context.getString(R.string.odds_button_name_n)
                        }
                    }

                    checkKey(PlayCate.OU_OE.value) -> {
                        tv_name.text = when (adapterPosition) {
                            0 -> itemView.context.getString(R.string.odds_button_name_singular)
                            else -> itemView.context.getString(R.string.odds_button_name_dual)
                        }
                    }

                    checkKey(PlayCate.OU_TTS1ST.value) -> {
                        val oddsName = (odd?.nameMap?.get(LanguageManager.getSelectLanguage(context).key) ?: odd?.name)?.substringAfter("&")
                        tv_name.text = oddsName
                    }

                    checkKey(PlayCate.SINGLE_FLG.value) -> {
                        val oddsName = (odd?.nameMap?.get(LanguageManager.getSelectLanguage(context).key) ?: odd?.name)?.substringAfter("-")
                        tv_name.text = oddsName?.plus(context.getString(R.string.odds_button_name_plus_flg))
                    }
                }
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = mOddStateRefreshListener

    }


}