package org.cxct.sportlottery.ui.game.home.recommend

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.moshi.*
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import kotlinx.android.synthetic.main.home_recommend_champion.view.*
import kotlinx.android.synthetic.main.home_recommend_eps.view.*
import kotlinx.android.synthetic.main.home_recommend_vp.view.*
import kotlinx.android.synthetic.main.home_recommend_vp.view.tv_play_type
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayCate
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
    @Deprecated("playCateMappingList之後需移除改用playCateNameMap的翻譯") val playCateMappingList: List<PlayCateMapItem>?,
    val dynamicMarkets: Map<String, DynamicMarket>?,
    val playCateNameMap: Map<String?, Map<String?, String?>?>?
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
            override fun refreshOddButton(odd: Odd) {
                dataList.forEachIndexed { index, oddBean ->
                    if (oddBean.oddList.find { it?.id == odd.id } != null)
                        notifyItemChanged(index)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            isOutright != 0 -> ItemType.RECOMMEND_OUTRIGHT.ordinal //冠軍推薦賽事
            dataList[position].playTypeCode == PlayCate.EPS.value -> ItemType.RECOMMEND_EPS.ordinal //特優賠率
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
                    holder.bind(data, playCateNameMap)
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
            val playCateMapItem = playCateMappingList?.find {
                it.gameType == gameType && it.playCateCode == oddBean.playTypeCode
            }

            spannedList.add(OddBean(oddBean.playTypeCode, oddBean.oddList.filterIndexed { index, _ ->
                index < playCateMapItem?.playCateNum ?: 0
            }))
        }
        return spannedList
    }

    inner class ViewHolderHdpOu(
        itemView: View,
        override val oddStateChangeListener: OddStateChangeListener = mOddStateRefreshListener
    ) : OddStateViewHolder(itemView) {

        fun bind(data: OddBean, playCateNameMap: Map<String?, Map<String?, String?>?>?) {
            when (data.playTypeCode) {
                PlayCate.EPS.value -> {
                    itemView.apply {

                        tv_play_type_eps.text = playCateNameMap?.get(data.playTypeCode)?.get(LanguageManager.getSelectLanguage(context).key)

                        setupOddsButton(btn_odd_eps, data.playTypeCode, data.oddList[0])
                    }
                }
                else -> {
                    itemView.apply {

                        tv_play_type.text = playCateNameMap?.get(data.playTypeCode)?.get(LanguageManager.getSelectLanguage(context).key)

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
                setOnClickListener {
                    val playCateName = itemView.tv_play_type.text.toString()

                    odd?.let {
                        onClickOddListener?.onClickBet(matchOdd.apply {
                            this.matchInfo?.gameType = sportCode
                        }, odd, playCateCode, playCateName)
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

                rec_champ_btn_pre1.apply {
                    if (data.oddList.isEmpty() || data.oddList.getOrNull(0) == null) {
                        visibility = View.GONE
                        return
                    }

                    setupOdd(data.oddList.getOrNull(0), oddsType)

                    tv_name.apply {
                        text = data.oddList.getOrNull(0)?.getSpreadName(context)
                        visibility = View.VISIBLE
                    }

                    tv_spread.text = ""

                    this@ViewHolderRecOutright.setupOddState(this, data.oddList.getOrNull(0))

                    isSelected = data.oddList.getOrNull(0)?.isSelected ?: false

                    setOnClickListener {
                        data.oddList.getOrNull(0)?.let { odd ->
                            onClickOutrightOddListener?.onClickBet(matchOdd.apply {
                                this.matchInfo?.gameType = sportCode
                            }, odd,PlayCate.UNCHECK.value ,data.playTypeCode)
                        }
                    }
                }

                rec_champ_btn_pre2.apply {
                    if (data.oddList.size < 2|| data.oddList.getOrNull(1) == null) {
                        visibility = View.GONE
                        return
                    }

                    setupOdd(data.oddList.getOrNull(1), oddsType)

                    tv_name.apply {
                        text = data.oddList.getOrNull(1)?.getSpreadName(context)
                        visibility = View.VISIBLE
                    }

                    tv_spread.text = ""

                    this@ViewHolderRecOutright.setupOddState(this, data.oddList.getOrNull(1))

                    isSelected = data.oddList.getOrNull(1)?.isSelected ?: false

                    setOnClickListener {
                        data.oddList.getOrNull(1)?.let { odd ->
                            onClickOutrightOddListener?.onClickBet(matchOdd.apply {
                                this.matchInfo?.gameType = sportCode
                            }, odd, PlayCate.UNCHECK.value ,data.playTypeCode)
                        }
                    }
                }

                rec_champ_btn_pre3.apply {
                    if (data.oddList.size < 3 || data.oddList.getOrNull(2) == null) {
                        visibility = View.GONE
                        return
                    }

                    setupOdd(data.oddList.getOrNull(2), oddsType)

                    tv_name.apply {
                        text = data.oddList.getOrNull(2)?.getSpreadName(context)
                        visibility = View.VISIBLE
                    }

                    tv_spread.text = ""

                    this@ViewHolderRecOutright.setupOddState(this, data.oddList.getOrNull(2))

                    isSelected = data.oddList.getOrNull(2)?.isSelected ?: false

                    setOnClickListener {
                        data.oddList.getOrNull(2)?.let { odd ->
                            onClickOutrightOddListener?.onClickBet(matchOdd.apply {
                                this.matchInfo?.gameType = sportCode
                            }, odd,PlayCate.UNCHECK.value ,data.playTypeCode)
                        }
                    }
                }

                rec_champ_btn_pre4.apply {
                    if (data.oddList.size < 4|| data.oddList.getOrNull(3) == null) {
                        visibility = View.GONE
                        return
                    }

                    setupOdd(data.oddList.getOrNull(3), oddsType)

                    tv_name.apply {
                        text = data.oddList.getOrNull(3)?.getSpreadName(context)
                        visibility = View.VISIBLE
                    }

                    tv_spread.text = ""

                    this@ViewHolderRecOutright.setupOddState(this, data.oddList.getOrNull(3))

                    isSelected = data.oddList.getOrNull(3)?.isSelected ?: false

                    setOnClickListener {
                        data.oddList.getOrNull(3)?.let { odd ->
                            onClickOutrightOddListener?.onClickBet(matchOdd.apply {
                                this.matchInfo?.gameType = sportCode
                            }, odd,PlayCate.UNCHECK.value ,data.playTypeCode)
                        }
                    }
                }

                rec_champ_btn_pre5.apply {
                    if (data.oddList.size < 5|| data.oddList.getOrNull(4) == null) {
                        visibility = View.GONE
                        return
                    }

                    setupOdd(data.oddList.getOrNull(4), oddsType)

                    tv_name.apply {
                        text = data.oddList.getOrNull(4)?.getSpreadName(context)
                        visibility = View.VISIBLE
                    }

                    tv_spread.text = ""

                    this@ViewHolderRecOutright.setupOddState(this, data.oddList.getOrNull(4))

                    isSelected = data.oddList.getOrNull(4)?.isSelected ?: false

                    setOnClickListener {
                        data.oddList.getOrNull(4)?.let { odd ->
                            onClickOutrightOddListener?.onClickBet(matchOdd.apply {
                                this.matchInfo?.gameType = sportCode
                            }, odd,PlayCate.UNCHECK.value ,data.playTypeCode)
                        }
                    }
                }

                rec_champ_more.apply {
                    setOnClickListener {
                        onClickMoreListener?.onClickMore(data.playTypeCode, matchOdd)
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

        fun bind(data: OddBean, playCateNameMap: Map<String?, Map<String?, String?>?>?) {
            itemView.apply {

                tv_play_type_eps.text = playCateNameMap?.get(data.playTypeCode)?.get(LanguageManager.getSelectLanguage(context).key)

                tv_title_eps.text = data.oddList.getOrNull(0)?.name

                setupOddForEPS(btn_odd_eps, data.oddList[0], oddsType)
            }
        }

        private fun setupOddForEPS(oddsButton: OddsButton, odd: Odd?, oddsType: OddsType) {
            oddsButton.apply {
                setupOddForEPS(odd, oddsType)
                setupOddState(oddsButton, odd)
                setOnClickListener {
                    val playCateName = itemView.tv_play_type_eps.text.toString()

                    odd?.let {
                        onClickOddListener?.onClickBet(matchOdd.apply {
                            this.matchInfo?.gameType = sportCode
                        }, odd, PlayCate.UNCHECK.value ,playCateName)
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