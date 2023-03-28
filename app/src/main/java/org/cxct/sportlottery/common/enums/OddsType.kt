package org.cxct.sportlottery.common.enums

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.proguard.KeepClasses

@KeepClasses
enum class OddsType(val code: String, val res: Int) {
    HK("HK", R.string.odd_type_hk),
    EU("EU", R.string.odd_type_eu),
    MYS("MY", R.string.odd_type_mys),
    IDN("ID", R.string.odd_type_idn)
}