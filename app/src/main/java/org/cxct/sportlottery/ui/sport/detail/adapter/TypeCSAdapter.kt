package org.cxct.sportlottery.ui.sport.detail.adapter

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.common.PlayCode
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.sport.detail.OddStateViewHolderDetail
import org.cxct.sportlottery.ui.sport.detail.OddsDetailListData
import org.cxct.sportlottery.ui.sport.detail.OnOddClickListener
import org.cxct.sportlottery.ui.sport.oddsbtn.OddsButtonDetail
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TextUtil


class TypeCSAdapter(
    private val oddsDetail: OddsDetailListData,
    private val oddsList: List<Odd?>,
    private val onOddClickListener: OnOddClickListener,
    private val oddsType: OddsType,
    private val isOddPercentage:Boolean? = false
) : RecyclerView.Adapter<TypeCSAdapter.ViewHolder>() {


    private val mOddStateRefreshListener by lazy {
        object : OddStateViewHolderDetail.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                notifyItemChanged(oddsList.indexOf(oddsList.find { o -> o == odd }))
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val oddsBtn = OddsButtonDetail(parent.context)
        oddsBtn.layoutParams = LinearLayout.LayoutParams(-1, -2)
        return ViewHolder(oddsBtn)
    }


    override fun getItemCount(): Int = oddsList.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bindModel(oddsList[position])


    inner class ViewHolder(private val btnOdds: OddsButtonDetail) : OddStateViewHolderDetail(btnOdds) {

        private fun checkKey(key: String): Boolean {
            return TextUtil.compareWithGameKey(oddsDetail.gameType, key)
        }

        fun bindModel(odd: Odd?) = btnOdds.run {
            setupOdd(odd,
                oddsType,
                isOddPercentage = isOddPercentage,
                matchInfo = oddsDetail.matchInfo)
            setupOddState(this, odd)
            clickDelay {
                odd?.let { o -> onOddClickListener.getBetInfoList(o, oddsDetail) }
            }

            when {
                checkKey(PlayCate.SINGLE_OU.value) || checkKey(PlayCate.DC_OU.value) -> {
                    binding.tvName.text = when (bindingAdapterPosition) {
                        0 -> itemView.context.getString(R.string.odd_button_ou_o)
                        else -> itemView.context.getString(R.string.odd_button_ou_u)
                    }
                }

                checkKey(PlayCate.OU_BTS.value) -> {
                    val oddsName = (odd?.nameMap?.get(LanguageManager.getSelectLanguage(context).key) ?: odd?.name)?.substringAfter("&")
                    binding.tvName.text = oddsName
                }

                checkKey(PlayCate.SINGLE_BTS.value) || checkKey(PlayCate.DC_BTS.value) -> {
                    binding.tvName.text = when (bindingAdapterPosition) {
                        0 -> itemView.context.getString(R.string.odds_button_name_y)
                        else -> itemView.context.getString(R.string.odds_button_name_n)
                    }
                }

                checkKey(PlayCate.OU_OE.value) -> {
                    binding.tvName.text = when (odd?.playCode) {
                        PlayCode.OU_OE_O_O.value -> this.context.getString(R.string.odds_button_name_singular)
                        PlayCode.OU_OE_O_E.value -> this.context.getString(R.string.odds_button_name_dual)
                        PlayCode.OU_OE_U_O.value -> this.context.getString(R.string.odds_button_name_singular)
                        PlayCode.OU_OE_U_E.value -> this.context.getString(R.string.odds_button_name_dual)
                        else -> ""
                    }

                    binding.tvSpread.text = ""
                }

                checkKey(PlayCate.OU_TTS1ST.value) -> {
                    binding.tvName.text = when (odd?.playCode) {
                        PlayCode.OU_TTS1ST_O_H.value -> this.context.getString(R.string.odds_button_name_home)
                        PlayCode.OU_TTS1ST_U_H.value -> this.context.getString(R.string.odds_button_name_home)
                        PlayCode.OU_TTS1ST_O_C.value -> this.context.getString(R.string.odds_button_name_away)
                        PlayCode.OU_TTS1ST_U_C.value -> this.context.getString(R.string.odds_button_name_away)
                        else -> ""
                    }

                    binding.tvSpread.text = ""
                }

                checkKey(PlayCate.DC_FLG.value) -> {
                    binding.tvName.text = when (odd?.playCode) {
                        PlayCode.DC_FLG_HD_H.value -> this.context.getString(R.string.odds_button_name_hdh)
                        PlayCode.DC_FLG_HD_C.value -> this.context.getString(R.string.odds_button_name_hdc)
                        PlayCode.DC_FLG_CD_H.value -> this.context.getString(R.string.odds_button_name_cdh)
                        PlayCode.DC_FLG_CD_C.value -> this.context.getString(R.string.odds_button_name_cdc)
                        PlayCode.DC_FLG_HC_H.value -> this.context.getString(R.string.odds_button_name_hch)
                        PlayCode.DC_FLG_HC_C.value -> this.context.getString(R.string.odds_button_name_hcc)
                        else -> ""
                    }
                }

                checkKey(PlayCate.SINGLE_FLG.value) -> {
                    binding.tvName.text = when (odd?.playCode) {
                        PlayCode.SINGLE_FLG_H_H.value -> this.context.getString(R.string.odds_button_name_hdh)
                        PlayCode.SINGLE_FLG_H_C.value -> this.context.getString(R.string.odds_button_name_hdc)
                        PlayCode.SINGLE_FLG_D_H.value -> this.context.getString(R.string.odds_button_name_cdh)
                        PlayCode.SINGLE_FLG_D_C.value -> this.context.getString(R.string.odds_button_name_cdc)
                        PlayCode.SINGLE_FLG_C_H.value -> this.context.getString(R.string.odds_button_name_hch)
                        PlayCode.SINGLE_FLG_C_C.value -> this.context.getString(R.string.odds_button_name_hcc)
                        else -> ""
                    }
                }
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = mOddStateRefreshListener

    }


}