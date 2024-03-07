package org.cxct.sportlottery.ui.sport.endcard.record

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.style.TextAppearanceSpan
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentEndcardRecordDetailBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.sport.endcard.EndCardActivity
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.util.Spanny

class EndCardRecordDetailFragment: BaseFragment<EndCardVM, FragmentEndcardRecordDetailBinding>() {

    private val orderId by lazy { arguments?.getString("orderId") }
    private val rowAdapter by lazy { EndCardRecordRowAdapter() }
    private val oddAdapter by lazy { EndCardRecordOddAdapter() }
    override fun onInitView(view: View) {
        binding.linBack.setOnClickListener {
            (activity as EndCardActivity).removeFragment(this)
        }
        initOddList()
        initResultList()

        binding.tvTips.text = Spanny(binding.tvTips.text).findAndSpan("₱ 8500") {
            TextAppearanceSpan(null,
                Typeface.NORMAL,
                14,
                ColorStateList.valueOf(requireContext().getColor(R.color.color_6AA4FF)),
                null)
        }
    }
    private fun initOddList(){
        binding.rvOdd.apply {
            layoutManager = GridLayoutManager(context,10)
            addItemDecoration(GridSpacingItemDecoration(10,4.dp,false))
            adapter = oddAdapter
            oddAdapter.setList(listOf("","","","","","","","","","","","","","","","","","","","","","",""))
        }
    }
    private fun initResultList(){
        binding.rvResult.apply {
            layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
            addItemDecoration(SpaceItemDecoration(requireContext(), R.dimen.margin_0_5))
            adapter = rowAdapter
            rowAdapter.setList(listOf("","","","","",))
        }
    }
}