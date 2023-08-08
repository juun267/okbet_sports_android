package org.cxct.sportlottery.ui.maintab.worldcup


import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.util.SvgUtil

class FIBAItem(svgIcon: String, code: String, name: String, num: Int, sortNum: Int) : Item(code, name, num, sortNum) {

    private var fibaSVG: String? = svgIcon
    val icon by lazy {
        val sDrawable = SvgUtil.getSvgDrawable(fibaSVG!!)
        fibaSVG = null
        sDrawable
    }

}