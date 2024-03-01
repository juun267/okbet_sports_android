package org.cxct.sportlottery.ui.sport.endcard.record

import androidx.recyclerview.widget.GridLayoutManager
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemEndcardRecordBinding
import org.cxct.sportlottery.util.DisplayUtil.dp

class EndCardRecordAdapter:BindingAdapter<String,ItemEndcardRecordBinding>() {

    override fun onBinding(position: Int, binding: ItemEndcardRecordBinding, item: String) {
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