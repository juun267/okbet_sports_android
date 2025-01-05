package org.cxct.sportlottery.ui.profileCenter.taskCenter

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogLimitedGamesBinding
import org.cxct.sportlottery.databinding.ViewBirthdayConfirmBinding
import org.cxct.sportlottery.network.quest.info.LimitedGame
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.GridItemDecoration
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.TimeUtil
import java.util.*
import kotlin.collections.ArrayList

class LimitedGamesDialog: BaseDialog<BaseViewModel,DialogLimitedGamesBinding>() {
    companion object{
        fun newInstance(items: ArrayList<LimitedGame>) = LimitedGamesDialog().apply{
            arguments = Bundle().apply {
                putParcelableArrayList("items",items)
            }
        }
    }
    val items by lazy { arguments?.getParcelableArrayList<LimitedGame>("items") }

    init {
        marginHorizontal = 38.dp
    }

    override fun onInitView() {
//        LogUtil.toJson(items)
        binding.ivClose.setOnClickListener { dismiss() }
        binding.rvGames.apply {
            isVerticalScrollBarEnabled = true
            isScrollbarFadingEnabled = false
            overScrollMode = View.OVER_SCROLL_ALWAYS
            layoutManager = GridLayoutManager(requireContext(),3)
            addItemDecoration(GridItemDecoration(20.dp, 12.dp, Color.TRANSPARENT,false))
            adapter = LimitedGameAdapter().apply {
                setOnItemClickListener{adapter, view, position ->
                    items?.getOrNull(position)?.let {
                        dismiss()
                        (requireActivity() as TaskCenterActivity).jumpToLimitedGame(it)
                    }
                }
                setList(items)
            }
        }
    }
}