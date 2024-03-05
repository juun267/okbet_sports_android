package org.cxct.sportlottery.ui.sport.endcard.record

import androidx.recyclerview.widget.GridLayoutManager
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemEndcardRecordBinding
import org.cxct.sportlottery.util.DisplayUtil.dp

class EndCardRecordAdapter:BindingAdapter<String,ItemEndcardRecordBinding>() {

    init {
        addChildClickViewIds(R.id.ivStatus)
    }

    override fun onBinding(position: Int, binding: ItemEndcardRecordBinding, item: String) {
        if(position%2==0){
            binding.ivStatus.setImageResource( R.drawable.ic_tag_win)
            binding.tvWinner.setTextColor(context.getColor(R.color.color_00FF81))
            binding.tvWinnableAmount.setTextColor(context.getColor(R.color.color_00FF81))
        }else{
            binding.ivStatus.setImageResource( R.drawable.ic_tag_lost)
            binding.tvWinner.setTextColor(context.getColor(R.color.color_8B96AD))
            binding.tvWinnableAmount.setTextColor(context.getColor(R.color.color_FFFFFF))
        }
      binding.rvOdd.apply {
          if (adapter==null){
              layoutManager = GridLayoutManager(context,10)
              addItemDecoration(GridSpacingItemDecoration(10,4.dp,false))
              adapter = EndCardRecordOddAdapter()
          }
          (adapter as EndCardRecordOddAdapter).setList(listOf("","","","","","","","","","","","","","","","","","","","","","",""))
      }
    }
}