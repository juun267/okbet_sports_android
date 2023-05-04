package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.view_home_bingo.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.view.onClick

class HomeBingoView(context: Context, attrs: AttributeSet
)  :RelativeLayout(context,attrs){
    private val extraAdapter=RecyclerBingoExtraAdapter()
    init {
        initView()
    }

    private fun initView(){
        LayoutInflater.from(context).inflate(R.layout.view_home_bingo,this,true)

        val tempExtras= arrayListOf<String>()
        tempExtras.add("")
        tempExtras.add("")
        tempExtras.add("")
        extraAdapter.setList(tempExtras)
        val manager=LinearLayoutManager(context)
        manager.orientation=LinearLayoutManager.HORIZONTAL
        recyclerExtra.layoutManager=manager
        recyclerExtra.adapter=extraAdapter

        linearMgea.onClick {
            selectM()
        }

        linearRush.onClick {
            selectRush()
        }
    }


    private fun selectM(){
        ivRushIcon.setImageResource(R.drawable.ic_bingo_rush_normal)
        tvRushName.setTextColor(ContextCompat.getColor(context,R.color.color_0199FC))
        linearRush.setBackgroundResource(R.drawable.bg_home_bingo_normal)

        ivMgeaIcon.setImageResource(R.drawable.ic_bingo_mgea_press)
        tvMgeaName.setTextColor(ContextCompat.getColor(context,R.color.color_FFFFFF))
        linearMgea.setBackgroundResource(R.drawable.bg_home_bingo_press)
    }

    private fun selectRush(){
        ivRushIcon.setImageResource(R.drawable.ic_bingo_rush_press)
        tvRushName.setTextColor(ContextCompat.getColor(context,R.color.color_FFFFFF))
        linearRush.setBackgroundResource(R.drawable.bg_home_bingo_press)

        ivMgeaIcon.setImageResource(R.drawable.ic_bingo_mgea_normal)
        tvMgeaName.setTextColor(ContextCompat.getColor(context,R.color.color_0199FC))
        linearMgea.setBackgroundResource(R.drawable.bg_home_bingo_normal)
    }


}