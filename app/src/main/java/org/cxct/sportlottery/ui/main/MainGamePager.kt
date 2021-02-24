package org.cxct.sportlottery.ui.main

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.main_game_pager.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues
import org.cxct.sportlottery.ui.common.AlignLeftSnapHelper
import org.cxct.sportlottery.ui.main.entity.GameItemData

class MainGamePager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    private var mDataList = mutableListOf<GameItemData>()
    private var mOnSelectThirdGameListener: OnSelectItemListener<ThirdDictValues?>? = null

    private lateinit var mMainGameRvAdapter: MainGameRvAdapter
    private val mAlignLeftSnapHelper = AlignLeftSnapHelper()

    //记录目标项位置
    private var mToPosition = Int.MAX_VALUE / 2

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.main_game_pager, this, false)
        addView(view)

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.CustomView, 0, 0)
        try {
            val spanCount = typedArray.getInt(R.styleable.CustomView_cvSpanCount, 1)
            mMainGameRvAdapter = MainGameRvAdapter(spanCount)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        mAlignLeftSnapHelper.attachToRecyclerView(alignLeftRv)
        alignLeftRv.adapter = mMainGameRvAdapter

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        alignLeftRv.layoutManager = layoutManager
        alignLeftRv.layoutManager?.scrollToPosition(Integer.MAX_VALUE / 2)
    }

    private fun refreshView() {
        mMainGameRvAdapter.setOnSelectThirdGameListener(mOnSelectThirdGameListener)
        mMainGameRvAdapter.setData(mDataList)
    }

    //設定選擇第三方的listener
    fun setOnSelectThirdGameListener(onSelectItemListener: OnSelectItemListener<ThirdDictValues?>?) {
        mOnSelectThirdGameListener = onSelectItemListener
    }

    fun setData(newDataList: MutableList<GameItemData>?) {
        mDataList = newDataList ?: mutableListOf()
        refreshView()
    }


    /**
     * 滑动到指定位置
     * https://blog.csdn.net/shanshan_1117/article/details/78780137
     */
    private fun smoothMoveToPosition(recyclerView: RecyclerView, position: Int) {
        try {
            // 第一个可见位置
            val firstItem: Int = recyclerView.getChildLayoutPosition(recyclerView.getChildAt(0))
            // 最后一个可见位置
            val lastItem: Int = recyclerView.getChildLayoutPosition(recyclerView.getChildAt(recyclerView.childCount - 1))

            if (position < firstItem) {
                // 第一种可能:跳转位置在第一个可见位置之前
                recyclerView.smoothScrollToPosition(position)
            } else if (position <= lastItem) {
                // 第二种可能:跳转位置在第一个可见位置之后
                val movePosition = position - firstItem
                if (movePosition >= 0 && movePosition < recyclerView.childCount) {
                    val left: Int = recyclerView.getChildAt(movePosition).left
                    recyclerView.smoothScrollBy(left, 0)
                }
            } else {
                // 第三种可能:跳转位置在最后可见项之后
                recyclerView.smoothScrollToPosition(position)
                mToPosition = lastItem
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}