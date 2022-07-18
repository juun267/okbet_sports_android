package org.cxct.sportlottery.ui.game.publicity

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.databinding.PublicityRecommendItemBinding
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.menu.OddsType

class PublicityNewRecommendAdapter(private val publicityAdapterListener: GamePublicityAdapter.PublicityAdapterListener) :
    RecyclerView.Adapter<PublicityNewRecommendItemHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicityNewRecommendItemHolder {
        return PublicityNewRecommendItemHolder(
            PublicityRecommendItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), publicityAdapterListener
        )
    }

    private var mOddsType: OddsType = MultiLanguagesApplication.mInstance.mOddsType.value ?: OddsType.IDN

    private var mRecommendList: List<Recommend> = listOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setupRecommendItem(recommendList: List<Recommend>, oddsType: OddsType) {
        mRecommendList = recommendList
        mOddsType = oddsType
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: PublicityNewRecommendItemHolder, position: Int) {
        holder.bind(data = mRecommendList[position], oddsType = mOddsType)
    }

    override fun getItemCount(): Int = mRecommendList.size
}