package org.cxct.sportlottery.ui.game.hall.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.tools.ScreenUtils
import kotlinx.android.synthetic.main.itemview_play_category_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.SelectionType
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.network.sport.query.PlayCate

class PlayCategoryAdapter : RecyclerView.Adapter<PlayCategoryAdapter.ViewHolderPlayCategory>() {
    var data = listOf<Play>()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            var notifyAll = false
            if (field.size != value.size) {
                notifyAll = true
            }
            field = value
            if (notifyAll) {
                notifyDataSetChanged()
            } else {
                field.forEachIndexed { index, _ ->
                    notifyItemChanged(index)
                }
            }
        }

    var playCategoryListener: PlayCategoryListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderPlayCategory {
        return ViewHolderPlayCategory(LayoutInflater.from(parent.context).inflate(R.layout.itemview_play_category_v4, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolderPlayCategory, position: Int) {
        holder.bind(data[position], playCategoryListener)
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolderPlayCategory constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(item: Play, playCategoryListener: PlayCategoryListener?) {
            var initSpinnerAdapter = true
            var initialSetItem: Boolean
            val playSpinnerAdapter by lazy { PlaySpinnerAdapter(item.playCateList?.toMutableList() ?: mutableListOf()) }
            with(itemView.sp_play) {
                if (item.selectionType == SelectionType.SELECTABLE.code) {
                    adapter = playSpinnerAdapter

                    //region 調整Spinner選單位置
                    dropDownVerticalOffset = ScreenUtils.dip2px(context, 48F)
                    dropDownHorizontalOffset = ScreenUtils.dip2px(context, -20F)
                    //endregion

                    //region 配置預設位置
                    //初次選擇、選擇後更新被選中的項目
                    item.playCateList?.indexOfFirst { it.isSelected }
                        ?.let { selectedIndex ->
                            //indexOfFirst無符合條件回傳-1
                            if (selectedIndex == -1)
                                setSelection(count)
                            else
                                setSelection(selectedIndex)

                            if (item.isSelected) {
                                itemView.play_name.text = item.playCateList[selectedIndex].name
                            } else {
                                itemView.play_name.text = item.name
                            }
                        }
                    initialSetItem = false
                    //endregion

                    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                            item.playCateList?.let { playList ->
                                //不是在設置初始值或Spinner初始化才回傳選擇項目
                                if (!initSpinnerAdapter && !initialSetItem) {
                                    playList.getOrNull(position)?.let { playCate ->
                                        playCategoryListener?.onSelectPlayCateListener(item, playCate)
                                        itemView.play_name.text = playCate.name
                                    }
                                }
                                initSpinnerAdapter = false
                            }
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {
                            //do nothing
                        }

                    }
                } else {
                    itemView.play_name.text = item.name
                }
            }

            itemView.play_arrow.visibility =
                if (item.selectionType == SelectionType.SELECTABLE.code) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            itemView.isSelected = item.isSelected

            itemView.setOnClickListener {
                if (item.selectionType == SelectionType.SELECTABLE.code) {
                    when {
                        //這個是沒有點選過的狀況 第一次進來 ：開啟選單
                        !item.isSelected && item.isLocked == null -> {
                            showPlayCateSpinner()
                        }
                        //當前被點選的狀態
                        item.isSelected -> {
                            showPlayCateSpinner()
                        }
                        //之前點選過然後離開又回來 要預設帶入
                        !item.isSelected && item.isLocked == false -> {
                            playCategoryListener?.onClickSetItemListener(item)
                        }
                    }
                } else {
                    playCategoryListener?.onClickNotSelectableListener(item)
                }
            }
        }

        private fun showPlayCateSpinner() {
            itemView.sp_play.performClick()
        }
    }
}

class PlayCategoryListener(
    private val onClickSetItemListener: (item: Play) -> Unit,
    private val onClickNotSelectableListener: (item: Play) -> Unit,
    private val onSelectPlayCateListener: (item: Play, selectItem: PlayCate) -> Unit
) {
    fun onClickSetItemListener(item: Play) = onClickSetItemListener.invoke(item)
    fun onClickNotSelectableListener(item: Play) = onClickNotSelectableListener.invoke(item)
    fun onSelectPlayCateListener(item: Play, selectItem: PlayCate) = onSelectPlayCateListener.invoke(item, selectItem)
}