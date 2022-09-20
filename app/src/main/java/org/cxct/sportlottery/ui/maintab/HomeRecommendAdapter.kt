package org.cxct.sportlottery.ui.maintab

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.databinding.ItemHomeRecommendBinding
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.menu.OddsType

class HomeRecommendAdapter(private val homeRecommendListener: HomeRecommendListener) :
    RecyclerView.Adapter<ItemHomeRecommendHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHomeRecommendHolder {
        return ItemHomeRecommendHolder(
            ItemHomeRecommendBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), homeRecommendListener
        )
    }

    private var mOddsType: OddsType =
        MultiLanguagesApplication.mInstance.mOddsType.value ?: OddsType.IDN

    private var mRecommendList: List<Recommend> = listOf()

    fun getRecommendListData(): List<Recommend> {
        return mRecommendList
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setupRecommendItem(recommendList: List<Recommend>, oddsType: OddsType) {
        mRecommendList = recommendList
        mOddsType = oddsType
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ItemHomeRecommendHolder, position: Int) {
        val itemData = mRecommendList[position]
        holder.bind(data = itemData, oddsType = mOddsType)
    }

    override fun onBindViewHolder(
        holder: ItemHomeRecommendHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            payloads.forEach { payload ->
                when (payload) {
                    is Recommend -> {
                        holder.update(payload, oddsType = mOddsType)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = mRecommendList.size

    open class HomeRecommendListener(
        private val onItemClickListener: () -> Unit,
        private val onGoHomePageListener: () -> Unit,
        private val onClickBetListener: (gameType: String, matchType: MatchType, matchInfo: MatchInfo?, odd: Odd, playCateCode: String, playCateName: String, betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?, playCateMenuCode: String?) -> Unit,
        private val onClickFavoriteListener: (matchId: String?) -> Unit,
        private val onClickStatisticsListener: (matchId: String) -> Unit,
        private val onClickPlayTypeListener: (gameType: String, matchType: MatchType?, matchId: String?, matchInfoList: List<MatchInfo>) -> Unit,
        private val onClickLiveIconListener: (gameType: String, matchType: MatchType?, matchId: String?, matchInfoList: List<MatchInfo>) -> Unit,
        private val onClickAnimationIconListener: (gameType: String, matchType: MatchType?, matchId: String?, matchInfoList: List<MatchInfo>) -> Unit
    ) {
        fun onItemClickListener() = onItemClickListener.invoke()

        fun onClickBetListener(
            gameType: String,
            matchType: MatchType,
            matchInfo: MatchInfo?,
            odd: Odd,
            playCateCode: String,
            playCateName: String,
            betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
            playCateMenuCode: String?
        ) = onClickBetListener.invoke(
            gameType,
            matchType,
            matchInfo,
            odd,
            playCateCode,
            playCateName,
            betPlayCateNameMap,
            playCateMenuCode
        )

        fun onClickFavoriteListener(matchId: String?) = onClickFavoriteListener.invoke(matchId)
        fun onClickStatisticsListener(matchId: String) = onClickStatisticsListener.invoke(matchId)
        fun onClickPlayTypeListener(
            gameType: String,
            matchType: MatchType?,
            matchId: String?,
            matchInfoList: List<MatchInfo>
        ) =
            onClickPlayTypeListener.invoke(gameType, matchType, matchId, matchInfoList)

        fun onClickLiveIconListener(
            gameType: String,
            matchType: MatchType?,
            matchId: String?,
            matchInfoList: List<MatchInfo>
        ) = onClickLiveIconListener.invoke(gameType, matchType, matchId, matchInfoList)

        fun onClickAnimationIconListener(
            gameType: String,
            matchType: MatchType?,
            matchId: String?,
            matchInfoList: List<MatchInfo>
        ) = onClickAnimationIconListener.invoke(gameType, matchType, matchId, matchInfoList)
    }
}