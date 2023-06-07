package org.cxct.sportlottery.ui.sport.oddsbtn

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

// 赔率显示按钮抽象
abstract class AbsOddButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    abstract fun resetStatu()                       // 从(不可用, 锁盘, 上涨, 降低)状态恢复到默认状态
    abstract fun onDeactivated()                    // 赔率不可用
    abstract fun onLock()                           // 锁盘
    abstract fun onRise()                           // 赔率上涨
    abstract fun onDecrease()                       // 赔率降低

    abstract fun onSelected()                       // 选中
    abstract fun onUnselected()                     // 未选中

    abstract fun setOddName(name: String)           // 显示赔率玩法名称
    abstract fun setOddValue(value: String)         // 显示赔率值

}