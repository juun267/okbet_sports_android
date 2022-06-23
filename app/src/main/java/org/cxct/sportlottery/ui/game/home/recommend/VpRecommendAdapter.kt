package org.cxct.sportlottery.ui.game.home.recommend

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.squareup.moshi.*
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import kotlinx.android.synthetic.main.home_recommend_champion.view.*
import kotlinx.android.synthetic.main.home_recommend_eps.view.*
import kotlinx.android.synthetic.main.home_recommend_vp.view.*
import kotlinx.android.synthetic.main.home_recommend_vp.view.tv_play_type
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.matchCategory.result.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.outright.odds.DynamicMarket
import org.cxct.sportlottery.ui.common.PlayCateMapItem
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.home.OnClickMoreListener
import org.cxct.sportlottery.ui.game.home.OnClickOddListener
import org.cxct.sportlottery.ui.game.widget.OddsButton
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*


class VpRecommendAdapter(
    val sportCode: String?,
    val dataList: List<OddBean>,
    private val isOutright: Int?,
    val matchOdd: MatchOdd,
    val dynamicMarkets: Map<String, DynamicMarket>?,
    var playCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    var matchInfo: MatchInfo?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        RECOMMEND_OUTRIGHT, RECOMMEND, RECOMMEND_EPS
    }

    var oddsType: OddsType = OddsType.EU
    var onClickOddListener: OnClickOddListener? = null
    var onClickOutrightOddListener: OnClickOddListener? = null
    var onClickMoreListener: OnClickMoreListener? = null

    private val mOddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) { }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            isOutright != 0 -> ItemType.RECOMMEND_OUTRIGHT.ordinal //冠軍推薦賽事
