package org.cxct.sportlottery.ui.game.bottomNavigation

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.IdRes
import kotlinx.android.synthetic.main.sport_bottom_navigation.view.*
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.view.*
import org.cxct.sportlottery.R

class SportBottomNavigation @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) :
    LinearLayout(context, attributeSet, defStyle) {
    interface NavigationItemClickListener {
        fun onItemClick(@IdRes viewId: Int): Boolean //回傳true才需要改變該項目選擇狀態
    }

    private var navigationItemClickListener: NavigationItemClickListener? = null

    private var selectedItem: BottomNavigationItem? = null

    init {
        inflate(context, R.layout.sport_bottom_navigation, this)
        setupClickEvent()
        selectItem(navigation_sport)
    }

    private fun setupClickEvent() {
        setupNavigationItemClick(navigation_sport)
        setupNavigationItemClick(navigation_game)
        setupNavigationItemClick(item_bet_list)
        setupNavigationItemClick(navigation_account_history)
        setupNavigationItemClick(navigation_transaction_status)
    }

    private fun setupNavigationItemClick(item: View) {
        item.setOnClickListener {
            navigationItemClickListener?.let { listener ->
                try {
                    if (listener.onItemClick(item.id)) {
                        //只有BottomNavigationItem才能改變選擇狀態顏色
                        val clickNavigationItem = (item as BottomNavigationItem)
                        selectItem(clickNavigationItem)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }

    fun setNavigationItemClickListener(listener: (viewId: Int) -> Boolean) {
        navigationItemClickListener = object : NavigationItemClickListener {
            override fun onItemClick(viewId: Int): Boolean {
                return listener.invoke(viewId)
            }
        }
    }

    private fun selectItem(item: BottomNavigationItem) {
        if (selectedItem != item) {
            selectedItem?.checked = false
            item.checked = true
            selectedItem = item
        }
    }

    fun setSelected(@IdRes viewId: Int) {
        try {
            val navigationItem = findViewById<BottomNavigationItem>(viewId)
            selectItem(navigationItem)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBetCount(betCount: Int) {
        val betCountStr = betCount.toString()
        if(tv_bet_count.text != betCountStr) {
            laExplo.visibility = VISIBLE
            laExplo.playAnimation()
            tv_bet_count.text = betCountStr
        }
    }

    /**
     * 給不需要選中任何選項的頁面使用
     */
    fun clearSelectedStatus() {
        selectedItem?.checked = false
        selectedItem = null
    }
}