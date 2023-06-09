package org.cxct.sportlottery.ui.sport.oddsbtn

// 赔率显示按钮抽象
interface AbsOddButton {

     fun resetStatu()                       // 从(不可用, 锁盘, 上涨, 降低)状态恢复到默认状态
     fun onDeactivated()                    // 赔率不可用
     fun onLock()                           // 锁盘
     fun onRise()                           // 赔率上涨
     fun onDecrease()                       // 赔率降低

     fun onSelected()                       // 选中
     fun onUnselected()                     // 未选中

     fun setOddName(name: String)           // 显示赔率玩法名称
     fun setOddValue(value: String)         // 显示赔率值

}