//            dataList[position].playTypeCode == PlayCate.EPS.value -> ItemType.RECOMMEND_EPS.ordinal //特優賠率  需求先隱藏特優賠率
            else -> ItemType.RECOMMEND.ordinal //一般推薦賽事
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.RECOMMEND.ordinal -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_recommend_vp, parent, false)
                ViewHolderHdpOu(view)
            }
            ItemType.RECOMMEND_EPS.ordinal -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_recommend_eps, parent, false)
                ViewHolderEPS(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_recommend_champion, parent, false)
                ViewHolderRecOutright(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        try {
            when (holder) {
                is ViewHolderHdpOu -> {
                    val data = dataList.filterPlayCateSpanned(sportCode)[position]
                    holder.bind(data, playCateNameMap, matchInfo)
                }
                is ViewHolderEPS -> {
                    val data = dataList.filterPlayCateSpanned(sportCode)[position]
                    holder.bind(data, playCateNameMap)
                }
                is ViewHolderRecOutright -> {
                    val data = dataList.first()
                    holder.bind(data, dynamicMarkets)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = dataList.size

    private fun List<OddBean>.filterPlayCateSpanned(gameType: String?): List<OddBean> {
        val spannedList = mutableListOf<OddBean>()
        this.forEach { oddBean ->
            val playCateNum =
                when { //根據IOS給的規則判斷顯示數量
                    oddBean.playTypeCode.contains(PlayCate.HDP.value) || oddBean.playTypeCode.contains(PlayCate.OU.value) || oddBean.playTypeCode.contains(
                        PlayCate.CORNER_OU.value
                    ) -> 2

                    oddBean.playTypeCode.contains(PlayCate.SINGLE.value) || oddBean.playTypeCode.contains(PlayCate.NGOAL.value) -> 3

                    else -> 3
                }
            spannedList.add(OddBean(oddBean.playTypeCode, oddBean.oddList.filterIndexed { index, _ ->
                index < playCateNum
            }.toMutableList()))
        }
        return spannedList
    }

    fun notifySelectedOddsChanged(selectedOdds: MutableList<String>) {
        dataList.forEach {
            it.oddList.forEach{ odd ->
                odd?.id?.let { id ->
                    odd.isSelected = selectedOdds.contains(id)
                }
            }
        }
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    inner class ViewHolderHdpOu(
        itemView: View,
        override val oddStateChangeListener: OddStateChangeListener = mOddStateRefreshListener
    ) : OddStateViewHolder(itemView) {

        fun bind(
            data: OddBean,
            playCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
            matchInfo: MatchInfo?
        ) {
            when (data.playTypeCode) {
                PlayCate.EPS.value -> {
                    itemView.apply {

                        tv_play_type_eps.text = playCateNameMap?.get(data.playTypeCode)?.get(LanguageManager.getSelectLanguage(context).key)

                        setupOddsButton(btn_odd_eps, data.playTypeCode, data.oddList[0])
                    }
                }
                else -> {
                    itemView.apply {

                        tv_play_type.text = playCateNameMap?.get(data.playTypeCode)?.get(LanguageManager.getSelectLanguage(context).key)?.replace("||", "\n")?.replace("{H}","${matchInfo?.homeName}")?.replace("{C}","${matchInfo?.awayName}")

                        if (data.oddList.isNotEmpty()) {
                            odd_btn_home.visibility = View.VISIBLE
                            setupOddsButton(odd_btn_home, data.playTypeCode, data.oddList[0])
                        } else {
                            odd_btn_home.visibility = View.GONE
                        }

                        if (data.oddList.size > 1) {
                            odd_btn_away.visibility = View.VISIBLE
                            setupOddsButton(odd_btn_away, data.playTypeCode, data.oddList[1])
                        } else {
                            odd_btn_away.visibility = View.GONE
                        }

                        if (data.oddList.size > 2) {
                            odd_btn_draw.visibility = View.VISIBLE
                            setupOddsButton(odd_btn_draw, data.playTypeCode, data.oddList[2])
                        } else {
                            odd_btn_draw.visibility = View.GONE
                        }
                    }
                }
            }

        }

        private fun setupOddsButton(oddsButton: OddsButton, playCateCode: String, odd: Odd?) {

            oddsButton.apply homeButtonSettings@{
                setupOdd(odd, oddsType)
                this@ViewHolderHdpOu.setupOddState(oddsButton, odd)
                odd?.let {
                    this.isSelected = it.isSelected ?: false

                    setOnClickListener {
                        val playCateName = itemView.tv_play_type.text.toString()

                        onClickOddListener?.onClickBet(matchOdd.apply {
                            this.matchInfo?.gameType = sportCode
                        }, odd, playCateCode, playCateName ,betPlayCateNameMap)
                    }
                }
            }
        }

    }

    inner class ViewHolderRecOutright(
        itemView: View,
        override val oddStateChangeListener: OddStateChangeListener = mOddStateRefreshListener
    ) : OddStateViewHolder(itemView) {

        fun bind(data: OddBean, dynamicMarkets: Map<String, DynamicMarket>?) {

            itemView.apply {
                tv_play_type_champion.text =
                    dynamicMarkets?.get(data.playTypeCode)?.getTranslate(itemView.context)

                val oddList = data.oddList
                setupOddsButton(rec_champ_btn_pre1, data.playTypeCode, oddList, 0)
                setupOddsButton(rec_champ_btn_pre2, data.playTypeCode, oddList, 1)
                setupOddsButton(rec_champ_btn_pre3, data.playTypeCode, oddList, 2)
                setupOddsButton(rec_champ_btn_pre4, data.playTypeCode, oddList, 3)
                setupOddsButton(rec_champ_btn_pre5, data.playTypeCode, oddList, 4)

                rec_champ_more.apply {
                    isVisible = data.oddList.size > 5
                    setOnClickListener {
                        onClickMoreListener?.onClickMore(data.playTypeCode, matchOdd)
                    }
                }
            }
        }

        private fun setupOddsButton(oddsButton: OddsButton, playCateCode:String, oddList: List<Odd?>, index: Int) {
            oddsButton.apply {
                val odds = oddList.getOrNull(index)
                if (oddList.size <= index || odds == null) {
                    visibility = View.GONE
                    return
                }
                odds.let { odd ->
                    setupOdd(odd, oddsType)

                    tv_name.apply {
                        text = odd.getSpreadName(context)
                        visibility = View.VISIBLE
                    }

                    tv_spread.text = ""
                    this@ViewHolderRecOutright.setupOddState(this, odd)

                    odd.id?.let { id ->
                        this.isSelected = odd.isSelected ?: false
                    }

                    setOnClickListener {
                        it.isSelected = !it.isSelected
                        onClickOutrightOddListener?.onClickBet(matchOdd.apply {
                            this.matchInfo?.gameType = sportCode
                        }, odd, PlayCate.UNCHECK.value, playCateCode, betPlayCateNameMap)
                    }
                }
            }
        }

        private fun DynamicMarket.getTranslate(context: Context): String? {
            return when (LanguageManager.getSelectLanguage(context)) {
                LanguageManager.Language.ZH -> {
                    this.zh
                }
                LanguageManager.Language.VI -> {
                    this.vi
                }
                else -> {
                    this.en
                }
            }
        }

        private fun Odd.getSpreadName(context: Context): String? {
            return when (LanguageManager.getSelectLanguage(context)) {
                LanguageManager.Language.ZH -> {
                    this.nameMap?.get("zh")
                }
                LanguageManager.Language.VI -> {
                    this.nameMap?.get("vi")
                }
                else -> this.nameMap?.get("en")
            }
        }
    }

    inner class ViewHolderEPS(
        itemView: View,
        override val oddStateChangeListener: OddStateChangeListener = mOddStateRefreshListener
    ) : OddStateViewHolder(itemView) {

        fun bind(data: OddBean, playCateNameMap: MutableMap<String?, Map<String?, String?>?>?) {
            itemView.apply {
                tv_play_type_eps.text = playCateNameMap?.get(data.playTypeCode)?.get(LanguageManager.getSelectLanguage(context).key)

                val odd = data.oddList.getOrNull(0)
                tv_title_eps.text = odd?.name
                setupOddForEPS(btn_odd_eps, odd, oddsType)
            }
        }

        private fun setupOddForEPS(oddsButton: OddsButton, odd: Odd?, oddsType: OddsType) {
            oddsButton.apply {
                setupOddForEPS(odd, oddsType)
                setupOddState(this, odd)

                odd?.let {
                    odd.id?.let { id ->
                        this.isSelected = it.isSelected ?: false
                    }

                    setOnClickListener {
                        val playCateName = itemView.tv_play_type_eps.text.toString()

                        it.isSelected = !it.isSelected
                        onClickOddListener?.onClickBet(matchOdd.apply {
                            this.matchInfo?.gameType = sportCode
                        }, odd, PlayCate.UNCHECK.value, playCateName, betPlayCateNameMap)
                    }
                }
            }
        }
    }

    private fun PlayCateMapItem.getPlayCateName(l: LanguageManager.Language): String {
        return when (l) {
            LanguageManager.Language.EN -> {
                this.playCateNameEn
            }
            LanguageManager.Language.VI -> {
                this.playCateNameVi
            }
            else -> {
                this.playCateName
            }
        }
    }
}