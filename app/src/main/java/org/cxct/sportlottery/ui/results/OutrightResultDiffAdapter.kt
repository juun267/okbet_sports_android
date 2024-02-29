package org.cxct.sportlottery.ui.results

import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingMutilAdapter
import org.cxct.sportlottery.databinding.ItemOutrightResultOutrightBinding
import org.cxct.sportlottery.databinding.ItemOutrightResultTitleBinding
import org.cxct.sportlottery.databinding.ItemviewGameNoRecordBinding
import org.cxct.sportlottery.util.TimeUtil

class OutrightResultDiffAdapter(private val outrightItemClickListener: OutrightItemClickListener) : BindingMutilAdapter<OutrightResultData>() {
    override fun initItemType() {
        addItemType(OutrightType.TITLE.ordinal,object :OnMultiItemAdapterListener<OutrightResultData,ItemOutrightResultTitleBinding>(){
            override fun onBinding(
                position: Int,
                binding: ItemOutrightResultTitleBinding,
                item: OutrightResultData,
            ) {
                binding.bind(item)
            }
        })
        addItemType(OutrightType.OUTRIGHT.ordinal,object :OnMultiItemAdapterListener<OutrightResultData,ItemOutrightResultOutrightBinding>(){
            override fun onBinding(
                position: Int,
                binding: ItemOutrightResultOutrightBinding,
                item: OutrightResultData,
            ) {
                setupBottomLine(position, binding.bottomLine)
                binding.bind(item)
            }
        })
        addItemType(OutrightType.NO_DATA.ordinal,object :OnMultiItemAdapterListener<OutrightResultData,ItemviewGameNoRecordBinding>(){
            override fun onBinding(
                position: Int,
                binding: ItemviewGameNoRecordBinding,
                item: OutrightResultData,
            ) {

            }
        })
    }

    override fun onItemType(position: Int): Int {
        return getItem(position).dataType.ordinal
    }
    private fun ItemOutrightResultTitleBinding.bind(item: OutrightResultData){
        val seasonData = item.seasonData
        tvSeason.text = seasonData?.name
        titleArrowRotate(item)
        root.setOnClickListener {
            outrightItemClickListener.seasonClick(item)
            titleArrowRotate(item)
        }
    }
    private fun ItemOutrightResultTitleBinding.titleArrowRotate(outrightResultData: OutrightResultData) {
        if (outrightResultData.seasonExpanded) {
            llTitleBackground.setBackgroundResource(R.drawable.bg_shape_top_8dp_blue_stroke_no_bottom_stroke)
            ivArrow.rotation = 0f
        } else {
            llTitleBackground.setBackgroundResource(R.drawable.bg_shape_8dp_blue_stroke)
            ivArrow.rotation = 180f
        }
    }
    private fun ItemOutrightResultOutrightBinding.bind(item: OutrightResultData){
        val seasonData = item.seasonData
        val outrightData = item.outrightData
        if (seasonData?.end != null) tvDate.text = TimeUtil.timeFormat(seasonData.end, TimeUtil.YMD_HM_FORMAT_2)
        tvContent.text = outrightData?.playCateName
        tvWinner.text = outrightData?.playName

        when (item.isLastOutrightData) {
            true -> {
                tvDate.setBackgroundResource(R.drawable.bg_shape_bottom_8dp_gray_stroke_no_top_stroke)
            }
            false -> {
                tvDate.setBackgroundResource(R.drawable.bg_no_top_bottom_stroke_gray)
            }
        }
    }


    private fun setupBottomLine(position: Int, bottomLine: View) {
        bottomLine.visibility = if (position + 1 < itemCount && getItemViewType(position + 1) != OutrightType.TITLE.ordinal) View.VISIBLE else View.GONE
    }

}
class OutrightItemClickListener(private val seasonClickListener: (clickItem: OutrightResultData) -> Unit) {
    fun seasonClick(clickItem: OutrightResultData) = seasonClickListener.invoke(clickItem)
}
