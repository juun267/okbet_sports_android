package org.cxct.sportlottery.ui.main.accountHistory.first

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_account_history_title_bar.view.*
import kotlinx.android.synthetic.main.view_status_selector.view.cl_root
import kotlinx.android.synthetic.main.view_status_spinner.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemAccountHistoryBinding
import org.cxct.sportlottery.network.bet.settledList.Row
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.setTextWithStrokeWidth
import timber.log.Timber

class AccountHistoryAdapter(
    private val clickListener: ItemClickListener,
    private val backClickListener: BackClickListener,
    private val sportSelectListener: SportSelectListener
) : ListAdapter<DataItem, RecyclerView.ViewHolder>(DiffCallback()) {

    enum class ItemType {
        TITLE_BAR, ITEM, FOOTER, NO_DATA
    }

    private var mRowList: List<Row?> = listOf() //資料清單
    private var mIsLastPage: Boolean = false //是否最後一頁資料
    private var mSportTypeList: List<StatusSheetData> = listOf() //球種篩選清單

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addFooterAndSubmitList(list: MutableList<Row?>, isLastPage: Boolean) {
        adapterScope.launch {
            mRowList = list
            mIsLastPage = isLastPage

            updateData()
        }
    }

    /**
     * 配置當前可用球種代號
     */
    fun setSportCodeSpinner(sportCodeList: List<StatusSheetData>) {
        mSportTypeList = sportCodeList

        updateData()
    }

    /**
     * 更新 球種篩選清單、資料清單
     *
     * 根據原邏輯提取為function
     */
    private fun updateData() {
        val reverseList = mRowList.reversed()
        val items = listOf(DataItem.TitleBar(mSportTypeList)) + when {
            reverseList.isEmpty() -> listOf(DataItem.NoData)
            mIsLastPage -> reverseList.map { DataItem.Item(it) } + listOf(DataItem.Footer)
            else -> reverseList.map { DataItem.Item(it) }
        }

        CoroutineScope(Dispatchers.Main).launch {
            submitList(items)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.TITLE_BAR.ordinal -> TitleBarViewHolder.from(parent)
            ItemType.ITEM.ordinal -> ItemViewHolder.from(parent)
            ItemType.FOOTER.ordinal -> FooterViewHolder.from(parent)
            else -> NoDataViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TitleBarViewHolder -> {
                when (val data = getItem(position)) {
                    is DataItem.TitleBar -> {
                        holder.bind(data, backClickListener, sportSelectListener)
                    }
                    else -> {
                        Timber.e("data of TitleBar no match")
                    }
                }

            }

            is ItemViewHolder -> {
                val data = getItem(position) as DataItem.Item
                holder.bind(data.row, clickListener, position)
            }

            is FooterViewHolder -> {
            }

            is NoDataViewHolder -> {
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.TitleBar -> ItemType.TITLE_BAR.ordinal
            is DataItem.Item -> ItemType.ITEM.ordinal
            is DataItem.Footer -> ItemType.FOOTER.ordinal
            else -> ItemType.NO_DATA.ordinal
        }
    }

    class ItemViewHolder private constructor(val binding: ItemAccountHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Row?, clickListener: ItemClickListener, position: Int) {
            binding.row = data
            binding.textUtil = TextUtil

            itemView.setOnClickListener {
                if (data != null) {
                    clickListener.onClick(data)
                }
            }
            if (position % 2 == 0) itemView.setBackgroundResource(R.color.color_242424_f2f2f2)
            else itemView.setBackgroundResource(R.color.color_191919_FCFCFC)
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAccountHistoryBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }

    }

    class TitleBarViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(data: DataItem.TitleBar, backClickListener: BackClickListener, sportSelectListener: SportSelectListener) {
            itemView.apply {
                iv_back.setOnClickListener {
                    backClickListener.onClick()
                }

                tv_title.setTextWithStrokeWidth(context?.getString(R.string.account_history_overview) ?: "", 0.7f)

                sportSelectListener.onSelect("")
                status_spinner.cl_root.layoutParams.height = 40.dp
                status_spinner.tv_name.gravity = Gravity.CENTER_VERTICAL or Gravity.START
                status_spinner.setItemData(data.spinnerList.toMutableList())
                status_spinner.setOnItemSelectedListener {
                    sportSelectListener.onSelect(it.code)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup) =
                TitleBarViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_account_history_title_bar, parent, false))
        }
    }

    class NoDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup) = NoDataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_no_record, parent, false))
        }
    }

    class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup) = NoDataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_footer_no_data, parent, false))
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }
    }
}

class ItemClickListener(val clickListener: (data: Row) -> Unit) {
    fun onClick(data: Row) = clickListener(data)
}

class SportSelectListener(val selectedListener: (sport: String?) -> Unit) {
    fun onSelect(sport: String?) = selectedListener(sport)
}

class BackClickListener(val clickListener: () -> Unit) {
    fun onClick() = clickListener()
}


sealed class DataItem {

    abstract val rowItem: Row?

    data class Item(val row: Row?) : DataItem() {
        override val rowItem = row
    }

    data class TitleBar(val spinnerList: List<StatusSheetData>) : DataItem() {
        override val rowItem: Row? = null
    }

    object Footer : DataItem() {
        override val rowItem: Row? = null
    }

    object NoData : DataItem() {
        override val rowItem: Row? = null
    }
}