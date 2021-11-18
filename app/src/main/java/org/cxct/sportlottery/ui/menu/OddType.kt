package org.cxct.sportlottery.ui.menu

import org.cxct.sportlottery.R

enum class OddsType(val code: String, val res: Int) {
    HK("HK", R.string.odd_type_hk),
    EU("EU", R.string.odd_type_eu),
    MYS("MYS", R.string.odd_type_mys),
    IDN("IDN", R.string.odd_type_idn),
}