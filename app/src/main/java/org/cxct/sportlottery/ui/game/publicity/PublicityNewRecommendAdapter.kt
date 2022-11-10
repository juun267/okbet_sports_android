package org.cxct.sportlottery.ui.game.publicity

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.databinding.PublicityNewRecommendItemBinding
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.DisplayUtil.dp

class PublicityNewRecommendAdapter(val lifecycleOwner: LifecycleOwner, private val publicityAdapterListener: GamePublicityAdapter.PublicityAdapterListener) :
    RecyclerView.Adapter<PublicityNewRecommendItemHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicityNewRecommendItemHolder {
        return PublicityNewRecommendItemHolder(
            lifecycleOwner,
            PublicityNewRecommendItemBinding.inflate(
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

    fun updateRecommendItem(recommendList: List<Recommend>, oddsType: OddsType) {
        mRecommendList = recommendList
        mOddsType = oddsType
        mRecommendList.forEachIndexed { index, recommend ->
            notifyItemChanged(index, recommend)
        }
    }

    override fun onBindViewHolder(holder: PublicityNewRecommendItemHolder, position: Int) {
        val itemData = mRecommendList[position]
        holder.setupItemMargin(itemData)
        holder.bind(data = itemData, oddsType = mOddsType)
    }

    override fun onBindViewHolder(holder: PublicityNewRecommendItemHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            payloads.forEach { payload ->
                when (payload) {
                    is Recommend -> {
                        holder.setupItemMargin(payload)
                        holder.update(payload, oddsType = mOddsType)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = mRecommendList.size

    /**
     * 根據Item的位置配置不同的Margin
     */
    private fun PublicityNewRecommendItemHolder.setupItemMargin(recommend: Recommend) {
        val cvBlockLayoutParams = (binding.cvBlock.layoutParams as ConstraintLayout.LayoutParams)
        when (recommend) {
            mRecommendList.firstOrNull() -> {
                cvBlockLayoutParams.setMargins(10.dp, 10.dp, 4.dp, 10.dp)
            }
            mRecommendList.lastOrNull() -> {
                cvBlockLayoutParams.setMargins(4.dp, 10.dp, 10.dp, 10.dp)
            }
            else -> {
                cvBlockLayoutParams.setMargins(4.dp, 10.dp, 4.dp, 10.dp)
            }
        }
    }
}