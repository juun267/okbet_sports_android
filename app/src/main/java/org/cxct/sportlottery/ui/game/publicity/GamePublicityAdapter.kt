package org.cxct.sportlottery.ui.game.publicity

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.HomeBottomNavigationBinding
import org.cxct.sportlottery.databinding.ItemPublicityRecommendBinding
import org.cxct.sportlottery.databinding.PublicitySubTitleViewBinding
import org.cxct.sportlottery.databinding.PublicityTitleViewBinding
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.menu.OddsType

class GamePublicityAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //排序對應表
    private val SORTMAP = mapOf<Any, Int>(
        //標題圖片
        PublicityTitleImageData::class to 1,
        //熱門推薦..更多
        PublicitySubTitleImageData::class to 2,
        //足球, 滾球, 數量, 聯賽名, 國旗, 賽事內容
        Recommend::class to 3,
        BottomNavigationItem::class to 4
    )

    var oddsType: OddsType = OddsType.EU
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    enum class ItemType {
        PUBLICITY_TITLE,
        PUBLICITY_SUB_TITLE,
        RECOMMEND,
        BOTTOM_NAVIGATION,
        NONE
    }

    // region ItemClass
    class PublicityTitleImageData
    class PublicitySubTitleImageData
    class BottomNavigationItem
    // endregion

    // 接收任何型別
    private var mDataList = mutableListOf<Any>()

    //region addData Function
    fun addTitle() {
        removeDatas(PublicityTitleImageData())
        addDataWithSort(PublicityTitleImageData())
    }

    fun addSubTitle() {
        removeDatas(PublicitySubTitleImageData())
        addDataWithSort(PublicitySubTitleImageData())
    }

    fun addRecommend(recommendList: List<Recommend>) {
        removeDatas(recommendList.firstOrNull())
        recommendList.forEach { addDataWithSort(it) }
    }

    fun addBottomView() {
        removeDatas(BottomNavigationItem())
        addDataWithSort(BottomNavigationItem())
    }
    //endregion

    //region update Function
    fun updateRecommendData(position: Int, payload: Recommend) {
        val recommendIndexList = mutableListOf<Int>()
        mDataList.forEachIndexed { index, item -> if (item is Recommend) recommendIndexList.add(index) }
        notifyItemChanged(recommendIndexList[position], payload)
    }
    //endregion

    override fun getItemViewType(position: Int): Int {
        return when (mDataList[position]) {
            is PublicityTitleImageData -> {
                ItemType.PUBLICITY_TITLE.ordinal
            }
            is PublicitySubTitleImageData -> {
                ItemType.PUBLICITY_SUB_TITLE.ordinal
            }
            is Recommend -> {
                ItemType.RECOMMEND.ordinal
            }
            is BottomNavigationItem -> {
                ItemType.BOTTOM_NAVIGATION.ordinal
            }
            else -> {
                ItemType.NONE.ordinal
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.PUBLICITY_TITLE.ordinal -> {
                PublicityTitleViewHolder(
                    PublicityTitleViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            ItemType.PUBLICITY_SUB_TITLE.ordinal -> {
                PublicitySubTitleViewHolder(
                    PublicitySubTitleViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            ItemType.RECOMMEND.ordinal -> {
                PublicityRecommendViewHolder(
                    ItemPublicityRecommendBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            ItemType.BOTTOM_NAVIGATION.ordinal -> {
                BottomNavigationViewHolder(
                    HomeBottomNavigationBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> UndefinedViewHolder(View(parent.context))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNullOrEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            payloads.forEachIndexed {index, payload ->
                when (payload) {
                    is Recommend -> {
                        (holder as PublicityRecommendViewHolder).updateLeagueOddList(index, payload, oddsType)
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = mDataList[position]
        when (holder) {
            is PublicityRecommendViewHolder -> {
                if (data is Recommend) {
                    holder.bind(data, oddsType)
                }
            }
            is BottomNavigationViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemCount(): Int = mDataList.size

    // region ItemViewHolder
    inner class PublicityTitleViewHolder(binding: PublicityTitleViewBinding) : RecyclerView.ViewHolder(binding.root)
    inner class PublicitySubTitleViewHolder(binding: PublicitySubTitleViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class BottomNavigationViewHolder(val binding: HomeBottomNavigationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context

        fun bind() {
            with(binding) {
                ContextCompat.getDrawable(context, R.color.colorWhite1)?.let { background ->
                    bottomNavigationView.setTopBackground(background)
                }
                ContextCompat.getDrawable(context, R.color.colorGrayDark3)?.let { background ->
                    bottomNavigationView.setBottomBackground(background)
                }
            }
        }
    }

    class UndefinedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    // endregion

    //region Data Getter
    fun getRecommendData(): MutableList<Recommend> {
        val result = mutableListOf<Recommend>()
        mDataList.filterIsInstance<Recommend>().forEach { result.add(it) }
        return result
    }
    //endregion

    // region private functions
    // 依照傳入參數刪除同一個類別的資料
    private fun removeDatas(src: Any?) {
        src?.let {
            val iterator = mDataList.iterator()
            while (iterator.hasNext()) {
                if (iterator.next()::class.isInstance(src))
                    iterator.remove()
            }
        }
    }

    // 依照SORTMAP的順序插入資料
    private fun addDataWithSort(src: Any) {
        Log.d("Dean", "更新：${src::class.java.simpleName}")
        // 如果列表裡面沒東西，直接插
        if (mDataList.isEmpty()) {
            mDataList.add(src)
            notifyItemChanged(0)
            return
        }
        mDataList.forEachIndexed { index, target ->
            if (isPrev(src, target)) {
                mDataList.add(index, src)
                notifyItemChanged(index)
                return
            }
            if (index == mDataList.size) return
        }
        mDataList.add(src)
        notifyItemChanged(mDataList.size - 1)
        //print("${src}")
    }

    private fun isPrev(src: Any, target: Any): Boolean {
        if (getSortPoint(src) < getSortPoint(target)) return true
        return false
    }

    private fun getSortPoint(item: Any): Int = SORTMAP[item::class] ?: 0
    // endregion
}