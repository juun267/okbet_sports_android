package org.cxct.sportlottery.view.dialog

import android.app.Dialog
import android.content.Context
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.list.EndScoreInfo
import org.cxct.sportlottery.ui.betRecord.BetRecordEndScoreAdapter
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.view.onClick

class BetEndScoreDialog(mContext: Context) : Dialog(mContext)  {
    private var recyclerBetEndScore:RecyclerView?=null
    private val scoreAdapter = BetRecordEndScoreAdapter()
    init {
        initDialog()
    }

    private fun initDialog() {
        setContentView(R.layout.dialog_bet_end_score)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(true)

        recyclerBetEndScore=findViewById(R.id.recyclerBetEndScore)
        recyclerBetEndScore?.let {
            it.layoutManager= GridLayoutManager(context,7)
            it.addItemDecoration(GridSpacingItemDecoration(7, 2.dp, false))
            it.adapter=scoreAdapter
        }

        findViewById<ImageView>(R.id.ivClose).onClick {
            dismiss()
        }
    }

    fun showEndScoreDialog(data:List<EndScoreInfo>){
        scoreAdapter.setList(data)
        show()
    }
}