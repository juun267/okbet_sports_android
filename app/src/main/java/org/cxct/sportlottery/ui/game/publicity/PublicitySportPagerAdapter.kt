package org.cxct.sportlottery.ui.game.publicity

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.PublicitySportPageBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.SportMenu
import org.cxct.sportlottery.util.LocalUtils

//一頁需要顯示的球種數量
const val PublicitySportPageItemSize = 6
class PublicitySportPagerAdapter : RecyclerView.Adapter<PublicitySportPagerAdapter.PublicitySportPageHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun setSportPageData(sportPageDataList: List<List<SportMenu>>) {
        mSportPageDataList = sportPageDataList
        notifyDataSetChanged()
    }

    private var mSportPageDataList: List<List<SportMenu>> = listOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicitySportPageHolder {
        return PublicitySportPageHolder(
            PublicitySportPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PublicitySportPageHolder, position: Int) {
        holder.bind(mSportPageDataList[position])
    }

    override fun getItemCount(): Int = mSportPageDataList.size

    inner class PublicitySportPageHolder(val binding: PublicitySportPageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        /**
         * 當前頁面的6個View
         */
        private val itemBindingView =
            listOf(binding.item1, binding.item2, binding.item3, binding.item4, binding.item5, binding.item6)

        fun bind(data: List<SportMenu>) {
            itemBindingView.forEachIndexed { index, publicitySportPageItemBinding ->
                when (val itemData = data.getOrNull(index)) {
                    null -> {
                        when {
                            //為靠右的View時
                            index % 2 != 0 -> {
                                //判斷左側的View是否顯示, 若顯示需設為INVISIBLE避免左側View延展
                                when(itemBindingView[index - 1].root.isVisible) {
                                    true -> publicitySportPageItemBinding.root.visibility = View.INVISIBLE
                                    false -> publicitySportPageItemBinding.root.visibility = View.GONE
                                }
                            }
                        }
                    }
                    else -> {
                        with(publicitySportPageItemBinding) {
                            root.visibility = View.VISIBLE
                            val title =
                                GameType.getGameTypeString(LocalUtils.getLocalizedContext(), itemData.gameType.key)
                            root.setBackgroundResource(GameType.getGameTypePublicityItemBackground(itemData.gameType))
                            tvType.text = title.ifEmpty { itemData.sportName }
                            tvCount.text = itemData.gameCount.toString()
                        }
                    }
                }
            }
        }
    }
}